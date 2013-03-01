/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.imirsel.nema.analytics.evaluation.resultpages;

/**
 * A page item implementation for image items.
 * 
 * @author kris.west@gmail.com
 */
public class ImageItem extends PageItem{
    protected String imagePath;

    public ImageItem(String name, String caption, String imagePath){
        super(name,caption);
        this.imagePath = imagePath;
    }

    /**
     * @return the relative path to the image
     */
    public String getImagePath(){
        return imagePath;
    }

    /**
     * @param imagePath the relative path to the image
     */
    public void setImagePath(String imagePath){
        this.imagePath = imagePath;
    }
    
    public String getHeadStaticDeclarations(){
    	return "";
    }
    
    @Override
    public String getHeadData(){
        return "";
    }

    @Override
    public String getBodyData(boolean topLink){
        String out = "\t<a name=\"" + getName() + "\"></a>\n" +
                "\t<h4>" + getCaption();
        if (topLink){
            out += "&nbsp;&nbsp;&nbsp;&nbsp;<span class=\"toplink\"><a href=\"#top\">[top]</a></span>";
        }
        out += "</h4>\n";
        out += "\t<a href=\"" + imagePath + "\">\n";
	out += "\t\t<img width=\"710\" src=\"" + imagePath + "\">\n";
	out += "\t</a>\n";
        out += "\t<br><br>\n\n";
        return out;
    }

    
}
