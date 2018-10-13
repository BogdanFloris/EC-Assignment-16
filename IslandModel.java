import org.vu.contest.ContestEvaluation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class IslandModel implements IPopulation {
    private List<Population> populations;
    private int numberPopulations;

    public IslandModel(Random rnd_, int populationSize) {
        this.numberPopulations = Util.N_POPULATIONS;
        this.populations = new ArrayList<>();
        int subPopSize = populationSize / numberPopulations;
        for (int i = 0; i < numberPopulations; i++) {
            populations.add(new Population(rnd_, subPopSize));
        }
    }

    @Override
    public int evalInitialPopulation(ContestEvaluation eval) {
        int evaluations = 0;
        for (Population population: populations) {
            evaluations += population.evalInitialPopulation(eval);
        }
        return evaluations;
    }

    @Override
    public int evalOffspring(ContestEvaluation eval) {
        int evaluations = 0;
        for (Population population: populations) {
            evaluations += population.evalOffspring(eval);
        }
        return evaluations;
    }

    @Override
    public void selectParents(Random rnd_) {
        for (Population population: populations) {
            population.selectParents(rnd_);
        }
    }

    @Override
    public void recombine(Random rnd_) {
        for (Population population: populations) {
            population.recombine(rnd_);
        }
    }

    @Override
    public void mutate(Random rnd_, double epsilon) {
        for (Population population: populations) {
            population.mutate(rnd_, epsilon);
        }
    }

    @Override
    public void selectSurvivors() {
        for (Population population: populations) {
            population.selectSurvivors();
        }
    }

    public void printFitness()
    {
        for (Population population: populations) {
            population.printFitness();
        }
    }

    public void makeExchangeRingModel() {
        int n = Util.N_EXCHANGED;
        for (int i = 0; i < numberPopulations; i++) {
            populations.get(i).bestNIndividuals(n);
            populations.get(i).removeWorst(n);
        }

        for (int i = 0; i < numberPopulations; i++) {
            int neighbour = i + 1;
            if (neighbour == numberPopulations) {
                neighbour = 0;
            }
            populations.get(i).addToPopulation(populations.get(neighbour).getExchange());
        }
    }
}
