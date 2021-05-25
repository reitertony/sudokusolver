import java.io.FileReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Scanner;

public class GeneticSudoku {

    private static int[][] initial = new int[9][9];
    private static final String filename = "sNewspaper.txt";
    private static final int N = 500;
    private static final float mutationChance = 0.07f;
    private static ArrayList<int[][]> parents = new ArrayList<>(N);
//    private static ArrayList<int> fitnessForParent = new ArrayList<>(N);

    public static void main(String[] args) throws Exception{
        Scanner in = new Scanner(new FileReader(filename));
        for(int i = 0; i < 9; i++) {
            for(int j = 0; j < 9; j++) {
                initial[i][j] = in.nextInt();
            }
        }
        while(true) {
            int loops = 0;
            generateInitialParents();
            int prevFitness = Integer.MAX_VALUE;
            int generationsStagnent = 0;

            while (generationsStagnent < 100) {
                int totalFitness = 0;
                parents.sort(new Comparator<int[][]>() {
                    @Override
                    public int compare(int[][] o1, int[][] o2) {
                        // can use cache / pass by reference array here? (in fitness)
                        // BETTER: makes it easier to update/clear cache in our control, rather than in this method ran 1000000 times
                        // BETTER: (?) run through list ahead of time for the sole purpose of calculating fitnesses.
                        // then, cache those, and use those for the remainder of the loop
                        return fitness(o1) - fitness(o2);
                    }
                });
                int bestFitness = fitness(parents.get(0));
                if (bestFitness >= prevFitness) {
                    generationsStagnent++;
                } else {
                    prevFitness = bestFitness;
                    generationsStagnent = 0;
                }
                if (fitness(parents.get(0)) == 0) {
                    //DONE
                    printSudoku(parents.get(0));
                    System.out.println("DONE");
                    break;
                }
                for (int parentNum = 0; parentNum < N; parentNum++) {
                    totalFitness += fitness(parents.get(parentNum));
                }
                loops++;
                if (loops % 100 == 0) {
                    // TODO: check for perfect squares (or column or row)
                    // TODO: and keep intact when making kids
                    if (loops % 1000 == 0) {
                        printSudoku(parents.get(0));
                        System.out.println("Fitness: " + fitness(parents.get(0)));
                    }
                    System.out.println(loops);
                }
                int maxAccumulatedFitness = (int) (Math.random() * totalFitness);
                int lastParent = 0;
                int accumulatedFitness = 0;
                while (accumulatedFitness < maxAccumulatedFitness) {
                    accumulatedFitness += fitness(parents.get(lastParent++));
                }
                ArrayList<int[][]> children = new ArrayList<>(N);
                for (int newParentCount = 0; newParentCount < N; newParentCount++) {
                    children.add(generateChild(parents.get((int) (Math.random() * lastParent)), parents.get((int) (Math.random() * lastParent))));
                }
                parents = children;
            }
            System.out.println("RESTARTING");
        }
    }

    private static void generateInitialParents() {
        for(int n = 0; n < N; n++) {
            int[][] parent = deepCopyInitial();
            for(int i = 0; i < 9; i++) {
                for(int j = 0; j < 9; j++) {
                    parent[i][j] = rand9();
                }
            }
            parents.add(n, parent);
        }
    }

    //double crossover
    private static int[][] generateChild(int[][] parent1, int[][] parent2) {
        int crossoverPoint = (int)(Math.random() * 9 * 9);
        int crossoverPoint2 = (int)(Math.random() * 9 * 9 - crossoverPoint) + crossoverPoint;
        int[][] child = deepCopyInitial();
        for(int i = 0; i < 9; i++) {
            for(int j = 0; j < 9; j++) {
                if (child[i][j] == 0) {
                    if ((i + 1) * (j + 1) > crossoverPoint2) {
                        child[i][j] = parent1[i][j];
                    }
                    else if ((i + 1) * (j + 1) > crossoverPoint) {
                        child[i][j] = parent2[i][j];
                    } else {
                        child[i][j] = parent1[i][j];
                    }
                    if(Math.random() < mutationChance) {
                        child[i][j] = rand9();
                    }
                }
            }
        }
        return child;
    }

    private static int rand9() {
        return (int)(Math.random()*9 + 1);
    }

    private static int[][] deepCopyInitial() {
        int copy[][] = new int [9][9];
        for(int i = 0; i < 9; i++) {
            for(int j = 0; j < 9; j++) {
                copy[i][j] = initial[i][j];
            }
        }
        return copy;
    }

    // don't need this i think
    private static int fitness(int indexInParents) {
        return 0;
    }
    // TODO: use pass by reference arrays to cache (array reference as key?)
    private static int fitness(int[][] sudoku) {
        int fitness = 0;
        for(int i = 0; i < 9; i++) {
            for(int j = 0; j < 9; j++) {
//                if (initial[i][j] > 0) {
                    fitness += overlapCount(i, j, sudoku);
//                }
            }
        }
        return fitness;
    }

    // return overlap count
    // TODO: maybe return just 1 regardless of # of overlaps (overcounting?)
    // defintely double counts dups, not necessarily a bug (fitness is just x2 overlaps)
    private static int overlapCount(int i, int j, int[][] sudoku) {
        //check square
        int count = 0;
        int iStart = i/3*3;
        int jStart = j/3*3;
        //square
        for(int i1 = iStart; i1 < iStart + 3; i1++) {
            for(int j1 = jStart; j1 < jStart + 3; j1++) {
                if (sudoku[i1][j1] == sudoku[i][j] && i1 != i && j1 != j) {
                    count++;
                }
            }
        }
        //column
        for(int i1 = 0; i1 < 9; i1++) {
            if(sudoku[i1][j] == sudoku[i][j] && i1 != i) {
                count++;
            }
        }

        //row
        for(int j1 = 0; j1 < 9; j1++) {
            if(sudoku[i][j1] == sudoku[i][j] && j1 != j) {
                count++;
            }
        }
        return count;
    }



    private static void printSudoku(int[][] sudoku) {
        for(int k = 0; k <= (9+3)*2; k++) {
            System.out.print("-");
        }
        System.out.println();
        for(int i = 0; i < sudoku.length; i++) {
            for(int j = 0; j < sudoku[i].length; j++) {
                if (j % 3 == 0) {
                    System.out.print("| ");
                }
                System.out.print((sudoku[i][j] >= 1 ? sudoku[i][j] : "-") + " ");
            }
            System.out.println("|");
            if ((i + 1) % 3 == 0) {
                for(int k = 0; k <= (9+3)*2; k++) {
                    System.out.print("-");
                }
                System.out.println();
            }
        }
    }
}
