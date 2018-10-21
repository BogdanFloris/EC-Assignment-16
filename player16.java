import org.vu.contest.ContestSubmission;
import org.vu.contest.ContestEvaluation;

import java.util.Arrays;
import java.util.Random;
import java.util.Properties;

public class player16 implements ContestSubmission {
    Random rnd_;
    ContestEvaluation evaluation_;
    private int evaluations_limit_;
    private int populationSize;
    private Util util;

    public static void main(String[] args) {
        System.out.println("Test");
    }

    public player16() {
        rnd_ = new Random();
    }

    public void setSeed(long seed) {
        // Set seed of algorithms random process
        rnd_.setSeed(rnd_.nextInt(10000000));
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

        // get the function that we are evaluating
        boolean bentCigarFunction = !(isMultimodal || hasStructure || isSeparable);
        boolean schaffersFunction = isMultimodal && hasStructure && !isSeparable;
        boolean katsuuraFunction = isMultimodal && !(hasStructure || isSeparable);

        // initialize util class depending on the function that we are evaluating
        util = new Util();
        if (bentCigarFunction) {
            populationSize = 50;
            util.changeMutationParameters(0.02, 2, 0.2);
        }
        else if (schaffersFunction) {
            populationSize = 500;
            util.changeIslandUtils(10, 50);
            util.changeMutationParameters(0.02, 2, 0.2);
        }
        else if (katsuuraFunction) {
            populationSize = 250;
            util.changeIslandUtils(5, 50);
            util.changeMutationParameters(0.02, 2, 0.2);
        }
    }

    public void run() {
        // Run your algorithm here
        int evaluations = evaluations_limit_;
        // initialize time dependent variables
        double timeDependentEval;
        double mutationEpsilon;
        // Population statistic
        double[] sumValues;
        double[] meanValues = new double[Util.DIMENSION];
        double[] diversity;
        // init population
        IPopulation population;
        if (util.ISLAND_MODEL) {
            population = new IslandModel(rnd_, util, populationSize);
        }
        else {
            population = new Population(rnd_, util, populationSize);
        }
        // calculate fitness
        evaluations -= population.evalInitialPopulation(evaluation_);
        // generation counter
        int generation = 0;
        // loop
        while (evaluations > 0) {
            if (util.ISLAND_MODEL && generation % util.EPOCH == 0) {
                try {
                    population.makeExchange(rnd_);
                }
                catch (UnsupportedOperationException e) {
                    System.err.println("Not Island Model");
                }
            }
            // change time dependent variables
            timeDependentEval = (double) evaluations / evaluations_limit_;
            mutationEpsilon = util.epsilon * Math.pow(timeDependentEval, 4);
            // Select parents
            population.selectParents(rnd_);
            // Apply crossover / mutation operators
            population.recombine(rnd_);
            population.mutate(rnd_, mutationEpsilon);

            try {
                evaluations -= population.evalOffspring(evaluation_);
            }
            catch (NullPointerException e) {
                System.out.println("Evaluation limit reached");
                break;
            }
            // Select survivors
            population.selectSurvivors();
            // population.printFitness();

            // Compute generational population statistics
            if (Util.COMPUTE_STATS) {
                // Get the sum of the population allele-values
                sumValues = population.getSumValues();
                // Compute the mean from the sum value
                for (int i = 0; i < Util.DIMENSION; i++) {
                    meanValues[i] = sumValues[i] / (double) populationSize;
                }
                // Compute the single diversity value
                diversity = population.getDiversity(meanValues);
                double d = 0.0;
                for (int i = 0; i < Util.DIMENSION; i++) {
                    d += diversity[i];
                }
                System.out.println("Generation: " + generation + "Diversity: " + d);
            }
            generation++;
        }
    }
}
