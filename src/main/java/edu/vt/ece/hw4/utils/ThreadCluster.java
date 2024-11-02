package edu.vt.ece.hw4.utils;

import edu.vt.ece.spin.ThreadID;

public class ThreadCluster {

    /**
     * The next thread ID to be assigned
     **/
    private static volatile int nextID = 0;

    /**
     * My thread-local ID.
     **/
    private static ThreadLocalID threadID = new ThreadLocalID();

    public static int get() {
        return threadID.get();
    }

    /**
     * When running multiple tests, reset thread id state
     **/
    public static void reset() {
        nextID = 0;
    }
    public static void set(int value) {
        threadID.set(value);
    }

    public static int getCluster() {
        return threadID.get() / 2;
    }
    private static class ThreadLocalID extends ThreadLocal<Integer> {
        protected synchronized Integer initialValue() {
            return nextID++;
        }
    }
}