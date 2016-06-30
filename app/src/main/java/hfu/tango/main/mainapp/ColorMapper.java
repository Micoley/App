package hfu.tango.main.mainapp;

import android.graphics.Color;

import java.util.Map;
import java.util.TreeMap;

/**
 * Klasse fuer die Zuordnung von Farben zu den Tiefeninformationen, wobei die Verteilung logarithmisch ist
 */
public class ColorMapper {
    private Map<Double, Integer> colorMap;

    public ColorMapper(double min, double max, int precision) {
        double step = (max - min) / precision;
        colorMap = new TreeMap<>();
        for (double i = min; i <= max; i += step) {
            double key = (Math.log(i) / Math.log(max));
            int r = (int) (key * 255);
            int g = 255 - ((int) (key * 255));
            int value = Color.rgb(r, g, Math.abs(r - g));
            colorMap.put(key, value);
        }
    }

    public int mapToColor(double x) {
        for (double v : colorMap.keySet()) {
            if (x * 1 / 5 <= v) {
                return colorMap.get(v);
            }
        }
        return Color.rgb(0, 0, 0);
    }

}
