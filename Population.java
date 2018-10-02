import org.vu.contest.ContestEvaluation;
import java.util.Random;

/**
 * Main Population class
 */
public class Population implements IPopulation {
    private int size;
    private double offspringRatio;
    private int offspringSize;
    Individual[] population;
    Individual[] matingpool;
    Individual[] offspring;

    /**
     * Constructor with only a Random object
     *
     * @param rnd_ Random class to be used
     */
    public Population(Random rnd_) {
        this.size = Util.POPULATION_SIZE;

        offspringRatio = Util.OFFSPRING_RATIO;
        offspringSize = (int) (size * offspringRatio);

        population = new Individual[this.size];
        matingpool = new Individual[offspringSize];
        offspring = new Individual[offspringSize];

        for (int i = 0; i < size; i++) {
            population[i] = new Individual(rnd_);
        }
    }

    @Override
    public void evalInitialPopulation(ContestEvaluation evaluation_) {
        for (int i = 0; i < size; i++) {
            population[i].setFitness((double) evaluation_.evaluate(population[i].values));
        }
    }

    @Override
    public void evalOffspring(ContestEvaluation eval) {

    }

    @Override
    public void selectParents() {

    }

    @Override
    public void selectSurvivors() {

    }
}
