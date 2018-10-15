import org.vu.contest.ContestEvaluation;

import java.util.ArrayList;
import java.util.Collections;
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

    @Override
    public void makeExchange() {
        switch (Util.topology) {
            case RING:
                makeExchangeRingModel();
                break;
            case TORUS:
                makeExchangeTorus(Util.TORUS_N, Util.TORUS_M);
                break;
            case RANDOM:
                makeExchangeRandom();
        }
    }

    private void makeExchangeRandom() {
        int n = Util.N_EXCHANGED;
        for (int i = 0; i < numberPopulations; i++) {
            populations.get(i).bestNIndividuals(n);
            populations.get(i).removeWorst(n);
        }
        List<Population> shuffled = new ArrayList<>(populations);
        Collections.shuffle(shuffled);
        for (int i = 0; i < numberPopulations; i++) {
            populations.get(i).addToPopulation(shuffled.get(i).getExchange());
        }
    }

    private void makeExchangeRingModel() {
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

    private void makeExchangeTorus(int n, int m) {
        int numberExchanged = Util.N_EXCHANGED;
        for (int i = 0; i < numberPopulations; i++) {
            populations.get(i).bestNIndividuals(numberExchanged);
            populations.get(i).removeWorst(numberExchanged);
        }
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                // Left neighbor
                if (j != 0) {
                    populations.get(i * m + j).addToPopulation(populations
                            .get(i * m + j - 1).getExchange(numberExchanged / 4));
                } else {
                    populations.get(i * m + j).addToPopulation(populations
                            .get(i * m + m - 1).getExchange(numberExchanged / 4));
                }
                // Right neighbor
                if (j != m - 1) {
                    populations.get(i * m + j).addToPopulation(populations
                            .get(i * m + j + 1).getExchange(numberExchanged / 4));
                } else {
                    populations.get(i * m + j).addToPopulation(populations
                            .get(i * m).getExchange(numberExchanged / 4));
                }
                // Up
                if (i != 0) {
                    populations.get(i * m + j).addToPopulation(populations
                            .get((i - 1) * m + j).getExchange(numberExchanged / 4));
                } else {
                    populations.get(i * m + j).addToPopulation(populations
                            .get((n - 1) * m + j).getExchange(numberExchanged / 4));
                }
                // Down
                if (i != n - 1) {
                    populations.get(i * m + j).addToPopulation(populations
                            .get((i + 1) * m + j).getExchange(numberExchanged / 4));
                } else {
                    populations.get(i * m + j).addToPopulation(populations
                            .get(j).getExchange(numberExchanged / 4));
                }
            }
        }
    }
}
