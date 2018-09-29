import java.util.Random;

/**
 * Class that represents an Individual and contains
 * all the methods that are specific to a single
 * individual of a population.
 */
class Individual {
    // the 10 values of the individual (phenotypes)
    double[] values;
    // the fitness of this specific individual
    private double fitness;
    // the mutation step size used for uncorrelated mutations with one step size
    private double sigma = Util.MUTATION_STEP_SIZE;
    // the mutation step sizes used for uncorrelated mutations with n steps
    private double[] sigmas;
    // alphas used for the correlated mutation
    private double[] alphas;
    // covariance matrix used for the correlated mutation
    private double[][] cov;

    /**
     * Constructor with only a Random object
     *
     * @param rnd_ Random class to be used
     */
    Individual(Random rnd_) {
        this.values = new double[Util.DIMENSION];
        this.fitness = 0.0;
        // initialize values
        for (int i = 0; i < this.values.length; i++) {
            this.values[i] = generateInRange(rnd_.nextDouble());
        }
        this.sigmas = new double[Util.DIMENSION];
        for (int i = 0; i < this.sigmas.length; i++) {
            this.sigmas[i] = sigma;
        }
    }

    /**
     * Constructor with a parent's values
     *
     * @param values the values inherited
     */
    Individual(double[] values) {
        this.values = new double[Util.DIMENSION];
        this.fitness = 0.0;
        for (int i = 0; i < this.values.length; i++) {
            this.values[i] = values[i];
        }
        this.sigmas = new double[Util.DIMENSION];
        for (int i = 0; i < this.sigmas.length; i++) {
            this.sigmas[i] = sigma;
        }
    }

    void mutate(Util.Mutation mutation, Random rnd_, double epsilon) {
        switch (mutation) {
            case UNIFORM:
                uniformMutation(rnd_);
                break;
            case NON_UNIFORM:
                nonUniformMutation(rnd_);
                break;
            case UNCORRELATED_ONE_STEP:
                uncorrelatedMutationOneStep(rnd_, epsilon);
                break;
            case UNCORRELATED_N_STEP:
                uncorrelatedMutationNStep(rnd_, epsilon);
                break;
            case CORRELATED:
                correlatedMutation(rnd_, epsilon);
                break;
            default:
                System.err.println("Invalid mutation");
        }
    }

    public double getFitness() {
        return fitness;
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }

    /**
     * Uniform Mutation
     *
     * @param rnd_ Random object to be used
     */
    private void uniformMutation(Random rnd_) {
        for (int i = 0; i < this.values.length; i++) {
            double chance = rnd_.nextDouble();
            if (chance < sigma) {
                this.values[i] = generateInRange(rnd_.nextGaussian());
            }
        }
    }

    /**
     * Non-Uniform Mutation
     *
     * @param rnd_ Random object to be used
     */
    private void nonUniformMutation(Random rnd_) {
        for (int i = 0; i < this.values.length; i++) {
            this.values[i] += sigma * rnd_.nextGaussian();
            // make sure the values stay within the bounds
            this.values[i] = keepInRange(this.values[i]);
        }
    }

    /**
     * Uncorrelated mutation with one step size
     *
     * @param rnd_ Random object to be used
     */
    private void uncorrelatedMutationOneStep(Random rnd_, double epsilon) {
        this.sigma = Math.max(epsilon, this.sigma * Math.exp(
                Util.tauSimple * rnd_.nextGaussian()));
        for (int i = 0; i < this.values.length; i++) {
            this.values[i] += this.sigma * rnd_.nextGaussian();
            // make sure the values stay within bounds
            this.values[i] = keepInRange(this.values[i]);
        }
    }

    /**
     * Uncorrelated mutation with N step size
     *
     * @param rnd_ Random object to be used
     */
    private void uncorrelatedMutationNStep(Random rnd_, double epsilon) {
        double tau = Util.tau;
        double tauPrime = Util.tauPrime;
        double tauGauss = tauPrime * rnd_.nextGaussian();

        for (int i = 0; i < this.values.length; i++) {
            this.sigmas[i] = Math.max(epsilon, this.sigmas[i] * Math.exp(
                    tauGauss + tau * rnd_.nextGaussian()));
            this.values[i] += this.sigmas[i] * rnd_.nextGaussian();
            this.values[i] = keepInRange(this.values[i]);
        }
    }

    /**
     * Correlated mutation
     *
     * @param rnd_ Random object to be used
     */
    private void correlatedMutation(Random rnd_, double epsilon) {
        // initialize parameters
        double tau = Util.tau;
        double tauPrime = Util.tauPrime;
        double beta = 5;
        int n = Util.DIMENSION;

        // calculate n alpha
        int nAlpha = n * (n - 1) / 2;
        // calculate tau multiplied by a normal distribution sample
        double tauGauss = tauPrime * rnd_.nextGaussian();

        // initialize arrays
        this.alphas = new double[nAlpha];
        // covariance matrix
        this.cov = new double[n][n];
        // means used for the multivariate normal distribution
        double[] means = new double[n];
        // change in x to be added (sampled from the multivariate normal dist)
        double[] dx = new double[n];

        // mutate sigmas
        for (int i = 0; i < n; i++) {
            this.sigmas[i] = Math.max(epsilon, this.sigmas[i] * Math.exp(
                    tauGauss + tau * rnd_.nextGaussian()));
        }

        // mutate alphas
        for (int j = 0; j < nAlpha; j++) {
            this.alphas[j] += beta * rnd_.nextGaussian();
            if (Math.abs(this.alphas[j]) > Math.PI) {
                this.alphas[j] -= 2 * Math.PI * Math.signum(this.alphas[j]);
            }
        }

        // calculate covariance matrix
        calculateCovarianceMatrix(n);

        // get the samples from the multivariate normal distribution
        // mutate the genotype
        for (int i = 0; i < n; i++) {
            this.values[i] += dx[i];
            this.values[i] = keepInRange(this.values[i]);
        }
    }

    /**
     * Calculates the covariance matrix
     *
     * @param n dimension of the function
     */
    private void calculateCovarianceMatrix(int n) {
        // index used to traverse the alphas array
        int alphaIndex = 0;
        // calculate values on the diagonal and above it
        for (int i = 0; i < n; i++) {
            this.cov[i][i] = Math.pow(this.sigmas[i], 2);
            for (int j = i + 1; j < n; j++) {
                this.cov[i][j] = 0.5 * (Math.pow(this.sigmas[i], 2) -
                        Math.pow(this.sigmas[j], 2)) * Math.tan(
                                2 * this.alphas[alphaIndex]);
            }
            alphaIndex++;
        }

        // calculate values under the diagonal
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < i; j++) {
                this.cov[i][j] = this.cov[j][i];
            }
        }
    }

    /**
     * Generates values in range [-5,5]
     *
     * @param val the random generated value
     * @return the value in range
     */
    private double generateInRange(double val) {
        return val * (Util.MAX_VALUE - Util.MIN_VALUE) + Util.MIN_VALUE;
    }

    /**
     * Makes sure that {@param val} is in the range [-5,5]
     *
     * @param val the value to be kept in range
     * @return the value if it's in range, -5 if it's lower or 5 if it's higher
     */
    private double keepInRange(double val) {
        return Math.min(Util.MAX_VALUE, Math.max(
                Util.MIN_VALUE, val));
    }
}
