package net.alureon.ircbutt.libmath;

/**
 * Created by alureon on 1/28/15.
 */

public class Trigonometry {

    public static String getSin(String input) {
        double x = Double.parseDouble(input);
        return String.valueOf(Math.sin(x));
    }

}