package drawing

import model.Route

import javax.imageio.ImageIO
import java.awt.*
import java.awt.image.BufferedImage

class RouteDrawer {

    int width = 1000
    int height = 1000
    int padding = 50

    def drawRoute(Route route, String filename) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        Graphics2D graphics = image.createGraphics()

        // draw here
//        graphics.setColor(Color.BLACK)

        graphics.drawString(route.name, (int) ((1000 - graphics.getFontMetrics().stringWidth(route.name)) / 2), 990);

        def minLat = route.platforms.min {it.lat}.lat
        def maxLat = route.platforms.max {it.lat}.lat
        def minLon = route.platforms.min {it.lon}.lon
        def maxLon = route.platforms.max {it.lon}.lon

        def minScale = Math.min(maxLat - minLat, maxLon - minLon)
        def maxScale = Math.max(maxLat - minLat, maxLon - minLon)
        if (minScale < 1e-6 || maxScale - minScale ) { minScale = maxScale}
        def xt = { double lon -> (int) (lon - minLon) / minScale * (width - 2* padding) + padding }
        def yt = { double lat -> (int) (maxLat - lat) / minScale * (height - 2* padding) + padding }
        route.platforms.eachWithIndex { it, index ->
            int x = xt(it.lon)
            int y = yt(it.lat)
            graphics.drawOval(x - 1, y - 1, 3, 3)
            graphics.drawString( "${index + 1} ${it.name?:"none"}", x + 3, y + 3)
        }

        ImageIO.write(image, "PNG", new File(filename));
    }
}
