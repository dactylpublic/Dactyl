package me.chloe.moonlight.util;

public class TimeUtil {
    private long lastRecorded;

    public TimeUtil() {
        lastRecorded = System.nanoTime() / 1000000;
    }

    public boolean hasPassed(long ms) {
        return (((System.nanoTime() / 1000000) - lastRecorded) >= ms);
    }

    public void reset() {
        lastRecorded = System.nanoTime() / 1000000;
    }

    public void timeTravel(long time) {
        lastRecorded = (lastRecorded + time);
    }

    public long getPassedTime() { return ((System.nanoTime() / 1000000) - lastRecorded); }

    public long getCurrentTime() { return (System.nanoTime() / 1000000); }

    public long getLastRecorded() { return this.lastRecorded; }

    public boolean sleep(long time) {
        if ((System.nanoTime() / 1000000L-getLastRecorded()) >= time) {
            reset();
            return true;
        }
        return false;
    }
}
