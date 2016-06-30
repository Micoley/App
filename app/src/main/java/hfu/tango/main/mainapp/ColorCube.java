package hfu.tango.main.mainapp;

import java.util.ArrayList;

/**
 * Ein RGB-Farbwuerfel mit dem man Farben einteilen kann mit belieber Groesse (n^3)
 */
public class ColorCube {

    private int corners[][];
    private ArrayList<String> names;

    public ColorCube() {
        names = getStandardNames();
        createSubCubes(8);
    }

    /**
     * Traegt die Namen der Farben in der richtigen Reihenfolge in eine Liste
     *
     * @return ArrayList<String> Liste der Farbnamen
     */
    private ArrayList<String> getStandardNames() {
        ArrayList<String> names = new ArrayList<>();
      /*  names.add("schwarz");
        names.add("dunkelbau");
        names.add("blau");
        names.add("köngisblau");
        names.add("ovlivgrün");
        names.add("himmelblau");
        names.add("dunkelgrün");
        names.add("dschungelgrün");
        names.add("grün");
        names.add("lemonengrün");
        names.add("türkis");
        names.add("hellgrün");
        names.add("gelbgrün");

        names.add("kirschrot");
        names.add("rotviolet");
        names.add("rot");
        names.add("rubinrot");
        names.add("magenta");
        names.add("grau");
        names.add("orchidee");
        names.add("orange");
        names.add("pfirsich");
        names.add("rosa");
        names.add("gelb");
        names.add("zitronengelb");
        names.add("weiss");
        names.add("weiss");}
        */
        names.add("schwarz");
        names.add("blau");
        names.add("grün");
        names.add("hellblau");
        names.add("rot");
        names.add("magenta");
        names.add("gelb");
        names.add("weiss");
        return names;

    }

    /**
     * Unterteilt den RGB Farbwuerfel in kleinere Wuerfel
     *
     * @param count Anzahl der Wuerfel(N^3)
     */

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

    /**
     * Gibt zu einem rgb Array die Farbe zurueck
     *
     * @param rgb rgb Array
     * @return Eine Farbe als String
     */

    public String getColor(int rgb[]) {
        for (int i = 0; i < corners.length; i++) {
            if (isInSubCube(rgb, i)) {
                return names.get(i);
            }
        }
        return "undefiniert";
    }


    /**
     * Ueberprueft ob Farbe in Unterwuerfel ist
     *
     * @param rgb   rgb Array
     * @param index , index des Farbwuerfels
     * @return true wenn Farbe in Wuerfel enthalten ist
     */
    private boolean isInSubCube(int rgb[], int index) {
        return rgb[0] >= corners[index][0] && rgb[1] >= corners[index][1] && rgb[2] >= corners[index][2] &&
                rgb[0] <= corners[index][3] && rgb[1] <= corners[index][4] && rgb[2] <= corners[index][5];
    }

}
