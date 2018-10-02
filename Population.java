import org.vu.contest.ContestEvaluation;
import java.util.Arrays;
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
    private Individual[] matingpool;
    private Individual[] offspring;

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
        matingpool = new Individual[offspringSize];
        offspring = new Individual[offspringSize];

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
        for (int currentMember = 0; currentMember < matingpoolSize; currentMember++) {
            r = rnd_.nextDouble() / offspringSize;
            while (r <= cumulativeDistribution[i]) {
                matingpool[currentMember] = population[i];
                r += 1.0 / offspringSize;
            }
            i++;
        }

    }

    @Override
    public void selectSurvivors() {

    }
}
