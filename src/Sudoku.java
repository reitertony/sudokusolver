import javafx.util.Pair;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;

public class Sudoku {

    private static final String filename = "sNewspaper.txt";
    private static int[][] solved;
    private static Map<Pair<Integer, Integer>, Set<Integer>> choicesMap;
    private static Set<Integer> allNums;


    // NOTE: i == y, j == x (reversed)

    public static void main(String[] args) throws FileNotFoundException {
        choicesMap = new HashMap<>();
        allNums = new HashSet<>();
        for(int i = 1; i <= 9; i++) {
            allNums.add(i);
        }
        solved = new int[9][9];
        Scanner in = new Scanner(new FileReader(filename));
        for(int i = 0; i < solved.length; i++) {
            for(int j = 0; j < solved[i].length; j++) {
                solved[i][j] = in.nextInt();
            }
        }
        printSudoku(solved);
        iterateChoices(solved, true);
        printSudoku(solved);
    }

    private static int[][] deepCopy(int[][] original) {
        int[][] copy = new int[9][9];
        for(int i = 0; i < 9; i++) {
            for(int j = 0; j < 9; j++) {
                copy[i][j] = original[i][j];
            }
        }
        return copy;
    }

    private static boolean iterateChoices(int[][] sudoku, boolean first) {
        boolean lastLoopModified = true;
        while(!solved(sudoku)) {
            boolean modified = false;
            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 9; j++) {
                    if (sudoku[i][j] < 1) {
                        Iterator<Integer> choicesIt = choices(i, j, sudoku).iterator();
                        if (choicesIt.hasNext()) {
                            int val = choicesIt.next();
                            // JUST ONE POSSIBILITY, GUARANTEED TO BE RIGHT
                            if (!choicesIt.hasNext()) {
                                sudoku[i][j] = val;
//                                if(first) printSudoku(sudoku);
                                modified = true;
//                                iterateChoices(sudoku, false);
                                continue;
                            }
                            else {
                                int[][] copy = deepCopy(sudoku);
                                Set<Integer> unique = uniqueChoices(i, j, sudoku);
                                Iterator<Integer> uniqueIt = unique.iterator();
                                // AT LEAST 1 CHOICE SPECIFIC TO THIS SQUARE (should never be >1 ?)
                                // ^if wrong, need to loop over uniqueIt
                                if (uniqueIt.hasNext()) {
                                    sudoku[i][j] = uniqueIt.next();
//                                    if(first) printSudoku(sudoku);
                                    modified = true;
//                                    iterateChoices(sudoku, false);
                                    continue;
                                }
                                else if (!lastLoopModified) {
//                                    // START GUESSING
                                    while (choicesIt.hasNext()) {
                                        copy[i][j] = choicesIt.next();
                                        if (first) {
                                            System.out.println("Guessing \""+copy[i][j]+"\" for " + i + ", " + j);
                                        }
                                        if (iterateChoices(copy, false)) {
                                            replaceContents(sudoku, copy);
                                            return true;
                                        }
                                    }
                                }
                            } // end multiple choices if
                        } else if(!first) {
                            return false;
                        }
                    }
                }
            }
            lastLoopModified = modified;
        }
        return true;
    }

    // replaces the contents of "original" with the contents of "updated"
    private static void replaceContents(int[][] original, int[][] updated) {
        for(int i = 0; i < 9; i++) {
            for(int j = 0; j < 9; j++) {
                original[i][j] = updated[i][j];
            }
        }
    }

    private static boolean solved (int[][] sudoku) {
        for(int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (sudoku[i][j] == 0) {
                    return false;
                }
            }
        }
        return true;
    }


    // does some fancy logic beyond just filtering all R/C/S
    private static Set<Integer> uniqueChoices(int i, int j, int[][] sudoku) {
        Set<Integer> rowFilteredChoices = choices(i, j, sudoku);
        Set<Integer> columnFilteredChoices = choices(i, j, sudoku);
        Set<Integer> squareFilteredChoices = choices(i, j, sudoku);
        int iStart = i/3*3;
        int jStart = j/3*3;
        for (int i1 = 0; i1 < 9; i1++) {
            if (i1 != 1 && sudoku[i1][j] == 0) {
                columnFilteredChoices.removeAll(choices(i1, j, sudoku));
            }
        }
        if (columnFilteredChoices.size() == 1) {
            return columnFilteredChoices;
        }
        for (int j1 = 0; j1 < 9; j1++) {
            if (j1 != j && sudoku[i][j1] == 0) {
                rowFilteredChoices.removeAll(choices(i, j1, sudoku));
            }
        }
        if (rowFilteredChoices.size() == 1) {
            return rowFilteredChoices;
        }
        for(int i1 = iStart; i1 < iStart + 3; i1++) {
            for(int j1 = jStart; j1 < jStart + 3; j1++) {
                if (!(i1 == i && j1 == j) && sudoku[i1][j1] == 0) {
                    squareFilteredChoices.removeAll(choices(i1, j1, sudoku));
                }
            }
        }
        if (squareFilteredChoices.size() == 1) {
            return squareFilteredChoices;
        }
        // TODO: check all overlaps?
        rowFilteredChoices.removeAll(columnFilteredChoices);
        rowFilteredChoices.removeAll(squareFilteredChoices);
        return rowFilteredChoices;
    }

    private static Set<Integer> choices(int i, int j, int[][] sudoku) {
        // TODO: cache choices.
        // HAVE TO UPDATE (remove/put back) choices for R/C/S during iterate
//        Pair<Integer, Integer> pair = new Pair<>(i, j);
//        if (choicesMap.containsKey(pair)) {
//            return choicesMap.get(pair);
//        }
        Set<Integer> choices = choicesForColumn(i, j, sudoku);
        choices.retainAll(choicesForRow(i, j, sudoku));
        choices.retainAll(choicesForSquare(i, j, sudoku));
//        choicesMap.put(pair, choices);
        return choices;
    }


    private static Set<Integer> choicesForRow(int i, int j, int[][] sudoku) {
        Set<Integer> choices = new HashSet<>(allNums);
        for(int j1 = 0; j1 < 9; j1++) {
            if (sudoku[i][j1] > 0) {
                choices.remove(sudoku[i][j1]);
            }
        }
        return choices;
    }

    private static Set<Integer> choicesForColumn(int i, int j, int[][] sudoku) {
        Set<Integer> choices = new HashSet<>(allNums);
        for(int i1 = 0; i1 < 9; i1++) {
            if (sudoku[i1][j] > 0) {
                choices.remove(sudoku[i1][j]);
            }
        }
        return choices;
    }

    private static Set<Integer> choicesForSquare(int i, int j, int[][] sudoku) {
        Set<Integer> choices = new HashSet<>(allNums);
        int iStart = i/3*3;
        int jStart = j/3*3;
        for(int i1 = iStart; i1 < iStart + 3; i1++) {
            for(int j1 = jStart; j1 < jStart + 3; j1++) {
                if (sudoku[i1][j1] > 0) {
                    choices.remove(sudoku[i1][j1]);
                }
            }
        }
        return choices;
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
