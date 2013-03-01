/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.imirsel.nema.analytics.evaluation.resultpages;

import java.util.List;
import java.util.Map;

/**
 * A PageItem implementation for plotting Bar charts.
 * 
 * Implements Javascript rendering using Protovis.
 * 
 * @author kris.west@gmail.com
 */
public class ProtovisBarChartPlotItem extends PageItem{

	private List<String> seriesNames;
	private List<Double> seriesVals;
    
    public ProtovisBarChartPlotItem(String name, String caption, 
    		List<String> seriesNames, List<Double> seriesVals){
    	
        super(name,caption);
        this.setSeriesNames(seriesNames);
        this.setSeriesVals(seriesVals);
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
        		"\t\t<div style=\"width: 500px; height: 470px; padding: 2px; margin: 3px; border-width: 1px; border-color: black; border-style:solid;\">\n" +
        		"\t\t\t<script type=\"text/javascript+protovis\">\n";
        out +=  "\t\t\tvar " + getName() + " = new function() {\n";
        
        //setup data
        String i = "\t\t\t\t";
        
        int numSeries = seriesNames.size();
        
        out += i + "var seriesNames = [";
        for (int j = 0; j < numSeries; j++) {
			out += "\"" + seriesNames.get(j) + "\"";
			if (j < numSeries-1){
				out += ",";
			}
		}
        out += "];\n";

        out += i + "var seriesVals = [";
        for (int s = 0; s < numSeries-1; s++) {
        	
        	out += seriesVals.get(s) + ",";
		}
        out += seriesVals.get(numSeries-1);
        out += "];\n\n";

		out += i + "/* Scales and sizing. */\n";
		out += i + "var w = 475,\n";
		out += i + "    h = 300,\n";
		out += i + "    x = pv.Scale.ordinal(pv.range(" + numSeries + ")).splitBanded(0, w, 4/5),\n";
		//out += i + "    y = pv.Scale.linear(0, pv.max(pv.blend(allSeries), function(d) d.y)).range(0, h2);\n";
		out += i + "    y = pv.Scale.linear(0, 1).range(0, h);\n";

		out += i + "/* Root panel. */\n";
		out += i + "var vis = new pv.Panel()\n";
		out += i + "    .width(w)\n";
		out += i + "    .height(h)\n";
		out += i + "    .bottom(170)\n";
		out += i + "    .left(20)\n";
		out += i + "    .right(5)\n";
		out += i + "    .top(15);\n\n";

		out += i + "/* Add bars. */\n";
		out += i + "var bar = vis.add(pv.Bar)\n";
		out += i + "    .data(seriesVals)\n";
		out += i + "    .left(function() x(this.index))\n";
		out += i + "    .width(x.range().band)\n";
		out += i + "    .bottom(0)\n";
		out += i + "    .height(y)\n";
		out += i + "    .fillStyle(\"#1f77b4\");\n\n";
		
		out += i + "bar.anchor(\"top\").add(pv.Label)\n";
		out += i + "	.textBaseline(\"top\")\n";
		out += i + "	.textStyle(\"white\")\n";
		out += i + "	.text(function() seriesVals[this.index].toFixed(2));\n\n";
		
		out += i + "bar.anchor(\"bottom\").add(pv.Label)\n";
		out += i + "    .textMargin(5)\n";
		out += i + "    .textBaseline(\"middle\")\n";
		out += i + "    .textAlign(\"left\")\n";
		out += i + "    .text(function() seriesNames[this.index])\n";
		out += i + "    .textAngle(Math.PI / 2);\n\n";
		
		out += i + "vis.add(pv.Rule)\n";
		out += i + "    .data(y.ticks())\n";
		out += i + "    .bottom(function(d) Math.round(y(d)) - .5)\n";
		out += i + "    .strokeStyle(function(d) d ? \"rgba(255,255,255,.3)\" : \"#000\")\n";
		out += i + "  .add(pv.Rule)\n";
		out += i + "    .left(0)\n";
		out += i + "    .width(5)\n";
		out += i + "    .strokeStyle(\"#000\")\n";
		out += i + "  .anchor(\"left\").add(pv.Label)\n";
		out += i + "    .text(y.tickFormat);\n\n";
		
		out += i + "vis.render();\n\n";

		out +=  "\t\t\t};\n";		
		
        out +=  "\t\t\t</script>\n" +
        		"\t\t</div>\n" +
        		"\t</div>\n";

		out +=  "\t<br><br>\n";
        return out;
    }

	public void setSeriesVals(List<Double> seriesVals) {
		this.seriesVals = seriesVals;
	}

	public List<Double> getSeriesVals() {
		return seriesVals;
	}

	public void setSeriesNames(List<String> seriesNames) {
		this.seriesNames = seriesNames;
	}

	public List<String> getSeriesNames() {
		return seriesNames;
	}

}
