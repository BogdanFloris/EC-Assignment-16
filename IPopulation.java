import org.vu.contest.ContestEvaluation;
import java.util.Random;

/**
 * Interface for a IPopulation of individuals.
 * Implement this in order to create a population.
 */
public interface IPopulation {
    /**
     * Evaluates the initial population
     *
     * @param eval the ContestEvaluation
     *             needed to evaluate the individuals
     * @return the number of evaluations used
     */
    int evalInitialPopulation(ContestEvaluation eval);

    /**
     * Evaluates the newly created offspring
     *
     * @param eval the ContestEvaluation
     *             needed to evaluate the individuals
     * @return the number of evaluations used
     */
    int evalOffspring(ContestEvaluation eval);

    /**
     * Selects the parents used to create children
     */
    void selectParents(Random rnd_);

    /**
     * Recombines parent genotype to create children
     */
    void recombine(Random rnd_);


    /**
     * Mutates
     */
    void mutate(Random rnd_, double epsilon);

    /**
     * Selects the individuals that will survive in the next generation
     */
    void selectSurvivors();

    /**
     * Prints the fitness of the population
     */
    void printFitness();

    /**
     * Computes the sum of the allele-values of all of the individuals in the population.
     */
    double[] getSumValues();

    /**
     * Computes population diversity measure due to Morrison & de Jong (2001).
     */
    double[] getDiversity(double[] meanValues);

    void makeExchange(Random rnd_);
}
