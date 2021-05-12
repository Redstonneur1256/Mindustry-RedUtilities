package fr.redstonneur1256.processing.image;

import arc.func.Floatc;
import arc.graphics.Pixmap;
import arc.math.geom.Point2;
import arc.math.geom.Rect;
import arc.struct.IntMap;
import arc.struct.IntSeq;
import arc.struct.Seq;
import fr.redstonneur1256.redutilities.async.Task;
import fr.redstonneur1256.redutilities.async.ThreadPool;
import fr.redstonneur1256.redutilities.async.Threads;
import fr.redstonneur1256.redutilities.async.pools.ReusableThreadPool;

import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("StatementWithEmptyBody")
public class AsyncImageSimplifier extends ImageSimplifier {

    private static final int threads;
    private static final ThreadPool pool;

    static {
        threads = Math.max(4, Runtime.getRuntime().availableProcessors() * 2);
        pool = new ReusableThreadPool(Threads::daemon, threads);
    }

    @Override
    public IntMap<Seq<Rect>> simplify(Pixmap image, Floatc progress) {
        IntMap<IntSeq> byColors = new IntMap<>();

        AtomicInteger value = new AtomicInteger();
        float max = image.getWidth() * image.getHeight();

        int width = image.getWidth();
        int height = image.getHeight();
        for(int x = width - 1; x >= 0; x--) {
            for(int y = height - 1; y >= 0; y--) {
                byColors.get(image.getPixel(x, y), IntSeq::new).add(Point2.pack(x, y));
            }
        }

        IntMap<Task<Seq<Rect>>> tasks = new IntMap<>();

        boolean[] visited = new boolean[width * height];

        for(IntMap.Entry<IntSeq> entry : byColors) {
            int color = entry.key;
            IntSeq points = entry.value;

            Seq<Rect> rectangles = new Seq<>();

            Task<Seq<Rect>> task = pool.execute(() -> {
                while(!points.isEmpty()) {
                    int xy = points.pop();
                    int x = Point2.x(xy);
                    int y = Point2.y(xy);

                    Rect rectangle = new Rect(x, y, 1, 1);

                    while(expand(rectangle, +1, +0, image, color, visited, width)) ;
                    while(expand(rectangle, +0, +1, image, color, visited, width)) ;

                    for(int xx = 0; xx < rectangle.width; xx++) {
                        for(int yy = 0; yy < rectangle.height; yy++) {
                            visited[xx + yy * width] = true;
                        }
                    }

                    int newValue = value.addAndGet((int) rectangle.width * (int) rectangle.height);
                    progress.get(newValue / max);

                    rectangles.add(rectangle);
                }

                return rectangles;
            });

            tasks.put(color, task);
        }

        IntMap<Seq<Rect>> simplified = new IntMap<>();
        for(IntMap.Entry<Task<Seq<Rect>>> entry : tasks) {
            simplified.put(entry.key, entry.value.get());
        }
        return simplified;
    }

    @Override
    public int getThreads() {
        return threads;
    }
}
