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

    // mutation options for an individual
    enum Mutation {
        UNIFORM,
        NON_UNIFORM,
        UNCORRELATED_ONE_STEP,
        UNCORRELATED_N_STEP,
        C0RRELATED
    }
}
