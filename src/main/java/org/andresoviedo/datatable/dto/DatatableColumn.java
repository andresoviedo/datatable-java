package org.andresoviedo.datatable.dto;

import javax.validation.constraints.NotNull;

/**
 * Datatable column specification
 * 
 * @author afoviedo
 */
public class DatatableColumn {

    /**
     * Column's data source
     * 
     * @see http://datatables.net/reference/option/columns.data
     */
    private String data;

    /**
     * Column's name
     * 
     * @see http://datatables.net/reference/option/columns.name
     */
    private String name;

    /**
     * Flag to indicate if this column is searchable (true) or not (false).
     * 
     * @see http://datatables.net/reference/option/columns.searchable
     */
    @NotNull
    private boolean searchable;

    /**
     * Flag to indicate if this column is orderable (true) or not (false).
     * 
     * @see http://datatables.net/reference/option/columns.orderable
     */
    @NotNull
    private boolean orderable;

    /**
     * Flag pour indiquer que ce champ contient des spaces a ignorer
     */
    private boolean searchWithoutSpaces;

    /**
     * Search value to apply to this specific column.
     */
    @NotNull
    private DatatableSearch search;
    /**
     * Type du champ
     */
    private Class<?> type = String.class;

    /**
     * Construct a datatable column
     */
    public DatatableColumn() {
        super();
    }

    /**
     * Construction d'une colonne bàsique
     * 
     * @param name
     *            nom de colonne
     * @param data
     *            path atribut de l'entité
     */
    public DatatableColumn(final String name, final String data) {
        this(name, data, false, false, null);
    }

    /**
     * Construct a datatable column with the specified parameters
     * 
     * @param name´nom
     *            de colonne
     * @param data
     *            path atribut de l'entité
     * @param searchable
     *            atribut avec filtre ou pas
     * @param orderable
     *            atribut avec ordre ou pas
     * @param search
     *            texte a chercher ou <code>null</null>
     */
    public DatatableColumn(final String name, final String data, final boolean searchable, final boolean orderable,
        final DatatableSearch search) {
        this.name = name;
        this.data = data;
        this.searchable = searchable;
        this.orderable = orderable;
        this.search = search;
    }

    /**
     * Set the search value to apply to this column
     *
     * @param searchValue
     *            if any, the search value to apply
     */
    public void setSearchValue(final String searchValue) {
        this.search.setValue(searchValue);
    }

    /**
     * @return atribut de l'entité
     */
    public String getData() {
        return data;
    }

    /**
     * @param data
     *            atribut de l'entité
     * @return this
     */
    public DatatableColumn setData(final String data) {
        this.data = data;
        return this;
    }

    /**
     * @return description de colonne
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            description de colonne
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * @return colonne avec recherche configuré
     */
    public boolean getSearchable() {
        return searchable;
    }

    /**
     * @param searchable
     *            <code>true</code> si la colonne est a filtrer
     */
    public void setSearchable(final Boolean searchable) {
        this.searchable = searchable;
    }

    /**
     * @return si la recherche dois ignorer les espaces
     */
    public boolean isSearchWithoutSpaces() {
        return searchWithoutSpaces;
    }

    /**
     * @param searchWithoutSpaces
     *            ignorer les espaces o pas
     */
    public DatatableColumn setSearchWithoutSpaces(final boolean searchWithoutSpaces) {
        this.searchWithoutSpaces = searchWithoutSpaces;
        return this;
    }

    /**
     * @return <code>true</code> si la colonne est a mette en ordre
     */
    public Boolean getOrderable() {
        return orderable;
    }

    /**
     * @param orderable
     *            si la colonne est a mette en ordre
     */
    public DatatableColumn setOrderable(final Boolean orderable) {
        this.orderable = orderable;
        return this;
    }

    /**
     * @return recherche globale
     */
    public DatatableSearch getSearch() {
        return search;
    }

    /**
     * @param search
     *            recherche globale
     */
    public void setSearch(final DatatableSearch search) {
        this.search = search;
    }

    public Class<?> getType() {
        return type;
    }

    public void setType(final Class<?> type) {
        this.type = type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Column [data=" + data + ", name=" + name + ", searchable=" + searchable + ", orderable=" + orderable
            + ", search=" + search + "]";
    }

}
