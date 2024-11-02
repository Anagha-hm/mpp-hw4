package edu.vt.ece.hw4.utils;

import edu.vt.ece.hw4.bench.Counter;
import edu.vt.ece.hw4.locks.SimpleHLock;

public class ClusterThread extends Thread {
    private final SimpleHLock lock;
    private final int clusterId;
    private final int iterations;
    private final Counter resource;
    
    public ClusterThread(SimpleHLock lock, int clusterId, int iterations, Counter resource) {
        this.lock = lock;
        this.clusterId = clusterId;
        this.iterations = iterations;
        this.resource = resource;
    }
    
    @Override
    public void run() {
        for (int i = 0; i < iterations; i++) {
            lock.lock();
            try {
                resource.getAndIncrement();
                // Simulate some work
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lock.unlock();
            }
        }
    }
}