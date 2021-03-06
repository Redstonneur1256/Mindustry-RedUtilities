package fr.redstonneur1256.utils.graphics;

import arc.graphics.Color;
import arc.graphics.Pixmap;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class Palette<T extends Palette.ColorContainer> {

    private final List<T> colors;
    private final Map<Integer, T> cachedColors;
    private final Function<Integer, T> cacheLoader;
    private T defaultValue;
    private boolean useCache;

    public Palette() {
        this(null);
    }

    public Palette(T defaultValue) {
        this.colors = new ArrayList<>();
        this.cachedColors = new HashMap<>();
        this.cacheLoader = this::matchColor0;
        this.defaultValue = defaultValue;
    }

    private static double getDistance(int r1, int g1, int b1, int r2, int g2, int b2) {
        int dr = r1 - r2;
        int dg = g1 - g2;
        int db = b1 - b2;

        double redMean = (r1 + r2) / 2.0D;
        double weightR = 2.0D + redMean / 256.0D;
        double weightG = 4.0D;
        double weightB = 2.0D + (255.0D - redMean) / 256.0D;
        return (int) (weightR * dr * dr + weightG * dg * dg + weightB * db * db);
    }

    public Palette<T> useCache(boolean use) {
        this.useCache = use;
        if(!use) {
            clearCache();
        }
        return this;
    }

    public boolean useCache() {
        return this.useCache;
    }

    public Palette<T> addColor(T t) {
        this.colors.add(t);
        return this;
    }

    public Palette<T> clearCache() {
        cachedColors.clear();
        return this;
    }

    public int cacheSize() {
        return cachedColors.size();
    }

    public List<T> getColors() {
        return colors;
    }

    public void exportTo(File file) throws Exception {
        DataOutputStream output = new DataOutputStream(new FileOutputStream(file));
        output.writeInt(colors.size());
        for(T data : colors) {
            output.writeInt(data.getColorRGB());
        }
        output.flush();
        output.close();
        System.out.println("Exported " + colors.size() + " colors to file " + file);
    }

    public T matchColor(int rgb) {
        return useCache ? cachedColors.computeIfAbsent(rgb, cacheLoader) : matchColor0(rgb);
    }

    /* Internal methods */

    public Pixmap toImage(T[] data, int width, int height) {
        Pixmap image = new Pixmap(width, height, Pixmap.Format.rgba8888);
        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                int index = x + y * width;
                T t = data[index];
                if(t == null)
                    continue;
                image.draw(x, y, t.getColorRGB() << 8 | 0xFF);
            }
        }
        return image;
    }

    protected T matchColor0(int rgb) {
        T closest = defaultValue;
        int red = (rgb >> 16) & 0xFF;
        int green = (rgb >> 8) & 0xFF;
        int blue = rgb & 0xFF;

        double closestDistance = Double.POSITIVE_INFINITY;
        for(T container : colors) {
            int c2 = container.getColorRGB();
            int r2 = (c2 >> 16) & 0xFF;
            int g2 = (c2 >> 8) & 0xFF;
            int b2 = c2 & 0xFF;
            double distance = getDistance(red, green, blue, r2, g2, b2);
            if(distance < closestDistance) {
                closest = container;
                closestDistance = distance;
            }
        }
        return closest;
    }

    public static class Container<T> extends ColorContainer {

        private T value;

        private Container(int rgb, T value) {
            this(value, rgb);
        }

        public Container(Color color, T value) {
            this(value, color);
        }

        public Container(T value, int rgb) {
            this(value, new Color(rgb << 8 | 0xFF));
        }

        public Container(T value, Color color) {
            super(color);
            this.value = value;
        }

        public T getValue() {
            return value;
        }

    }

    public static class ColorContainer {
        private Color color;

        public ColorContainer() {
        }

        public ColorContainer(Color color) {
            this.color = color;
        }

        public Color getColor() {
            return color;
        }

        public int getColorRGB() {
            return color.rgba8888() >> 8 | 0xFF000000;
        }

    }

}