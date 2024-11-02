package edu.vt.ece.hw4.locks;

import edu.vt.ece.hw4.utils.ThreadCluster;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleHLock implements Lock {
    private static final int BATCH_COUNT = 5; // Number of local threads allowed before releasing global lock
    private final ReentrantLock globalLock;
    private final Map<Integer, ClusterLock> clusterLocks;
    public int batchCount;
        
    public SimpleHLock(int batchCount) {
        this.globalLock = new ReentrantLock(true); // Fair lock
        this.clusterLocks = new ConcurrentHashMap<>();
        this.batchCount = batchCount;
    }
    
    private ClusterLock getOrCreateClusterLock(int clusterId) {
        return clusterLocks.computeIfAbsent(clusterId, k -> new ClusterLock());
    }
    
    @Override
    public void lock() {
        int clusterId = ThreadCluster.getCluster();
        ClusterLock clusterLock = getOrCreateClusterLock(clusterId);
        clusterLock.acquire();
    }
    
    @Override
    public void unlock() {
        int clusterId = ThreadCluster.getCluster();
        ClusterLock clusterLock = getOrCreateClusterLock(clusterId);
        clusterLock.release();
    }
    
    private class ClusterLock {
        private final ReentrantLock localLock;
        private final AtomicBoolean hasGlobalLock;
        private final AtomicInteger localThreads;
        private final AtomicInteger batchSize;
        
        ClusterLock() {
            this.localLock = new ReentrantLock(true);
            this.hasGlobalLock = new AtomicBoolean(false);
            this.localThreads = new AtomicInteger(0);
            this.batchSize = new AtomicInteger(0);
        }
        
        void acquire() {
            // Acquire local lock first
            localLock.lock();
            
            // Increment number of threads in critical section
            int threadCount = localThreads.incrementAndGet();
            
            // First thread needs to acquire global lock
            if (!hasGlobalLock.get()) {
                globalLock.lock();
                hasGlobalLock.set(true);
                batchSize.set(1);
            } else {
                // If we already have global lock, increment batch size
                batchSize.incrementAndGet();
            }
        }
        
        void release() {
            int remainingThreads = localThreads.decrementAndGet();
            int currentBatch = batchSize.get();
            
            // Release global lock if:
            // 1. No more threads in critical section, or
            // 2. Batch size limit reached
            if (hasGlobalLock.get() && (remainingThreads == 0 || currentBatch >= BATCH_COUNT)) {
                hasGlobalLock.set(false);
                batchSize.set(0);
                globalLock.unlock();
            }
            
            // Always release local lock
            localLock.unlock();
            
            // If we just released the global lock and there are waiting threads,
            // yield to give other clusters a chance
            if (!hasGlobalLock.get() && remainingThreads > 0) {
                Thread.yield();
            }
        }
        
        // Helper method to check if global lock is held
        boolean hasGlobalLock() {
            return hasGlobalLock.get();
        }
    }
}
