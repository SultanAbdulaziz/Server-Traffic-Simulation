package Variates;

// Returns fixed values from an array, in order (used for the Section 5 verification trace).
// When the array runs out it starts again from the first value.
public class Deterministic extends Variate {
    private final double[] values;
    private int index = 0;

    public Deterministic(double[] values) {
        super(1); // Variate needs a seed; it is never used here
        if (values == null || values.length == 0)
            throw new IllegalArgumentException("Deterministic variate needs at least one value");
        this.values = values.clone();
    }

    public double generateNumber() {
        double value = values[index];
        index = (index + 1) % values.length;
        return value;
    }

}
