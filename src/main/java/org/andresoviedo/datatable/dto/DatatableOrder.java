package org.andresoviedo.datatable.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * Datatable column order specification
 * 
 * @author afoviedo
 */
public class DatatableOrder {

    /**
     * Column to which ordering should be applied. This is an index reference to the columns array of information that
     * is also submitted to the server.
     */
    @NotNull
    @Min(0)
    private Integer column;

    /**
     * Ordering direction for this column. It will be asc or desc to indicate ascending ordering or descending
     * ordering, respectively.
     */
    @NotNull
    @Pattern(regexp = "(desc|asc)")
    private String dir;

    /**
     * Order specification for a column
     */
    public DatatableOrder() {
        super();
    }

    /**
     * Creates a column order specification with the specified parameters
     * 
     * @param index
     *            index of the column
     * @param direction
     *            direction 'asc' or 'desc'
     */
    public DatatableOrder(final int index, final String direction) {
        this.column = index;
        this.dir = direction;
    }

    public Integer getColumn() {
        return column;
    }

    public void setColumn(final Integer column) {
        this.column = column;
    }

    public String getDir() {
        return dir;
    }

    public void setDir(final String dir) {
        this.dir = dir;
    }

    @Override
    public String toString() {
        return "Order [column=" + column + ", dir=" + dir + "]";
    }

}
