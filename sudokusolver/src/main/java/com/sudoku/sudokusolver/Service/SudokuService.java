package com.sudoku.sudokusolver.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

@Service
public class SudokuService {

    private static final int N = 9; // Size of Sudoku board
    private static final int K = 40; // Number of cells to be left blank
    private ObjectMapper objectMapper = new ObjectMapper();
    DLX solverDLX = new DLX();
    Backtracking solverBT = new Backtracking();

    public String newConfiguration() {
        Sudoku sudoku = new Sudoku(N, K);
        sudoku.fillValues();
        return convertBoardToJson(sudoku.getMat());
    }

    public String solveSudokuDLX(int[][] board) {
        long startTime = System.currentTimeMillis();
        boolean valid = solverDLX.solveSudoku(board);
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        String durationStr = String.valueOf(duration);
        System.out.println("DLX : " + duration);
        String message = valid ? null : "No solution exists for this Sudoku configuration.";
    
        SudokuResult result = new SudokuResult(board, message, durationStr);
        try {
            // Convert the result to JSON format
            return objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "{\"error\": \"Error processing JSON\"}";
        }
    }

    public String solveSudokuBT(int[][] board) {
        long startTime = System.currentTimeMillis();
        boolean valid = solverBT.solveSudoku(board);
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        System.out.println("backtracking : " + duration);
        String durationStr = String.valueOf(duration);
        String message = valid ? null : "No solution exists for this Sudoku configuration.";
        SudokuResult result = new SudokuResult(board, message, durationStr);
        try {
            // Convert the result to JSON format
            return objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "{\"error\": \"Error processing JSON\"}";
        }
    }

    // Method to convert the Sudoku board to JSON format
    private String convertBoardToJson(int[][] board) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(board);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "{}";
        }
    }
}

// Sudoku generator class
class Sudoku {
    private int[][] mat;
    private final int N; // number of columns/rows
    private final int SRN; // square root of N
    private final int K; // Number of missing digits

    // Constructor
    Sudoku(int N, int K) {
        this.N = N;
        this.K = K;
        this.SRN = (int) Math.sqrt(N);
        this.mat = new int[N][N];
    }

    // Sudoku Generator
    public void fillValues() {
        // Fill the diagonal of SRN x SRN matrices
        fillDiagonal();

        // Fill remaining blocks
        fillRemaining(0, SRN);

        // Remove Randomly K digits to make game
        removeKDigits();
    }

    // Fill the diagonal SRN number of SRN x SRN matrices
    private void fillDiagonal() {
        for (int i = 0; i < N; i = i + SRN) {
            // for diagonal box, start coordinates->i==j
            fillBox(i, i);
        }
    }

    // Returns false if given SRN x SRN block contains num
    private boolean unUsedInBox(int rowStart, int colStart, int num) {
        for (int i = 0; i < SRN; i++)
            for (int j = 0; j < SRN; j++)
                if (mat[rowStart + i][colStart + j] == num)
                    return false;
        return true;
    }

    // Fill a SRN x SRN matrix
    private void fillBox(int row, int col) {
        int num;
        for (int i = 0; i < SRN; i++) {
            for (int j = 0; j < SRN; j++) {
                do {
                    num = randomGenerator(N);
                } while (!unUsedInBox(row, col, num));
                mat[row + i][col + j] = num;
            }
        }
    }

    // Random generator
    private int randomGenerator(int num) {
        return (int) Math.floor((Math.random() * num + 1));
    }

    // Check if safe to put in cell
    private boolean checkIfSafe(int i, int j, int num) {
        return (unUsedInRow(i, num) &&
                unUsedInCol(j, num) &&
                unUsedInBox(i - i % SRN, j - j % SRN, num));
    }

    // Check in the row for existence
    private boolean unUsedInRow(int i, int num) {
        for (int j = 0; j < N; j++)
            if (mat[i][j] == num)
                return false;
        return true;
    }

    // Check in the column for existence
    private boolean unUsedInCol(int j, int num) {
        for (int i = 0; i < N; i++)
            if (mat[i][j] == num)
                return false;
        return true;
    }

    // A recursive function to fill remaining matrix
    private boolean fillRemaining(int i, int j) {
        if (j >= N && i < N - 1) {
            i = i + 1;
            j = 0;
        }
        if (i >= N && j >= N)
            return true;

        if (i < SRN) {
            if (j < SRN)
                j = SRN;
        } else if (i < N - SRN) {
            if (j == (int) (i / SRN) * SRN)
                j = j + SRN;
        } else {
            if (j == N - SRN) {
                i = i + 1;
                j = 0;
                if (i >= N)
                    return true;
            }
        }

        for (int num = 1; num <= N; num++) {
            if (checkIfSafe(i, j, num)) {
                mat[i][j] = num;
                if (fillRemaining(i, j + 1))
                    return true;
                mat[i][j] = 0;
            }
        }
        return false;
    }

    // Remove K number of digits to complete game
    private void removeKDigits() {
        int count = K;
        while (count != 0) {
            int cellId = randomGenerator(N * N) - 1;

            // Extract coordinates i and j
            int i = (cellId / N);
            int j = cellId % N;

            if (mat[i][j] != 0) {
                count--;
                mat[i][j] = 0;
            }
        }
    }

    // Get the Sudoku matrix
    public int[][] getMat() {
        return mat;
    }
}
