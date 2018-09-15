import java.util.Arrays;
import java.util.Random;

/**
 * Class that represents an Individual and contains
 * all the methods that are specific to a single
 * individual of a population.
 */
public class Individual {
    // the 10 values of the individual (phenotypes)
    public double[] values;
    // the fitness of this specific individual
    public double fitness;

    /**
     * Constructor with only a randomizer
     *
     * @param rnd_ Random class to be used
     */
    public Individual(Random rnd_) {
        this.values = new double[Util.DIMENSION];
        this.fitness = 0.0;
        // initialize values
        for (int i = 0; i < Util.DIMENSION; i++) {
            this.values[i] = rnd_.nextDouble() * (Util.MAX_VALUE
                    - Util.MIN_VALUE) + Util.MIN_VALUE;
        }
    }

    /**
     * Constructor with a parent's values
     *
     * @param values the values inherited
     */
    public Individual(double[] values) {
        this.values = new double[Util.DIMENSION];
        this.fitness = 0.0;
        for (int i = 0; i < Util.DIMENSION; i++) {
            this.values[i] = values[i];
        }
    }

    /**
     * Nonuniform mutation
     *
     * @param rnd_ Random class to be used
     */
    public void nonUniformMutation(Random rnd_) {
        for (int i = 0; i < Util.DIMENSION; i++) {
            this.values[i] += Util.MUTATION_STEP_SIZE * rnd_.nextGaussian();
            // make sure the values stay within the bounds
            this.values[i] = Math.min(Util.MAX_VALUE, Math.max(
                    Util.MIN_VALUE, this.values[i]));
        }
    }
}
