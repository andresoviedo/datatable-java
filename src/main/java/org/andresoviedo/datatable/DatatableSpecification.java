package org.andresoviedo.datatable;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.andresoviedo.datatable.dto.DatatableColumn;
import org.andresoviedo.datatable.dto.DatatableQuery;

/**
 * Spécification JPA d'aprés les parametres du datatable.
 * 
 * @author afoviedo
 * @param <T>
 *            type de l'entité JPA
 */
public class DatatableSpecification<T> implements Specification<T> {

    /**
     * Separateur des atributs de l'entité
     */
    public static final String ATTRIBUTE_SEPARATOR = ".";
    /**
     * Regexp pour trouver des separateurs
     */
    public static final String ESCAPED_ATTRIBUTE_SEPARATOR = "\\.";
    /**
     * Escape char de valeur "like"(dependant de la base de données)
     */
    public static final char ESCAPE_CHAR = '\\';

    private Logger LOGGER = Logger.getLogger("");

    private final DatatableQuery<?, T> input;

    /**
     * Constructor de l'especification JPA Criteria avec les parametres specifiés
     * 
     * @param input
     *            paramétres: filtres et ordres
     */
    DatatableSpecification(final DatatableQuery<?, T> input) {
        this.input = input;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Predicate toPredicate(final Root<T> root, final CriteriaQuery<?> query, final CriteriaBuilder cb) {
        Predicate predicate = cb.conjunction();
        predicate = getPredicateColonnes(root, query, cb, predicate);
        predicate = getPredicateGlobalSearch(root, query, cb, predicate);
        return predicate;
    }

    private Predicate getPredicateGlobalSearch(final Root<T> root, final CriteriaQuery<?> query, final CriteriaBuilder cb,
        Predicate predicate) {
        // check whether a global filter value exists
        final String globalFilterValue = input.getSearch().getValue();
        if (globalFilterValue != null && globalFilterValue.trim().length() > 0) {
            LOGGER.fine("filtre global: {"+ globalFilterValue+"}");

            Predicate matchOneColumnPredicate = cb.disjunction();
            // add a 'WHERE .. LIKE' clause on each searchable column
            for (final DatatableColumn column : input.getColumns()) {
                if (column.getSearchable()) {
                    LOGGER.log(Level.FINE, "filtre global pour colonne: {}", column);
                    final Expression<String> expression = DatatableHelper.getExpression(root, column.getData(), String.class);

                    matchOneColumnPredicate = cb.or(matchOneColumnPredicate,
                        cb.like(cb.lower(expression), getLikeFilterValue(globalFilterValue), ESCAPE_CHAR));
                }
            }
            predicate = cb.and(predicate, matchOneColumnPredicate);
        }
        return predicate;
    }

    private Predicate getPredicateColonnes(final Root<T> root, final CriteriaQuery<?> query, final CriteriaBuilder cb,
        Predicate predicate) {
        // check for each searchable column whether a filter value exists
        for (final DatatableColumn column : input.getColumns()) {
            final boolean isColumnSearchable =
                column.getSearchable() && column.getSearch() != null
                    && hasText(column.getSearch().getValue());
            if (!isColumnSearchable) {
                continue;
            }

            LOGGER.fine("colonne a filtrer: {"+ column+"}");

            // the filter contains only one value, add a 'WHERE .. LIKE'
            // clause
            if (isBoolean(column.getSearch().getValue())) {
                final Expression<Boolean> booleanExpression = DatatableHelper.getExpression(root, column.getData(), Boolean.class);
                predicate =
                    cb.and(predicate, cb.equal(booleanExpression, Boolean.valueOf(column.getSearch().getValue())));
            } else {
                final Expression<String> stringExpression = DatatableHelper.getExpression(root, column.getData(), String.class);
                predicate = cb.and(predicate, cb.like(cb.lower(stringExpression),
                    getLikeFilterValue(column.getSearch().getValue()), ESCAPE_CHAR));
            }

        }
        return predicate;
    }

    private boolean hasText(String value) {
		return value != null && value.trim().length() > 0;
	}

	private static boolean isBoolean(final String filterValue) {
        return "TRUE".equalsIgnoreCase(filterValue) || "FALSE".equalsIgnoreCase(filterValue);
    }

    /**
     * Helper pour creer la valeur like '%filterValue%'".
     * 
     * @param filterValue
     *            le valeur a filtrer
     * @return la valeur du clause where
     */
    public static String getLikeFilterValue(final String filterValue) {
        return "%" + filterValue.toLowerCase().replaceAll("%", "\\\\" + "%").replaceAll("_", "\\\\" + "_") + "%";
    }

}
