/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.imirsel.nema.analytics.evaluation.resultpages;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

/**
 * A PageItem implementation for plotting one or more sequences of onset times
 * on a timeline.
 * 
 * Implements Javascript rendering using Protovis.
 * 
 * @author kris.west@gmail.com
 * @since 0.3.0
 */
public class ProtovisOnsetPlotItem extends PageItem{

	public static final String INDENT = "\t\t\t";
	public static final DecimalFormat MS_FORMAT = new DecimalFormat("###.# ms");
	public static final DecimalFormat TIMESTAMP_FORMAT = new DecimalFormat("###.###");
	
	private static final int HOFFSET = 0;
	private static final int FOCUSOFFSET = 15;
	private static final int HSEP = 15;
	private static final int LEGENDOFFSET = 60;
	private static final int FOCUS_SERIES_HEIGHT = 23;
	private static final int CONTEXT_SERIES_HEIGHT = 10;
	
	private double startTime;
    private double endTime;
    private Map<String,double[]> series;
    private List<String> seriesNames; 
    private List<Boolean> isGroundtruth;
    
    public ProtovisOnsetPlotItem(String name, String caption, 
    		double startTime, double endTime, 
    		Map<String,double[]> series, List<String> seriesNames, 
    		List<Boolean> isGroundtruth,File outputDir) throws IOException{
    	
        super(name,caption);
        setStartTime(startTime);
        setEndTime(endTime);
        setSeries(series);
        setSeriesNames(seriesNames);
        setIsGroundtruth(isGroundtruth);
        writeOutData(outputDir);
    }
    
    private void writeOutData(File dir) throws IOException{
    	File outFile = new File(dir.getPath() + File.separator + this.getName() + ".js");
    	
    	String out = "var " + this.getName() + "_data = [\n";
        for (int s = 0; s < getSeriesNames().size(); s++) {
        	out += "[";
        	double[] data = series.get(seriesNames.get(s));
			for (int i=0;i<data.length;i++) {
				out += TIMESTAMP_FORMAT.format(data[i]);
				if(i<data.length-1){
					out +=", ";
				}
			}
			if(s<seriesNames.size()-1){
				out +="],\n";
			}else{
				out +="]\n";
			}
		}
        out += "];\n\n";
        
        out += "var " + this.getName() + "_seriesNames = [";
        for (int j = 0; j < getSeriesNames().size(); j++) {
			out += "\"" + getSeriesNames().get(j) + "\"";
			if (j < getSeriesNames().size()-1){
				out += ",";
			}
		}
        out += "];\n";
        
        out += "var " + this.getName() + "_isGroundtruth = [";
        for (int j = 0; j < getIsGroundtruth().size(); j++) {
			out += getIsGroundtruth().get(j);
			if (j < getIsGroundtruth().size()-1){
				out += ",";
			}
		}
        out += "];\n";
        
        
        BufferedWriter writer = null;
        try{
	        writer = new BufferedWriter(new FileWriter(outFile));
	        writer.write(out);
	        writer.flush();
	        
        }finally{
        	if (writer != null){
        		writer.close();
        	}
        }
    }

    public String getHeadStaticDeclarations(){
    	//for javascript debugging use: protovis-d3.2.js
    	//String out = "<script type=\"text/javascript\" src=\"protovis-d3.2.js\"></script>\n" +
		
    	String out = "<script type=\"text/javascript\" src=\"protovis-r3.2.js\"></script>\n\n" +
		"<script type=\"text/javascript+protovis\">\n" +
		"	var predictionColor = \"salmon\";\n" +
		"	var gtColor = \"steelblue\";\n" +
		"	 \n" +
		"	function loadScript(url, plot){\n" +
		"		var loadedscript = document.createElement('script');\n" +
		"	    loadedscript.setAttribute(\"type\",\"text/javascript\");\n" +
		"	  	\n" +
		"	    if (loadedscript.readyState){  //IE\n" +
		"	        loadedscript.onreadystatechange = function(){\n" +
		"	            if (loadedscript.readyState == \"loaded\" ||\n" +
		"	                    loadedscript.readyState == \"complete\"){\n" +
		"	                loadedscript.onreadystatechange = null;\n" +
		"	                plot.setLoaded();\n" +
		"	            }\n" +
		"	        };\n" +
		"	    } else {  //Others\n" +
		"	        loadedscript.onload = function(){\n" +
		"	            plot.setLoaded();\n" +
		"	        };\n" +
		"	    }\n" +
		"	    loadedscript.setAttribute(\"src\", url);\n" +
		"	    document.body.appendChild(loadedscript);\n" +
		"	}\n" +
		"	 \n" +
		"	function init_onset_plot(start,end,numseries){\n" +
		"		/* Scales and sizing. */\n" +
		"		var w = 810,\n" +
		"		    hOffset = " + HOFFSET + ",\n" +
		"			hSep = " + HSEP + ",\n" + 
		"		    legendOffset = " + LEGENDOFFSET + ",\n" +
		"		    h1 = " + FOCUSOFFSET + " + 3 + " + FOCUS_SERIES_HEIGHT + " * numseries,\n" +
		"		    h2 = " + CONTEXT_SERIES_HEIGHT + " * numseries,\n" +
		"		    totalHeight = h1 + 20 + h2 + 15 + hOffset + hSep,\n" +
		"		    x = pv.Scale.linear(start, end).range(0, w-legendOffset),\n" +
		"		    i = -1;\n" +
		"\n" + 
		"		/* Root panel. */\n" +
		"		var vis = new pv.Panel()\n" +
		"		    .width(w)\n" +
		"		    .height(h1 + 20 + h2 + hOffset + hSep)\n" +
		"		    .bottom(25)\n" +
		"		    .left(30)\n" +
		"		    .right(20)\n" +
		"		    .top(10);\n" +
		"\n" + 
		"		vis.render();\n" +
		"		\n" +
		"		var loaded = false;\n" +
		"		\n" +
		"		return {\n" +
		"			setLoaded : function(){loaded = true;},\n" +
		"			isLoaded : function(){return loaded;},\n" +
		"			plot : function(data,seriesNames,isGroundtruth){\n" +
		"				/* Interaction state. Focus scales will have domain set on-render. */\n" +
		"				var i = {x:0, dx:100},\n" +
		"				    fx = pv.Scale.linear().range(0, w-legendOffset);\n" +
		"\n" + 
		"				/* Legend area. */\n" +
		"				var legend = vis.add(pv.Panel)\n" +
		"				    .left(0)\n" +
		"				    .width(legendOffset)\n" +
		"				    .height(totalHeight)\n" +
		"				    .top(0);\n" +
		"				legend.add(pv.Label)\n" +
		"				    .data(seriesNames)\n" +
		"				    .textAlign(\"right\")\n" +
		"				    .textBaseline(\"middle\")\n" +
		"				    .top(function() " + (FOCUSOFFSET + 18) + " +((numseries - (1+this.index)) * " + FOCUS_SERIES_HEIGHT + ")) \n" +
		"				    .height(10)\n" +
		"				    .right(0)\n" +
		"				    .text(function(d) d);\n" +
		"				legend.add(pv.Label)\n" +
		"				    .text('time (secs)')\n" +
		"				    .textAlign(\"right\")\n" +
		"				    .height(10)\n" +
		"				    .right(5)\n" +
		"				    .textBaseline(\"top\")\n" +
		"				    .bottom(15);\n" +
		"				legend.add(pv.Label)\n" +
		"				    .text('time (secs)')\n" +
		"				    .textAlign(\"right\")\n" +
		"				    .height(10)\n" +
		"				    .right(5)\n" +
		"				    .textBaseline(\"top\")\n" +
		"				    .top(hOffset + h1);\n" +
		"\n" + 
		"				/* Focus panel (zoomed in). */\n" +
		"				var focus = vis.add(pv.Panel)\n" +
		"				    .left(legendOffset)\n" +
		"				    .def(\"init_data\", function() {\n" +
		"				        var d1 = x.invert(i.x),\n" +
		"				            d2 = x.invert(i.x + i.dx);\n" +
		"				        var out = new Array(numseries);\n" +
		"				        for(s=0;s<numseries;s=s+1){;\n" +
		"				            offsetsearch = pv.search.index(data[s], d1, function(d) d),\n" +
		"				            firstvisible = offsetsearch >= 0 ? offsetsearch : -(1+offsetsearch),\n" +
		"				            onsetsearch = pv.search.index(data[s], d2, function(d) d),\n" +
		"				            lastvisible = onsetsearch >= 0 ? onsetsearch : -(1+onsetsearch),\n" +
		"					          out[s] = data[s].slice(firstvisible,lastvisible+1);\n" +
		"					    }\n" +
		"					    fx.domain(d1, d2);\n" +
		"					    return out;\n" +
		"				      })\n" +
		"					.def(\"focus_length\", function() {\n" +
		"						return \"showing: \" + x.invert(i.x).toFixed(2) + \" to \" + x.invert(i.x + i.dx).toFixed(2) + \" seconds\";\n" + 
		"					 })\n" +
		"				    .top(hOffset)\n" +
		"				    .height(h1);\n" +
		"\n" + 
		"				/* X-axis ticks. */\n" +
		"				focus.add(pv.Rule)\n" +
		"				    .data(function() fx.ticks())\n" +
		"				    .left(fx)\n" +
		"				    .strokeStyle(\"#aaa\")\n" +
		"				  .anchor(\"bottom\").add(pv.Label)\n" +
		"				    .text(fx.tickFormat);\n" +
		"\n" + 
		"				/* Focus area chart. */\n" +
		"				focus.add(pv.Panel)\n" +
		"				    .overflow(\"hidden\")\n" +
		"				    .data(function() focus.init_data())\n" +
		"				  .add(pv.Dot)\n" +
		"					.data(function(array) array)\n" +
		"				    .overflow(\"hidden\")\n" +
		"				    .lineWidth(1.5)\n" +
		"				    .antialias(true)\n" +
		"				    .left(function(d) fx(d))\n" +
		"				    .bottom(function() 8 + (" + FOCUS_SERIES_HEIGHT + "*this.parent.index))\n" +
		"				    .size(7)\n" +
//		"				    .strokeStyle(function() isGroundtruth == true ? gtColor : predictionColor)\n" +
//		"				    .fillStyle(function() this.strokeStyle().alpha(.2))\n" +
		"				    .fillStyle(function() isGroundtruth[this.parent.index] ? gtColor : predictionColor)\n" +
		"				    .strokeStyle(null)\n" +
		"				    .title(function(d) d.toFixed(2));\n" +
		"\n" + 
		"				focus.add(pv.Label)\n" + 
		"				   .right(10)\n" +  
		"				   .top(12)\n" +  
		"				   .textAlign(\"right\")\n" +  
		"				   .text(function() focus.focus_length());\n" +  
		"\n" + 
		"				focus.add(pv.Dot)\n" + 
		"				   .data([\"Predictions\"])\n" +  
		"				   .left(10)\n" +  
		"				   .top(12)\n" + 
		"				   .size(7)\n" + 
		"				   .fillStyle(predictionColor)\n" +
		"				   .strokeStyle(null)\n" +
		"				   .anchor(\"right\").add(pv.Label);\n" +  
		"\n" + 
		"				focus.add(pv.Dot)\n" + 
		"				   .data([\"Ground-truth\"])\n" +  
		"				   .left(90)\n" +  
		"				   .top(12)\n" + 
		"				   .size(6)\n" + 
		"				   .fillStyle(gtColor)\n" +
		"				   .strokeStyle(null)\n" +
		"				   .anchor(\"right\").add(pv.Label);\n" +  
		"\n" + 
		"				/* Context panel (zoomed out). */\n" +
		"				var context = vis.add(pv.Panel)\n" +
		"				    .left(legendOffset)\n" +
		"				    .bottom(0)\n" +
		"				    .height(h2);\n" +
		"\n" + 
		"				/* X-axis ticks. */\n" +
		"				context.add(pv.Rule)\n" +
		"				    .data(x.ticks())\n" +
//		"				    .bottom(15)\n" +
		"				    .left(x)\n" +
		"				    .strokeStyle(\"#eee\")\n" +
		"				  .anchor(\"bottom\").add(pv.Label)\n" +
		"				    .text(x.tickFormat);\n" +
		"\n" + 
		"				context.add(pv.Rule)\n" +
		"				    .bottom(0);\n" +
		"				context.add(pv.Rule)\n" +
		"				    .left(0);\n" +
		"\n" + 
		"				focus.add(pv.Rule)\n" +
		"				    .bottom(0);\n" +
		"				focus.add(pv.Rule)\n" +
		"				    .left(0);\n" +
		"\n" + 
		"				/* Context area chart. */\n" +
		"				context.add(pv.Panel)\n" +
		"				    .data(data)\n" +
		"				  .add(pv.Dot)\n" +
		"					.data(function(array) array)\n" +
		"				    .overflow(\"hidden\")\n" +
		"				    .lineWidth(1.5)\n" +
		"				    .antialias(true)\n" +
		"				    .left(function(d) x(d))\n" +
		"				    .bottom(function() 3 + (" + CONTEXT_SERIES_HEIGHT + "*this.parent.index))\n" +
		"				    .height(10)\n" +
		"				    .size(3)\n" +
		"				    .strokeStyle(null)\n" +
		"				    .fillStyle(function() isGroundtruth[this.parent.index] ? gtColor : predictionColor)\n" +
		"				    .title(function(d) d.toFixed(2));\n" +
		"\n" + 
		"				/* The selectable, draggable focus region. */\n" +
		"				context.add(pv.Panel)\n" +
		"				    .data([i])\n" +
		"				    .bottom(0)\n" +
		"				    .cursor(\"crosshair\")\n" +
		"				    .events(\"all\")\n" +
		"				    .event(\"mousedown\", pv.Behavior.select())\n" +
		"				    .event(\"select\", focus)\n" +
		"					.title(\"click and drag to select new focus region\")" + 
		"				  .add(pv.Bar)\n" +
		"				    .left(function(d) d.x)\n" +
		"				    .width(function(d) d.dx)\n" +
		"				    .fillStyle(\"rgba(255, 128, 128, .4)\")\n" +
		"				    .strokeStyle(\"rgb(255, 128, 128)\")\n" +
		"				    .lineWidth(1)\n" +
		"				    .antialias(false)\n" +
		"					.title(\"drag to move focus region\")" + 
		"				    .cursor(\"move\")\n" +
		"				    .event(\"mousedown\", pv.Behavior.drag())\n" +
		"				    .event(\"drag\", focus);\n" +
		"\n" + 
		"					vis.render();\n" +
		"					\n" +
		"				}\n" +
		"			};\n" +
		"		}\n" +
		"</script>\n";
		return out;
    }
    
    @Override
    public String getHeadData(){
    	return "";
    }
    
    @Override
    public String getBodyData(boolean topLink){
    	System.out.println("generating onset plot HTML for: " + getName());
    	String out = "\t<a name=\"" + getName() + "\"></a>\n" +
        "\t<h4>" + getCaption();
		if (topLink){
		    out += "&nbsp;&nbsp;&nbsp;&nbsp;<span class=\"toplink\"><a href=\"#top\">[top]</a></span>";
		}
		out += "</h4>\n";
		
		int height = HOFFSET + FOCUSOFFSET + (3 + FOCUS_SERIES_HEIGHT * series.size()) + HSEP + (CONTEXT_SERIES_HEIGHT * series.size()) + 20 + 2 + 20 + 10;
		
		out += 	"\t<div id=\"center\">\n" + 
		       	"\t\t<div style=\"width: 860px; height: " + height + "px;; padding: 2px; margin: 3px; border-width: 1px; border-color: black; border-style:solid;\">\n";
		
		out +=  "\t\t<script type=\"text/javascript+protovis\">\n" +
				"\t\t\tvar " + getName() + "_onset_plot = init_onset_plot(" + 
				startTime + ", " + endTime + ", " + series.size() + ");\n\n" +
				"\t\t\tvar " + getName() + "_interval;\n" +
				"\t\t\tfunction " + getName() + "_serviceInterval(){\n" +
				"\t\t\t\tif(" + getName() + "_onset_plot.isLoaded()){\n" +
				"\t\t\t\t\tclearInterval(" + getName() + "_interval);\n" + 
				"\t\t\t\t\t" + getName() + "_onset_plot.plot(" + getName() + "_data," + getName() + "_seriesNames," + getName() + "_isGroundtruth);\n" +
				"\t\t\t\t\tdocument.getElementById(\"" + getName() + "_button\").setAttribute(\"value\",\"done.\");\n" +
				"\t\t\t\t}\n" + 
				"\t\t\t}\n" +
				"\t\t\t</script>\n\n" +
				"\t\t\t</div>\n" + 
				//add button to trigger plot function
				"\t\t\t<div style=\"text-align:left;padding-left:10px;\">\n" +
				"\t\t\t\t<input type=\"button\" value=\"Plot\" id=\"" + getName() + "_button\" onClick=\"\n" +
				"\t\t\t\t\tthis.value='loading data...';\n" + 
				"\t\t\t\t\tthis.disabled=true;\n" + 
				"\t\t\t\t\tloadScript('" + getName() + ".js'," + getName() + "_onset_plot);\n" + 
				"\t\t\t\t\t" + getName() + "_interval = setInterval('" + getName() + "_serviceInterval()',500);\n" + 
				"\t\t\t\t\t\">\n" + 
				"\t\t\t\t<label for=\"" + getName() + "_button\">Click here to plot the figure</label>\n" +
				"\t\t\t\t&nbsp;&nbsp;<a href=\"" + getName() + ".js\">download JSON data file</a>\n" + 
				"\t\t\t</div>\n" + 
				"\t\t</div>\n<br>\n";
        
        return out;
    }

	public void setStartTime(double startTime) {
		this.startTime = startTime;
	}

	public double getStartTime() {
		return startTime;
	}

	public void setEndTime(double endTime) {
		this.endTime = endTime;
	}

	public double getEndTime() {
		return endTime;
	}

	public void setSeries(Map<String,double[]> series) {
		this.series = series;
	}

	public Map<String,double[]> getSeries() {
		return series;
	}


	public void setSeriesNames(List<String> seriesNames) {
		this.seriesNames = seriesNames;
	}


	public List<String> getSeriesNames() {
		return seriesNames;
	}


	public void setIsGroundtruth(List<Boolean> isGroundtruth) {
		this.isGroundtruth = isGroundtruth;
	}


	public List<Boolean> getIsGroundtruth() {
		return isGroundtruth;
	}

}
