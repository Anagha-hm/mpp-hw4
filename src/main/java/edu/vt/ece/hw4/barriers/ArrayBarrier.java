package edu.vt.ece.hw4.barriers;
import java.util.concurrent.atomic.*;

// import edu.vt.ece.hw4.utils.ThreadCluster

public class ArrayBarrier implements Barrier {
    private final AtomicIntegerArray b;
    private final int totalThreads;

    public ArrayBarrier(int totalThreads) {
        this.totalThreads = totalThreads;
        this.b = new AtomicIntegerArray(totalThreads);
    }

    @Override
    public void enter() {
        int threadIdx = (int) Thread.currentThread().getId();

        if ( threadIdx == 0) {
            b.set(0, 1);
        }
        else {
            while( b.get(threadIdx -1) !=1){

            }
            //set b[idx] =1
            b.set(threadIdx,1);
        }

        // special case for laast thread
        if ( threadIdx == totalThreads - 1) {
            b.set(threadIdx, 2);
        } else {
            // wait until last element is 2
            while ( b.get(totalThreads - 1) != 2) {
                // busy wait   
            }
        }

    }
    
}
