import org.vu.contest.ContestEvaluation;

import java.util.*;

/**
 * Main Population class
 */
public class Population implements IPopulation {
    private int populationSize;
    private int offspringSize;
    private List<Individual> population;
    private List<Individual> offspring;
    private List<Individual> matingPool;
    private List<Individual> exchange;

    /**
     * Constructor with only a Random object
     *
     * @param rnd_ Random class to be used
     */
    public Population(Random rnd_, int populationSize) {
        this.populationSize = populationSize;

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
    public int evalInitialPopulation(ContestEvaluation eval) {
        int evaluations = 0;
        for (Individual individual: population) {
            individual.setFitness((double) eval.evaluate(individual.values));
            evaluations++;
        }
        return evaluations;
    }

    @Override
    public int evalOffspring(ContestEvaluation eval) {
        int evaluations = 0;
        for (Individual individual: offspring) {
            individual.setFitness((double) eval.evaluate(individual.values));
            evaluations++;
        }
        return evaluations;
    }

    /* *******************
     * PARENT SELECTION
     *********************/

    @Override
    public void selectParents(Random rnd_) {
        if (Util.FITNESS_SHARING) {
            fitnessSharing();
        }
        if (Util.parentSelection == Util.ParentSelection.TOURNAMENT) {
            tournamentSelection(rnd_);
        }
        else {
            switch (Util.parentSelection) {
                case UNIFORM:
                    uniformParentSelection();
                case FPS:
                    fitnessProportionalSelection();
                    break;
                case LINEAR_RANK:
                    rankingSelectionLinear();
                    break;
                case EXPONENTIAL_RANK:
                    rankingSelectionExponential();
                    break;
            }
            sampleParentSUS(rnd_);
        }
    }

    private void tournamentSelection(Random rnd_) {
        while (matingPool.size() < offspringSize) {
            List<Individual> tournament = new ArrayList<>();
            for (int i = 0; i < Util.TOURNAMENT_K; i++) {
                tournament.add(population.get(rnd_.nextInt(populationSize)));
            }
            tournament.sort(Comparator.comparingDouble(Individual::getFitness).reversed());
            matingPool.add(tournament.get(0));
        }
    }

    private void uniformParentSelection() {
        for (Individual individual: population) {
            individual.setSelectionProbability(1.0 / populationSize);
        }
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
        double prob;
        double s = Util.PARENT_LINEAR_S;
        for (int i = 0; i < populationSize; i++) {
            prob = ((2 - s) / populationSize) + (2 * i * (s - 1) /
                    (populationSize * (populationSize - 1)));
            population.get(i).setSelectionProbability(prob);
        }
    }

    /**
     * Exponential ranking
     */
    private void rankingSelectionExponential() {
        sortPopulation();
        double prob;
        double normalisation = 0.0;
        for (int i = 0; i < populationSize; i++) {
            prob = 1 - Math.exp(-i);
            population.get(i).setSelectionProbability(prob);
            normalisation += prob;
        }
        // normalise the selection probabilities
        for (int i = 0; i < populationSize; i++) {
            population.get(i).setSelectionProbability(
                    population.get(i).getSelectionProbability() / normalisation);
        }
    }

    /**
     * Roulette wheel sampling
     */
    private void sampleParentRoulette(Random rnd_) {
        List<Double> cumulativeProb = getCumulativeProb();
         while (matingPool.size() < offspringSize) {
             int i = 0;
             double r = rnd_.nextDouble();
             while (cumulativeProb.get(i) < r) {
                 i++;
             }
             matingPool.add(population.get(i));
        }
    }

    /**
     * Stochastic Universal Sampling
     */
    private void sampleParentSUS(Random rnd_) {
        List<Double> cumulativeProb = getCumulativeProb();
        System.out.println(cumulativeProb.get(cumulativeProb.size() - 1));
        double r = rnd_.nextDouble() / (double) offspringSize;
        int i = 0;
        while (matingPool.size() < offspringSize) {
            while (r <= cumulativeProb.get(i)) {
                matingPool.add(population.get(i));
                r += 1 / (double) offspringSize;
            }
            i++;
        }
    }

    private List<Double> getCumulativeProb() {
        List<Double> cumulativeProb = new ArrayList<>();
        cumulativeProb.add(population.get(0).getSelectionProbability());
        for (int i = 1; i < populationSize; i++) {
            cumulativeProb.add(cumulativeProb.get(i - 1) + population.get(i).getSelectionProbability());
        }
        return cumulativeProb;
    }

    /* ****************************
     * RECOMBINATION
     ******************************/

    @Override
    public void recombine(Random rnd_) {
        if (Util.DETERMINISTIC_CROWDING) {
            deterministicCrowding(rnd_);
            return;
        }

        double[][] parentsValues = new double[Util.N_PARENTS][Util.DIMENSION];
        double[][] childrenValues;

        offspring.clear();
        for (int i = 0; i < offspringSize; i += Util.N_PARENTS) {
            for (int j = 0; j < Util.N_PARENTS; j++) {
                int index = rnd_.nextInt(matingPool.size());
                parentsValues[j] = matingPool.get(index).values;
                matingPool.remove(index);
            }

            try {
                childrenValues = chooseRecombination(rnd_, parentsValues);
            }
            catch (NullPointerException e) {
                throw new NullPointerException("Invalid recombination");
            }

            for (int j = 0; j < Util.N_PARENTS; j++) {
                if (childrenValues != null) {
                    offspring.add(new Individual(childrenValues[j]));
                }
            }
        }
    }

    private double[][] chooseRecombination(Random rnd_, double[][] parentsValues) {
        switch (Util.recombination) {
            case SIMPLE_ARITHMETIC:
                return singleArithmeticRecombination(rnd_, parentsValues);
            case SINGLE_ARITHMETIC:
                return simpleArithmeticRecombination(rnd_, parentsValues);
            case WHOLE_ARITHMETIC:
                return wholeArithmeticRecombination(rnd_, parentsValues);
            case BLEND:
                return blendRecombination(rnd_, parentsValues);
            default:
                return null;
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
            childrenValues[0][i] = alpha * parentsValues[0][i] + (1 - alpha) * parentsValues[1][i];
            childrenValues[1][i] = alpha * parentsValues[1][i] + (1 - alpha) * parentsValues[0][i];
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
                generational();
                break;
            case MU_PLUS_LAMBDA:
                muPlusLambda();
                break;
            case TOURNAMENT:
                break;
        }
    }

    /**
     * Replaces all parents with the children.
     */
    private void generational() {
        population.clear();
        population.addAll(offspring);
        offspring.clear();
    }

    /**
     * Merges parents with children and keeps the best @{populationSize}.
     */
    private void muPlusLambda() {
        population.addAll(offspring);
        sortPopulationReverse();
        population.subList(populationSize, populationSize + offspringSize).clear();
    }

    /* ****************************
     * METHODS FOR MULTI-MODALITY
     ******************************/

    /**
     * Applies fitness sharing to the entire population.
     */
    private void fitnessSharing() {
        for (int i = 0; i < populationSize; i++) {
            double sum = 0.0;
            for (int j = 0; j < populationSize; j++) {
                sum += sh(distance(population.get(i), population.get(j)));
            }
            population.get(i).setFitness(population.get(i).getFitness() / sum);
        }
    }

    /**
     * Gets the value of the sharing function
     *
     * @param distance between two individuals
     * @return the value of the function
     */
    private double sh(double distance) {
        // determines shape of the sharing function
        // 1.0 means linear
        double alpha = 1.0;
        double sigma_share = Util.SIGMA_SHARE;
        if (distance <= sigma_share) {
            return 1 - Math.pow(distance / sigma_share, alpha);
        }
        else {
            return 0.0;
        }
    }

    /**
     * Applies deterministic crowding to the population
     */
    private void deterministicCrowding(Random rnd_) {
        double[][] parentsValues = new double[Util.N_PARENTS][Util.DIMENSION];
        double[][] childrenValues;

        Collections.shuffle(matingPool);
        for (int i = 0; i < populationSize; i += Util.N_PARENTS) {
            parentsValues[0] = matingPool.get(i).values;
            parentsValues[1] = matingPool.get(i + 1).values;

            try {
                childrenValues = chooseRecombination(rnd_, parentsValues);
            }
            catch (NullPointerException e) {
                throw new NullPointerException("Invalid recombination");
            }

            for (int j = 0; j < Util.N_PARENTS; j++) {
                if (childrenValues != null) {
                    offspring.add(new Individual(childrenValues[j]));
                }
            }
        }
    }

    /* ****************************
     * ISLAND MODEL FUNCTIONS
     ******************************/

    /**
     * Selects the best N individuals from the population.
     *
     * @param n the amount of individuals
     */
    void bestNIndividuals(int n) {
        exchange = new ArrayList<>();
        sortPopulationReverse();
        for (int i = 0; i < n; i++) {
            exchange.add(population.get(i));
        }
    }

    /**
     * Selects N random individuals from the population.
     *
     * @param n the amount of individuals
     * @param rnd_ randomizer
     */
    void randomNIndividuals(int n, Random rnd_) {
        exchange = new ArrayList<>();
        sortPopulationReverse();
        for (int i = 0; i < n; i++) {
            int index = rnd_.nextInt(populationSize);
            exchange.add(population.get(index));
        }
    }

    /**
     * Returns a copy of the individuals to be exchanged
     *
     * @return the copied exchange list
     */
    List<Individual> getExchange() {
        return new ArrayList<>(this.exchange);
    }

    /**
     * Returns a copy of {@param n} individuals to be exchanged.
     *
     * @param n the number of individuals to be exchanged
     * @return the individuals
     */
    List<Individual> getExchange(int n) {
        List<Individual> toExchange = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            toExchange.add(exchange.remove(i));
        }
        return toExchange;
    }

    /**
     * Adds the list of individuals to the population.
     *
     * @param toAdd list of individuals
     */
    void addToPopulation(List<Individual> toAdd) {
        population.addAll(toAdd);
    }

    /**
     * Removes the worst n individuals from the population.
     *
     * @param n the number of individuals
     */
    void removeWorst(int n)
    {
        sortPopulationReverse();
        population.subList(populationSize - n, populationSize).clear();
    }

    /**
     * Removes the n randomly selected individuals from the population.
     *
     * @param n the number of individuals
     */
    void removeRandom(int n)
    {
        Collections.shuffle(population);
        population.subList(populationSize - n, populationSize).clear();
    }


    /* ****************************
     * AUXILIARY FUNCTIONS
     ******************************/

    /**
     * Calculates the distance between two individuals. (Euclidian)
     *
     * @param a first individual
     * @param b second individual
     * @return distance between a and b
     */
    private double distance(Individual a, Individual b) {
        double distance = 0.0;
        for (int i = 0; i < a.values.length; i++) {
            distance += Math.pow(a.values[i] - b.values[i], 2);
        }
        return Math.sqrt(distance);
    }

    /**
     * Sorts the population
     */
    private void sortPopulation() {
        population.sort(Comparator.comparingDouble(Individual::getFitness));
    }

    /**
     * Sorts the population and reverses it.
     */
    private void sortPopulationReverse() {
        population.sort(Comparator.comparingDouble(Individual::getFitness).reversed());
    }

    public void printFitness()
    {
        int i = 0;
        StringBuilder s = new StringBuilder();
        s.append("[");
        for (Individual ind: population) {
            s.append(ind.getFitness());
            s.append(", ");
            i++;
            if (i % 8 == 0) {
                s.append("\n");
            }
        }
        s.append("]\n");
        System.out.print(s.toString());
    }

    @Override
    public void makeExchange(Random rnd_) {
        throw new UnsupportedOperationException();
    }
}
