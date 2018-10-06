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
        Population population = new Population(rnd_);
        // calculate fitness
        population.evalInitialPopulation(evaluation_);
        // loop
        while (evals < evaluations_limit_) {
            // Select parents
            population.selectParents(rnd_);
            // Apply crossover / mutation operators
            population.recombine(rnd_);
            population.mutate(rnd_, Util.epsilon);
            // Select survivors
            population.selectSurvivors();

            // Increment evaluations
            evals++;
            population.printFitness();
        }
    }
}
