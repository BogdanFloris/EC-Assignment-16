/**
 * Utilities for the algorithm:
 * e.g.: constants, public methods, variables used for the program
 */
class Util {
    // Dimension of the functions; 10 dimensions
    final static int DIMENSION = 10;
    // The minimum value that the variables (phenotypes) can take
    final static double MIN_VALUE = -5.0;
    // The maximum value that the variables (phenotypes) can take
    final static double MAX_VALUE = 5.0;
    // The step size of the mutation / standard deviation used for nextGaussian
    final static double MUTATION_STEP_SIZE = 0.25;
    // The mutation rate used in the uniform mutation to determine which genotypes are changed
    final static double MUTATION_RATE = 0.1;
    // The parameter s used in linear parent selection (P. 82)
    final static double PARENT_LINEAR_S = 2;
    // The number of parents used for recombination
    final static int N_PARENTS = 2;

    // tauSimple
    static double tauSimple;
    // local tauSimple
    static double tauPrime;
    // global tauSimple
    static double tau;
    // epsilon
    static double epsilon;

    static Mutation mutation = Mutation.CORRELATED;
    static ParentSelection parentSelection = ParentSelection.EXPONENTIAL_RANK;
    static Recombination recombination = Recombination.BLEND;
    static SurvivorSelection survivorSelection = SurvivorSelection.MU_PLUS_LAMBDA;

    // mutation options for an individual
    enum Mutation {
        UNIFORM,
        NON_UNIFORM,
        UNCORRELATED_ONE_STEP,
        UNCORRELATED_N_STEP,
        CORRELATED
    }

    // parent selection options for a population
    enum ParentSelection {
        LINEAR_RANK,
        EXPONENTIAL_RANK,
        FPS
    }

    // recombination options for parents
    enum Recombination {
        SIMPLE_ARITHMETIC,
        SINGLE_ARITHMETIC,
        WHOLE_ARITHMETIC,
        BLEND
    }

    // survivor selection options
    enum SurvivorSelection {
        GENERATIONAL,
        MU_PLUS_LAMBDA,
        TOURNAMENT
    }

    // The ratio of offspring to population size
    final static double OFFSPRING_RATIO = 1.0;
    // The number of individuals in the population
    final static int POPULATION_SIZE = 10;

    Util() {
        tauSimple = 1 / Math.sqrt(DIMENSION);
        tauPrime = 1 / Math.sqrt(2 * DIMENSION);
        tau = 1 / Math.sqrt(2 * Math.sqrt(DIMENSION));
        epsilon = 0.01;
    }
}
