/**
 * Utilities for the algorithm:
 * e.g.: constants, public methods, variables used for the program
 */
class Util {
    // Compute population statistics (diversity)
    final static boolean COMPUTE_STATS = false;
    // Dimension of the functions; 10 dimensions
    final static int DIMENSION = 10;
    // The minimum value that the variables (phenotypes) can take
    final static double MIN_VALUE = -5.0;
    // The maximum value that the variables (phenotypes) can take
    final static double MAX_VALUE = 5.0;
    // The step size of the mutation / standard deviation used for nextGaussian
    final static double MUTATION_STEP_SIZE = 0.05;
    // Rate of the mutation
    final static double MUTATION_RATE = 0.1;
    // The parameter s used in linear parent selection (P. 82)
    final static double PARENT_LINEAR_S = 2.0;
    // The number of parents used for recombination
    final static int N_PARENTS = 2;
    // Sigma share used in fitness sharing (value should be between 5 and 10)
    final static double SIGMA_SHARE = 5.0;
    // Tournament selection k
    final static int TOURNAMENT_K = 2;

    // tauSimple
    double tauSimple;
    // local tauSimple
    double tauPrime;
    // epsilon
    double epsilon;

    Mutation mutation = Mutation.UNCORRELATED_N_STEP;
    ParentSelection parentSelection = ParentSelection.TOURNAMENT;
    Recombination recombination = Recombination.WHOLE_ARITHMETIC;
    SurvivorSelection survivorSelection = SurvivorSelection.MU_PLUS_LAMBDA;
    Topology topology = Topology.RING;
    Policy policy = Policy.BEST_WORST;
    // use fitness sharing or not
    boolean FITNESS_SHARING;
    // use deterministic crowding or not
    boolean DETERMINISTIC_CROWDING;
    // use island model
    boolean ISLAND_MODEL;

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
        UNIFORM,
        LINEAR_RANK,
        EXPONENTIAL_RANK,
        FPS,
        TOURNAMENT
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

    // topologies
    enum Topology {
        RING,
        TORUS,
        RANDOM
    }

    // migration policy
    enum Policy {
        RANDOM_RANDOM,
        BEST_WORST
    }

    // The ratio of offspring to population size
    final static double OFFSPRING_RATIO = 1.0;
    // The number of individuals in the population
    int POPULATION_SIZE; // depends
    // Number of populations in island model
    int N_POPULATIONS;
    // epoch (for exchange)
    int EPOCH; // kinda 50 ish
    // number of exchanged individuals
    final static int N_EXCHANGED = 8; // between 2-5
    // torus n and m
    final static int TORUS_N = 2;
    final static int TORUS_M = 5;

    Util() {
        this.DETERMINISTIC_CROWDING = false;
        this.FITNESS_SHARING = false;
        this.ISLAND_MODEL = false;
        this.POPULATION_SIZE = 100;
        this.N_POPULATIONS = 1;
        this.EPOCH = 0;
        tauSimple = 1 / Math.sqrt(2 * DIMENSION);
        tauPrime = 1 / Math.sqrt(2 * Math.sqrt(DIMENSION));
        epsilon = 0.01;
    }

    // Constructor using another Util
    Util(Util util) {
        this.POPULATION_SIZE = util.POPULATION_SIZE;
        this.N_POPULATIONS = util.N_POPULATIONS;
        this.EPOCH = util.EPOCH;
        this.DETERMINISTIC_CROWDING = util.DETERMINISTIC_CROWDING;
        this.FITNESS_SHARING = util.FITNESS_SHARING;
        this.ISLAND_MODEL = util.ISLAND_MODEL;
    }

    void changeIslandUtils(int nPopulations, int epoch) {
        this.N_POPULATIONS = nPopulations;
        this.EPOCH = epoch;
        this.ISLAND_MODEL = true;
    }

    void changeMutationParameters(double tauSimple, double tauPrime, double epsilon) {
        this.tauSimple = tauSimple;
        this.tauPrime = tauPrime;
        this.epsilon = epsilon;
    }
}
