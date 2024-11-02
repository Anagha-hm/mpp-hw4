package edu.vt.ece.hw4.locks;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class PriorityQueueLock implements Lock {
    private final AtomicBoolean locked;
    private final PriorityBlockingQueue<QueueNode> waitQueue;
    private final long defaultTimeout;

    public PriorityQueueLock(long timeoutMs) {
        this.locked = new AtomicBoolean(false);
        this.waitQueue = new PriorityBlockingQueue<>();
        this.defaultTimeout = timeoutMs;
    }

    private static class QueueNode implements Comparable<QueueNode> {
        final Thread thread;
        final int priority;
        final long entryTime;
        final AtomicBoolean shouldPark;
        final AtomicReference<Long> waitingTime;

        QueueNode(Thread thread, int priority) {
            this.thread = thread;
            this.priority = priority;
            this.entryTime = System.currentTimeMillis();
            this.shouldPark = new AtomicBoolean(true);
            this.waitingTime = new AtomicReference<>(0L);
        }

        @Override
        public int compareTo(QueueNode other) {
            // Lower priority number means higher priority
            return Integer.compare(this.priority, other.priority);
        }
    }

    @Override
    public void lock() {
        QueueNode myNode = new QueueNode(Thread.currentThread(), getThreadPriority());
        waitQueue.offer(myNode);

        while (myNode.shouldPark.get() && 
               (locked.get() || !waitQueue.peek().thread.equals(Thread.currentThread()))) {
            Thread.yield();
        }

        // Calculate waiting time
        long waitTime = System.currentTimeMillis() - myNode.entryTime;
        myNode.waitingTime.set(waitTime * myNode.priority);

        waitQueue.remove(myNode);
        locked.set(true);
    }

    @Override
    public void unlock() {
        locked.set(false);
        if (!waitQueue.isEmpty()) {
            QueueNode nextNode = waitQueue.peek();
            if (nextNode != null) {
                nextNode.shouldPark.set(false);
            }
        }
    }

    public boolean tryLock() {
        return tryLock(defaultTimeout, TimeUnit.MILLISECONDS);
    }

    public boolean tryLock(long timeout, TimeUnit unit) {
        QueueNode myNode = new QueueNode(Thread.currentThread(), getThreadPriority());
        waitQueue.offer(myNode);

        long remainingNanos = unit.toNanos(timeout);
        long deadline = System.nanoTime() + remainingNanos;

        while (myNode.shouldPark.get() && 
               (locked.get() || !waitQueue.peek().thread.equals(Thread.currentThread()))) {
            if (System.nanoTime() > deadline) {
                waitQueue.remove(myNode);
                return false;
            }
            Thread.yield();
        }

        // Calculate waiting time
        long waitTime = System.currentTimeMillis() - myNode.entryTime;
        myNode.waitingTime.set(waitTime * myNode.priority);

        waitQueue.remove(myNode);
        locked.set(true);
        return true;
    }

    public long getWaitingTime() {
        Thread currentThread = Thread.currentThread();
        for (QueueNode node : waitQueue) {
            if (node.thread.equals(currentThread)) {
                return node.waitingTime.get();
            }
        }
        return 0;
    }

    private int getThreadPriority() {
        // Map Java thread priorities (1-10) to our priority range (1-5)
        int javaPriority = Thread.currentThread().getPriority();
        return Math.max(1, Math.min(5, (javaPriority + 1) / 2));
    }
}