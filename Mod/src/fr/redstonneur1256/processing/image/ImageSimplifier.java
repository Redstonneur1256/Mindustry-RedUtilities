package fr.redstonneur1256.processing.image;

import arc.func.Floatc;
import arc.graphics.Pixmap;
import arc.math.geom.Point2;
import arc.math.geom.Rect;
import arc.struct.IntMap;
import arc.struct.IntSeq;
import arc.struct.Seq;

@SuppressWarnings("StatementWithEmptyBody")
public class ImageSimplifier {

    public IntMap<Seq<Rect>> simplify(Pixmap image, Floatc progress) {
        IntMap<Seq<Rect>> map = new IntMap<>();

        int width = image.getWidth();
        int height = image.getHeight();
        float max = width * height;

        boolean[] visited = new boolean[width * height];

        IntSeq pixels = new IntSeq(width * height);

        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                if(pixels.contains(Point2.pack(x, y))) {
                    continue;
                }
                Rect rectangle = new Rect(x, y, 1, 1);
                int color = image.getPixel(x, y);

                while(expand(rectangle, +1, +0, image, color, visited, width)) ;
                while(expand(rectangle, +0, +1, image, color, visited, width)) ;

                for(int xx = 0; xx < rectangle.width; xx++) {
                    for(int yy = 0; yy < rectangle.height; yy++) {
                        visited[xx + yy * width] = true;
                    }
                }

                map.get(color, Seq::new).add(rectangle);
                progress.get((x * height + y) / max);
            }
        }

        return map;
    }

    public int getThreads() {
        return 1;
    }

    protected boolean expand(Rect rectangle, int dirX, int dirY, Pixmap image, int color, boolean[] visited, int width) {

        if(dirX != 0) {
            int x = (int) (dirX < 0 ? rectangle.x - dirX : rectangle.x + rectangle.width + dirX);
            if(x < 0 || x >= image.getWidth()) {
                return false;
            }

            int y = (int) rectangle.y;
            for(int offY = 0; offY < rectangle.height; offY++) {
                y++;

                if(y < 0 ||
                        y >= image.getHeight() ||
                        visited[x + y * width] ||
                        image.getPixel(x, y) != color) {
                    return false;
                }
            }

            if(dirX < 0) {
                rectangle.x -= dirX;
            }
            rectangle.width += dirX;
        }

        if(dirY != 0) {
            int y = (int) (dirY < 0 ? rectangle.y - dirY : rectangle.y + rectangle.height + dirY);
            if(y < 0 || y >= image.getHeight()) {
                return false;
            }

            int o = y * width;

            int x = (int) rectangle.x;
            for(int offX = 0; offX < rectangle.width; offX++) {
                x++;

                if(x < 0 ||
                        x >= image.getWidth() ||
                        visited[x + o] ||
                        image.getPixel(x, y) != color) {
                    return false;
                }
            }

            if(dirY < 0) {
                rectangle.y -= dirY;
            }
            rectangle.height += dirY;
        }

        return true;
    }

}
