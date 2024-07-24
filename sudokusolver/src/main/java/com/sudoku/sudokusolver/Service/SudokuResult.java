package com.sudoku.sudokusolver.Service;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SudokuResult {
    private int[][] board;
    private String message;
    private String duration;

    public SudokuResult(int[][] board, String message, String duration) {
        this.board = board;
        this.message = message;
        this.duration = duration;
    }

    // Getters and Setters
    public int[][] getBoard() {
        return board;
    }

    public void setBoard(int[][] board) {
        this.board = board;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }
}
