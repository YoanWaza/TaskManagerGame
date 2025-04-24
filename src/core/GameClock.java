package core;

public class GameClock {
    private static long startTime = -1;
    private static final int SESSION_DURATION_SEC = 60;

    public static void start() {
        startTime = System.currentTimeMillis();
    }

    public static int getSecondsElapsed() {
        if (startTime == -1) return 0;
        return (int)((System.currentTimeMillis() - startTime) / 1000);
    }

    public static boolean isSessionOver() {
        return getSecondsElapsed() >= SESSION_DURATION_SEC;
    }
    public static boolean isRunning() {
        return !isSessionOver();
    }
}
