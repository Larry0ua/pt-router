var map;
var _data;
var lines = [];
var start, end;
$(document).ready(function() {
    map = L.map('map').setView([48.29, 25.93], 13);
//    new L.TileLayer('http://openmapsurfer.uni-hd.de/tiles/roads/x={x}&y={y}&z={z}', {
//          maxZoom: 19,
//          attribution: "Map data &copy; <a href='http://osm.org'>OpenStreetMap</a> contributors, rendering <a href=\"http://giscience.uni-hd.de/\" target=\"_blank\">GIScience Research Group @ Heidelberg University</a>"}
//    ).addTo(map);
    new L.TileLayer('http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {maxZoom: 19, attribution: "Map data &copy; <a href='http://osm.org'>OpenStreetMap</a> contributors"}).addTo(map);
    map.on('click', function(e) {
        if (start && end) return;
        var m = L.marker(e.latlng, {draggable:true}).addTo(map);
        if (start) {
            end = m;
            end.bindPopup(
            "<a href='#' onclick='findRoutes(0)'>Find route</a><br>"+
            "<a href='#' onclick='findRoutes(1)'>Find route (1 switch)</a><br>"+
            "<a href='#' onclick='findRoutes(2)'>Find route (2 switches)</a>");
        } else {
            start = m;
            start.bindPopup("Start");
        }
        m.openPopup();
    })
});
function findRoutes(n) {
    if (start && end) {
        $.ajax({
            url: "/route/from/"+start.getLatLng().lat+','+start.getLatLng().lng+"/to/"+end.getLatLng().lat+','+end.getLatLng().lng+"/switches/"+n // load
        }).then(function(data) {
           process(data);
        });
    }
}
function process(data) {
    _data = data;
    $("#main").empty()
    clearLines();
    if (data.length == 0) {
        $("#main").text("No routes found.");
    }
    for (var i=0;i<data.length;i++) {
        var route = data[i]
        var item = $("<div class='item'>")
        // TODO: escape data strings before adding to html
        for (var j=0; j<route.routeChunks.length; j++) {
            var chunk = route.routeChunks[j]
            if (chunk.routes.length == 0) {
                item.append('Walk ' + distance(chunk.start, chunk.end) + ' m<br>')
            } else {
                s = 'Use '
                s += chunk.routes[0].name
                for (var t=1;t<chunk.routes.length;t++) {
                    s += ' or ' + chunk.routes[t].name
                }
                s += ' from platform ' + chunk.start.name + ' to platform ' + chunk.end.name + '<br>'
                item.append(s)
            }
        }
        item.append("<hr>");
        item.on('click', i, showRoute);
        $("#main").append(item)
    }
}
function clearLines() {
    if (lines) {
        for(var i=0;i<lines.length;i++) map.removeLayer(lines[i]);
    }
    lines = [];
}

function showRoute(event) {
    clearLines();
    if (!_data[event.data]) return;
    var route = _data[event.data];
    if (!route || route.routeChunks.length == 0) return;
    var bounds = L.latLngBounds(route.routeChunks[0].start, route.routeChunks[route.routeChunks.length-1].end);
    for(var j=0; j<route.routeChunks.length; j++) {
        var chunk = route.routeChunks[j];
        if (chunk.routes.length == 0) {
            var line = L.polyline([L.latLng(chunk.start), L.latLng(chunk.end)], {color:'black'}).addTo(map);
            bounds.extend(line.getBounds());
            lines.push(line);
        }
    }
    map.fitBounds(bounds);
}
function distance(a,b) {
    var dist = Math.sqrt(Math.pow(a.lat - b.lat, 2) + Math.pow(a.lon - b.lon, 2)) * Math.PI * 6.4e6 / 180.0;
    if (dist > 1000) {
        return Math.round(dist / 100) * 100
    }
    if (dist > 10) {
        return Math.round(dist / 10) * 10
    }
    return 10
}
