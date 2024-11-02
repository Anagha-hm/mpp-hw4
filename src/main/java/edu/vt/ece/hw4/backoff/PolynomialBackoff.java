package edu.vt.ece.hw4.backoff;

public class PolynomialBackoff implements Backoff {
    private final int exponent = 3;
    private int attempt = 0;

    @Override
    public void backoff() throws InterruptedException {
        long delay = (long) Math.pow(attempt, exponent) * 100; // Scale for milliseconds
        Thread.sleep(delay);
        attempt++;
    }
}
