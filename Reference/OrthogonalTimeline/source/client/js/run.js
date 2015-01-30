//"obamacare.behavior.json"
var net = new BehaviorNet();
d3.json("data.json", function(error,data) {
	net.data(data).layout().render();
});