dojo.require("dojox.charting.widget.Legend");
dojo.require("dojox.charting.Chart2D");

// Require the theme of our choosing
// "Claro", new in Dojo 1.6, will be used
dojo.require("dojox.charting.themes.Dollar");

function createChart(cData) {
	
//this legend is created within an element with a "legend1" ID.
//console.debug("*** Creating data");
chartData30 = [ 8900, 8200, 5811, 6600, 7662, 3887, 7200, 7222, 7000, 8509, 8888, 3099 ];
chartData20 = [ 1000, 2000, 7733, 1876, 2783, 2899, 3888, 3277, 4299, 2345, 8345, 8763 ];
chartData10 = [ 1000, 1500, 7233, 1576, 1783, 1899, 2888, 1277, 3299, 1345, 1345, 1763 ];


//cData = {s2:chartData20,s3:chartData10};
var keys = Object.keys(cData);
//console.debug("*** keys=",keys);
//keys.forEach(function(key) {
//    var value = cData[key];
//    console.log("key:",key,"  value:",value);
//});
//var chartData = JSON.parse("["+ s +"]");


//console.debug("creating chart cdahartdata="+cData);
var chart = new dojox.charting.Chart2D("chartNode");

// Set the theme
chart.setTheme(dojox.charting.themes.Dollar);

// Add the only/default plot
//chart.addPlot("default", {type : "StackedAreas",markers  : true});
//chart.addPlot("default", {type: "Lines"});
chart.addPlot("default", {type: "Markers"});


// Add axes
chart.addAxis("x");
chart.addAxis("y", {
	min : 0,
	max : 9000,
	vertical : true,
	fixLower : "major",
	fixUpper : "major"
});

//console.debug("adding series");
keys.forEach(function(key) {
    var value = cData[key];
    //console.log("key:",key,"  value:",value);
    chart.addSeries("RPS-"+key, value, {
    	stroke : {
    		color : "blue",
    		width : 2
    	},
    	fill : "#FFFFFF"
    });

});


//console.debug("rending");
chart.render();
var legend1 = new dojox.charting.widget.Legend({
	chart : chart
}, "legend");

}
