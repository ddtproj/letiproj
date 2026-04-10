package utils;



public class DebugLogger {

    private static boolean enabled = false;

    public DebugLogger() {
    }

    public static void setEnabled(boolean enabled)
    {
        enabled = enabled;
    }

    public static boolean isEnabled()
    {
        return enabled;
    }

    public static void println() {
        if (enabled) {
            System.out.println();
        }

    }

    public static void println(String line) {
        if (enabled) {
            System.out.println(line);
        }

    }
}
