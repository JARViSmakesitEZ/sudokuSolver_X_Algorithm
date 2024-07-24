import React, { useState, useEffect } from "react";
import axios from "axios";
import "./Sudoku.css";

const Sudoku = () => {
  const [grid, setGrid] = useState(generateEmptyGrid());
  const [isSolved, setIsSolved] = useState(false);

  useEffect(() => {
    // Optionally, fetch initial configuration here
    // fetchNewConfiguration();
  }, []);

  const fetchNewConfiguration = async () => {
    try {
      const response = await axios.get("http://localhost:8080/api/new");
      setGrid(response.data);
      setIsSolved(false);
    } catch (error) {
      console.error("Error fetching new configuration", error);
    }
  };

  const solveSudoku = async (endpoint) => {
    try {
      const response = await axios.post(
        "http://localhost:8080/api/solve/" + endpoint,
        grid
      );
      setGrid(response.data.board);
      setIsSolved(true);
    } catch (error) {
      console.error("Error solving Sudoku", error);
    }
  };

  const handleChange = (row, col, value) => {
    if (value >= 1 && value <= 9) {
      const newGrid = grid.map((r, i) =>
        i === row ? r.map((cell, j) => (j === col ? value : cell)) : r
      );
      setGrid(newGrid);
      setIsSolved(false);
    }
  };

  const handleCellChange = (event, row, col) => {
    const value = parseInt(event.target.value, 10);
    handleChange(row, col, value);
  };

  return (
    <div className="sudoku-container">
      <div className="sudoku-grid">
        {grid.map((row, rowIndex) =>
          row.map((cell, colIndex) => (
            <input
              key={`${rowIndex}-${colIndex}`}
              type="number"
              min="1"
              max="9"
              value={cell || ""}
              onChange={(e) => handleCellChange(e, rowIndex, colIndex)}
              disabled={isSolved}
              className="sudoku-cell"
            />
          ))
        )}
      </div>
      <div className="buttons">
        <button className="button" onClick={fetchNewConfiguration}>
          New Configuration
        </button>
        <button className="button" onClick={() => solveSudoku("dlx")}>
          Solve using DLX Algorithm
        </button>
        <button className="button" onClick={() => solveSudoku("backtracking")}>
          Solve using Backtracking Algorithm
        </button>
      </div>
    </div>
  );
};

// Utility function to generate an empty grid
const generateEmptyGrid = () => {
  const size = 9;
  return Array.from({ length: size }, () => Array(size).fill(null));
};

export default Sudoku;
