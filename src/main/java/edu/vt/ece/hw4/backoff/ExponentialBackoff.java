package edu.vt.ece.hw4.backoff;

public class ExponentialBackoff implements Backoff {
    private int attempt = 0;

    @Override
    public void backoff() throws InterruptedException {
        long delay = (long) Math.pow(2, attempt) * 100; // Scale for milliseconds
        Thread.sleep(delay);
        attempt++;
    }

}
