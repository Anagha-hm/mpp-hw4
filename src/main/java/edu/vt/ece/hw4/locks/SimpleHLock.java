package edu.vt.ece.hw4.locks;

// // import edu.vt.ece.hw4.utils.ThreadCluster;
// // import java.util.concurrent.atomic.AtomicInteger;
// // import java.util.concurrent.locks.ReentrantLock;

// // public class SimpleHLock implements Lock {
// //     private final int clusters;
// //     private final ReentrantLock[] localLocks;
// //     private final ReentrantLock globalLock;
// //     private final AtomicInteger[] localWaiters;
// //     private final int BATCH_COUNT = 2;  // Configurable batch count for fairness
// //     private final AtomicInteger globalWaiterCount;

// //     public SimpleHLock(int clusters) {
// //         this.clusters = clusters;
// //         this.localLocks = new ReentrantLock[clusters];
// //         this.localWaiters = new AtomicInteger[clusters];
// //         this.globalLock = new ReentrantLock();
// //         this.globalWaiterCount = new AtomicInteger(0);

// //         // Initialize local locks and waiter counters
// //         for (int i = 0; i < clusters; i++) {
// //             localLocks[i] = new ReentrantLock();
// //             localWaiters[i] = new AtomicInteger(0);
// //         }
// //     }

// //     @Override
// //     public void lock() {
// //         int threadCluster = ThreadCluster.getCluster(clusters);
// //         ReentrantLock localLock = localLocks[threadCluster];
        
// //         // Increment local waiters
// //         localWaiters[threadCluster].incrementAndGet();
        
// //         try {
// //             // Acquire local lock
// //             localLock.lock();

// //             // Check if global lock needs to be acquired
// //             if (localWaiters[threadCluster].get() == 1) {
// //                 globalWaiterCount.incrementAndGet();
// //                 globalLock.lock();
// //             }
// //         } finally {
// //             // Ensure local waiters are decremented if an exception occurs
// //             localWaiters[threadCluster].decrementAndGet();
// //         }
// //     }

// //     @Override
// //     public void unlock() {
// //         int threadCluster = ThreadCluster.getCluster(clusters);
// //         ReentrantLock localLock = localLocks[threadCluster];

// //         // Release local lock
// //         localLock.unlock();

// //         // Check if we should release global lock
// //         if (localWaiters[threadCluster].get() == 0) {
// //             // Implement batch fairness
// //             if (globalWaiterCount.decrementAndGet() <= 0 || 
// //                 globalWaiterCount.get() % BATCH_COUNT == 0) {
// //                 globalLock.unlock();
// //             }
// //         }
// //     }
// // }

// // package edu.vt.ece.hw4.locks;

// // import java.util.concurrent.atomic.AtomicInteger;
// // import java.util.concurrent.locks.ReentrantLock;

// // public class SimpleHLock implements Lock {
// //     private final ReentrantLock globalLock = new ReentrantLock();
// //     private final ReentrantLock[] localLocks;
// //     private final int clusterCount;
// //     private final int BATCH_COUNT = 5; // Number of local threads before releasing the global lock
// //     private final AtomicInteger[] localLockUsage;

// //     public SimpleHLock(int clusterCount) {
// //         this.clusterCount = clusterCount;
// //         this.localLocks = new ReentrantLock[clusterCount];
// //         this.localLockUsage = new AtomicInteger[clusterCount];
        
// //         for (int i = 0; i < clusterCount; i++) {
// //             localLocks[i] = new ReentrantLock();
// //             localLockUsage[i] = new AtomicInteger(0);
// //         }
// //     }

// //     @Override
// //     public void lock() {
// //         int clusterId = getCurrentThreadClusterId(); // Assume you have a method to get the current thread's cluster ID
// //         if (clusterId < 0 || clusterId >= clusterCount) {
// //             throw new IllegalArgumentException("Invalid cluster ID");
// //         }

// //         ReentrantLock localLock = localLocks[clusterId];
// //         localLock.lock(); // Acquire the local lock
        
// //         // Check if this is the first time acquiring the local lock
// //         if (localLockUsage[clusterId].getAndIncrement() == 0) {
// //             globalLock.lock(); // Acquire the global lock
// //         }
// //     }

// //     @Override
// //     public void unlock() {
// //         int clusterId = getCurrentThreadClusterId(); // Assume you have a method to get the current thread's cluster ID
// //         if (clusterId < 0 || clusterId >= clusterCount) {
// //             throw new IllegalArgumentException("Invalid cluster ID");
// //         }

// //         ReentrantLock localLock = localLocks[clusterId];
// //         localLock.unlock(); // Release the local lock
        
// //         // Decrement the usage count
// //         int usageCount = localLockUsage[clusterId].decrementAndGet();
        
// //         // If no more threads are using the local lock, consider releasing the global lock
// //         if (usageCount == 0) {
// //             // Check how many local locks are being held
// //             boolean shouldReleaseGlobal = true;
// //             for (int i = 0; i < clusterCount; i++) {
// //                 if (localLockUsage[i].get() > 0) {
// //                     shouldReleaseGlobal = false;
// //                     break;
// //                 }
// //             }

// //             // If no local locks are held, release the global lock
// //             if (shouldReleaseGlobal) {
// //                 globalLock.unlock();
// //             } else {
// //                 // If there are still threads using local locks, allow BATCH_COUNT threads to hold it
// //                 if (usageCount % BATCH_COUNT == 0) {
// //                     globalLock.unlock();
// //                 }
// //             }
// //         }
// //     }

// //     // Placeholder method to get the current thread's cluster ID
// //     private int getCurrentThreadClusterId() {
// //         // Implement logic to return the current thread's cluster ID
// //         // This could be based on thread-local storage or some other mechanism
// //         // For the sake of this example, we will return a random cluster ID
// //         return (int) (Math.random() * clusterCount);
// //     }
// // }
// package edu.vt.ece.hw4.locks;

// import java.util.concurrent.atomic.AtomicInteger;
// import java.util.concurrent.locks.ReentrantLock;

// public class SimpleHLock implements Lock {
//     private final ReentrantLock globalLock = new ReentrantLock();
//     private final ReentrantLock[] localLocks;
//     private final int clusterCount;
//     private final int BATCH_COUNT = 5; // Number of local threads before releasing the global lock
//     private final AtomicInteger[] localLockUsage;

//     // ThreadLocal to store the cluster ID for each thread
//     private final ThreadLocal<Integer> threadClusterId = ThreadLocal.withInitial(() -> -1);

//     public SimpleHLock(int clusterCount) {
//         this.clusterCount = clusterCount;
//         this.localLocks = new ReentrantLock[clusterCount];
//         this.localLockUsage = new AtomicInteger[clusterCount];
        
//         for (int i = 0; i < clusterCount; i++) {
//             localLocks[i] = new ReentrantLock();
//             localLockUsage[i] = new AtomicInteger(0);
//         }
//     }

//     @Override
//     public void lock() {
//         int clusterId = getCurrentThreadClusterId(); // Get the current thread's cluster ID
//         if (clusterId < 0 || clusterId >= clusterCount) {
//             throw new IllegalArgumentException("Invalid cluster ID");
//         }

//         // Set the ThreadLocal cluster ID
//         threadClusterId.set(clusterId);
//         ReentrantLock localLock = localLocks[clusterId];
//         localLock.lock(); // Acquire the local lock
        
//         // Check if this is the first time acquiring the local lock
//         if (localLockUsage[clusterId].getAndIncrement() == 0) {
//             globalLock.lock(); // Acquire the global lock
//         }
//     }
//     private int getCurrentThreadClusterId() {
//                 // Implement logic to return the current thread's cluster ID
//                 // This could be based on thread-local storage or some other mechanism
//                 // For the sake of this example, we will return a random cluster ID
//                 return (int) (Math.random() * clusterCount);
//             }

//     @Override
//     public void unlock() {
//         int clusterId = threadClusterId.get(); // Get the current thread's cluster ID from ThreadLocal
//         if (clusterId < 0 || clusterId >= clusterCount) {
//             throw new IllegalArgumentException("Invalid cluster ID");
//         }

//         ReentrantLock localLock = localLocks[clusterId];

//         // Ensure the local lock is held by this thread before unlocking
//         if (localLock.isHeldByCurrentThread()) {
//             localLock.unlock(); // Release the local lock
//         } else {
//             throw new IllegalMonitorStateException("Current thread does not hold the local lock for cluster: " + clusterId);
//         }

//         // Decrement the usage count
//         int usageCount = localLockUsage[clusterId].decrementAndGet();
        
//         // If no more threads are using the local lock, consider releasing the global lock
//         if (usageCount == 0) {
//             // Check how many local locks are being held
//             boolean shouldReleaseGlobal = true;
//             for (int i = 0; i < clusterCount; i++) {
//                 if (localLockUsage[i].get() > 0) {
//                     shouldReleaseGlobal = false;
//                     break;
//                 }
//             }

//             // If no local locks are held, release the global lock
//             if (shouldReleaseGlobal) {
//                 globalLock.unlock();
//             } else {
//                 // If there are still threads using local locks, allow BATCH_COUNT threads to hold it
//                 if (usageCount % BATCH_COUNT == 0) {
//                     globalLock.unlock();
//                 }
//             }
//         }
//     }
// }
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class SimpleHLock implements Lock {
    private final ReentrantLock globalLock = new ReentrantLock();
    private final ReentrantLock[] localLocks;
    private final int clusterCount;
    private final int BATCH_COUNT = 5;
    private final AtomicInteger[] localLockUsage;

    private final ThreadLocal<Integer> threadClusterId = ThreadLocal.withInitial(() -> -1);

    public SimpleHLock(int clusterCount) {
        this.clusterCount = clusterCount;
        this.localLocks = new ReentrantLock[clusterCount];
        this.localLockUsage = new AtomicInteger[clusterCount];

        for (int i = 0; i < clusterCount; i++) {
            localLocks[i] = new ReentrantLock();
            localLockUsage[i] = new AtomicInteger(0);
        }
    }

    @Override
    public void lock() {
        int clusterId = getCurrentThreadClusterId();
        if (clusterId < 0 || clusterId >= clusterCount) {
            throw new IllegalArgumentException("Invalid cluster ID");
        }

        threadClusterId.set(clusterId);
        ReentrantLock localLock = localLocks[clusterId];
        localLock.lock();

        if (localLockUsage[clusterId].getAndIncrement() == 0) {
            globalLock.lock();
        }
    }

    private int getCurrentThreadClusterId() {
        // Implement logic to return the current thread's cluster ID
        return (int) (Math.random() * clusterCount);
    }

    @Override
    public void unlock() {
        int clusterId = threadClusterId.get();
        if (clusterId < 0 || clusterId >= clusterCount) {
            throw new IllegalArgumentException("Invalid cluster ID");
        }

        ReentrantLock localLock = localLocks[clusterId];
        if (localLock.isHeldByCurrentThread()) {
            localLock.unlock();
        } else {
            throw new IllegalMonitorStateException("Current thread does not hold the local lock for cluster: " + clusterId);
        }

        int usageCount = localLockUsage[clusterId].decrementAndGet();
        if (usageCount == 0) {
            boolean shouldReleaseGlobal = true;
            for (int i = 0; i < clusterCount; i++) {
                if (localLockUsage[i].get() > 0) {
                    shouldReleaseGlobal = false;
                    break;
                }
            }

            if (shouldReleaseGlobal) {
                globalLock.unlock();
            }
        }
    }
}