package hfu.tango.main.mainapp;

import java.util.ArrayList;

public class ColorCube {

    private int corners[][];
    private ArrayList<String> names;

    public ColorCube() {
        names = getStandardNames();
        createSubCubes(27);
    }


    private ArrayList<String> getStandardNames() {
        ArrayList<String> names = new ArrayList<>();
        names.add("schwarz");
        names.add("dunkelbau");
        names.add("blau");
        names.add("kirschrot");
        names.add("rotviolet");
        names.add("rot");
        names.add("rubinrot");
        names.add("magenta");
        names.add("dunkelgrün");
        names.add("dschungelgrün");
        names.add("köngisblau");
        names.add("ovlivgrün");
        names.add("grau");
        names.add("orchidee");
        names.add("orange");
        names.add("pfirsich");
        names.add("rosa");
        names.add("grün");
        names.add("lemonengrün");
        names.add("türkis");
        names.add("hellgrün");
        names.add("gelbgrün");
        names.add("himmelblau");
        names.add("gelb");
        names.add("zitronengelb");
        names.add("weiss");
        names.add("weiss");
        return names;

    }

    private void createSubCubes(int count) {
        corners = new int[count][6];
        int count1D = (int) Math.pow((double) count, 1.0 / 3.0);
        int cubeLength = (255 / count1D) + 1;
        int postion = 0;
        for (int r = 0; r < count1D; r++) {
            for (int g = 0; g < count1D; g++) {
                for (int b = 0; b < count1D; b++) {

                    corners[postion][0] = r * cubeLength;
                    corners[postion][1] = g * cubeLength;
                    corners[postion][2] = b * cubeLength;


                    corners[postion][3] = r * cubeLength + cubeLength;
                    corners[postion][4] = g * cubeLength + cubeLength;
                    corners[postion][5] = b * cubeLength + cubeLength;
                    postion++;
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
