	BehaviorNet.prototype.data = function(x) {
		if(arguments.length == 0) {
			return _cells;
		}
		_cells = x;
		return _self;
	};