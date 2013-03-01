var segmentation_colors = ["salmon", "steelblue", "green", "pink", "navy"];
 
function loadScript(url, plot){
	var loadedscript = document.createElement('script');
    loadedscript.setAttribute("type","text/javascript");
  	
    if (loadedscript.readyState){  //IE
        loadedscript.onreadystatechange = function(){
            if (loadedscript.readyState == "loaded" ||
                    loadedscript.readyState == "complete"){
                loadedscript.onreadystatechange = null;
                plot.setLoaded();
            }
        };
    } else {  //Others
        loadedscript.onload = function(){
            plot.setLoaded();
        };
    }
    loadedscript.setAttribute("src", url);
    document.body.appendChild(loadedscript);
}
 
function init_segmentation_plot(start,end,numseries){
	/* Scales and sizing. */
	var w = 810,
	    hOffset = 0,
	    legendOffset = 60,
	    h1 = 3 + 33 * numseries,
	    h2 = 15 + (15 * numseries),
	    totalHeight = h1 + 20 + h2 + hOffset,
	    x = pv.Scale.linear(start, end).range(0, w-legendOffset),
	    i = -1;

	/* Root panel. */
	var vis = new pv.Panel()
	    .width(w)
	    .height(h1 + 20 + h2 + hOffset)
	    .bottom(20)
	    .left(30)
	    .right(20)
	    .top(5);

	vis.render();
	
	var loaded = false;
	
	return {
		setLoaded : function(){loaded = true;},
		isLoaded : function(){return loaded;},
		plot : function(data,seriesNames){
			/* Interaction state. Focus scales will have domain set on-render. */
			var i = {x:0, dx:100},
			    fx = pv.Scale.linear().range(0, w-legendOffset);

			/* Legend area. */
			var legend = vis.add(pv.Panel)
			    .left(0)
			    .width(legendOffset)
			    .height(totalHeight)
			    .top(0);
			legend.add(pv.Label)
			    .data(seriesNames)
			    .textAlign("right")
			    .textBaseline("middle")
			    .top(function() 18+((numseries - (1+this.index)) * 33)) 
			    .height(10)
			    .right(0)
			    .text(function(d) d);
			legend.add(pv.Label)
			    .text('time (secs)')
			    .textAlign("right")
			    .height(10)
			    .right(5)
			    .textBaseline("top")
			    .bottom(15);
			legend.add(pv.Label)
			    .text('time (secs)')
			    .textAlign("right")
			    .height(10)
			    .right(5)
			    .textBaseline("top")
			    .top(hOffset + h1);

			/* Focus panel (zoomed in). */
			var focus = vis.add(pv.Panel)
			    .left(legendOffset)
			    .def("init_data", function() {
			        var d1 = x.invert(i.x),
			            d2 = x.invert(i.x + i.dx);
			        var out = new Array(numseries);
			        for(s=0;s<numseries;s=s+1){;
			            offsetsearch = pv.search.index(data[s], d1, function(d) d.f),
			            firstvisible = offsetsearch >= 0 ? offsetsearch : -(1+offsetsearch),
			            onsetsearch = pv.search.index(data[s], d2, function(d) d.f),
			            lastvisible = onsetsearch >= 0 ? onsetsearch : -(1+onsetsearch),
				          out[s] = data[s].slice(firstvisible,lastvisible+1);
				    }
				    fx.domain(d1, d2);
				    return out;
			      })
			    .top(hOffset)
			    .height(h1);

			/* X-axis ticks. */
			focus.add(pv.Rule)
			    .data(function() fx.ticks())
			    .left(fx)
			    .strokeStyle("#eee")
			  .anchor("bottom").add(pv.Label)
			    .text(fx.tickFormat);

			/* Focus area chart. */
			focus.add(pv.Panel)
			    .overflow("hidden")
			    .data(function(d) focus.init_data())
			  .add(pv.Bar)
			    .data(function(array) array)
			    .overflow("hidden")
			    .left(function(d) fx(d.o))
			    .width(function(d) fx(d.f) - fx(d.o))
			    .bottom(function() 3 + (33*this.parent.index))
			    .height(30)
			    .fillStyle(function() segmentation_colors[this.parent.index % segmentation_colors.length])
			    .strokeStyle("black")
			    .lineWidth(1)
			    .title(function(d) d.l)
			    .anchor("left").add(pv.Label).text(function(d) d.l);

			/* Context panel (zoomed out). */
			var context = vis.add(pv.Panel)
			    .left(legendOffset)
			    .bottom(0)
			    .height(h2);

			/* X-axis ticks. */
			context.add(pv.Rule)
			    .data(x.ticks())
			    .bottom(15)
			    .left(x)
			    .strokeStyle("#eee")
			  .anchor("bottom").add(pv.Label)
			    .text(x.tickFormat);

			/* Y-axis ticks. */
			context.add(pv.Rule)
			    .bottom(15);

			focus.add(pv.Rule)
			    .bottom(0);

			/* Context area chart. */
			context.add(pv.Panel)
			    .data(data)
			    .add(pv.Panel)
			      .data(function(array) array)
			      .left(function(d) x(d.o))
			      .bottom(function() 15 + 3 + (13 * this.parent.index))
			      .height(10)
			      .strokeStyle("Black")
			      .fillStyle(function() segmentation_colors[this.parent.index % segmentation_colors.length])
			      .lineWidth(1);

			/* The selectable, draggable focus region. */
			context.add(pv.Panel)
			    .data([i])
			    .bottom(15)
			    .cursor("crosshair")
			    .events("all")
			    .event("mousedown", pv.Behavior.select())
			    .event("select", focus)
			  .add(pv.Bar)
			    .left(function(d) d.x)
			    .width(function(d) d.dx)
			    .fillStyle("rgba(255, 128, 128, .4)")
			    .cursor("move")
			    .event("mousedown", pv.Behavior.drag())
			    .event("drag", focus);

			vis.render();
			
		}
	};
}