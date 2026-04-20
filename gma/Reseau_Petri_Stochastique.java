import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
//import org.apache.commons.math3.linear.*;

public class Reseau_Petri_Stochastique{

    int[] M0;
    String[] place;
    String[] transition;
    int[][] pre;
    int[][] post;
    double[] lambda;
    boolean borne ;   
    double[][] Q;

    public Reseau_Petri_Stochastique(int[] M0, String[] place, String[] transition, int[][] pre, int[][] post, double[] lambda) {
        this.M0 = M0;
        this.place = place;
        this.transition = transition;
        this.pre = pre;
        this.post = post;
        this.lambda = lambda;
    }
    boolean domine(int[] M1, int[] M2) {
        boolean strict = false;

        for (int i = 0; i < M1.length; i++) {
            if (M2[i] < M1[i]) {
                return false;
            }
            if (M2[i] > M1[i]) {
                strict = true;
            }
        }
        return strict;
    }
    boolean atteindreM0(int[] depart, ArrayList<int[]> arcs) {

        ArrayList<int[]> file = new ArrayList<>();
        ArrayList<int[]> visites = new ArrayList<>();

        file.add(depart);

        while (!file.isEmpty()) {

            int[] courant = file.remove(0);

            if (Arrays.equals(courant, M0)) {
                return true;
            }

            visites.add(courant);

            for (int i = 0; i < arcs.size(); i += 2) {
                int[] src = arcs.get(i);
                int[] dst = arcs.get(i + 1);

                if (Arrays.equals(src, courant) && !marqageexiste(visites, dst)) {
                    file.add(dst );
                }    
            }
        }
        return false;
    }
    int indexmarquage(ArrayList<int[]> A, int[] M) {
        for (int i = 0; i < A.size(); i++) {
            if (Arrays.equals(A.get(i), M)) {
                return i;
            }
        }
        return -1;
    }
    public double[][] construireMatriceQ(ArrayList<int[]> A, ArrayList<int[]> arcs, ArrayList<Double> arcLambdas) {
        int n = A.size();
        double[][] Q = new double[n][n];

        
        for (int k = 0; k < arcs.size(); k += 2) {
            int[] src = arcs.get(k);
            int[] dst = arcs.get(k + 1);
            double lambda = arcLambdas.get(k / 2);

            int i = indexmarquage(A, src);
            int j = indexmarquage(A, dst);

            if (i != j) {
                Q[i][j] = lambda; 
            }
        }

        
        for (int i = 0; i < n; i++) {
            double  somme = 0;
            for (int j = 0; j < n; j++) {
                if (i != j) {
                    somme += Q[i][j];
                }
            }
            Q[i][i] = -somme;
        }

        return Q;
    }



    public boolean estReinitialisable(ArrayList<int[]> A, ArrayList<int[]> arcs) {

        for (int[] M : A) {
            if (!atteindreM0(M, arcs)) {
                return false;
            }
        }
        return true;
    }

    boolean franchisable(int[] M, int j) {
        for (int i = 0; i < place.length; i++) {
            if (M[i] < pre[i][j]){
                 return false;
            }
        }
        return true;
    }
    public boolean marqageexiste(ArrayList<int[]> A, int[] M_nv) {
        for (int[] M : A) {
            if (Arrays.equals(M, M_nv)) {
                return true;
            }
        }
        return false;
    }
    public ArrayList<Integer> creetransition(int[] M) {
        ArrayList<Integer> Z = new ArrayList<>();

        for (int j = 0; j < transition.length; j++) {
            if (franchisable(M, j)) {
                Z.add(j);
            }
        }
        return Z;
    }
    
    
    public ArrayList<int[]> construireGraphe(boolean borne, ArrayList<int[]> A, ArrayList<String> labels, ArrayList<Double> arcLambdas) {

        
        ArrayList<int[]> A1 = new ArrayList<>();
        ArrayList<int[]> arcs = new ArrayList<>();

        A.add(M0);
        A1.add(M0);

        while (!A1.isEmpty()) {

            int[] M = A1.remove(0);
            ArrayList<Integer> Z = creetransition(M);
            boolean bol=true;
            while (!Z.isEmpty()) {
                
                int t = Z.remove(0);
                int[] M_nv = new int[place.length];

                for (int i = 0; i < place.length; i++) {
                    M_nv[i] = M[i] - pre[i][t] + post[i][t];
                }
                for (int[] ancien : A) {
                    if (domine(ancien, M_nv)) {
                        System.out.println("reseau non borne");
                        this.borne=false;
                        
                        bol=false;
                        break;
                    }
                    if(bol==false){break;}
                }
                if(bol==true){
                if (!marqageexiste(A, M_nv)) {
                    A.add(M_nv);
                    A1.add(M_nv);
                }}else{
                    if (!marqageexiste(A, M_nv)) {
                            A.add(M_nv);
                        }  
                }
                
                System.out.println("Arc : " + Arrays.toString(M) +
                        " --" + transition[t] +
                        "--> " + Arrays.toString(M_nv) +
                        " | λ=" + lambda[t]);

                arcs.add(M);
                arcs.add(M_nv);
                labels.add(transition[t]);
                arcLambdas.add(lambda[t]);
            }
        }
        this.borne=true;
        return arcs;
    }

    
    public void genererImageGraphe(ArrayList<int[]> arcs, ArrayList<String> labels, ArrayList<Double> arcLambdas) {
        try {
            FileWriter writer = new FileWriter("graphe.dot");
            writer.write("digraph G {\n");

            for (int i = 0; i < arcs.size(); i += 2) {
                String from = Arrays.toString(arcs.get(i));
                String to = Arrays.toString(arcs.get(i + 1));
                String t = labels.get(i / 2);
                double lam = arcLambdas.get(i / 2);
                writer.write("  \"" + from + "\" -> \"" + to + "\" [label=\"" + t + " (λ=" + lam + ")\"];\n");
            }

            writer.write("}\n");
            writer.close();

            Runtime.getRuntime().exec("dot -Tpng graphe.dot -o graphes.png");
            System.out.println("graphe genere en graphes.png");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public double[] frequence(ArrayList<int[]> A, double[] pi) {

        double[] F = new double[transition.length]; 
        for (int j = 0; j < transition.length; j++) {       
            for (int k = 0; k < A.size(); k++) {             
                if (franchisable(A.get(k), j)) {             
                    F[j] += lambda[j] * pi[k];           
                }
            }
        }
        return F;
    }
    public double[] nbmoyen(ArrayList<int[]> A, double[] pi) {

        double[] M = new double[place.length]; 

        for (int p = 0; p < place.length; p++) {        
            for (int k = 0; k < A.size(); k++) {         
                M[p] += A.get(k)[p] * pi[k];         
            }
        }
        return M;
    }

    public double[] tempssejour( double[] M, double[] F) {

        double[] T = new double[place.length];
        
        for (int p = 0; p < place.length; p++) {        
            double c = 0;
            for (int j = 0; j < transition.length; j++) {
                c += post[p][j] * F[j];           
            }        
            T[p] = M[p] / c;                                                
        }
        return T;
    }
    
    public double[] TMS(ArrayList<int[]> A) {

        double[] TMS = new double[A.size()];

        for (int j = 0; j < A.size(); j++) {        
            double c = 0;
            for (int k = 0; k < transition.length; k++) {         
                if (franchisable(A.get(j), k)) {             
                    c += lambda[k];           
                }
            }        
            TMS[j] = 1 / c;                                                
        }
        return TMS;
    }





    public double[] calculPiSimple(double[][] Q) {
    int n = Q.length;
    double[][] A = new double[n][n];
    double[] b = new double[n];

    // π.Q = 0  →  on prend les n-1 premières équations + somme π = 1
    for (int i = 0; i < n - 1; i++) {
        for (int j = 0; j < n; j++) {
            A[i][j] = Q[j][i];   // transposition
        }
        b[i] = 0;
    }

    // Dernière ligne : somme π_i = 1
    for (int j = 0; j < n; j++) {
        A[n-1][j] = 1.0;
    }
    b[n-1] = 1.0;

    // Gauss-Jordan avec meilleure stabilité
    for (int i = 0; i < n; i++) {
        // Recherche du meilleur pivot (partial pivoting simple)
        int max = i;
        for (int k = i + 1; k < n; k++) {
            if (Math.abs(A[k][i]) > Math.abs(A[max][i])) {
                max = k;
            }
        }
        // Échange de lignes
        double[] tempRow = A[i];
        A[i] = A[max];
        A[max] = tempRow;
        double tempB = b[i];
        b[i] = b[max];
        b[max] = tempB;

        if (Math.abs(A[i][i]) < 1e-12) {
            System.out.println("Matrice singulière ou mal conditionnée");
            return null;
        }

        double pivot = A[i][i];
        for (int j = 0; j < n; j++) A[i][j] /= pivot;
        b[i] /= pivot;

        for (int k = 0; k < n; k++) {
            if (k != i) {
                double factor = A[k][i];
                for (int j = 0; j < n; j++) {
                    A[k][j] -= factor * A[i][j];
                }
                b[k] -= factor * b[i];
            }
        }
    }

    return b;
}












    /*public double[] calculPiExact(double[][] Q) {

        int n = Q.length;

        
        RealMatrix QT = new Array2DRowRealMatrix(Q).transpose();

        
        double[][] A = new double[n + 1][n];
        double[] b = new double[n + 1];

        // π.Q = 0
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                A[i][j] = QT.getEntry(i, j);
            }
            b[i] = 0;
        }

        // Σπ = 1
        for (int j = 0; j < n; j++) {
            A[n][j] = 1;
        }
        b[n] = 1;

    
        RealMatrix matrix = new Array2DRowRealMatrix(A);
        RealVector vector = new ArrayRealVector(b);

        DecompositionSolver solver =
                new SingularValueDecomposition(matrix).getSolver();

        RealVector solution = solver.solve(vector);

        return solution.toArray();
    }*/



    public static void main(String[] args) {

        String[] place = {"P1", "P2", "P3","P4"};
        String[] transition = {"T1", "T2", "T3"};
        int[] M0 = {1,1, 0, 0};
        
        int[][] pre = {
                {1, 0, 0}, 
                {0, 1, 0}, 
                {0, 0, 1}, 
                {0, 0, 1}  
        };

        int[][] post = {
                {0, 0, 1}, 
                {0, 0, 1}, 
                {1, 0, 0}, 
                {0, 1, 0} 
        };
        /*String[] place = {"P1", "P2", "P3"};
        String[] transition = {"T1", "T2", "T3"};
        int[] M0 = {1,0,  0};
        
        int[][] pre = {
                {1, 0, 0}, 
                {0, 1, 0}, 
                {0, 0, 1}  
        };

        int[][] post = {
                {0, 1, 1}, 
                {1, 0, 0}, 
                {1, 0, 0} 
        };*/
        double[] lambda = {1.0, 2.0, 2.0};
        Reseau_Petri_Stochastique reseau = new Reseau_Petri_Stochastique(M0, place, transition, pre, post,lambda);

        ArrayList<int[]> A = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        ArrayList<Double> arcLambdas = new ArrayList<>();
        
        ArrayList<int[]> arcs = reseau.construireGraphe(reseau.borne, A, labels, arcLambdas);

        if (!reseau.borne) {
            System.out.println("le reseau n'est pas borne fin du programme");
            reseau.genererImageGraphe(arcs, labels, arcLambdas);
            return;
        }
        System.out.println("le reseau  est borne");
        reseau.genererImageGraphe(arcs, labels, arcLambdas);
        boolean result = reseau.estReinitialisable(A, arcs);

        if (result) {
            System.out.println("le reseau est renutialisable");
        } else {
            System.out.println("le reseau n'est pas renutialisable ");
        }
        int n = A.size();
        reseau.Q = new double[n][n];
        reseau.Q = reseau.construireMatriceQ( A,  arcs, arcLambdas);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                System.out.printf("%.2f\t", reseau.Q[i][j]);
            }
            System.out.println();
        }
        double[] pi =new double[reseau.Q.length];
        pi = reseau.calculPiSimple(reseau.Q);   
         System.out.println("Distribution stationnaire π : " + Arrays.toString(pi));
        double[] F = reseau.frequence(A, pi);
        System.out.println("Frequences de transition : " + Arrays.toString(F));
        double[] M = reseau.nbmoyen(A, pi);
        System.out.println("Nombre moyen de jetons : " + Arrays.toString(M));
        double[] T = reseau.tempssejour(M, F);
        System.out.println("Temps de sejour moyen : " + Arrays.toString(T));
        double[] TMS = reseau.TMS(A);
        System.out.println("Temps moyen de sojour : " + Arrays.toString(TMS));
    }
}    