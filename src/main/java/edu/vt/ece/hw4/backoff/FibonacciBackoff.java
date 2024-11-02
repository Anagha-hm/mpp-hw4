package edu.vt.ece.hw4.backoff;

public class FibonacciBackoff implements Backoff {
    private int attempt = 0;

    @Override
    public void backoff() throws InterruptedException {
        long delay = fibonacci(attempt) * 100; // Scale for milliseconds
        Thread.sleep(delay);
        attempt++;
    }

    private long fibonacci(int n) {
        if (n <= 1) return n;
        long a = 0, b = 1;
        for (int i = 2; i <= n; i++) {
            long temp = a + b;
            a = b;
            b = temp;
        }
        return b;
    }
}
