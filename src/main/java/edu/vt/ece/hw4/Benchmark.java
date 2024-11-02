package edu.vt.ece.hw4;

import java.util.concurrent.atomic.AtomicLong;

import edu.vt.ece.hw4.barriers.ArrayBarrier;
import edu.vt.ece.hw4.barriers.Barrier;
import edu.vt.ece.hw4.barriers.TTASBarrier;
import edu.vt.ece.hw4.bench.*;
import edu.vt.ece.hw4.locks.ALock;
import edu.vt.ece.hw4.locks.BackoffLock;
import edu.vt.ece.hw4.locks.Lock;
import edu.vt.ece.hw4.locks.MCSLock;
// import edu.vt.ece.hw4.locks.PriorityQueueLock;
import edu.vt.ece.hw4.locks.SimpleHLock;
import edu.vt.ece.hw4.locks.SpinSleepLock;
import edu.vt.ece.hw4.locks.TTASLock;
import edu.vt.ece.hw4.utils.ClusterThread;

public class Benchmark {

    private static final String ALOCK = "ALock";
    private static final String BACKOFFLOCK = "BackoffLock";
    private static final String MCSLOCK = "MCSLock";
    private static final String SPINSLEEPLOCK = "SpinSleepLock";
    private static final String SHLOCK = "SimpleHLock";
    private static final String PRIORITYQUEUE = "PriorityQueueLock";
    private static final String TTAS = "TTASLock";


    public static void main(String[] args) throws Exception {
        String mode = args.length <= 0 ? "normal" : args[0];
        String lockClass = (args.length <= 1 ? ALOCK : args[1]);
        int threadCount = (args.length <= 2 ? 16 : Integer.parseInt(args[2]));
        int totalIters = (args.length <= 3 ? 64000 : Integer.parseInt(args[3]));
        int iters = totalIters / threadCount;

        run(args, mode, lockClass, threadCount, iters);

    }

    private static void run(String[] args, String mode, String lockClass, int threadCount, int iters) throws Exception {
        for (int i = 0; i < 5; i++) {
            Lock lock = null;
            switch (lockClass.trim()) {
                case ALOCK:
                    lock = new ALock(threadCount);
                    break;
                case BACKOFFLOCK:
                    lock = new BackoffLock(args[4]);
                    break;
                case MCSLOCK:
                    lock = new MCSLock();
                    break;
                case SPINSLEEPLOCK:
                    lock = new SpinSleepLock(Integer.parseInt(args[4]));
                    break;
                case SHLOCK:
                    lock = new SimpleHLock(Integer.parseInt(args[4]));
                    break;
                case TTAS: 
                    lock = new TTASLock();
                    break;
                // case PRIORITYQUEUE:
                //     lock = new PriorityQueueLock();
            }

            switch (mode.trim().toLowerCase()) {
                case "normal":
                    final Counter counter = new SharedCounter(0, lock);
                    runNormal(counter, threadCount, iters);
                    break;
                case "empty":
                    runEmptyCS(lock, threadCount, iters);
                    break;
                case "long":
                    runLongCS(lock, threadCount, iters);
                    break;
                case "barrier":
                    // System.out.println("First Implementation");
                    // Barrier b = new TTASBarrier(threadCount); // first implementation 
                    // runBarrierCS(b, threadCount, iters);
                    // System.out.println("Second Implementation");
                    Barrier b1 = new ArrayBarrier(threadCount); // second implementation
                    runBarrierTest(b1, threadCount, iters);
                    
                    break;
                case "cluster":
                    runClusterCS(lock, threadCount, iters, Integer.parseInt(args[4]));
                    break;
                default:
                    throw new UnsupportedOperationException("Implement this");
            }
        }
    }

    private static void runNormal(Counter counter, int threadCount, int iters) throws Exception {
        final TestThread[] threads = new TestThread[threadCount];
        TestThread.reset();

        for (int t = 0; t < threadCount; t++) {
            threads[t] = new TestThread(counter, iters);
        }

        for (int t = 0; t < threadCount; t++) {
            threads[t].start();
        }

        long totalTime = 0;
        for (int t = 0; t < threadCount; t++) {
            threads[t].join();
            totalTime += threads[t].getElapsedTime();
        }

        System.out.println("Average time per thread is " + totalTime / threadCount + "ms");
    }

    private static void runEmptyCS(Lock lock, int threadCount, int iters) throws Exception {

        final EmptyCSTestThread[] threads = new EmptyCSTestThread[threadCount];
        EmptyCSTestThread.reset();

        for (int t = 0; t < threadCount; t++) {
            threads[t] = new EmptyCSTestThread(lock, iters);
        }

        for (int t = 0; t < threadCount; t++) {
            threads[t].start();
        }

        long totalTime = 0;
        for (int t = 0; t < threadCount; t++) {
            threads[t].join();
            totalTime += threads[t].getElapsedTime();
        }

        System.out.println("Average time per thread is " + totalTime / threadCount + "ms");
    }

    static void runLongCS(Lock lock, int threadCount, int iters) throws Exception {
        final Counter counter = new Counter(0);
        final LongCSTestThread[] threads = new LongCSTestThread[threadCount];
        LongCSTestThread.reset();

        for (int t = 0; t < threadCount; t++) {
            threads[t] = new LongCSTestThread(lock, counter, iters);
        }

        for (int t = 0; t < threadCount; t++) {
            threads[t].start();
        }

        long totalTime = 0;
        for (int t = 0; t < threadCount; t++) {
            threads[t].join();
            totalTime += threads[t].getElapsedTime();
        }

        System.out.println("Average time per thread is " + totalTime / threadCount + "ms");
    }
    static void runBarrierCS(Barrier barrier, int threadCount, int iters) throws Exception {
        AtomicLong totalElapsedTime = new AtomicLong(0);
        Thread[] threads = new Thread[threadCount];

        for (int t = 0; t < threadCount; t++) {
            threads[t] = new BarrierTestThread(barrier, iters, totalElapsedTime);
        }

        for (int t = 0; t < threadCount; t++) {
            threads[t].start();
        }

        for (int t = 0; t < threadCount; t++) {
            threads[t].join();
        }

        long averageTime = totalElapsedTime.get() / threadCount;
        System.out.println("Average time per thread is " + averageTime + "ms");
    }

    static void runClusterCS(Lock lock, int threadCount, int iters, int cluster) throws InterruptedException{
        if (threadCount % cluster != 0) {
            throw new IllegalArgumentException("Thread count must be divisible by number of clusters");
        }
        final ClusterCSTestThread[] threads = new ClusterCSTestThread[threadCount];
        ClusterCSTestThread.reset();
        final Counter counter = new Counter(0);
        for (int t = 0; t < threadCount; t++) {
            threads[t] = new ClusterCSTestThread(lock, counter, iters);
        }

        // Start threads
        for (int t = 0; t < threadCount; t++) {
            threads[t].start();
        }

        // Wait for threads to complete and collect timing
        long totalTime = 0;
        for (int t = 0; t < threadCount; t++) {
            threads[t].join();
            totalTime += threads[t].getElapsedTime();
        }

        // Print average time
        System.out.println("Clusters: " + cluster + 
                           ", Threads: " + threadCount + 
                           ", Average time per thread: " + totalTime / threadCount + "ms");    

    }

    
    private static void runBarrierTest(Barrier barrier, int threadCount, int iters) throws InterruptedException {
        Thread[] threads = new Thread[threadCount];

        // start timer
        // long startTime = System.currentTimeMillis();

        for (int t = 0; t < threadCount; t++) {
            threads[t] = new Thread(() -> {
                try {
                    
                    for (int i=0; i < iters; i++) {
                    // Simulate foo() work
                    // Enter barrier (all threads must reach this point before proceeding)
                    barrier.enter();
    
                    // Simulate bar() work after all threads reach the barrier
                    // System.out.println("Thread " + Thread.currentThread().getId() + " executing bar()");
                    }
                  
                } catch (Exception e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        long startTime = System.currentTimeMillis();

        for( Thread thread: threads) {
            thread.start();
        }
        for( Thread thread: threads) {
            thread.join();
        }

        long endTime = System.currentTimeMillis();

        // Output total barrier time
        System.out.println("Barrier time for " + threadCount + " threads: " + (endTime - startTime) + " ms");
    }

}