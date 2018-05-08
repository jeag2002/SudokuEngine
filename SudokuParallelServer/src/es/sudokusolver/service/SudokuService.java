package es.sudokusolver.service;

import es.sudokusolver.bean.BWrapper;

public interface SudokuService {
	BWrapper solveSudoku(BWrapper wrapper);
}
