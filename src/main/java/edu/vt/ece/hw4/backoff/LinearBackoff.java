package edu.vt.ece.hw4.backoff;

public class LinearBackoff implements Backoff {
    private int attempt = 0;

    @Override
    public void backoff() throws InterruptedException {
        long delay = attempt * 100; // Example: 100 ms per attempt
        Thread.sleep(delay);
        attempt++;
    }
}
