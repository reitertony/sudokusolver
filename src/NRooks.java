import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;

public class NRooks {

    private static final int N = 16;
    private static final int NUMBER_OF_PARENTS = 100;
    private static final float MUTATION_CHANCE = 0.05f;
    private static ArrayList<int[]> parents = new ArrayList<>(NUMBER_OF_PARENTS);


    public static void main(String[] args) {
        generateInitialParents();
        int bestFitness = Integer.MAX_VALUE;
        int bestFitnessLoc = -1;
        while(true) {
            int totalFitness = 0;
            parents.sort(new Comparator<int[]>() {
                @Override
                public int compare(int[] o1, int[] o2) {
                    return fitness(o1) - fitness(o2);
                }
            });
            if (fitness(0) == 0) {
                //DONE
                printBoard(0);
                System.out.println("DONE");
                break;
            }
            for (int parentNum = 0; parentNum < NUMBER_OF_PARENTS; parentNum++) {
    //            printBoard(parentNum);
    //            System.out.println(fitness(parentNum));
                totalFitness += fitness(parentNum);
            }
            int maxAccumulatedFitness = (int) (Math.random() * totalFitness);
            int lastParent = 0;
            int accumulatedFitness = 0;
            while (accumulatedFitness < maxAccumulatedFitness) {
                accumulatedFitness += fitness(lastParent++);
            }
            ArrayList<int[]> children = new ArrayList<>(NUMBER_OF_PARENTS);
            for (int newParentCount = 0; newParentCount < NUMBER_OF_PARENTS; newParentCount++) {
                children.add(newParentCount, generateChild((int) (Math.random() * lastParent), (int) (Math.random() * lastParent)));
            }
            parents = children;
        }
    }

    private static void generateInitialParents() {
        for (int parentNum = 0; parentNum < NUMBER_OF_PARENTS; parentNum++) {
            int[] temp = new int[N];
            for (int i = 0; i < N; i++) {
                temp[i] = (int) (Math.random() * N);
            }
            parents.add(parentNum, temp);
        }
    }

    private static int[] generateChild(int parentOneNum, int parentTwoNum) {
        // SINGLE POINT CROSSOVER AT RANDOM POINT
        int crossoverPoint = (int) (Math.random() * N);
        int[] child = new int[N];
        for (int n = 0; n < crossoverPoint; n++) {
            child[n] = parents.get(parentOneNum)[n];
            if (Math.random() < MUTATION_CHANCE) {
                child[n] = (int) (Math.random() * N);
            }
        }
        for (int n = crossoverPoint; n < N; n++) {
            child[n] = parents.get(parentTwoNum)[n];
            if (Math.random() < MUTATION_CHANCE) {
                child[n] = (int) (Math.random() * N);
            }
        }
        return child;
    }


    // 0 is most fit, N is least fit
    // TODO: IT WOULD BE SMART TO CACHE THESE BASED ON GENERATION + NUM/BOARD
    // TODO: but its pretty cheap in our case so who cares for N-Rooks...
    private static int fitness(int parentNum) {
        int[] board = parents.get(parentNum);
        return fitness(board);
    }

    private static int fitness(int[] board) {
        HashSet<Integer> uniqueRooks = new HashSet<Integer>(8);
        for (int n = 0; n < N; n++) {
            uniqueRooks.add(board[n]);
        }
        return N - uniqueRooks.size();
    }

    private static void printBoard(int parentNum) {
        int[] board = parents.get(parentNum);
        for (int n = 0; n < N; n++) {
            System.out.print(board[n] + ", ");
        }
        System.out.println();
    }
}
