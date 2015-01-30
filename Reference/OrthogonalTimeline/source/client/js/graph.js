graph = function() {
	
	this.nodes = [];
	this.links = [];
	this.neighbor = {};
	this.neilinks = {};
	this.metainfo = {};
	
	this.refresh = function() {
		if(this.links.length == 0) {
			return;
		}
		
		var pool = {};
		if(typeof this.links[0].target == 'string') {
			for(var i = 0; i < this.nodes.length; ++i) {
				var sid = id(this.nodes[i]);
				if(sid == '') {
					sid = this.nodes[i].id = 'n' + i;
				}
				pool[sid] = this.nodes[i];
			}
		}
		
		this.neilinks = {};
		this.neighbor = {};
		for(var i = 0; i < this.links.length; ++i) {
			var link = this.links[i];
			var n1 = link.source;
			var n2 = link.target;
			
			if(typeof n1 == 'number') {
				n1 = link.source = this.nodes[n1];
			} else if(typeof n1 == 'string') {
				n1 = link.source = pool[n1];
			}
			if (typeof n2 == 'number') {
				n2 = link.target = this.nodes[n2];
			} else if(typeof n2 == 'string') {
				n2 = link.target = pool[n2];
			}
			
			var id1 = id(n1);
			var id2 = id(n2);
			
			if(!this.neighbor[id1]) {
				this.neighbor[id1] = [];
			} 
			this.neighbor[id1].push(n2);
			if(!this.neighbor[id2]) {
				this.neighbor[id2] = [];
			}
			this.neighbor[id2].push(n1);
			
			if(!this.neilinks[id1]) {
				this.neilinks[id1] = [];
			} 
			this.neilinks[id1].push(link);
			if(!this.neilinks[id2]) {
				this.neilinks[id2] = [];
			}
			this.neilinks[id2].push(link);
		}
	};
	
	this.add = function(node) {
		var nid = id(node);
		if(!this.neighbor[nid]) {
			this.neighbor[nid] = [];
		}
		if(!this.neilinks[nid]) {
			this.neilinks[nid] = [];
		}
		this.nodes.push(node);
	};
	
	this.remove = function(node) {
		this.nodes.splice(this.node.indexOf(node), 1);
		delete this.neighbor[id(node)];
		delete this.neilinks[id(node)];
	};
	
	this.degree = function(n) {
		return this.neighbor[id(n)].length;
	};
	
	this.neighbor = function(n) {
		return this.neighbor[id(n)];
	};
	
	this.ego = function(n) {
		return this.neilinks[id(n)];
	};
	
	this.link = function(n1, n2) {
		var list = this.ego(n1).length > this.ego(n2).length ? this.ego(n2) : this.ego(n1);
		var id1 = id(n1), id2 = id(n2);
		for(var i = 0; i < list.length; ++i) {
			if((id(list[i].target) === id1 && id(list[i].source) === id2) || 
				(id(list[i].target) === id2 && id(list[i].source) === id1)) {
				return list[i];
			}
		}
		return null;
	};
	
	this.connect = function(n1, n2) {
		var nid1 = id(n1);
		var nid2 = id(n2);
		
		if(!this.neighbor[nid1]) {
			if(typeof n1 === 'string') {
				n1 = {id:nid1};
			}
			this.add(n1);
		}
		if(!this.neighbor[nid2]) {
			if(typeof n2 === 'string') {
				n2 = {id:nid2};
			}
			this.add(n2);
		}
		var link = this.link(n1, n2);
		if(link == null) {
			link = {source:n1, target:n2, value:0};
			this.links.push(link);
			this.neighbor[nid1].push(n2);
			this.neighbor[nid2].push(n1);
			this.neilinks[nid1].push(link);
			this.neilinks[nid2].push(link);
		}
		link.value = link.value + 1;
		return link;
	};
	
	this.disconnect = function(link) {
		var n1 = link.source;
		var n2 = link.target;
		
		var nid1 = id(n1);
		var nid2 = id(n2);
		
		this.neighbor[nid1].splice(this.neighbor[nid1].indexOf(n2), 1);
		this.neighbor[nid2].splice(this.neighbor[nid2].indexOf(n1), 1);
		
		this.neilinks[nid1].splice(this.neighbor[nid1].indexOf(link), 1);
		this.neilinks[nid2].splice(this.neighbor[nid2].indexOf(link), 1);
		
		this.links.splice(this.links.indexOf(link), 1);
	};
	
	function id(node) {
		var id = '';
		if(node.id) {
			id = node.id;
		} else if(node._id) {
			id = node._id;
		} else if(node.name) {
			id = node.name;
		} else if(node.label) {
			id = node.label;
		} else if(typeof node === 'string') {
			id = node;
		}
		return id;
	};
};