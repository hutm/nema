/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.imirsel.nema.analytics.evaluation.resultpages;

import java.util.List;
import java.util.Map;

/**
 * A PageItem implementation for plotting a confusion matrix.
 * 
 * Implements Javascript rendering using Protovis.
 * 
 * @author kris.west@gmail.com
 */
public class ProtovisConfusionMatrixPlotItem extends PageItem{

	private List<String> seriesNames;
	private double[][] seriesVals;
    
    public ProtovisConfusionMatrixPlotItem(String name, String caption, 
    		List<String> seriesNames, double[][] seriesVals){
    	
        super(name,caption);
        this.setSeriesNames(seriesNames);
        this.setSeriesVals(seriesVals);
        
        if (seriesVals.length != seriesNames.size() || seriesVals[0].length != seriesNames.size()){
        	throw new IllegalArgumentException("The number of series names (" + seriesNames.size() + ") " +
        			"did not match the dimensions of the confusion matrix (" + seriesVals.length + " x " + seriesVals[0].length +")");
        }        
    }

    @Override
	public String getHeadStaticDeclarations(){
    	//for javascript debugging use: protovis-d3.2.js
    	//String out = "<script type=\"text/javascript\" src=\"protovis-d3.2.js\"></script>\n" +
		
    	String out = "<script type=\"text/javascript\" src=\"protovis-r3.2.js\"></script>\n";
		return out;
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
        out += 	"\t<div id=\"center\">\n" +
        		"\t\t<div style=\"width: 710px; height: 670px; padding: 2px; margin: 3px; border-width: 1px; border-color: black; border-style:solid;\">\n" +
        		"\t\t\t<script type=\"text/javascript+protovis\">\n";
        out +=  "\t\t\tvar " + getName() + " = new function() {\n";
        
        //setup data
        String i = "\t\t\t\t";
        
        int numSeries = seriesNames.size();
        
        out += i + "var conf = [\n" +
        //header row
        		i + "[\"Name\",";
        for (int x = 0; x < numSeries; x++) {
			out += "\"" + seriesNames.get(x) + "\"";
			if (x < numSeries-1){
				out += ",";
			}else{
				out += "],\n";
			}
        }
		//body rows
        for (int x = 0; x < numSeries; x++) {
			out += i + "[\"" + seriesNames.get(x) + "\",";
			for (int y = 0; y < numSeries; y++){
				out += seriesVals[x][y];
				if (y < numSeries-1){
					out += ",";
				}
			}
			out += "]";
			if (x < numSeries-1){
				out += ",\n";
			}else{
				out += "\n";
			}
		}
        out += i + "];\n\n";

        
		out +=	i + "/* Convert from tabular format to array of objects. */\n" + 
				i + "var cols = conf.shift();\n" +
				i + "conf = conf.map(function(d) pv.dict(cols, function() d[this.index]));\n" +
				i + "cols.shift();\n" +
				i + "\n" +
				i + "/* Sort out color scale  */\n" +
				i + "var fill = pv.dict(cols, function(f) pv.Scale.linear()\n" +
				i + "\t.domain(0, 1)\n" +
				i + "\t.range(\"white\", \"black\"));\n" +
				i + "\n" +
				i + "/* The cell dimensions. */\n" +
				i + "var numClasses = cols.length;\n" +
				i + "var w = 600/numClasses, h = 600/numClasses;\n" +
				i + "\n" +
				i + "var vis = new pv.Panel()\n" +
				i + "\t.width(cols.length * w)\n" +
				i + "\t.height(conf.length * h)\n" +
				i + "\t.top(60)\n" +
				i + "\t.left(100);\n" +
				i + "\n" +
				i + "vis.add(pv.Panel)\n" +
				i + "\t.data(cols)\n" +
				i + "\t.left(function() this.index * w)\n" +
				i + "\t.width(w)\n" +
				i + ".add(pv.Panel)\n" +
				i + "\t.data(conf)\n" +
				i + "\t.top(function() this.index * h)\n" +
				i + "\t.height(h)\n" +
				i + "\t.fillStyle(function(d, f) fill[f](d[f]))\n" +
				i + "\t.strokeStyle(\"white\")\n" +
				i + "\t.lineWidth(1)\n" +
				i + "\t.antialias(false)\n" +
				i + "\t.title(function(d, f) d.Name + \" vs \" + f + \": \" + (d[f]*100).toFixed(2) + \"%\");\n" +
				i + "\n" +
				i + "vis.add(pv.Label)\n" +
				i + "\t.data(cols)\n" +
				i + "\t.left(function() this.index * w + w / 2)\n" +
				i + "\t.textAngle(-Math.PI / 2)\n" +
				i + "\t.textBaseline(\"middle\")\n" +
				i + "\t.font(\"12px sans-serif\");\n" +
				i + "\n" +
				i + "vis.add(pv.Label)\n" +
				i + "\t.data(conf)\n" +
				i + "\t.top(function() this.index * h + h / 2)\n" +
				i + "\t.textAlign(\"right\")\n" +
				i + "\t.textBaseline(\"middle\")\n" +
				i + "\t.text(function(d) d.Name)\n" +
				i + "\t.font(\"12px sans-serif\");\n" +
				i + "\n" +
				i + "vis.render();\n\n";

		out +=  "\t\t\t};\n";		
		
        out +=  "\t\t\t</script>\n" +
        		"\t\t</div>\n" +
        		"\t</div>\n";

		out +=  "\t<br><br>\n";
        return out;
    }

	public void setSeriesNames(List<String> seriesNames) {
		this.seriesNames = seriesNames;
	}

	public List<String> getSeriesNames() {
		return seriesNames;
	}

	public void setSeriesVals(double[][] seriesVals) {
		this.seriesVals = seriesVals;
	}

	public double[][] getSeriesVals() {
		return seriesVals;
	}

}
