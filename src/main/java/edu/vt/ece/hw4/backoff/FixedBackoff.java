package edu.vt.ece.hw4.backoff;

public class FixedBackoff implements Backoff {
    private final long fixedDelay = 150;
    @Override
    public void backoff() throws InterruptedException {
        Thread.sleep(fixedDelay);
    }
}
