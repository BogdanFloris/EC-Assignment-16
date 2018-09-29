import org.vu.contest.ContestEvaluation;

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
     */
    void evalInitialPopulation(ContestEvaluation eval);

    /**
     * Evaluates the newly created offspring
     */
    void evalOffspring(ContestEvaluation eval);

    /**
     * Selects the parents used to create children
     */
    void selectParents();

    /**
     * Selects the individuals that will survive in the next generation
     */
    void selectSurvivors();
}
