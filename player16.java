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

        int evaluations = evaluations_limit_;
        // init population
        Population population = new Population(rnd_);
        // calculate fitness
        evaluations -= population.evalInitialPopulation(evaluation_);
        // loop
        while (evaluations > 0) {
            // Select parents
            population.selectParents(rnd_);
            // Apply crossover / mutation operators
            population.recombine(rnd_);
            population.mutate(rnd_, Util.epsilon);

            try {
                evaluations -= population.evalOffspring(evaluation_);
            }
            catch (NullPointerException e) {
                System.out.println("\033[1mEvaluation limit reached!\033[0m");
                break;
            }
            // Select survivors
            population.selectSurvivors();
            population.printFitness();
        }
    }
}
