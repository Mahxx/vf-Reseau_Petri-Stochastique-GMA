import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class Reseau_Petri {

    int[] M0;
    String[] place;
    String[] transition;
    int[][] pre;
    int[][] post;

    public Reseau_Petri(int[] M0, String[] place, String[] transition, int[][] pre, int[][] post) {
        this.place = place;
        this.transition = transition;
        this.pre = pre;
        this.post = post;
        this.M0 = M0;
    }

    boolean franchisable(int[] M, int j) {
        for (int i = 0; i < place.length; i++) {
            if (M[i] < pre[i][j]) {
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

    // On sépare la construction du graphe et la génération de l'image
    public ArrayList<int[]> construireGraphe(ArrayList<String> labels) {

        ArrayList<int[]> A = new ArrayList<>();
        ArrayList<int[]> A1 = new ArrayList<>();
        ArrayList<int[]> arcs = new ArrayList<>();

        A.add(M0);
        A1.add(M0);

        while (!A1.isEmpty()) {

            int[] M = A1.remove(0);
            ArrayList<Integer> Z = creetransition(M);

            while (!Z.isEmpty()) {
                if(A.size()>30) {
                    System.out.println("Trop de marquages, arrêt de la construction du graphe.");
                    break;
                }
                int t = Z.remove(0);
                int[] M_nv = new int[place.length];

                for (int i = 0; i < place.length; i++) {
                    M_nv[i] = M[i] - pre[i][t] + post[i][t];
                }

                if (!marqageexiste(A, M_nv)) {
                    A.add(M_nv);
                    A1.add(M_nv);
                }

                System.out.println("Arc : " + Arrays.toString(M) +
                        " --" + transition[t] +
                        "--> " + Arrays.toString(M_nv));

                arcs.add(M);
                arcs.add(M_nv);
                labels.add(transition[t]);
            }
        }

        return arcs;
    }

    // Méthode pour générer le PNG
    public void genererImageGraphe(ArrayList<int[]> arcs, ArrayList<String> labels) {
        try {
            FileWriter writer = new FileWriter("graphe.dot");
            writer.write("digraph G {\n");

            for (int i = 0; i < arcs.size(); i += 2) {
                String from = Arrays.toString(arcs.get(i));
                String to = Arrays.toString(arcs.get(i + 1));
                String t = labels.get(i / 2);
                writer.write("  \"" + from + "\" -> \"" + to + "\" [label=\"" + t + "\"];\n");
            }

            writer.write("}\n");
            writer.close();

            Runtime.getRuntime().exec("dot -Tpng graphe.dot -o graphe.png");
            System.out.println("Graphe généré en graphe.png");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        String[] place = {"P1", "P2", "P3", "P4"};
        String[] transition = {"T1", "T2", "T3"};
        int[] M0 = {1, 1, 0, 0};

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

        Reseau_Petri reseau = new Reseau_Petri(M0, place, transition, pre, post);

        
        ArrayList<String> labels = new ArrayList<>();
        
        ArrayList<int[]> arcs = reseau.construireGraphe(labels);

        
        reseau.genererImageGraphe(arcs, labels);
    }
}