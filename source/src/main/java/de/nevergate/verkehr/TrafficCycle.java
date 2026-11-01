package de.nevergate.verkehr;

/** Deterministic two-group junction cycle. All times are Minecraft ticks. */
public final class TrafficCycle {
    public enum Light { RED, RED_YELLOW, GREEN, YELLOW }
    private final int green, yellow, clearance, redYellow;
    public TrafficCycle(int green, int yellow, int clearance, int redYellow) {
        if (green < 20 || yellow < 1 || clearance < 1 || redYellow < 1)
            throw new IllegalArgumentException("Invalid signal durations");
        this.green=green; this.yellow=yellow; this.clearance=clearance; this.redYellow=redYellow;
    }
    public int period() { return 2 * (green + yellow + clearance + redYellow); }
    public Light light(long tick, int group) {
        if (group < 0 || group > 1) throw new IllegalArgumentException("Group must be 0 or 1");
        int half=period()/2;
        int t=(int)Math.floorMod(tick - (long)group*half, period());
        if(t < redYellow) return Light.RED_YELLOW;
        if(t < redYellow+green) return Light.GREEN;
        if(t < redYellow+green+yellow) return Light.YELLOW;
        return Light.RED;
    }
}
