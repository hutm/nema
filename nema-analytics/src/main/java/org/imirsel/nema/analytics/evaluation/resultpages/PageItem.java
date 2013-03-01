/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.imirsel.nema.analytics.evaluation.resultpages;

/**
 * Abstract super class for items to display on a page.
 * 
 * @author kris.west@gmail.com
 */
public abstract class PageItem {
    protected String name;
    protected String caption;

    public PageItem(String name, String caption){
    	this.name = cleanName(name);
        this.caption = caption;

        if(this.name.contains("\\s")){
            throw new IllegalArgumentException("The item name should be suitable for use as an identifier in html and should not contain reserved characters or spaces");
        }
    }
    
    /**
     * Removes foreign characters and symbols from strings, maps to lowercase
     * and replaces whitespace with underscores. Used to create names for
     * items.
     * 
     * @param name String to be cleaned
     * @return Cleaned version of String.
     */
    public static String cleanName(String name){
        return name.toLowerCase().replaceAll("\\s+", "_").replaceAll("[^a-z0-9]", "");
    }

    /**
     * @return the name
     */
    public String getName(){
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name){
        this.name = name;
    }

    /**
     * @return the caption
     */
    public String getCaption(){
        return caption;
    }

    /**
     * @param caption the caption to set
     */
    public void setCaption(String caption){
        this.caption = caption;
    }

    /**
     * Returns data to add to the head tag of the page.
     * @return HTML head tag data.
     */
    public abstract String getHeadData();
    
    /**
     * Returns static declarations to add to the head tag of the page, i.e.
     * importing required scripts etc.
     * @return Tags to add to the HTML head tag.
     */
    public abstract String getHeadStaticDeclarations();

    /**
     * Returns data to add to the body tag of the page.
     * @param topLink A flag that determines whether a link is added to scroll
     * to the top of the page from the item.
     * @return Returns the HTML for the item.
     */
    public abstract String getBodyData(boolean topLink);

}
