package org.andresoviedo.datatable.dto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.andresoviedo.datatable.Specification;


/**
 * Datatable AJAX request
 * 
 * @param <R>
 *            type du retour
 * @param <E>
 *            type de l'entité
 * @author afoviedo
 */
public class DatatableQuery<R, E> {

    private static final int DEFAULT_LENGTH = 10;

    /**
     * Draw counter. This is used by DataTables to ensure that the Ajax returns from server-side processing requests
     * are drawn in sequence by DataTables (Ajax requests are asynchronous and thus can return out of sequence). This
     * is used as part of the draw return parameter (see below).
     */
    @NotNull
    @Min(0)
    private Integer draw = 1;

    /**
     * Paging first record indicator. This is the start point in the current data set (0 index based - i.e. 0 is the
     * first record).
     */
    @NotNull
    @Min(0)
    private Integer start = 0;

    /**
     * Number of records that the table can display in the current draw. It is expected that the number of records
     * returned will be equal to this number, unless the server has fewer records to return. Note that this can be -1
     * to indicate that all records should be returned (although that negates any benefits of server-side processing!)
     */
    @NotNull
    @Min(-1)
    private Integer length = DEFAULT_LENGTH;

    /**
     * Global search parameter.
     */
    @NotNull
    private DatatableSearch search = new DatatableSearch();
    /**
     * Order parameter
     */
    private List<DatatableOrder> order = new ArrayList<DatatableOrder>();
    /**
     * Per-column search parameter
     */
    private List<DatatableColumn> columns = new ArrayList<DatatableColumn>();
    /**
     * Extra properties envoyés depuis la vue
     */
    private Map<String, Object> extraProps = new HashMap<String, Object>();
    // ------------------------------------------ atributs moteur datatable ---------------------------------------- //
    /**
     * Type de retour
     */
    private Class<R> queryClass;
    /**
     * Liste de colonnes a grouper
     */
    private List<DatatableColumn> groupByColumns;
    /**
     * Specification de base
     */
    private Specification<E> baseSpecification;
    /**
     * Specification complémentaire
     */
    private Specification<E> additionalSpecification;

    /**
     * New datatable query with default length
     */
    public DatatableQuery() {
        this(DEFAULT_LENGTH);
    }

    /**
     * New datatable query with specified length
     * 
     * @param length
     *            number of records to return
     */
    public DatatableQuery(final Integer length) {
        super();
        this.length = length;
    }

    /**
     * @param queryClass
     *            Type de retour
     */
    public DatatableQuery<R, E> setQueryClass(final Class<R> queryClass) {
        this.queryClass = queryClass;
        return this;
    }

    /**
     * @return type de retour
     */
    public Class<R> getQueryClass() {
        return queryClass;
    }

    /**
     * @param colonne
     *            colonnes a ajouter
     */
    public DatatableQuery<R, E> addColumn(final DatatableColumn... colonne) {
        if (colonne == null || colonne.length == 0) {
            throw new IllegalArgumentException("Liste de colonnes vide");
        }
        getColumns().addAll(Arrays.asList(colonne));
        return this;
    }

    /**
     * @return liste de colonnes a regrouper
     */
    public List<DatatableColumn> getGroupByColumns() {
        return groupByColumns;
    }

    /**
     * @param groupByColumns
     *            liste de colonnes a regrouper
     */
    public void setGroupByColumns(final List<DatatableColumn> groupByColumns) {
        this.groupByColumns = groupByColumns;
    }

    /**
     * @return a {@link Map} of {@link DatatableColumn} indexed by name
     */
    public Map<String, DatatableColumn> getColumnsAsMap() {
        final Map<String, DatatableColumn> map = new HashMap<String, DatatableColumn>();
        for (final DatatableColumn column : columns) {
            map.put(column.getData(), column);
        }
        return map;
    }

    /**
     * Find a column by its name
     *
     * @param columnName
     *            the name of the column
     * @return the given Column, or <code>null</code> if not found
     */
    public DatatableColumn getColumn(final String columnName) {
        if (columnName == null) {
            return null;
        }
        for (final DatatableColumn column : columns) {
            if (columnName.equals(column.getData())) {
                return column;
            }
        }
        return null;
    }

    /**
     * Removes column with its order
     * 
     * @param columnName
     *            column data to remove
     */
    public DatatableColumn removeColumn(final String columnName) {
        final int columIndex = getColumIndex(columnName);
        final DatatableColumn ret = columns.remove(columIndex);
        for (final Iterator<DatatableOrder> it = order.iterator(); it.hasNext();) {
            final DatatableOrder next = it.next();
            if (next.getColumn() == columIndex) {
                it.remove();
            } else if (next.getColumn() >= columIndex) {
                next.setColumn(next.getColumn() - 1);
            }
        }
        return ret;
    }

    /**
     * Add a new column
     *
     * @param name
     *            the name of the column
     * @param data
     *            the name of the attribute
     * @param searchable
     *            whether the column is searchable or not
     * @param orderable
     *            whether the column is orderable or not
     * @param searchValue
     *            if any, the search value to apply
     */
    public void addColumn(final String name, final String data, final boolean searchable, final boolean orderable,
        final String searchValue) {
        this.addColumn(name, data, searchable, orderable, new DatatableSearch(searchValue, false));
    }

    /**
     * @param name
     *            the name of the column
     * @param data
     *            the name of the attribute
     * @param searchable
     *            whether the column is searchable or not
     * @param orderable
     *            whether the column is orderable or not
     * @param searchValue
     *            if any, the search value to apply
     * @param order
     *            if any, the order to apply
     */
    public void addColumn(final String name, final String data, final boolean searchable, final boolean orderable,
        final String searchValue, final String order) {
        this.addColumn(name, data, searchable, orderable, new DatatableSearch(searchValue, false), order);
    }

    /**
     * @param name
     *            the name of the column
     * @param data
     *            the name of the attribute
     * @param searchable
     *            whether the column is searchable or not
     * @param orderable
     *            whether the column is orderable or not
     * @param search
     *            if any, the search to apply
     */
    public void addColumn(final String name, final String data, final boolean searchable, final boolean orderable,
        final DatatableSearch search) {
        this.addColumn(name, data, searchable, orderable, search, (DatatableOrder) null);
    }

    /**
     * Add a new column
     * 
     * @param name
     *            the name of the column
     * @param data
     *            the name of the attribute
     * @param searchable
     *            whether the column is searchable or not
     * @param orderable
     *            whether the column is orderable or not
     * @param search
     *            if any, the search to apply
     * @param order
     *            if any, the order to apply
     */
    public void addColumn(final String name, final String data, final boolean searchable, final boolean orderable,
        final DatatableSearch search, final String order) {
        final int newIndex = this.columns.size();
        this.columns.add(newIndex, new DatatableColumn(name, data, searchable, orderable, search));
        if (order != null) {
            this.order.add(new DatatableOrder(newIndex, order));
        }
    }

    /**
     * @param name
     *            the name of the column
     * @param data
     *            the name of the attribute
     * @param searchable
     *            whether the column is searchable or not
     * @param orderable
     *            whether the column is orderable or not
     * @param search
     *            if any, the search to apply
     * @param order
     *            if any, the order to apply
     */
    public void addColumn(final String name, final String data, final boolean searchable, final boolean orderable,
        final DatatableSearch search, final DatatableOrder order) {
        if (order == null) {
            this.addColumn(name, data, searchable, orderable, search, (String) null);
        } else {
            this.addColumn(name, data, searchable, orderable, search, order.getDir());
        }
    }

    /**
     * Returns true if order has been specified for specified column
     * 
     * @param indexColonne
     *            index of column
     * @return true if order has been specified for specified column
     */
    public boolean containsOrder(final int indexColonne) {
        for (final DatatableOrder orderTemp : getOrder()) {
            if (orderTemp.getColumn() == indexColonne) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get he order of the specified column
     * 
     * @param nomColonne
     *            name of the column
     * @return the order of the column
     */
    public DatatableOrder getOrder(final String nomColonne) {
        final int idxColonne = getColumIndex(nomColonne);
        for (final Iterator<DatatableOrder> it = getOrder().iterator(); it.hasNext();) {
            final DatatableOrder orderTemp = it.next();
            if (orderTemp.getColumn() == idxColonne) {
                return orderTemp;
            }
        }
        return null;
    }

    /**
     * Removes the order of the specified column
     * 
     * @param nomColonne
     *            name of the colum
     * @return <code>true</code> if the order was removed, <code>false</code> otherwise
     */
    public boolean removeOrder(final String nomColonne) {
        final int idxColonne = getColumIndex(nomColonne);
        for (final Iterator<DatatableOrder> it = getOrder().iterator(); it.hasNext();) {
            if (it.next().getColumn() == idxColonne) {
                it.remove();
                return true;
            }
        }
        return false;
    }

    /**
     * Add an order on the given column
     *
     * @param colonne
     *            laquelle on va ajouter l'ordre
     * @param ascending
     *            whether the sorting is ascending or descending
     */
    public DatatableQuery<R, E> addOrder(final DatatableColumn colonne, final String dir) {
        if (!dir.matches("asc|desc"))
            throw new IllegalArgumentException("wrong direction. Accepted values are [asc|desc]");
        order.add(new DatatableOrder(getColumIndex(colonne), dir));
        return this;
    }

    /**
     * Add an order on the given column
     *
     * @param columnName
     *            the name of the column
     * @param ascending
     *            whether the sorting is ascending or descending
     */
    public DatatableQuery<R, E> addOrder(final String columnName, final String dir) {
        if (!dir.matches("asc|desc"))
            throw new IllegalArgumentException("wrong direction. Accepted values are [asc|desc]");
        order.add(new DatatableOrder(getColumIndex(columnName), dir));
        return this;
    }

    /**
     * Add an order on the given column
     *
     * @param columnName
     *            the name of the column
     * @param ascending
     *            whether the sorting is ascending or descending
     */
    public DatatableQuery<R, E> addOrder(final String columnName, final boolean ascending) {
        if (ascending) {
            order.add(new DatatableOrder(getColumIndex(columnName), "asc"));
        } else {
            order.add(new DatatableOrder(getColumIndex(columnName), "desc"));
        }
        return this;
    }

    /**
     * Get the index of the specified column
     *
     * @param column
     *            colonne
     * @return the index of the column
     */
    public int getColumIndex(final DatatableColumn column) {
        for (int i = 0; i < columns.size(); i++) {
            if (column == columns.get(i)) {
                return i;
            }
        }
        throw new IllegalArgumentException("La colonne '" + column + "' n\'existe pas");
    }

    /**
     * Get the index of the specified column
     *
     * @param columnName
     *            the name of the column
     * @return the index of the column
     */
    public int getColumIndex(final String columnName) {
        for (int i = 0; i < columns.size(); i++) {
            if (columnName.equalsIgnoreCase(columns.get(i).getData())) {
                return i;
            }
        }
        throw new IllegalArgumentException("La colonne '" + columnName + "' n\'existe pas");
    }

    /**
     * @return specification avec le filtre base
     */
    public Specification<E> getBaseSpecification() {
        return baseSpecification;
    }

    /**
     * @param baseSpecification
     *            specification avec le filtre base
     */
    public void setBaseSpecification(final Specification<E> baseSpecification) {
        this.baseSpecification = baseSpecification;
    }

    /**
     * @return specification avec des filtres optionelles
     */
    public Specification<E> getAdditionalSpecification() {
        return additionalSpecification;
    }

    /**
     * @param additionalSpecification
     *            specification avec des filtres optionelles
     */
    public void setAdditionalSpecification(final Specification<E> additionalSpecification) {
        this.additionalSpecification = additionalSpecification;
    }

    public Integer getDraw() {
        return draw;
    }

    public void setDraw(final Integer draw) {
        this.draw = draw;
    }

    public Integer getStart() {
        return start;
    }

    public void setStart(final Integer start) {
        this.start = start;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(final Integer length) {
        this.length = length;
    }

    public DatatableSearch getSearch() {
        return search;
    }

    public void setSearch(final DatatableSearch search) {
        this.search = search;
    }

    public List<DatatableOrder> getOrder() {
        return order;
    }

    public void setOrder(final List<DatatableOrder> order) {
        this.order = order;
    }

    public List<DatatableColumn> getColumns() {
        return columns;
    }

    public void setColumns(final List<DatatableColumn> columns) {
        this.columns = columns;
    }

    public Map<String, Object> getExtraProps() {
        return extraProps;
    }

    public void setExtraProps(final Map<String, Object> extraProps) {
        this.extraProps = extraProps;
    }

    @Override
    public String toString() {
        return "DataTablesInput [draw=" + draw + ", start=" + start + ", length=" + length + ", search=" + search
            + ", order=" + order + ", columns=" + columns + "]";
    }

}
