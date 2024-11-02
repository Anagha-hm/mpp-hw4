package edu.vt.ece.hw4.locks;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class SpinSleepLock implements Lock {
    private final int maxSpin;
    private final AtomicInteger currentSpinners = new AtomicInteger(0);
    private final ReentrantLock reentrantLock = new ReentrantLock();
    private final Condition canSpin = reentrantLock.newCondition();
    private volatile boolean isLocked = false;

    public SpinSleepLock(int maxSpin) {
        this.maxSpin = maxSpin;
    }

    @Override
    public void lock() {
        while (true) {
            // Check if we can spin
            if (currentSpinners.get() < maxSpin) {
                // Attempt to increment the number of spinning threads
                if (currentSpinners.incrementAndGet() <= maxSpin) {
                    // Try to acquire the lock
                    while (isLocked) {
                        // Busy-wait (spin)
                    }
                    // Acquire the lock
                    isLocked = true;
                    // Decrement the count of current spinners
                    currentSpinners.decrementAndGet();
                    return;
                } else {
                    // If we exceeded maxSpin, decrement and wait
                    currentSpinners.decrementAndGet();
                }
            }

            // If we cannot spin, sleep
            reentrantLock.lock();
            try {
                // Wait until we are signaled to try again
                canSpin.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return; // Exit the lock method if interrupted
            } finally {
                reentrantLock.unlock();
            }
        }
    }

    @Override
    public void unlock() {
        // Release the lock
        isLocked = false;

        // Signal one waiting thread to wake up and try to acquire the lock
        reentrantLock.lock();
        try {
            canSpin.signal();
        } finally {
            reentrantLock.unlock();
        }
    }
}