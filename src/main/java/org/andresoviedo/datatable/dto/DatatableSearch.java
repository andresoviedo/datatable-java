package org.andresoviedo.datatable.dto;

import javax.validation.constraints.NotNull;

/**
 * Datatable search value for a column specification
 * 
 * @author afoviedo
 */
public class DatatableSearch {

    /**
     * Global search value. To be applied to all columns which have searchable as true.
     */
    @NotNull
    private String value;

    /**
     * true if the global filter should be treated as a regular expression for advanced searching, false otherwise.
     * Note that normally server-side processing scripts will not perform regular expression searching for performance
     * reasons on large data sets, but it is technically possible and at the discretion of your script.
     */
    @NotNull
    private Boolean regex;

    /**
     * Construct a default column search
     */
    public DatatableSearch() {
    }

    /**
     * Construct a new column search with the specified parameters
     * 
     * @param searchValue
     *            search value
     * @param isRegex
     *            <code>true</code> if search value is a regex
     */
    public DatatableSearch(final String searchValue, final boolean isRegex) {
        this.value = searchValue;
        this.regex = isRegex;
    }

    public String getValue() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    public Boolean getRegex() {
        return regex;
    }

    public void setRegex(final Boolean regex) {
        this.regex = regex;
    }

    @Override
    public String toString() {
        return "Search [value=" + value + ", regex=" + regex + "]";
    }

}
