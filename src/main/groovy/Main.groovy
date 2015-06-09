import drawing.RouteDrawer
import dospring.processor.matrix.MatrixProcess
import model.Point
import model.Route
import model.Stop
import dospring.storage.parser.TransportStorage

//def filename = "D:\\some_transport.osm"
def filename = Main.classLoader.getResource("transport_ch.osm")


def parser = new TransportStorage()
parser.init(filename)

def draw(Collection<Route> routes) {
    def drawer = new RouteDrawer()
    routes.each {
        drawer.drawRoute(it, "d:/routes/${it.ref}_${it.type}_${it.id}.png")
    }
}

def process = new MatrixProcess()
def matrix = process.prepareMatrix(parser.routes)

Collection<Stop> allStops = parser.stops

Point point1 = new Point(lat:48.26892, lon:25.92709) // ������
Point point2 = new Point(lat:48.28331, lon:25.97252) // ���������
Point point3 = new Point(lat:48.28972, lon:25.95090) // ̳��

Collection<Stop> stop1 = allStops.findAll({it - point1 < 500})
Collection<Stop> stop2 = allStops.findAll({it - point3 < 500})

Set<Route> available = []
stop1.each {
    it1 -> stop2.each {
        it2 ->
            println "From ${it1.name} (${it1.id}) to ${it2.name} (${it2.id})"
            matrix.get(it1)?.get(it2)?.each {
                available << it
                println "    By ${it.name} ${it.id}"
            }
    }
}
println available