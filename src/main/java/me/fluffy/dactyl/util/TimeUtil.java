package me.fluffy.dactyl.util;

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

    public long getLastRecorded() { return this.lastRecorded; }
}
