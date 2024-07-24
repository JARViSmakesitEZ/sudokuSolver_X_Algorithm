package com.sudoku.sudokusolver.Service;

public class Backtracking {

    private static final int SIZE = 9;

    // Solve Sudoku using backtracking
    public boolean solveSudoku(int[][] grid) {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                // Find the next empty cell
                if (grid[row][col] == 0) {
                    // Try each number from 1 to 9
                    for (int num = 1; num <= SIZE; num++) {
                        if (isValid(grid, row, col, num)) {
                            // If it's valid, place the number
                            grid[row][col] = num;

                            // Recursively attempt to solve the rest of the grid
                            if (solveSudoku(grid)) {
                                return true; // Solution found
                            }

                            // Backtrack: undo the current cell for next attempts
                            grid[row][col] = 0;
                        }
                    }
                    return false; // No valid number found
                }
            }
        }
        return true; // Sudoku solved
    }

    // Check if placing num at grid[row][col] is valid
    private boolean isValid(int[][] grid, int row, int col, int num) {
        // Check row
        for (int c = 0; c < SIZE; c++) {
            if (grid[row][c] == num) {
                return false;
            }
        }

        // Check column
        for (int r = 0; r < SIZE; r++) {
            if (grid[r][col] == num) {
                return false;
            }
        }

        // Check 3x3 box
        int boxStartRow = row - row % 3;
        int boxStartCol = col - col % 3;
        for (int r = boxStartRow; r < boxStartRow + 3; r++) {
            for (int c = boxStartCol; c < boxStartCol + 3; c++) {
                if (grid[r][c] == num) {
                    return false;
                }
            }
        }

        return true;
    }
}
