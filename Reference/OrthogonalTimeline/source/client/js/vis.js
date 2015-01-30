///////////////////////////////////////// TODO List /////////////////////////////////////////////////
// 1. Summarize multiple users in GraphView, MatrixView, ScatterPlot View, MapView	
// 2. Support the feature of merging multiple glyphs together or split a summarized glyph into multiple individual glyphs. 
// 3. Statistical information for each glyph : duration, longest thread, abnormaly score, sentiment score, 
/////////////////////////////////////////////////////////////////////////////////////////////////////

var format = d3.time.format.utc("%Y%m%d%H%M"); //%S

BehaviorNet = function() {

	var _self = this;
	var _cells = null;	
	var _size = [800, 600];
	var _bdetail = true;
	
	var _view = 'pack';
	
	var _timeline_range = d3.scale.linear();
	var _thread_range = d3.scale.linear();
	
	var _thread = d3.time.scale();
	var _up_duration = d3.scale.linear();
	var _dw_duration = d3.scale.linear();
	var _esize = d3.scale.linear();
	var _nsize = d3.scale.linear();
	var _margin = {left:10, right:10, top:10, bottom:10};
	var _start_angle = Math.PI/16.0;
	var _end_angle = Math.PI*31/16.0;
	
	var color = d3.scale.linear()
    	.domain([-1.5, 0, 1.5])
    	.range(["#ff3300", "orange", "#66ff33"]);
	
	// rendering related parameters
	var _vis, _vbacteria, _vthreads, _vcontent, _vusers;
	
	//// initialize visualization ////
	_vis = d3.selectAll("#viscomp")
		.append("svg")
		.attr("width", _size[0])
		.attr("height", _size[1]);
	
	var defs = _vis.append('svg:defs');
		defs.append("linearGradient")
		   	.attr("id", "sentiment-gradient")
		   	.attr("gradientUnits", "userSpaceOnUse");
	
	defs.append('svg:marker')
    .attr('id', 'end-arrow')
    .attr('viewBox', '0 -5 10 10')
    .attr('refX', 6)
    .attr('markerWidth', 5)
    .attr('markerHeight', 5)
    .attr('orient', 'auto')
    .append('svg:path')
    .attr('d', 'M0,-5L10,0L0,5');
    
	defs.append('svg:marker')
    .attr('id', 'start-arrow')
    .attr('viewBox', '0 -5 10 10')
    .attr('refX', 6)
    .attr('markerWidth', 6)
    .attr('markerHeight', 6)
    .attr('orient', 'auto')
    .append('svg:circle')
    .attr('cx', 5)
    .attr("cy", 0)
    .attr("r",3);
	
	this.layout = function() {
		if(!_cells) {
			return;
		}
		
		max_thread_duration = -Infinity;
		min_thread_duration = Infinity;
		start = Infinity;
		end = -Infinity;
		// summarize the data
		for(var idx = 0; idx < _cells.length; ++idx) {
			var cell = _cells[idx];
			cell.min_content = Infinity;
			cell.max_content = -Infinity;
			cell.min_up_thread_duration = Infinity;
			cell.max_up_thread_duration = -Infinity;
			cell.min_dw_thread_duration = Infinity;
			cell.max_dw_thread_duration = -Infinity;
			
			for(var i = 0; i < cell.threads.length; ++i) {
				cell.min_content = Math.min(cell.min_content, cell.threads[i].content.length);
				cell.max_content = Math.max(cell.max_content, cell.threads[i].content.length);
				
				var start = format.parse(cell.threads[i].start);
				var touch = format.parse(cell.threads[i].touch);
				var end = format.parse(cell.threads[i].end);
				
				var d = touch.getTime() - start.getTime();
				cell.min_up_thread_duration = Math.min(cell.min_up_thread_duration, d); 
				cell.max_up_thread_duration = Math.max(cell.max_up_thread_duration, d); 
				
				d = end.getTime() - touch.getTime(); 
				cell.min_dw_thread_duration = Math.min(cell.min_dw_thread_duration, d);
				cell.max_dw_thread_duration = Math.max(cell.max_dw_thread_duration, d);
			}
			
			max_thread_duration = Math.max(max_thread_duration, cell.max_up_thread_duration);
			max_thread_duration = Math.max(max_thread_duration, cell.max_dw_thread_duration);
			min_thread_duration = Math.min(min_thread_duration, cell.min_up_thread_duration);
			min_thread_duration = Math.min(min_thread_duration, cell.min_dw_thread_duration);
			start = Math.min(start, format.parse(cell.start));
			end = Math.min(end, format.parse(cell.end));
		}
		
		_timeline_range.domain([start, end]).range([-Math.PI / 2.0 + Math.PI/16, 3 * Math.PI / 2 - Math.PI/16]);
		_thread_range.domain([min_thread_duration, max_thread_duration]);
		
		// layout glyph position
		if (_view == 'graph') {
			var n = _cells.length;
			var force = d3.layout.force()
			    .nodes(_graph.nodes)
			    .links(_graph.links)
			    .size([width, height]);
			force.on("tick", function(e) {
				var q = d3.geom.quadtree(_graph.nodes);
				var i = 0;
				while (++i < n) {
					q.visit(collide(_graph.nodes[i]));
				}
			});
			force.start();
			for (var i = n * n; i > 0; --i) force.tick();
			force.stop();
			for (var i = 0; i < n; ++i) {
				pack(_graph.nodes[i]);
			}
		} else if(_view == 'matrix') {
			var n = _cells.length;
			var size = Math.ceil(Math.sqrt(n));
			var range = Math.min(_size[0] - _margin.left - _margin.right, _size[1] - _margin.top - _margin.bottom);
			var radii = 0.5 * range / size;
			
			for(var i = 0; i < size; ++i) {
				for(var j = 0; j < size; ++j) {
					var idx = i * size + j;
					if(idx >= n) {
						break;
					}
					_cells[idx].x = _margin.left + radii + i * 2 * radii;
					_cells[idx].y = _margin.top + radii + j * 2 * radii;
					_cells[idx].r = radii / 3.0;
					_cells[idx].detail = false;
					_cells[idx].radii = radii;
					pack(_cells[idx]);
				}
			}
		} else if(_view == 'scatterplot'){
		} else if(_view == 'map') {
		} else if(_view == 'pack') {
		
			var root = {};
			root.x = _size[0] / 2.0;
			root.y = _size[1] / 2.0;			
			root.radius = 0;
			root.fixed = true;
			
		    var nodes = [];
		    var n = _cells.length;			
		    nodes.push(root);
			for(var i = 0; i < n; ++i) {
				nodes.push(_cells[i]);
				_cells[i].radius = 30 + 70 * Math.random();
				_cells[i].r = _cells[i].radius / 3.0;
				_cells[i].x = Math.random() * _size[0];
				_cells[i].y = Math.random() * _size[1];
				pack(_cells[i]);
			}
			
			var n = n + 1;
			var force = d3.layout.force()
		    .gravity(0.05)
		    .charge(function(d, i) { return i ? 0 : -500; })
		    .nodes(nodes)
		    .size([_size[0], _size[1]]);
			
		    force.on("tick", function(e) {
				var q = d3.geom.quadtree(nodes),
					i = 0,
					n = _cells.length;
				while (++i < n) q.visit(collide(nodes[i]));
				_vis.selectAll(".cell")
				.attr("transform", function(d){return 'translate ('+d.x+','+d.y+')';});
			});
			
			force.start();
/*
			for (var i = n * n; i > 0; --i) force.tick();
			force.stop();
*/
		} else {
			throw 'Unknow View Type : ' + _view;
		}
		
		return _self;
	};
	
	this.mouseClick = function() {
		var cell = d3.select(this);
		console.log(cell.r); 
        };
	
	this.render = function() {
		console.log('render cell ' + _cells.length);
		var n = _cells.length;
		for (var i = 0; i < n; ++i) {
			paint(_cells[i]);
		}
		return _self;
	};
		
	this.update = function() {
	
		// update cell position
		_vis.selectAll(".cell")
			.transition().duration(1000)
			.attr("transform", function(d){return 'translate ('+d.x+','+d.y+')';});
		
		// update threads:
		tg = _vis.selectAll(".thread");
		tg.selectAll("path")
			.transition().duration(1000)
			.attr("d", function(d) {
				return 'M' + d.x12 + ',' + d.y12 + 'L' + d.x11 + ',' + d.y11 
					+ 'A10,10,0,0,0,' + d.x21 + ',' + d.y21 
					+ 'L' + d.x22 + ',' + d.y22;
			});
		
		tg.selectAll("line")
			.transition().duration(1000)
			.attr("x1", function(d){return d.cx1;})
			.attr("y1", function(d){return d.cy1;})
			.attr("x2", function(d){return d.cx2;})
			.attr("y2", function(d){return d.cy2;});
		
		tg.selectAll("circle")
			.transition().duration(1000)
			.attr("class", "tag")
			.attr("cx", function(d){return d.cx1;})
			.attr("cy", function(d){return d.cy1;})
			.attr("r", function(d){return d.r;});
		
		/*
if(cell.detail) {
			// update thread details
			eg = tg.selectAll(".item");
			eg.selectAll("rect")
			.attr("transform", function(d){ return 'translate('+d.cx+','+d.cy+')'+' rotate('+d.angle+')';})
			.attr("width", function(d){return 3 + Math.log(d.rank + 1);})
			.attr("height", function(d){return d.h;});
									
			ug = bg.selectAll(".user")
			ug.selectAll("circle")
			.attr("cx", function(d){return (d.px+d.ox);})
			.attr("cy", function(d){return (d.py+d.oy);})
			.attr("r", function(d){return d.r;})
			.style("fill", function(d){
				return color(d.sentiment);
			});
		}
*/
	};
	
	this.clean = function() {
		_vis.selectAll(".cell").remove();
	};
	
	function pack(cell) {
	
		_nsize.domain([cell.min_content, cell.max_content]).range([3, 6]);
		
		// 1. layout the overview circular timeline

		cell.tduration = Math.max(cell.max_up_thread_duration, cell.max_dw_thread_duration);
		cell.r0 = cell.r * 4.0 / 5.0;  // r0 is used in layout users 
		// parameters to contorl the timeline arc
		var d = 15 * cell.r * Math.PI / 180, l = cell.r;
		
		// 2. layout the threads and the corresponding content 
		_up_duration.domain([cell.min_up_thread_duration, cell.max_up_thread_duration])
			.range([(2 * cell.r - l) * 0.3, (2 * cell.r - l) * 0.75]);
		_dw_duration.domain([cell.min_dw_thread_duration, cell.max_dw_thread_duration])
			.range([(2 * cell.r - l) * 0.3, (2 * cell.r - l) * 0.75]);
		
		for (var i = 0; i < cell.threads.length; ++i) {
			// 2.1 layout the thread line
			var t = cell.threads[i];
			
			t.r = _nsize(t.content.length);
			
			sdate = format.parse(t.start);
			tdate = format.parse(t.touch);
			edate = format.parse(t.end);
			
			var angle = _timeline_range(sdate);
			t.angle = angle;
			t.x0 = (cell.r0) * Math.cos(angle);
			t.y0 = (cell.r0) * Math.sin(angle);
			
			// near point
			delta = Math.atan( d/(cell.r + l));
			rn = (cell.r + l) / Math.cos(delta);
			t.x11 = rn * Math.cos(angle - delta);
			t.y11 = rn * Math.sin(angle - delta);
			t.cx1 = cell.r * Math.cos(angle);
			t.cy1 = cell.r * Math.sin(angle);
			t.cx2 = (cell.r + l - d) * Math.cos(angle);
			t.cy2 = (cell.r + l - d) * Math.sin(angle);
			t.x21 = rn * Math.cos(angle + delta);
			t.y21 = rn * Math.sin(angle + delta);
			
			// far point
			var length = _up_duration(tdate-sdate);
			delta = Math.atan(d / (cell.r + l + length));
			t.x12 = (cell.r + l + length + 5) * Math.cos(angle - delta);
			t.y12 = (cell.r + l + length + 5) * Math.sin(angle - delta);
			length = _dw_duration(edate-tdate);
			delta = Math.atan(d / (cell.r + l + length));
			t.x22 = (cell.r + l + length + 5) * Math.cos(angle + delta);
			t.y22 = (cell.r + l + length + 5) * Math.sin(angle + delta);
						
			// layout thread details
			if(cell.detail) {
				// 2.2 layout each tweet/email within the thread as a node
				for (var j = 0; j < t.content.length; ++j) {
					var em = t.content[j];
					var time = format.parse(em.time[i]);
					if(time < tdate) {
						length = _up_duration(tdate-sdate);
						_thread.domain([sdate, tdate]).range([0, length]);
						var dr = _thread(time)
						delta = Math.atan(d / (cell.r + l + dr));
						em.cx =  (cell.r + dr + 5) * Math.cos(angle - delta);
						em.cy =  (cell.r + dr + 5) * Math.sin(angle - delta);
						em.r = 5;
						em.h = 2; 
						em.angle = (angle*180/Math.PI)-90;
					} else if(time > tdate){
						length = _dw_duration(edate - tdate);
						_thread.domain([tdate,edate]).range([0, length]);
						var dr = _thread(time);
						delta = Math.atan(d / (cell.r + l + dr));
						em.cx =  (cell.r + dr + 5) * Math.cos(angle + delta);
						em.cy =  (cell.r + dr + 5) * Math.sin(angle + delta);
						em.r = 5;
						em.h = 2; 
						em.angle = (angle*180/Math.PI)-90;
					}
				}
			}
		}
		
		console.log('3. layout the users and run packing algorithm');
		//3. layout the users and run packing algorithm
		if(cell.detail) {
			//step 1: assign paramters to a user node
			for(var i = 0; i < cell.users.length; ++i) {
				var u = cell.users[i];
				u.ox = 0; u.oy = 0; 
				var sum = 0; 	
				for (var j = 0; j < u.behavior.length; ++j) {
					sum = sum + u.behavior[j]; 
				}
				u.px = 0; u.py = 0;  
				for (var j = 0; j < u.behavior.length; ++j){
					u.px += cell.threads[j].x0 * u.behavior[j]/sum; 
					u.py += cell.threads[j].y0 * u.behavior[j]/sum; 
				}
				u.r = 0.7 * Math.log(u.followers_count + 1);
			}
			for (var num = 0; num < 100; ++num){
				//step 2: repel i and j if they overlap 
				for (var i = 0; i < cell.users.length; ++i) {
					for (var j = 0; j< cell.users.length; ++j) {
						var ui = cell.users[i]; 
						var uj = cell.users[j]; 
						var dx = (ui.px + ui.ox) - (uj.px + uj.ox); 
						var dy = (ui.py + ui.oy) - (uj.py + uj.oy);
						var d2 = dx*dx + dy*dy; 
						var rij = ui.r + uj.r ; 
						var rij2 = rij * rij;   
						if (d2 < rij2) {
							var d = Math.sqrt(d2); 
							var f = (rij - d)/rij; 
							var fx = f*dx; var fy = f*dy; 
							if (d==0) {
								dx = Math.random() - 0.5; 
								var dx2 = dx*dx; 
								dy = Math.random() - 0.5; 
								var dy2 = dy*dy;
								fx = Math.sqrt(dx2/(dx2+dy2))*rij; 
								if (dx<0) fx = -fx; 
								fy = Math.sqrt(dy2/(dx2+dy2))*rij; 
								if (dy<0) fy = -fy; 
							}
							uj.ox = uj.ox - fx; uj.oy = uj.oy - fy; 
							ui.ox = ui.ox + fx; ui.oy = ui.oy + fy;
						}
					}
				}
				//step 3: sink to the linear interpolated position, and constrain in the circular timeline 
				for (var i = 0; i < cell.users.length; ++i) {
					var u = cell.users[i];
					u.ox = u.ox*0.95; 
					u.oy = u.oy*0.95; 	
					var dx = u.px+u.ox;
					var dy = u.py+u.oy; 
					var dx2 = dx*dx;
					var dy2 = dy*dy; 
					var d2 = dx2+dy2;
					var r2 = cell.r0 * cell.r0; 
					if (d2 > r2) {
						var d = Math.sqrt(d2); 
						var f = (d-cell.r0)/d;  
						var fx = f*dx; 
						var fy = f*dy;
						u.ox = u.ox - fx; 
						u.oy = u.oy - fy;  
					}
				}
			}
		}
		return _self;
	};
	
	function paint(cell) {
	
		_vis.selectAll("#c" + cell.id).remove();
				
		_vbacteria = _vis.selectAll("#c" + cell.id).data([cell]);	
		var bg = _vbacteria.enter()
			.append("g")
			.attr("id", function(d){return "c" + d.id;})
			.attr("class", "cell")
			.attr("transform", function(d){return 'translate ('+d.x+','+d.y+')';});
			
		_vbacteria.exit().remove();
		
		if(cell.detail) {
			var time = (_cell.tduration / 5000);
			var step = 100 / 5;
			for(var i = 0; i < 5; ++i) {
				bg.append("path").attr("d", 
					d3.svg.arc()
					.innerRadius(function(d){return (d.r + (i + 1) * step);})
					.outerRadius(function(d){return (d.r + (i + 1) * step);})
					.startAngle(function(d){return d.sa;})
					.endAngle(function(d){return d.ea;})
				)
				.attr("class", "timering");
				
				var t = (i + 1) * time;
				
				if(t < 60) {
					t = '+' + t.toFixed(2) + '(sec)'
				} else {
					t = t / 60.0;
					if(t < 60) {
						t = '+' + t.toFixed(2) + '(min)'
					} else {
						t = t / 60;
						if(t < 60) {
							t = '+' + t.toFixed(2) + '(hour)'
						} else {
							t = t / 24
							t = '+' + t.toFixed(2) + '(day)'
						}
					}
				}
				
				bg.append("text")
				.attr("dy", "5")
				.attr("transform", function(d){
					return "translate(" + (-1 * 4.5 * t.length / 2.0) + "," + -1 * (d.r + (i + 1) * step) + ")";
				}).text(t);
			}
		}
		
		bg.attr("transform", function(d){return 'translate ('+d.x+','+d.y+')';})
			.append("path")
			.attr("class", "timeline")
			.attr("d", function(d) {
				var sa = _timeline_range(format.parse(d.start));
				var ea = _timeline_range(format.parse(d.end));
				var sx = d.r * Math.cos(sa);
				var sy = d.r * Math.sin(sa);
				var ex = d.r * Math.cos(ea);
				var ey = d.r * Math.sin(ea);
				return "M" + sx + ',' + sy + 'A' + d.r + ',' + d.r + ',0,1,1,' + ex + ',' + ey;
			})
			.style('marker-end', 'url(#end-arrow)')
			.style('marker-start', 'url(#start-arrow)');
		
		bg.attr("transform", function(d){return 'translate ('+d.x+','+d.y+')';})
			.append("circle")
			.attr("r", function(d) {
				return d.radius;
			})
			.style('fill', "none")
			.style('stroke', 'lightgrey')
			.style('marker-end', 'url(#end-arrow)')
			.style('marker-start', 'url(#start-arrow)');
/*
			d3.svg.arc()
				.innerRadius(function(d){return (d.r-2);})
				.outerRadius(function(d){return (d.r+2);})
				.startAngle(function(d){return d.sa;})
				.endAngle(function(d){return d.ea;})
*/
/*
		bg.append("path")
			.attr("d", function(d){return 'M-12 -'+(d.r+8)+' L-12 -'+(d.r-8)+ ' L12 -'+d.r;})
			.attr("fill", "#A9A9A9")
			.attr("stroke", "none"); 
*/
		
		_vthreads = bg.selectAll(".thread")
				.data(function(d){return d.threads;});
		var tg = _vthreads.enter().append("g").attr("class", "thread");			
			tg.style("stroke", function(d){ 
				return color(d.sentiment);
			})
			.style("fill", function(d){ 
				return color(d.sentiment);
			});
			
		// draw threads:
		tg.append("path")
			.attr("d", function(d){
				return 'M' + d.x12 + ',' + d.y12 + 'L' + d.x11 + ',' + d.y11 
					+ 'A10,10,0,0,0,' + d.x21 + ',' + d.y21 
					+ 'L' + d.x22 +',' + d.y22;})
			.style('marker-end', 'url(#end-arrow)')
			.style('marker-start', 'url(#start-arrow)');
		tg.append("line")
			.attr("x1", function(d){return d.cx1;})
			.attr("y1", function(d){return d.cy1;})
			.attr("x2", function(d){return d.cx2;})
			.attr("y2", function(d){return d.cy2;});
		tg.append("circle")
			.attr("class", "tag")
			.attr("cx", function(d){return d.cx1;})
			.attr("cy", function(d){return d.cy1;})
			.attr("r", function(d){return d.r;});
					
		if(cell.detail) {
			_vcontent = tg.selectAll(".item")
			.data(function(d){return cell.detail ? d.content : [d.content];});
			var	eg = _vcontent.enter().append("g").attr("class", "item");
			eg.append("rect")
			.attr("transform", function(d){ return 'translate('+d.cx+','+d.cy+')'+' rotate('+d.angle+')';})
			.attr("width", function(d){return 3 + Math.log(d.rank + 1);})
			.attr("height", function(d){return d.h;})
			.style("fill", "orange")
			.style("stroke", "orange");
			
			_vusers = bg.selectAll(".user").data(function(d){return d.users;})
			var ug = _vusers.enter().append("g").attr("class", "user");
			ug.append("circle")
				.attr("cx", function(d){return (d.px+d.ox);})
				.attr("cy", function(d){return (d.py+d.oy);})
				.attr("r", function(d){return d.r;})
				.style("fill", function(d){
					return color(d.sentiment);
				});
		} else {

		}
		
			
		$('.user').tipsy({ 
				gravity: 'n', 
				html: true, 
				fade: false, 
				opacity: 1,
				title: function() {
				
				var d = this.__data__;
				
				var tip = "<div style='text-align:left'><div class='contents'>"
					+ "<a href='http://twitter.com/'" + d.screen_name + ">"
						+ "<img class='profile_icon' src='" + d.profile_image_url + "' width=32 height=32 alt='Twitter'>"
					+ "</a>"
					+ "<br>screen_name : " + d.screen_name
					+ "<br>followers : " + d.followers_count
					+ "<br>sentiment : " + d.sentiment
				+"</div></div>";
				return tip;
			}
		});
		
		$('.item').tipsy({ 
			gravity: 'n', 
			html: true, 
			fade: false, 
			opacity: 1,
			title: function() {
			
				var d = this.__data__;			
				
				var tip = ''
				
				if(d.screen_name) {
					tip = "<div style='text-align:left'><div class='contents'>"
					+ "<a href='http://twitter.com/'" + d.screen_name + ">"
						+ "<img class='profile_icon' src='" + d.profile_image_url + "' width=32 height=32 alt='Twitter'>"
					+ "</a>"
					+ "<br>screen_name : " + d.screen_name
					+ "<br>followers : " + d.followers_count
					+ "<br>sentiment : " + d.sentiment
					+"</div></div>";
				} else {
					tip = "<div style='text-align:left'><div class='contents'>"
						+ "<a href='http://twitter.com/'" + d.creator.id + ">"
							+ "<img class='profile_icon' src='" + d.creator.creator_img + "' width=32 height=32 alt='Twitter'>"
						+ "</a>"
						+ "<br>user : " + d.creator.creator
						+ "<br>rt count : " + d.retweet_count
						+ "<br><span class='time'>create at: " + format.parse(d.created_at) + "</span>"
						+ "<br><br>" + d.text
					+"</div>"
					+"<div class='intents' id='web_intent'>"
						+ "<img src='images/retweet_mini.png' width=16 height=16, alt='Retweet'/>"
						+ "<a href='http://twitter.com/intent/retweet?tweet_id="  + d.id + "'>Retweet</a>"
						+ "<img src='images/reply_mini.png' width=16 height=16, alt='Reply'/>"
						+ "<a href='http://twitter.com/intent/tweet?in_reply_to="  + d.id + "'>Reply</a>"
						+ "<img src='images/favorite_mini.png' width=16 height=16, alt='Favorite'/>"
						+ "<a href='http://twitter.com/intent/favorite?tweet_id="  + d.id + "'>Favorite</a>"
					+"</div></div>";
				}
				return tip;
			}
		});
		
		$('.tag').tipsy({ 
			gravity: 'n', 
			html: true, 
			fade: true, 
			opacity: 1,
			title: function() {
			
			var d = this.__data__;
			
			var tip = "<div style='text-align:left'><div class='contents'>"
					+ "content : #" + d.name
					+ "<br><span class='time'>first mention : " + format.parse(d.start) + "</span>"
					+ "<br><span class='time'>last mention : " + format.parse(d.end) + "</span>"
					+ "<br>number of mentions : " + d.content.length
					+ "<br>sentiment : " + d.sentiment
				+"</div></div>";
				return tip;
			}
		});		
		return _self;
	};
	
	function collide(node) {
		var r = node.radius + 16,
	      	nx1 = node.x - r,
	      	nx2 = node.x + r,
	      	ny1 = node.y - r,
	      	ny2 = node.y + r;
	    return function(quad, x1, y1, x2, y2) {
	    	if (quad.point && (quad.point !== node)) {
		    	var x = node.x - quad.point.x,
		    	y = node.y - quad.point.y,
		    	l = Math.sqrt(x * x + y * y),
		    	r = node.radius + quad.point.radius;
		    	if (l < r) {
			    	l = (l - r) / l * .5;
			    	node.x -= x *= l;
			    	node.y -= y *= l;
			    	quad.point.x += x;
			    	quad.point.y += y;
			    }
			}
			return x1 > nx2 || x2 < nx1 || y1 > ny2 || y2 < ny1;
		};
	};
	
	return this;
}