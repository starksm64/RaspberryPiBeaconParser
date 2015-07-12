package org.jboss.summit2015.beacon.common;

/**
 * Simple moving average for ints
 */
public class SMA {
    private int period;
    private int window[];
    private int index;
    private int total;

    public SMA(int period) {
        this.period = period;
        this.window = new int[period];
    }

    // Adds a value to the average, pushing one out if nescessary
    public void add(int val) {
        total = total - window[index] + val;
        window[index] = val;
        index = (index+1) % period;
    }

    /**
     * Get the current average. Only valid after period elements have been added.
     * @return
     */
    public int avg() {
        return total / period;
    }
}
