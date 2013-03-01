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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.imirsel.nema.model.NemaDataConstants;
import org.imirsel.nema.model.NemaSegment;

/**
 * A PageItem implementation for plotting one of more functions over a set of
 * fixed timesteps. Options are provided to downsample the function plotted.
 * 
 * Implements Javascript rendering using Protovis.
 * 
 * @author kris.west@gmail.com
 */
public class ProtovisFunctionTimestepPlotItem extends PageItem{

	public static final String INDENT = "\t\t\t";
	public static final DecimalFormat MS_FORMAT = new DecimalFormat("###.# ms");
	public static final DecimalFormat TIMESTAMP_FORMAT = new DecimalFormat("###.###");
	
	private double startTime;
    private double endTime;
    private Map<String,double[][]> series;
    private List<String> seriesNames; 
    private String yAxisLabel; 
    
    private static final int HOFFSET = 15;
	private static final int FOCUSOFFSET = 15;
	private static final int HSEP = 15;
	private static final int FOCUS_HEIGHT = 300;
	private static final int CONTEXT_HEIGHT = 40;
	private static final int LEGENDOFFSET = 100;
	
    private double resolutionInSecs;

    public ProtovisFunctionTimestepPlotItem(String name, String caption, 
    		double startTime, double endTime, double currentResolutionSecs,
    		double targetResolutionSecs, String yAxisLabel, 
    		Map<String,double[][]> series, List<String> seriesNames,
    		File outputDir) throws IOException{
    	
        super(name,caption);
        setStartTime(startTime);
        setEndTime(endTime);
        setResolutionInSecs(currentResolutionSecs);
        setSeries(reduceFunctionResolution(getResolutionInSecs(),targetResolutionSecs,series));
        setSeriesNames(seriesNames);
        setyAxisLabel(yAxisLabel);
        writeOutData(outputDir);
    }
    
    private void writeOutData(File dir) throws IOException{
    	File outFile = new File(dir.getPath() + File.separator + this.getName() + ".js");
    	
    	String out = "var " + this.getName() + "_data = [\n";
        for (int s = 0; s < getSeriesNames().size(); s++) {
        	out += "[";
        	double[][] data = series.get(getSeriesNames().get(s));
        	
        	for (int j = 0; j < data.length-1; j++) {
				out += "{x: " + TIMESTAMP_FORMAT.format(data[j][0]) + ", y: " + data[j][1] + "},\n";
			}
			out += "{x: " + data[data.length-1][0] + ", y: " + data[data.length-1][1] + "}";
			if(s < getSeriesNames().size()-1){
				out += "],\n";
			}else{
				out += "]\n";
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
        
        out += "var " + this.getName() + "_yAxisLabel = \"" + yAxisLabel + "\";\n";
        
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
    
    private Map<String,double[][]> reduceFunctionResolution(double currentIncrement, double targetIncrement, Map<String,double[][]> series){
    	if(currentIncrement >= targetIncrement){
    		setResolutionInSecs(currentIncrement);
    		System.out.println("Not reducing function from resolution to ");
    		//no need to reduce - should be safe to return original as downstream processing is not expected modify it
    		return series;
    	}else{
    		//Return new map with modified data
    		Map<String,double[][]> out = new HashMap<String, double[][]>(series.size());
    		
    		String[] seriesNames = series.keySet().toArray(new String[series.size()]);
    		for (int s = 0; s < seriesNames.length; s++) {
    			double[][] data = series.get(seriesNames[s]);
	    		
    			/* 
    			 * Set up the 0th-order interpolation to convert to the 
    			 * target time-grid 
    			 */
    			List<Double> interpTimeStamp = new ArrayList<Double>();
    			List<Double> interpValue = new ArrayList<Double>();
    			int nrows = data.length;
	    		
    			/* Indices into the new, interpolated data array-list */
    	        int index = 0;
    	        int oldindex = 0;

    	        /*
    	         *  minDiff and currDiff represent time-stamp differences to make 
    	         *  sure the value we use in the original data is the one 
    	         *  closest-in-time to the desired time-stamp
    	         */
    	        double minDiff = 10000000.0;
    	        double currDiff = 0.0;
    	        
    	        //init with first values
    	        interpTimeStamp.add(data[0][0]);
    	        interpValue.add(data[0][1]);
    	        
    	        /* Loop through original arbitrary time-stamped data */
    	        for (int i = 1; i < nrows; i++) {
    	            index = (int)Math.round(data[i][0]/targetIncrement);
    	            
    	            /* Case where the file's time-step is less than targetIncrement */
    	            if (index == oldindex) {
    	                currDiff = Math.abs(data[i][0] - targetIncrement*(double)index);
    	                if (currDiff < minDiff) {	
    	                	interpValue.set(index, new Double(data[i][1]));
    	                	interpTimeStamp.set(index, new Double(targetIncrement*(double)index));
    	                    minDiff = currDiff;
    	                }
    	            }
    	            
    	         	/*
    	         	 *  Case where the file's time-step is targetIncrement or has 'caught up' if 
    	         	 *  less than target increment and gone on to the next index in the targetIncrement grid
    	         	 */
    	            else if (index == oldindex + 1) {
    	            	interpValue.add(new Double(data[i][1]));
    	            	interpTimeStamp.add(new Double(targetIncrement*(double)index));
    	                minDiff = Math.abs(data[i][0] - targetIncrement*(double)index);
    	            }
    	            
    	            oldindex = index;                                
    	        }   
    			
    	        /*
    	         *  Put the contents of the Time-stamp and values array-lists into a 
    	         *  single 2 column 2d-double array 
    	         */
    	        double[][] interpolatedData = new double[interpValue.size()][2];
    	        for (int i = 0; i < interpolatedData.length; i++) {
    	        	interpolatedData[i][0] = (interpTimeStamp.get(i)).doubleValue();
    	        	interpolatedData[i][1] = (interpValue.get(i)).doubleValue();
    	        }
    			
    	        out.put(seriesNames[s], interpolatedData);
    			
    		}
    		setResolutionInSecs(targetIncrement);
    		return out;
    		
    	}
    }


    public String getHeadStaticDeclarations(){
    	//for javascript debugging use: protovis-d3.2.js
    	//String out = "<script type=\"text/javascript\" src=\"protovis-d3.2.js\"></script>\n" +
		
    	String out = "<script type=\"text/javascript\" src=\"protovis-r3.2.js\"></script>\n\n" +
		"<script type=\"text/javascript+protovis\">\n" +
		"	var colors = [\"salmon\", \"steelblue\", \"khakie\", \"green\", \"navy\"];\n" + 
		"	var scaleToFit = false;\n\n" +
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
		"	function init_melody_plot(start,end,numseries){\n" +
		"		/* Scales and sizing. */\n" +
		"		var w = 840,\n" +
		"		    hOffset = " + HOFFSET + ",\n" +
		"			hSep = " + HSEP + ",\n" + 
		"		    legendOffset = " + LEGENDOFFSET + ",\n" +
		"		    h1 = " + FOCUS_HEIGHT + ",\n" +
		"		    h2 = " + CONTEXT_HEIGHT + ",\n" +
		"		    totalHeight = h1 + 20 + h2 + 15 + hOffset + hSep,\n" +
		"		    x = pv.Scale.linear(start, end).range(0, w-legendOffset),\n" + 
		"		    i = -1;\n" +
		"\n" + 
		"		/* Root panel. */\n" +
		"		var vis = new pv.Panel()\n" +
		"		    .width(w)\n" +
		"		    .height(h1 + 20 + h2 + hOffset + hSep)\n" +
		"		    .bottom(25)\n" +
		"		    .left(10)\n" +
		"		    .right(10)\n" +
		"		    .top(10);\n" +
		"\n" + 
		"		vis.render();\n" +
		"		\n" +
		"		var loaded = false;\n" +
		"		\n" +
		"		return {\n" +
		"			toggleScaling : function() { scaleToFit = !scaleToFit;vis.render(); },\n" + 
		"			setLoaded : function(){loaded = true;},\n" +
		"			isLoaded : function(){return loaded;},\n" +
		"			plot : function(data,seriesNames,yAxisLabel){\n" +
		"				/* Interaction state. Focus scales will have domain set on-render. */\n" +
		"				var scale = -1;\n" + 
		"				for(s=0;s<numseries;s=s+1){;\n" +
		"					var tmp = pv.max(data[s], function(d) d.y);\n" + 
		"					if(scale < tmp){scale = tmp;}\n" +
		"				}\n" +
		
		
		"		    	y = pv.Scale.linear(0, scale+1).range(0, h2);\n" + 
		
		"				var i = {x:0, dx:100},\n" +
		"				    fx = pv.Scale.linear().range(0, w-legendOffset);\n" +
		"				    fy = pv.Scale.linear().range(0, h1);\n\n" + 
		"\n" + 
		
		"				/* Legend area. */\n" +
		"				var legend = vis.add(pv.Panel)\n" +
		"				    .overflow(\"hidden\")\n" +
		"				    .left(0)\n" +
		"				    .width(legendOffset)\n" +
		"				    .height(totalHeight)\n" +
		"				    .top(0);\n" +
		"				/* Y axis label */\n" + 
		"				legend.add(pv.Label)\n" +
		"				    .textAngle(-Math.PI / 2)\n" +
		"				    .textAlign(\"center\")\n" +
		"				    .textBaseline(\"bottom\")\n" +
		"				    .top(h1/2) \n" +
		"				    .height(h1)\n" +
		"				    .right(30)\n" +
		"				    .text(yAxisLabel);\n" +
		"				/* context X axis label */\n" + 
		"				legend.add(pv.Label)\n" +
		"				    .text('time (secs)')\n" +
		"				    .textAlign(\"right\")\n" +
		"				    .height(10)\n" +
		"				    .right(10)\n" +
		"				    .textBaseline(\"top\")\n" +
		"				    .bottom(15);\n" +
		"				/* focus X axis label */\n" + 
		"				legend.add(pv.Label)\n" +
		"				    .text('time (secs)')\n" +
		"				    .textAlign(\"right\")\n" +
		"				    .height(10)\n" +
		"				    .right(10)\n" +
		"				    .textBaseline(\"top\")\n" +
		"				    .top(hOffset + h1);\n" +
		"				/* series legend */\n" + 
		"				legend.add(pv.Dot)\n" + 
		"				  .data(seriesNames)\n" + 
		"				  .left(5)\n" + 
		"				  .top(function() 10 + this.index * 12)\n" + 
		"				  .height(10)\n" + 
		"				  .size(8)\n" + 
		"				  .strokeStyle(null)\n" + 
		"				  .fillStyle(function() colors[this.index % colors.length])\n" + 
		"				  .anchor(\"right\").add(pv.Label);\n\n" + 
		"\n" + 
		"\n" + 
		"				/* Focus panel (zoomed in). */\n" +
		"				var focus = vis.add(pv.Panel)\n" +
		"				    .left(legendOffset)\n" +
		"				    .def(\"init_data\", function() {\n" +
		"				        var d1 = x.invert(i.x),\n" +
		"				            d2 = x.invert(i.x + i.dx);\n" +
		"				        var out = new Array(numseries);\n" +
		"						var scale = -1;\n" + 
		"				        for(s=0;s<numseries;s=s+1){;\n" +
		"				            offsetsearch = pv.search.index(data[s], d1, function(d) d.x),\n" +
		"				            firstvisible = offsetsearch >= 0 ? offsetsearch : -(1+offsetsearch),\n" +
		"				            onsetsearch = pv.search.index(data[s], d2, function(d) d.x),\n" +
		"				            lastvisible = onsetsearch >= 0 ? onsetsearch : -(1+onsetsearch),\n" +
		"					        out[s] = data[s].slice(firstvisible > 0 ? firstvisible-1:0,lastvisible+1);\n" +
		"					        var tmp = pv.max(out[s], function(d) d.y);\n" + 
		"					        if(scale < tmp){scale = tmp;}\n" +
		"					    }\n" +
		"					    fx.domain(d1, d2);\n" +
		"						fy.domain(scaleToFit ? [0, scale+1] : y.domain());\n" + 
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
		"				/* Y-axis ticks. */\n" +
		"				focus.add(pv.Rule)\n" +
		"				    .data(function() fy.ticks(9))\n" +
		"				    .bottom(fy)\n" +
		"				    .strokeStyle(\"#aaa\")\n" +
		"				  .anchor(\"left\").add(pv.Label)\n" +
		"				    .text(fy.tickFormat);\n" +
		"\n" + 
		"				/* Focus area chart. */\n" +
		"				focus.add(pv.Panel)\n" +
		"				    .overflow(\"hidden\")\n" +
		"				    .data(function() focus.init_data())\n" +
		"				  .add(pv.Line)\n" +
		"				    .data(function(array) array)\n" +
		"				    .segmented(true)\n" +
		"				    .left(function(d) fx(d.x))\n" +
		"				    .bottom(function(d) fy(d.y))\n" +
		"				    .strokeStyle(function(d) pv.color(colors[this.parent.index % colors.length]))\n" +
		"				    .visible(function(d) d.y > 0 ? true : false)\n" +
		"				    .lineWidth(2);\n" +
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
		"				/* Add a note on focus region size */\n" + 
		"				focus.anchor(\"right\").add(pv.Label)\n" + 
		"				   .right(10)\n" +  
		"				   .top(-10)\n" +  
		"				   .textAlign(\"right\")\n" + 
		"				   .textBaseline(\"bottom\")\n" + 
		"				   .text(function() focus.focus_length());\n" +  
		"\n" + 
		"				/* Add a note on resolution */\n" + 
		"				focus.anchor(\"top\").add(pv.Label)\n" + 
		"				  .right(function() this.parent.width()/2)\n" + 
		"				  .top(-10)\n" + 
		"				  .textAlign(\"center\")\n" + 
		"				  .textBaseline(\"bottom\")\n" + 
		"				  .text(\"Resolution: " + MS_FORMAT.format(resolutionInSecs*1000.0) + "\");\n" + 
		"\n" + 
		"				/* Context area chart. */\n" +
		"				context.add(pv.Panel)\n" +
		"				    .overflow(\"hidden\")\n" +
		"				    .data(data)\n" +
		"				    .add(pv.Line)\n" +
		"				      .data(function(array) array)\n" +
		"				      .left(function(d) x(d.x))\n" +
		"				      .bottom(function(d) y(d.y))\n" +
		"				      .segmented(true)\n" +
		"				      .strokeStyle(function(d) pv.color(colors[this.parent.index % colors.length]))\n" +
		"				      .visible(function(d) d.y > 0 ? true : false)\n" +
		"				      .lineWidth(1.5);\n" +
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
    	System.out.println("generating line plot HTML for: " + getName());
    	String out = "\t<a name=\"" + getName() + "\"></a>\n" +
        "\t<h4>" + getCaption();
		if (topLink){
		    out += "&nbsp;&nbsp;&nbsp;&nbsp;<span class=\"toplink\"><a href=\"#top\">[top]</a></span>";
		}
		out += "</h4>\n";
		
		int height = HOFFSET + FOCUSOFFSET + FOCUS_HEIGHT + HSEP + CONTEXT_HEIGHT + 20 + 2 + 20 + 10;
		
		out += 	"\t<div id=\"center\">\n" + 
		       	"\t\t<div style=\"width: 860px; height: " + height + "px; padding: 2px; margin: 3px; border-width: 1px; border-color: black; border-style:solid;\">\n";
		
		out +=  "\t\t<script type=\"text/javascript+protovis\">\n" +
				"\t\t\tvar " + getName() + "_melody_plot = init_melody_plot(" + 
				startTime + ", " + endTime + ", " + series.size() + ");\n\n" +
				"\t\t\tvar " + getName() + "_interval;\n" +
				"\t\t\tfunction " + getName() + "_serviceInterval(){\n" +
				"\t\t\t\tif(" + getName() + "_melody_plot.isLoaded()){\n" +
				"\t\t\t\t\tclearInterval(" + getName() + "_interval);\n" + 
				"\t\t\t\t\t" + getName() + "_melody_plot.plot(" + getName() + "_data," + getName() + "_seriesNames,\"" + getyAxisLabel() + "\");\n" +
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
				"\t\t\t\t\tloadScript('" + getName() + ".js'," + getName() + "_melody_plot);\n" + 
				"\t\t\t\t\t" + getName() + "_interval = setInterval('" + getName() + "_serviceInterval()',500);\n" + 
				"\t\t\t\t\t\">\n" + 
				"\t\t\t\t<label for=\"" + getName() + "_button\">Click here to plot the figure</label>\n" +
//				"\t\t\t\t<input checked id=\"scale_" + getName() + "\" type=\"checkbox\" onMouseUp=\"" + getName() + "_melody_plot.toggleScaling()\">\n" +
//        		"\t\t\t\t<label for=\"scale_" + getName() + "\">Scale to fit</label>\n" +
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

	public void setSeries(Map<String,double[][]> series) {
		this.series = series;
	}

	public Map<String,double[][]> getSeries() {
		return series;
	}

	public void setResolutionInSecs(double resolutionInSecs) {
		this.resolutionInSecs = resolutionInSecs;
	}

	public double getResolutionInSecs() {
		return resolutionInSecs;
	}

	public void setSeriesNames(List<String> seriesNames) {
		this.seriesNames = seriesNames;
	}


	public List<String> getSeriesNames() {
		return seriesNames;
	}

	public void setyAxisLabel(String yAxisLabel) {
		this.yAxisLabel = yAxisLabel;
	}

	public String getyAxisLabel() {
		return yAxisLabel;
	}

}
