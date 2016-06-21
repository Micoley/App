package hfu.tango.main.mainapp;

import java.util.ArrayList;

/**
 * Created by Timo on 20.06.2016.
 */
public class ColorCube {

    private int corners[][];
    private ArrayList<String> names;

    public ColorCube() {
        names = getStandardNames();
        createSubCubes(8);
    }


    private ArrayList<String> getStandardNames() {
        ArrayList<String> names = new ArrayList<>();
        names.add("schwarz");
        names.add("blau");
        names.add("grün");
        names.add("hellblau");
        names.add("rot");
        names.add("violet");
        names.add("gelb");
        names.add("weis");
        return names;

    }

    private void createSubCubes(int count) {
        corners = new int[count][6];
        int count1D = (int) Math.pow((double) count, 1.0 / 3.0);
        int cubeLengt = 255 / count1D;
        for (int r = 0; r < count1D; r++) {
            for (int g = 0; g < count1D; g++) {
                for (int b = 0; b < count1D; b++) {
                    corners[r + g + b][0] = r * cubeLengt;
                    corners[r + g + b][1] = g * cubeLengt;
                    corners[r + g + b][2] = b * cubeLengt;


                    corners[r + g + b][3] = r * cubeLengt + cubeLengt;
                    corners[r + g + b][4] = g * cubeLengt + cubeLengt;
                    corners[r + g + b][5] = b * cubeLengt + cubeLengt;
                }
            }
        }


    }

    public String getColor(int rgb[]) {
        for (int i = 0; i < corners.length; i++) {
            if (isInSubCube(rgb, i)) {
                return names.get(i);
            }
        }
        return "undefiniert";
    }

    private boolean isInSubCube(int rgb[], int index) {
        return rgb[0] >= corners[index][0] && rgb[1] >= corners[index][1] && rgb[2] >= corners[index][2] &&
                rgb[0] <= corners[index][3] && rgb[1] <= corners[index][4] && rgb[2] <= corners[index][5];
    }

}
