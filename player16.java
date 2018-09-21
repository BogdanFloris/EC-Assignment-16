import org.vu.contest.ContestSubmission;
import org.vu.contest.ContestEvaluation;

import java.util.Arrays;
import java.util.Random;
import java.util.Properties;

public class player16 implements ContestSubmission {
    Random rnd_;
    ContestEvaluation evaluation_;
    private int evaluations_limit_;

    public static void main(String[] args) {
        System.out.println("Test");
    }

    public player16() {
        rnd_ = new Random();
    }

    public void setSeed(long seed) {
        // Set seed of algorithms random process
        rnd_.setSeed(seed);
    }

    public void setEvaluation(ContestEvaluation evaluation) {
        // Set evaluation problem used in the run
        evaluation_ = evaluation;

        // Get evaluation properties
        Properties props = evaluation.getProperties();
        // Get evaluation limit
        evaluations_limit_ = Integer.parseInt(props.getProperty("Evaluations"));
        // Property keys depend on specific evaluation
        // E.g. double param = Double.parseDouble(props.getProperty("property_name"));
        boolean isMultimodal = Boolean.parseBoolean(props.getProperty("Multimodal"));
        boolean hasStructure = Boolean.parseBoolean(props.getProperty("Regular"));
        boolean isSeparable = Boolean.parseBoolean(props.getProperty("Separable"));

        // Do sth with property values, e.g. specify relevant settings of your algorithm
        if (isMultimodal) {
            // Do sth
        } else {
            // Do sth else
        }
    }

    public void run() {
        // Run your algorithm here

        int evals = 0;
        // init population
        Individual parent = new Individual(this.rnd_);
        Individual child;
        // calculate fitness
        parent.fitness = (double) evaluation_.evaluate(parent.values);
        // loop
        while (evals < evaluations_limit_) {
            // Select parents
            // Apply crossover / mutation operators
            // inherit from parent
            child = new Individual(parent.values);
            // mutate the child
            child.mutate(Util.Mutation.NON_UNIFORM, this.rnd_);
            // Check fitness of unknown function
            child.fitness = (double) evaluation_.evaluate(child.values);
            evals++;
            // Select survivors
            if (child.fitness > parent.fitness) {
                // Select the individual with the highest fitness
                parent = child;
                System.out.println(evals + "parent: " + Arrays.toString(parent.values));
            }
        }
        // Display the values of the current parent
        System.out.println(evals + "parent: " + Arrays.toString(parent.values));
    }
}
