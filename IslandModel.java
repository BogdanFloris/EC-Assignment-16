import org.vu.contest.ContestEvaluation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class IslandModel implements IPopulation {
    private List<Population> populations;
    private int numberPopulations;
    private Util util;

    public IslandModel(Random rnd_, Util util, int populationSize) {
        this.util = util;
        this.numberPopulations = util.N_POPULATIONS;
        this.populations = new ArrayList<>();
        int subPopSize = populationSize / numberPopulations;
        for (int i = 0; i < numberPopulations; i++) {
            populations.add(new Population(rnd_, util, subPopSize));
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
    public void makeExchange(Random rnd_) {
        switch (util.topology) {
            case RING:
                makeExchangeRingModel(rnd_);
                break;
            case TORUS:
                makeExchangeTorus(Util.TORUS_N, Util.TORUS_M, rnd_);
                break;
            case RANDOM:
                makeExchangeRandom(rnd_);
        }
    }

    private void makeExchangeRandom(Random rnd_) {
        int n = Util.N_EXCHANGED;
        exchangeSelection(n,rnd_);
        List<Population> shuffled = new ArrayList<>(populations);
        Collections.shuffle(shuffled);
        for (int i = 0; i < numberPopulations; i++) {
            populations.get(i).addToPopulation(shuffled.get(i).getExchange());
        }
    }

    private void makeExchangeRingModel(Random rnd_) {
        int n = Util.N_EXCHANGED;
        exchangeSelection(n, rnd_);

        for (int i = 1; i <= numberPopulations; i++) {
            int neighbour = i;
            if (neighbour == numberPopulations) {
                neighbour = 0;
            }
            populations.get(i - 1).addToPopulation(populations.get(neighbour).getExchange());
        }
    }

    private void makeExchangeTorus(int n, int m, Random rnd_) {
        int numberExchanged = Util.N_EXCHANGED;
        exchangeSelection(numberExchanged, rnd_);
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

    private void exchangeSelection(int n, Random rnd_){
        switch (util.policy) {
            case BEST_WORST:
                for (int i = 0; i < numberPopulations; i++) {
                    populations.get(i).bestNIndividuals(n);
                    populations.get(i).removeWorst(n);
                }
                break;
            case RANDOM_RANDOM:
                for (int i = 0; i < numberPopulations; i++) {
                    populations.get(i).randomNIndividuals(n, rnd_);
                    populations.get(i).removeRandom(n);
                }
                break;
        }

    }
}
