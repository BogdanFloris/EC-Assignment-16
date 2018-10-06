import org.vu.contest.ContestEvaluation;

import java.util.List;
import java.util.Random;
import java.util.ArrayList;
import java.util.Comparator;

/**
 * Main Population class
 */
public class Population implements IPopulation {
    private int populationSize;
    private int offspringSize;
    private List<Individual> population;
    private List<Individual> offspring;
    private List<Individual> matingPool;

    /**
     * Constructor with only a Random object
     *
     * @param rnd_ Random class to be used
     */
    public Population(Random rnd_) {
        populationSize = Util.POPULATION_SIZE;

        double offspringRatio = Util.OFFSPRING_RATIO;
        offspringSize = (int) (populationSize * offspringRatio);

        population = new ArrayList<>();
        offspring = new ArrayList<>();
        matingPool = new ArrayList<>();

        for (int i = 0; i < populationSize; i++) {
            population.add(new Individual(rnd_));
        }
    }

    /* *******************
     * EVALUATIONS
     *********************/
    @Override
    public void evalInitialPopulation(ContestEvaluation eval) {
        for (Individual individual: population) {
            individual.setFitness((double) eval.evaluate(individual.values));
        }
    }

    @Override
    public void evalOffspring(ContestEvaluation eval) {
        for (Individual individual: offspring) {
            individual.setFitness((double) eval.evaluate(individual.values));
        }
    }

    /* *******************
     * PARENT SELECTION
     *********************/

    @Override
    public void selectParents(Random rnd_) {
        switch (Util.parentSelection) {
            case LINEAR_RANK:
                rankingSelectionLinear();
                break;
            case EXPONENTIAL_RANK:
                rankingSelectionExponential();
                break;
            case FPS:
                fitnessProportionalSelection();
                break;
        }
        sampleParentSUS(rnd_);
    }

    private void fitnessProportionalSelection() {
        double fitnessSum = 0.0;
        for (Individual individual: population) {
            fitnessSum += individual.getFitness();
        }
        for (Individual individual: population) {
            individual.setSelectionProbability(individual.getFitness() / fitnessSum);
        }
    }

    /**
     * Linear ranking
     */
    private void rankingSelectionLinear() {
        sortPopulation();
        int maxRank = populationSize - 1;
        double prob;
        double s = Util.PARENT_LINEAR_S;
        for (int i = 0; i < populationSize; i++) {
            prob = ((2 - s) / populationSize) + (2 * (maxRank - i) * (s - 1) /
                    (populationSize * (populationSize - 1)));
            population.get(i).setSelectionProbability(prob);
        }
    }

    /**
     * Exponential ranking
     */
    private void rankingSelectionExponential() {
        sortPopulation();
        int maxRank = populationSize - 1;
        double prob;
        double normalisation = 0.0;
        for (int i = 0; i < populationSize; i++) {
            prob = 1 - Math.exp(-maxRank + i);
            population.get(i).setSelectionProbability(prob);
            normalisation += prob;
        }
        // normalise the selection probabilities
        for (int i = 0; i < populationSize; i++) {
            population.get(i).setSelectionProbability(
                    population.get(i).getSelectionProbability() / normalisation);
        }
    }

    private void sampleParentSUS(Random rnd_) {
        double r = rnd_.nextDouble() / (double) offspringSize;
        int i = 0;
        double cumulativeProb = 0.0;
        while (matingPool.size() < offspringSize) {
            cumulativeProb += population.get(i).getSelectionProbability();
            while (r <= cumulativeProb) {
                matingPool.add(population.get(i));
                r += 1 / (double) offspringSize;
            }
            i++;
        }
    }

    /* ****************************
     * RECOMBINATION
     ******************************/

    @Override
    public void recombine(Random rnd_) {
        double[][] parentsValues = new double[Util.N_PARENTS][Util.DIMENSION];
        double[][] childrenValues = new double[Util.N_PARENTS][Util.DIMENSION];

        offspring.clear();
        for (int i = 0; i < offspringSize; i += Util.N_PARENTS) {
            for (int j = 0; j < Util.N_PARENTS; j++) {
                int index = rnd_.nextInt(matingPool.size());
                parentsValues[j] = matingPool.get(index).values;
                matingPool.remove(index);
            }

            switch (Util.recombination) {
                case SIMPLE_ARITHMETIC:
                    childrenValues = singleArithmeticRecombination(rnd_, parentsValues);
                    break;
                case SINGLE_ARITHMETIC:
                    childrenValues = simpleArithmeticRecombination(rnd_, parentsValues);
                    break;
                case WHOLE_ARITHMETIC:
                    childrenValues = wholeArithmeticRecombination(rnd_, parentsValues);
                    break;
                case BLEND:
                    childrenValues = blendRecombination(rnd_, parentsValues);
                    break;
            }

            for (int j = 0; j < Util.N_PARENTS; j++) {
                offspring.add(new Individual(childrenValues[j]));
            }
        }
    }

    /**
     * Simple Arithmetic Recombination
     *
     * @param rnd_ the random generator
     * @param parentsValues the values of the parents
     * @return the values of the children
     */
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

    /**
     * Single Arithmetic Recombination
     *
     * @param rnd_ the random generator
     * @param parentsValues the values of the parents
     * @return the values of the children
     */
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

    /**
     * Whole Arithmetic Recombination
     *
     * @param rnd_ the random generator
     * @param parentsValues the values of the parents
     * @return the values of the children
     */
    private double[][] wholeArithmeticRecombination(Random rnd_, double[][] parentsValues) {
        double[][] childrenValues = new double[Util.N_PARENTS][Util.DIMENSION];
        double alpha = rnd_.nextDouble();
        for (int i = 0; i < Util.DIMENSION; i++) {
            childrenValues[0][i] = alpha * parentsValues[0][i] + (1 - alpha * parentsValues[1][i]);
            childrenValues[1][i] = alpha * parentsValues[1][i] + (1 - alpha * parentsValues[0][i]);
        }
        return childrenValues;
    }

    /**
     * Blend recombination page 67
     *
     * @param rnd_ the random generator
     * @param parentsValues the values of the parents
     * @return the values of the children
     */
    private double[][] blendRecombination(Random rnd_, double[][] parentsValues) {
        double[][] childrenValues = new double[Util.N_PARENTS][Util.DIMENSION];
        double alpha = 0.5;
        for (int i = 0; i < Util.DIMENSION; i++) {
            double u = rnd_.nextDouble();
            double gamma = (1 - 2 * alpha) * u - alpha;
            childrenValues[0][i] = (1 - gamma) * parentsValues[0][i] + (gamma * parentsValues[1][i]);
            childrenValues[1][i] = (1 - gamma) * parentsValues[1][i] + (gamma * parentsValues[0][i]);
        }
        return childrenValues;
    }

    /* ****************************
     * MUTATION
     ******************************/
    public void mutate(Random rnd_, double epsilon)
    {
        for (Individual child: offspring) {
            child.mutate(Util.mutation, rnd_, epsilon);
        }
    }

    /* ****************************
     * SURVIVOR SELECTION
     ******************************/

    @Override
    public void selectSurvivors() {
        switch (Util.survivorSelection) {
            case GENERATIONAL:
                break;
            case MU_PLUS_LAMBDA:
                break;
            case TOURNAMENT:
                break;
        }
    }

    /* ****************************
     * AUXILIARY FUNCTIONS
     ******************************/

    /**
     * Sorts the population
     */
    private void sortPopulation() {
        population.sort(Comparator.comparingDouble(Individual::getFitness));
    }
}
