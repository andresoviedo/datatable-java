package org.andresoviedo.datatable;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;

import org.andresoviedo.datatable.dto.DatatableQuery;
import org.andresoviedo.datatable.dto.DatatableResult;

/**
 * Implementation du datatable server side en utilisent le criteria API pour construir les réquétes.
 * 
 * @author afoviedo
 * @param <T>
 *            type de l'entité JPA
 */
public class DatatableDAOImpl<T> implements DatatableDAO<T> {

	private Logger LOGGER = Logger.getLogger("");

    private final EntityManagerFactory entityManagerFactory;

    private final Class<T> entity;

    /**
     * Constructeur du DAO pour faire executer les requetes du datatable
     * 
     * @param entityManagerFactory
     *            le JPA entity manager
     * @param entity
     *            l'entité a gerer pour ce DAO
     */
    public DatatableDAOImpl(final EntityManagerFactory entityManagerFactory, final Class<T> entity) {
        super();
        this.entityManagerFactory = entityManagerFactory;
        this.entity = entity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R> DatatableResult<R> findAll(final DatatableQuery<R, T> input) {
        return findAll(input, null, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R> DatatableResult<R> findAll(final DatatableQuery<R, T> input,
        final Specification<T> additionalSpecification) {
        return findAll(input, additionalSpecification, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R> DatatableResult<R> findAll(final DatatableQuery<R, T> input,
        final Specification<T> additionalSpecification,
        final Specification<T> preFilteringSpecification) {

        // asserts
        if (input.getColumns() == null || input.getColumns().isEmpty()) {
            throw new IllegalArgumentException("Pas de colonnes configuré");
        }

        final DatatableResult<R> output = new DatatableResult<R>();
        output.setDraw(input.getDraw());
        if (input.getLength() == 0) {
            return output;
        }

        try {
            // Compter nombre de registres
            output.setRecordsTotal(count(preFilteringSpecification, input));
            LOGGER.log(Level.INFO, "Total prefiltered: {0}", output.getRecordsTotal());
            if (output.getRecordsTotal() == 0) {
                return output;
            }

            // Creer specification
            final Specifications<T> specifications = Specifications.where(new DatatableSpecification<T>(input))
                .and(additionalSpecification).and(preFilteringSpecification);

            // Compter le nombre de registres avec la spec
            output.setRecordsFiltered(count(specifications, input));
            LOGGER.log(Level.INFO, "Total filtered: {0}", output.getRecordsFiltered());

            // Executer la requete
            final Pageable pageable = DatatableHelper.getPageable(input);
            final List<R> result;
            if (input.getQueryClass() == null) {
                // INFO: il faut faire comment suive. Le type de query <R>=<T>
                result = findAllImpl1(input, specifications, pageable);
            } else {
                result = findAllImpl2(input, specifications, pageable);
            }
            output.setData(result);

        } catch (final IllegalStateException ex) {
        	LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            output.setError(ex.getMessage());
        } catch (final PersistenceException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            output.setError(ex.getMessage());
        }

        return output;
    }
    
    private long count(final Specification<T> specification, final DatatableQuery<?, T> input) {
        if (input.getGroupByColumns() == null){
            return countDefault(specification, input);
        }
        return countWhenGroupBy(specification, input);
    }

    private long countDefault(final Specification<T> specification, final DatatableQuery<?, T> input) {
    	final EntityManager entityManager = entityManagerFactory.createEntityManager();
        final CriteriaBuilder qb = entityManager.getCriteriaBuilder();
        final CriteriaQuery<Long> cq = qb.createQuery(Long.class);
        final Root<T> from = cq.from(this.entity);
        
        cq.select(qb.count(from));
        if (specification != null) {
            cq.where(specification.toPredicate(from, cq, qb));
        }
        final long ret = entityManager.createQuery(cq).getSingleResult();
        entityManager.close();
		return ret;
    }
    
    private long countWhenGroupBy(final Specification<T> specification, final DatatableQuery<?, T> input) {
    	final EntityManager entityManager = entityManagerFactory.createEntityManager();
        final CriteriaBuilder qb = entityManager.getCriteriaBuilder();
        final CriteriaQuery<Long> cq = qb.createQuery(Long.class);
        final Root<T> from = cq.from(this.entity);
        
        cq.select(qb.literal(1L));
        if (specification != null) {
            cq.where(specification.toPredicate(from, cq, qb));
        }
        if (input.getGroupByColumns() != null && !input.getGroupByColumns().isEmpty()) {
            final List<Expression<?>> groupByList = new ArrayList<Expression<?>>();
            DatatableHelper.getExpressions(from, input.getGroupByColumns(), groupByList);
            cq.groupBy(groupByList);
        }
        final long ret = entityManager.createQuery(cq).getResultList().size();
        entityManager.close();
		return ret;
    }

    @SuppressWarnings("unchecked")
    private <R> List<R> findAllImpl1(final DatatableQuery<R, T> input, final Specifications<T> specifications,
        final Pageable pageable) {

        // create query
    	final EntityManager entityManager = entityManagerFactory.createEntityManager();
        final CriteriaBuilder qb = entityManager.getCriteriaBuilder();
        final CriteriaQuery<T> cq = qb.createQuery(this.entity);

        // Etablir entité racine
        final Root<T> from = cq.from(this.entity);

        // select
        cq.select(from);

        // where
        if (specifications != null) {
            cq.where(specifications.toPredicate(from, cq, qb));
        }

        // group by
        if (input.getGroupByColumns() != null && !input.getGroupByColumns().isEmpty()) {
            final List<Expression<?>> groupByList = new ArrayList<Expression<?>>();
            DatatableHelper.getExpressions(from, input.getGroupByColumns(), groupByList);
            cq.groupBy(groupByList);
        }

        // order by
        if (pageable != null && pageable.getSort() != null) {
            cq.orderBy(DatatableHelper.getOrderBy(from, qb, pageable));
        }

        // page limit
        final TypedQuery<T> q = entityManager.createQuery(cq);
        if (pageable != null) {
            q.setFirstResult(pageable.getOffset());
            q.setMaxResults(pageable.getPageSize());
        }

        // execute query
        final List<R> ret = (List<R>) q.getResultList();
        entityManager.close();
		return ret;
    }

    private <R> List<R> findAllImpl2(final DatatableQuery<R, T> input, final Specifications<T> specifications,
        final Pageable pageable) {
        
        // create query
    	final EntityManager entityManager = entityManagerFactory.createEntityManager();
        final CriteriaBuilder qb = entityManager.getCriteriaBuilder();        
        final CriteriaQuery<R> cq = qb.createQuery(input.getQueryClass());

        // Etablir entité racine
        final Root<T> from = cq.from(this.entity);
        
        // select for custom class
        final List<Selection<?>> selectList = new ArrayList<Selection<?>>();
        if (input.getGroupByColumns() != null) {
            DatatableHelper.getExpressions(from, input.getGroupByColumns(), selectList);
        } else {
            DatatableHelper.getExpressions(from, input.getColumns(), selectList);
        }
        cq.multiselect(selectList);

        // where
        if (specifications != null) {
            cq.where(specifications.toPredicate(from, cq, qb));
        }

        // group by
        if (input.getGroupByColumns() != null && !input.getGroupByColumns().isEmpty()) {
            final List<Expression<?>> groupByList = new ArrayList<Expression<?>>();
            DatatableHelper.getExpressions(from, input.getGroupByColumns(), groupByList);
            cq.groupBy(groupByList);
        }

        // order by
        if (pageable != null && pageable.getSort() != null) {
            cq.orderBy(DatatableHelper.getOrderBy(from, qb, pageable));
        }

        // page limit
        final TypedQuery<R> q = entityManager.createQuery(cq);
        if (pageable != null) {
        	LOGGER.log(Level.FINE, "Pageable: offset: {0}, page size: {1}", new Object[]{pageable.getOffset(), pageable.getPageSize()});
            q.setFirstResult(pageable.getOffset());
            q.setMaxResults(pageable.getPageSize());
        }

        // execute query
        final List<R> ret = q.getResultList();
        entityManager.close();
		return ret;
    }
}
