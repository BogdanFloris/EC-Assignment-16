import org.vu.contest.ContestEvaluation;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Main Population class
 */
public class Population implements IPopulation {
    private int populationSize;
    private double offspringRatio;
    private int offspringSize;
    private int matingpoolSize;
    private Individual[] population;
    private Individual[] offspring;
    private List<Individual> matingpool;

    /**
     * Constructor with only a Random object
     *
     * @param rnd_ Random class to be used
     */
    public Population(Random rnd_) {
        populationSize = Util.POPULATION_SIZE;

        offspringRatio = Util.OFFSPRING_RATIO;
        offspringSize = (int) (populationSize * offspringRatio);

        matingpoolSize = offspringSize;

        population = new Individual[this.populationSize];
        offspring = new Individual[offspringSize];
        matingpool = new ArrayList<Individual>();

        for (int i = 0; i < populationSize; i++) {
            population[i] = new Individual(rnd_);
        }
    }

    @Override
    public void evalInitialPopulation(ContestEvaluation eval) {
        for (int i = 0; i < populationSize; i++) {
            population[i].setFitness((double) eval.evaluate(population[i].values));
        }
    }

    @Override
    public void evalOffspring(ContestEvaluation eval) {
        for (int i = 0; i < offspringSize; i++) {
            offspring[i].setFitness((double) eval.evaluate(offspring[i].values));
        }
    }

    @Override
    public void selectParents(Random rnd_, Util.ParentSelection selection) {
        switch (selection) {
            case LINEAR_RANK:
                rankingSelectionLinear();
                break;
            case EXPONENTIAL_RANK:
                rankingSelectionExponential();
                break;
        }
        sampleParentSUS(rnd_);
    }

    @Override
    public void recombine(Random rnd_, Util.Recombination recombination) {
        double[][] parentsValues = new double[Util.N_PARENTS][Util.DIMENSION];
        double[][] childrenValues = new double[Util.N_PARENTS][Util.DIMENSION];

        for (int i = 0; i < offspringSize; i += Util.N_PARENTS) {
            for (int j = 0; j < Util.N_PARENTS; j++) {
                int index = rnd_.nextInt(matingpoolSize);
                parentsValues[j] = matingpool.get(index).values;
                matingpool.remove(index);
            }

            switch (recombination) {
                case SIMPLE_ARITHMETIC:
                    childrenValues = singleArithmeticRecombination(rnd_, parentsValues);
                    break;
                case SINGLE_ARITHMETIC:
                    childrenValues = simpleArithmeticRecombination(rnd_, parentsValues);
                    break;
                default:
                    System.err.println("Invalid recombination");
            }

            for (int j = 0; j < Util.N_PARENTS; j++) {
                offspring[i+j] = (new Individual(childrenValues[j]));
            }
        }
    }

    @Override
    public void selectSurvivors() {

    }

    /**
     * Rank based selection
     *
     *
     */
    private void rankingSelectionLinear() {
        Arrays.sort(population);
        int maxRank = populationSize - 1;
        double prob;
        double s = Util.PARENT_LINEAR_S;
        for (int i = 0; i < populationSize; i++) {
            prob = ((2 - s) / populationSize) + (2 * (maxRank - i) * (s - 1) / (populationSize * (populationSize - 1)));
            population[i].setSelectionProbability(prob);
        }
    }

    private void rankingSelectionExponential() {
        Arrays.sort(population);
        int maxRank = populationSize - 1;
        double prob;
        double normalisation = 0.0;
        for (int i = 0; i < populationSize; i++) {
            prob = 1 - Math.exp(-maxRank + i);
            population[i].setSelectionProbability(prob);
            normalisation += prob;
        }
        // normalise the selection probabilities
        for (int i = 0; i < populationSize; i++) {
            population[i].setSelectionProbability(population[i].getSelectionProbability() / normalisation);
        }
    }

    private void sampleParentSUS(Random rnd_) {
        // Assumes we wish to select offspringSize number of parents for the mating pool
        double[] cumulativeDistribution = new double[populationSize];
        double r;
        // First we define the cumulative probability distribution of the parent selection probabilities
        for (int i = 0; i < populationSize; i++) {
            if (i == 0) {
                cumulativeDistribution[i] = population[i].getSelectionProbability();
            } else {
                cumulativeDistribution[i] = population[i].getSelectionProbability() + cumulativeDistribution[i-1];
            }
        }
        int i = 0;
        int currentMember = 0;
        while (currentMember < matingpoolSize) {
            r = rnd_.nextDouble() / offspringSize;
            while (r <= cumulativeDistribution[i]) {
                matingpool.add(population[i]);
                r += 1.0 / offspringSize;
            }
            i++;
            currentMember++;
        }
    }

    private double[][] simpleArithmeticRecombination(Random rnd_, double[][] parentsValues) {
        double[][] childrenValues = new double[Util.N_PARENTS][Util.DIMENSION];
        int k = rnd_.nextInt(Util.DIMENSION);
        for (int i = 0; i < k; i++) {
            childrenValues[0][i] = parentsValues[0][i];
            childrenValues[1][i] = parentsValues[1][i];
        }
        for (int i = k; i < Util.DIMENSION; i++) {
            childrenValues[0][i] = 0.5 * (parentsValues[0][i] + parentsValues[1][i]);
            childrenValues[1][i] = 0.5 * (parentsValues[0][i] + parentsValues[1][i]);
        }
        return childrenValues;
    }

    private double[][] singleArithmeticRecombination(Random rnd_, double[][] parentsValues) {
        double[][] childrenValues = new double[Util.N_PARENTS][Util.DIMENSION];
        int k = rnd_.nextInt(Util.DIMENSION);
        for (int i = 0; i < Util.DIMENSION; i++) {
            childrenValues[0][i] = parentsValues[0][i];
            childrenValues[1][i] = parentsValues[1][i];
        }
        childrenValues[0][k] = 0.5 * (parentsValues[0][k] + parentsValues[1][k]);
        childrenValues[1][k] = 0.5 * (parentsValues[0][k] + parentsValues[1][k]);
        return childrenValues;
    }

    private double[][] wholeArithmeticRecombination(Random rnd_, double[][] parentsValues) {
        double[][] childrenValues = new double[Util.N_PARENTS][Util.DIMENSION];
        int k = rnd_.nextInt(Util.DIMENSION);
        for (int i = 0; i < Util.DIMENSION; i++) {
            childrenValues[0][k] = Util.RECOMBINATION_ALPHA * parentsValues[0][k] + (1-Util.RECOMBINATION_ALPHA * parentsValues[1][k]);
            childrenValues[1][k] = Util.RECOMBINATION_ALPHA * parentsValues[1][k] + (1-Util.RECOMBINATION_ALPHA * parentsValues[0][k]);
        }
        return childrenValues;
    }
}
