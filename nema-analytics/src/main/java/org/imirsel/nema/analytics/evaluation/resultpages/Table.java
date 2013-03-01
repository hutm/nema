package org.imirsel.nema.analytics.evaluation.resultpages;

import java.util.List;

/**
 * Data-structure to hold the information relating to a table (column names and rows 
 * of string data).
 *  
 * @author kriswest
 */
public class Table{
    private String[] colHeaders;
    private List<String[]> rows;

    public Table(String[] colHeaders,
                 List<String[]> rows){
        this.colHeaders = colHeaders;
        this.rows = rows;
    }

    /**
     * @return the colHeaders
     */
    public String[] getColHeaders(){
        return colHeaders;
    }

    /**
     * @param colHeaders the colHeaders to set
     */
    public void setColHeaders(String[] colHeaders){
        this.colHeaders = colHeaders;
    }

    /**
     * @return the rows
     */
    public List<String[]> getRows(){
        return rows;
    }

    /**
     * @param rows the rows to set
     */
    public void setRows(List<String[]> rows){
        this.rows = rows;
    }
}