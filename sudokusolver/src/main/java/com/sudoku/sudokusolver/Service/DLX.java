package com.sudoku.sudokusolver.Service;

public class DLX {

    // Constants for Sudoku size
    private static final int SIZE = 9;
    private static final int SIZE_SQUARED = SIZE * SIZE;
    private static final int SIZE_SQRT = (int) Math.sqrt(SIZE);

    // DLX Data structures
    private static final int ROW_NB = SIZE * SIZE * SIZE;
    private static final int COL_NB = 4 * SIZE * SIZE;
    private Node head;
    private Node[] solution;
    private Node[] origValues;
    private boolean[][] matrix;
    private int solutionCount;

    // Constructor initializes necessary structures
    public DLX() {
        head = new Node();
        head.left = head;
        head.right = head;
        head.down = head;
        head.up = head;
        head.size = -1;
        head.head = head;
        solution = new Node[SIZE_SQUARED];
        origValues = new Node[SIZE_SQUARED];
        matrix = new boolean[ROW_NB][COL_NB];
        solutionCount = 0;
    }

    // Node structure for DLX algorithm
    static class Node {
        Node left, right, up, down, head;
        int size;
        int[] rowID = new int[3];
    }

    // Cover column in DLX matrix
    private void coverColumn(Node col) {
        col.left.right = col.right;
        col.right.left = col.left;
        for (Node node = col.down; node != col; node = node.down) {
            for (Node temp = node.right; temp != node; temp = temp.right) {
                temp.down.up = temp.up;
                temp.up.down = temp.down;
                temp.head.size--;
            }
        }
    }

    // Uncover column in DLX matrix
    private void uncoverColumn(Node col) {
        for (Node node = col.up; node != col; node = node.up) {
            for (Node temp = node.left; temp != node; temp = temp.left) {
                temp.head.size++;
                temp.down.up = temp;
                temp.up.down = temp;
            }
        }
        col.left.right = col;
        col.right.left = col;
    }

    // DLX algorithm search function
    private boolean search(int k,int[][] sudoku) {
        if (head.right == head) {
            solutionCount++;
            if (solutionCount == 1) {
                int[][] grid = new int[SIZE][SIZE];
                mapSolutionToGrid(grid);
                printGrid(grid,sudoku);
            }
            return true; // Return true to stop at the first solution found
        }

        // Choose column object deterministically: choose the column with the smallest size
        Node col = head.right;
        for (Node temp = col.right; temp != head; temp = temp.right) {
            if (temp.size < col.size) {
                col = temp;
            }
        }

        coverColumn(col);

        for (Node temp = col.down; temp != col; temp = temp.down) {
            solution[k] = temp;
            for (Node node = temp.right; node != temp; node = node.right) {
                coverColumn(node.head);
            }

            if (search(k + 1,sudoku)) {
                return true; // Return true if a solution is found
            }

            temp = solution[k];
            solution[k] = null;
            col = temp.head;
            for (Node node = temp.left; node != temp; node = node.left) {
                uncoverColumn(node.head);
            }
        }

        uncoverColumn(col);
        return false; // Return false if no solution is found
    }

    // Build initial DLX sparse matrix
    private void buildSparseMatrix(int[][] sudoku) {
        // Constraint 1: There can only be one value in any given cell
        int j = 0, counter = 0;
        for (int i = 0; i < ROW_NB; i++) {
            matrix[i][j] = true;
            counter++;
            if (counter >= SIZE) {
                j++;
                counter = 0;
            }
        }

        // Constraint 2: There can only be one instance of a number in any given row
        int x = 0;
        counter = 1;
        for (j = SIZE_SQUARED; j < 2 * SIZE_SQUARED; j++) {
            for (int i = x; i < counter * SIZE_SQUARED; i += SIZE) {
                matrix[i][j] = true;
            }

            if ((j + 1) % SIZE == 0) {
                x = counter * SIZE_SQUARED;
                counter++;
            } else {
                x++;
            }
        }

        // Constraint 3: There can only be one instance of a number in any given column
        j = 2 * SIZE_SQUARED;
        for (int i = 0; i < ROW_NB; i++) {
            matrix[i][j] = true;
            j++;
            if (j >= 3 * SIZE_SQUARED) {
                j = 2 * SIZE_SQUARED;
            }
        }

        // Constraint 4: There can only be one instance of a number in any given region
        x = 0;
        for (j = 3 * SIZE_SQUARED; j < COL_NB; j++) {
            for (int l = 0; l < SIZE_SQRT; l++) {
                for (int k = 0; k < SIZE_SQRT; k++) {
                    matrix[x + l * SIZE + k * SIZE_SQUARED][j] = true;
                }
            }

            int temp = j + 1 - 3 * SIZE_SQUARED;
            if (temp % (SIZE_SQRT * SIZE) == 0) {
                x += (SIZE_SQRT - 1) * SIZE_SQUARED + (SIZE_SQRT - 1) * SIZE + 1;
            } else if (temp % SIZE == 0) {
                x += SIZE * (SIZE_SQRT - 1) + 1;
            } else {
                x++;
            }
        }
    }

    // Build DLX toroidal doubly linked list from sparse matrix
    private void buildLinkedList(int[][] sudoku) {
        head = new Node();
        head.left = head;
        head.right = head;
        head.down = head;
        head.up = head;
        head.size = -1;
        head.head = head;

        Node temp = head;

        // Create all column nodes
        for (int i = 0; i < COL_NB; i++) {
            Node newNode = new Node();
            newNode.size = 0;
            newNode.up = newNode;
            newNode.down = newNode;
            newNode.head = newNode;
            newNode.right = head;
            newNode.left = temp;
            temp.right = newNode;
            temp = newNode;
        }

        int[] ID = { 0, 1, 1 };
        // Add a node for each true value in the sparse matrix and update column nodes accordingly
        for (int i = 0; i < ROW_NB; i++) {
            Node top = head.right;
            Node prev = null;

            if (i != 0 && i % SIZE_SQUARED == 0) {
                ID[0] -= SIZE - 1;
                ID[1]++;
                ID[2] -= SIZE - 1;
            } else if (i != 0 && i % SIZE == 0) {
                ID[0] -= SIZE - 1;
                ID[2]++;
            } else {
                ID[0]++;
            }

            for (int j = 0; j < COL_NB; j++, top = top.right) {
                if (matrix[i][j]) {
                    Node newNode = new Node();
                    newNode.rowID[0] = ID[0];
                    newNode.rowID[1] = ID[1];
                    newNode.rowID[2] = ID[2];
                    if (prev == null) {
                        prev = newNode;
                        prev.right = newNode;
                    }
                    newNode.left = prev;
                    newNode.right = prev.right;
                    newNode.right.left = newNode;
                    prev.right = newNode;
                    newNode.head = top;
                    newNode.down = top;
                    newNode.up = top.up;
                    top.up.down = newNode;
                    top.size++;
                    top.up = newNode;
                    if (top.down == top) {
                        top.down = newNode;
                    }
                    prev = newNode;
                }
            }
        }
    }

    // Cover values that are already present in the Sudoku grid
    private void transformListToCurrentGrid(int[][] sudoku) {
        int index = 0;
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (sudoku[i][j] > 0) {
                    Node col = null;
                    Node temp = null;
                    for (col = head.right; col != head; col = col.right) {
                        for (temp = col.down; temp != col; temp = temp.down) {
                            if (temp.rowID[0] == sudoku[i][j] && (temp.rowID[1] - 1) == i && (temp.rowID[2] - 1) == j) {
                                break;
                            }
                        }
                        if (temp != col) {
                            break;
                        }
                    }
                    coverColumn(col);
                    origValues[index] = temp;
                    index++;
                    for (Node node = temp.right; node != temp; node = node.right) {
                        coverColumn(node.head);
                    }
                }
            }
        }
    }

    // Map solution back to Sudoku grid
    private void mapSolutionToGrid(int[][] sudoku) {
        for (Node node : solution) {
            if (node != null) {
                sudoku[node.rowID[1] - 1][node.rowID[2] - 1] = node.rowID[0];
            }
        }
        for (Node node : origValues) {
            if (node != null) {
                sudoku[node.rowID[1] - 1][node.rowID[2] - 1] = node.rowID[0];
            }
        }
    }

    // Print Sudoku grid
    private void printGrid(int[][] sudoku,int[][] original) {
        for(int i = 0 ;i < 9;i++){
            for(int j = 0;j < 9; j++){
                original[i][j] = sudoku[i][j];
            }
        }
        System.out.println("Sudoku solved:");
        StringBuilder extBorder = new StringBuilder("+");
        StringBuilder intBorder = new StringBuilder("|");
        int counter = 1;
        int additional = SIZE > 9 ? SIZE : 0;
        for (int i = 0; i < ((SIZE + SIZE_SQRT - 1) * 2 + additional + 1); i++) {
            extBorder.append('-');

            if (i > 0 && i % ((SIZE_SQRT*2 + SIZE_SQRT*((SIZE > 9 ? 1 : 0)) + 1) * counter + counter - 1) == 0) {
                intBorder.append('+');
                counter++;
            } else {
                intBorder.append('-');
            }
        }
        extBorder.append('+');
        intBorder.append("|");

        System.out.println(extBorder);
        for (int i = 0; i < SIZE; i++) {
            System.out.print("| ");
            for (int j = 0; j < SIZE; j++) {
                if (sudoku[i][j] == 0) {
                    System.out.print(". ");
                } else {
                    System.out.print(sudoku[i][j] + " ");
                }
                if (additional > 0 && sudoku[i][j] < 10) {
                    System.out.print(" ");
                }
                if ((j + 1) % SIZE_SQRT == 0) {
                    System.out.print("| ");
                }
            }
            System.out.println();
            if ((i + 1) % SIZE_SQRT == 0 && (i + 1) < SIZE) {
                System.out.println(intBorder);
            }
        }
        System.out.println(extBorder + "\n");
    }

    // Solve Sudoku using DLX algorithm
    public boolean solveSudoku(int[][] sudoku) {
        buildSparseMatrix(sudoku);
        buildLinkedList(sudoku);
        transformListToCurrentGrid(sudoku);
        solutionCount = 0;
        if (!search(0,sudoku)) {
            return false;
        }
        return true;
    }
}
