package com.sudoku.sudokusolver.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.sudoku.sudokusolver.Service.SudokuService;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api") // Add a common base path for better organization
@CrossOrigin
public class API {

    @Autowired
    SudokuService service;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/new")
    public String getNewConfiguration() {
        return service.newConfiguration();
    }

    @PostMapping("/solve/dlx")
    public String solveDLX(@RequestBody String configuration) {
        try {
            // Convert the JSON string configuration to a 2D integer array
            int[][] board = objectMapper.readValue(configuration, int[][].class);
    
            // Solve the Sudoku puzzle
            return service.solveSudokuDLX(board);
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"Invalid configuration\"}";
        }
    }

    @PostMapping("/solve/backtracking")
    public String solveBacktracking(@RequestBody String configuration) {
        try {
            // Convert the JSON string configuration to a 2D integer array
            int[][] board = objectMapper.readValue(configuration, int[][].class);
    
            // Solve the Sudoku puzzle
            return service.solveSudokuBT(board);
    
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"Invalid configuration\"}";
        }
    }
}
