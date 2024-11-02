// First implementation 
package edu.vt.ece.hw4.barriers;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;

public class TTASBarrier implements Barrier {
    private final int n; 
    private final AtomicInteger counter = new AtomicInteger(0);
    private final AtomicBoolean lock = new AtomicBoolean(false);


    public TTASBarrier(int n){
        this.n = n;
    }

    public void enter() {
        while (true) {
            // Spin until we can acquire the lock
            while (lock.get()) {
                // Busy-wait (test)
            }
            // Try to set the lock
            if (lock.compareAndSet(false, true)) {
                // Increment the counter
                int currentCount = counter.incrementAndGet();

                // Release the lock
                lock.set(false);

                // If we reached the barrier limit, reset the counter
                if (currentCount == n) {
                    counter.set(0); // Reset the counter for the next use
                }
                return; // Exit the method
            }
        }
    }
}


