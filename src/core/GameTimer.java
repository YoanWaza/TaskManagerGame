package core;

public class GameTimer {
    private int remainingTime;

    public GameTimer(int durationInSeconds) {
        this.remainingTime = durationInSeconds;
    }

    public void startTimer() {
        new Thread(() -> {
            while (remainingTime > 0) {
                try {
                    Thread.sleep(1000);
                    remainingTime--;
                    System.out.println("[TIMER] Time left: " + remainingTime + "s");
                } catch (InterruptedException e) {
                    System.out.println("[TIMER] Interrupted.");
                }
            }
            System.out.println("[TIMER] Game Over! Time is up.");
        }).start();
    }
}