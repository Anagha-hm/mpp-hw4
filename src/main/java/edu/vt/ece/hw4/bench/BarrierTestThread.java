package edu.vt.ece.hw4.bench;

import java.util.concurrent.atomic.AtomicLong;

import edu.vt.ece.hw4.barriers.Barrier;

    public class BarrierTestThread extends Thread {
        private final Barrier barrier;
        private final int iterations;
        private final AtomicLong elapsedTime;

        public BarrierTestThread(Barrier barrier, int iterations, AtomicLong elapsedTime) {
            this.barrier = barrier;
            this.iterations = iterations;
            this.elapsedTime = elapsedTime;
        }

        @Override
        public void run() {
            long startTime = System.currentTimeMillis();

            for (int i = 0; i < iterations; i++) {
                foo(); // Simulate work
                barrier.enter(); // Wait at the barrier
                bar(); // Simulate work after barrier
            }

            long endTime = System.currentTimeMillis();
            elapsedTime.addAndGet(endTime - startTime);
        }

        private void foo() {
            // Simulate work in foo
            try {
                Thread.sleep((long) (Math.random())); // Random sleep to simulate work
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        private void bar() {
            // Simulate work in bar
            try {
                Thread.sleep((long) (Math.random())); // Random sleep to simulate work
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
