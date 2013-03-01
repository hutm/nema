/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.imirsel.nema.analytics.evaluation.resultpages;

import java.util.Iterator;
import java.util.List;

/**
 * A page item implementation for a list of file links.
 * 
 * @author kris.west@gmail.com
 */
public class FileListItem extends PageItem{

    private List<String> paths;

    public FileListItem(String name, String caption, List<String> paths){
        super(name,caption);
        this.paths = paths;
    }

    /**
     * @return the files
     */
    public List<String> getPaths(){
        return paths;
    }

    /**
     * @param paths the file paths to set
     */
    public void setPaths(List<String> paths){
        this.paths = paths;
    }

    public void addPath(String path){
        this.paths.add(path);
    }

    public String getHeadStaticDeclarations(){
    	return "";
    }
    
    @Override
    public String getHeadData(){
        return "";
    }

    public String getBodyData(boolean topLink){
        String out = "\t<a name=\"" + getName() + "\"></a>\n" +
                "\t<h4>" + getCaption();
        if (topLink){
            out += "&nbsp;&nbsp;&nbsp;&nbsp;<span class=\"toplink\"><a href=\"#top\">[top]</a></span>";
        }
        out += "</h4>\n";
        out += "\t<ul>\n";
        String path;
        for (Iterator<String> it = paths.iterator(); it.hasNext();){
            path = it.next();
            out += "\t\t<li><a href=\"" + path + "\">" + path + "</a>\n";
        }
	out += "\t</ul>\n";
	out += "\t<br>\n\n";
	return out;
    }

}
