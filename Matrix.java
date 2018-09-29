public class Matrix implements Cloneable, java.io.Serializable {

/* ------------------------
   Class variables
 * ------------------------ */

    /** Array for internal storage of elements.
     @serial internal array storage.
     */
    private double[][] A;

    /** Row and column dimensions.
     @serial row dimension column dimension.
     */
    private int m, n;

/* ------------------------
   Constructors
 * ------------------------ */

    /** Construct an m-by-n matrix of zeros.
     @param m    Number of rows.
     @param n    Number of colums.
     */

    public Matrix (int m, int n) {
        this.m = m;
        this.n = n;
        A = new double[m][n];
    }

    /** Construct an m-by-n constant matrix.
     @param m    Number of rows.
     @param n    Number of colums.
     @param s    Fill the matrix with this scalar value.
     */

    public Matrix (int m, int n, double s) {
        this.m = m;
        this.n = n;
        A = new double[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                A[i][j] = s;
            }
        }
    }

    /** Construct a matrix from a 2-D array.
     @param A    Two-dimensional array of doubles.
     @exception  IllegalArgumentException All rows must have the same length
     */

    public Matrix (double[][] A) {
        m = A.length;
        n = A[0].length;
        for (int i = 0; i < m; i++) {
            if (A[i].length != n) {
                throw new IllegalArgumentException("All rows must have the same length.");
            }
        }
        this.A = A;
    }

    /** Construct a matrix quickly without checking arguments.
     @param A    Two-dimensional array of doubles.
     @param m    Number of rows.
     @param n    Number of colums.
     */

    public Matrix (double[][] A, int m, int n) {
        this.A = A;
        this.m = m;
        this.n = n;
    }

    /** Construct a matrix from a one-dimensional packed array
     @param vals One-dimensional array of doubles, packed by columns (ala Fortran).
     @param m    Number of rows.
     @exception  IllegalArgumentException Array length must be a multiple of m.
     */

    public Matrix (double vals[], int m) {
        this.m = m;
        n = (m != 0 ? vals.length/m : 0);
        if (m*n != vals.length) {
            throw new IllegalArgumentException("Array length must be a multiple of m.");
        }
        A = new double[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                A[i][j] = vals[i+j*m];
            }
        }
    }

/* ------------------------
   Public Methods
 * ------------------------ */

    /** Make a deep copy of a matrix
     */

    private Matrix copy () {
        Matrix X = new Matrix(m,n);
        double[][] C = X.getArray();
        for (int i = 0; i < m; i++) {
            if (n >= 0) System.arraycopy(A[i], 0, C[i], 0, n);
        }
        return X;
    }

    /** Clone the Matrix object.
     */

    public Object clone () {
        return this.copy();
    }

    /** Access the internal two-dimensional array.
     @return     Pointer to the two-dimensional array of matrix elements.
     */

    public double[][] getArray () {
        return A;
    }

    /** Copy the internal two-dimensional array.
     @return     Two-dimensional array copy of matrix elements.
     */

    public double[][] getArrayCopy () {
        double[][] C = new double[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                C[i][j] = A[i][j];
            }
        }
        return C;
    }

    /** Get row dimension.
     @return     m, the number of rows.
     */

    public int getRowDimension () {
        return m;
    }

    /** Get column dimension.
     @return     n, the number of columns.
     */

    public int getColumnDimension () {
        return n;
    }

    /** Matrix transpose.
     @return    A'
     */

    public Matrix transpose () {
        Matrix X = new Matrix(n, m);
        double[][] C = X.getArray();
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                C[j][i] = A[i][j];
            }
        }
        return X;
    }

    /**  Unary minus
     @return    -A
     */

    public Matrix uminus () {
        Matrix X = new Matrix(m,n);
        double[][] C = X.getArray();
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                C[i][j] = -A[i][j];
            }
        }
        return X;
    }

    /** C = A + B
     @param B    another matrix
     @return     A + B
     */

    public Matrix plus (Matrix B) {
        checkMatrixDimensions(B);
        Matrix X = new Matrix(m,n);
        double[][] C = X.getArray();
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                C[i][j] = A[i][j] + B.A[i][j];
            }
        }
        return X;
    }

    /** C = A - B
     @param B    another matrix
     @return     A - B
     */

    public Matrix minus (Matrix B) {
        checkMatrixDimensions(B);
        Matrix X = new Matrix(m,n);
        double[][] C = X.getArray();
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                C[i][j] = A[i][j] - B.A[i][j];
            }
        }
        return X;
    }

    /** Multiply a matrix by a scalar, C = s*A
     @param s    scalar
     @return     s*A
     */

    public Matrix times (double s) {
        Matrix X = new Matrix(m,n);
        double[][] C = X.getArray();
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                C[i][j] = s*A[i][j];
            }
        }
        return X;
    }

    /** Linear algebraic matrix multiplication, A * B
     @param B    another matrix
     @return     Matrix product, A * B
     @exception  IllegalArgumentException Matrix inner dimensions must agree.
     */

    public Matrix times (Matrix B) {
        if (B.m != n) {
            throw new IllegalArgumentException("Matrix inner dimensions must agree.");
        }
        Matrix X = new Matrix(m,B.n);
        double[][] C = X.getArray();
        double[] Bcolj = new double[n];
        for (int j = 0; j < B.n; j++) {
            for (int k = 0; k < n; k++) {
                Bcolj[k] = B.A[k][j];
            }
            for (int i = 0; i < m; i++) {
                double[] Arowi = A[i];
                double s = 0;
                for (int k = 0; k < n; k++) {
                    s += Arowi[k]*Bcolj[k];
                }
                C[i][j] = s;
            }
        }
        return X;
    }

    /** Cholesky Decomposition
     @return     CholeskyDecomposition
     @see CholeskyDecomposition
     */

    public CholeskyDecomposition chol () {
        return new CholeskyDecomposition(this);
    }

    /** Generate matrix with random elements
     @param m    Number of rows.
     @param n    Number of colums.
     @return     An m-by-n matrix with uniformly distributed random elements.
     */

    public static Matrix random (int m, int n) {
        Matrix A = new Matrix(m,n);
        double[][] X = A.getArray();
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                X[i][j] = Math.random();
            }
        }
        return A;
    }

    /** Generate identity matrix
     @param m    Number of rows.
     @param n    Number of colums.
     @return     An m-by-n matrix with ones on the diagonal and zeros elsewhere.
     */

    public static Matrix identity (int m, int n) {
        Matrix A = new Matrix(m,n);
        double[][] X = A.getArray();
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                X[i][j] = (i == j ? 1.0 : 0.0);
            }
        }
        return A;
    }

/* ------------------------
   Private Methods
 * ------------------------ */

    /** Check if size(A) == size(B) **/

    private void checkMatrixDimensions (Matrix B) {
        if (B.m != m || B.n != n) {
            throw new IllegalArgumentException("Matrix dimensions must agree.");
        }
    }

    private static final long serialVersionUID = 1;
}

