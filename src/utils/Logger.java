package utils;

public class Logger {

    private static boolean isDebug = false;

    public static void printout(String message) {
        if (isDebug) {
            System.out.println(message);
        }
    }

    public static void debug() {
        isDebug = true;
    }
}
