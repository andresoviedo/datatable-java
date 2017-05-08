package org.andresoviedo.datatable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.Attribute.PersistentAttributeType;

import org.andresoviedo.datatable.Sort.Direction;
import org.andresoviedo.datatable.dto.DatatableColumn;
import org.andresoviedo.datatable.dto.DatatableOrder;
import org.andresoviedo.datatable.dto.DatatableQuery;
import org.andresoviedo.datatable.dto.DatatableSearch;

/**
 * Helper du datatable
 * 
 * @author afoviedo
 */
public final class DatatableHelper {

    private DatatableHelper() {
        // abstract
    }

    static void getExpressions(final Root<?> from, final List<DatatableColumn> columns,
        final List<? super Path<?>> output) {
        for (final DatatableColumn column : columns) {
            output.add(DatatableHelper.getExpression(from, column.getData(), null));
        }
    }

    static <T> List<Order> getOrderBy(final Root<T> from, final CriteriaBuilder qb, final Pageable pageable) {
        final List<Order> orderBy = new ArrayList<Order>();
        for (final Iterator<Sort.Order> it = pageable.getSort().iterator(); it.hasNext();) {
            final org.andresoviedo.datatable.Sort.Order order = it.next();
            final Path<?> entityProperty = DatatableHelper.getExpression(from, order.getProperty(), null);
            if (order.isAscending()) {
                orderBy.add(qb.asc(entityProperty));
            } else {
                orderBy.add(qb.desc(entityProperty));
            }
        }
        return orderBy;
    }

    /**
     * Cree une expression Criteria API avec l'atribut de l'entité passé en parametre
     * 
     * @param root
     *            entité JPA contenant le champ
     * @param columnData
     *            nom du champ
     * @param clazz
     *            class du champ
     * @param <S>
     *            type du champ
     * @return l'expression de l'atribut
     */
    public static <S> Path<S> getExpression(final Root<?> root, final String columnData, final Class<S> clazz) {
        if (!columnData.contains(DatatableSpecification.ATTRIBUTE_SEPARATOR)) {
            // columnData is like "attribute" so nothing particular to do
            return root.get(columnData);
        }
        // columnData is like "joinedEntity.attribute" so add a join clause
        final String[] values = columnData.split(DatatableSpecification.ESCAPED_ATTRIBUTE_SEPARATOR);
        final Attribute<?, ?> attribute = root.getModel().getAttribute(values[0]);
        if (attribute == null) {
            throw new IllegalArgumentException(
                "Colonne '" + values[0] + "' (" + columnData + ") introuvable depuis l'entité '" + root.getJavaType()
                    + "'");
        }
        if (attribute.getPersistentAttributeType() == PersistentAttributeType.EMBEDDED) {
            // with @Embedded attribute
            return root.get(values[0]).get(values[1]);
        }
        From<?, ?> from = root;
        for (int i = 0; i < values.length - 1; i++) {
    
            Join<?, ?> join = null;
            for (final Join<?, ?> joinCandidate : from.getJoins()) {
                if (joinCandidate.getAttribute().getName().equals(values[i])) {
                    // LOGGER.debug("Trouve joint d'entite: '{}'", values[i]);
                    join = joinCandidate;
                }
            }
            if (join == null) {
                // LOGGER.debug("Joigant entite '{}'...", values[i]);
                join = from.join(values[i], JoinType.INNER);
            }
            from = join;
        }
        return from.get(values[values.length - 1]);
    }

    /**
     * Construction d'une specification "or" avec la liste de colonnes passés en paramétre
     * 
     * @param input
     *            datatable query
     * @param colonnes
     *            liste de colonnes a faire le "where (x=y or z=a or ...)"
     * @param search
     *            texte a chercher
     * @return le predicat or
     */
    public static <T> Specification<T> creerSpecificationOr(final DatatableQuery<?, T> input,
        final List<DatatableColumn> colonnes, final DatatableSearch search) {
        return new DatatableOrSpecification<T>(colonnes, search);
    }

    /**
     * Creates a 'LIMIT .. OFFSET .. ORDER BY ..' clause for the given {@link DatatableQuery}.
     * 
     * @param input
     *            the {@link DatatableQuery} mapped from the Ajax request
     * @return a {@link Pageable}, must not be {@literal null}.
     */
    static <T> Pageable getPageable(final DatatableQuery<?, T> input) {
        final List<org.andresoviedo.datatable.Sort.Order> orders =
            new ArrayList<org.andresoviedo.datatable.Sort.Order>();
        for (final DatatableOrder order : input.getOrder()) {
            final DatatableColumn column = input.getColumns().get(order.getColumn());
            if (column.getOrderable()) {
                final String sortColumn = column.getData();
                final Direction sortDirection = Direction.fromString(order.getDir());
                orders.add(new org.andresoviedo.datatable.Sort.Order(sortDirection, sortColumn));
            }
        }
        org.andresoviedo.datatable.Sort sort = null;
        if (!orders.isEmpty()) {
            sort = new org.andresoviedo.datatable.Sort(orders);
        }
    
        if (input.getLength() == -1) {
            input.setStart(0);
            input.setLength(Integer.MAX_VALUE);
        }
        return new DataTablePage(input.getStart(), input.getLength(), sort);
    }

    static class DataTablePage implements Pageable {

        private final int offset;
        private final int pageSize;
        private final Sort sort;

        private DataTablePage(final int offset, final int pageSize, final Sort sort) {
            this.offset = offset;
            this.pageSize = pageSize;
            this.sort = sort;
        }

        @Override
        public int getOffset() {
            return offset;
        }

        @Override
        public int getPageSize() {
            return pageSize;
        }

        @Override
        public Sort getSort() {
            return sort;
        }

        @Override
        public Pageable next() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Pageable previousOrFirst() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Pageable first() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean hasPrevious() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getPageNumber() {
            throw new UnsupportedOperationException();
        }
    }
}
