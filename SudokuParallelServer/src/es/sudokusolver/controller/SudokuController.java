package es.sudokusolver.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import es.sudokusolver.bean.BWrapper;
import es.sudokusolver.service.SudokuService;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple Controller Method for resolving sudokus.
 * 
 * 1- Call Grid engine for solving sudokus ==> hazelcast(?)
 * 2- Cache engine hazelcast? 
 * @author Usuario
 */

@RestController
public class SudokuController {
	
	@Autowired
	SudokuService sService;
	
	private static final ExecutorService ex = Executors.newFixedThreadPool(10);
	
	private final Logger logger = LoggerFactory.getLogger(SudokuController.class);
	
	@RequestMapping(value = "/resolveSudoku", method = RequestMethod.POST)
    public ResponseEntity<BWrapper> resolveSudoku(@RequestBody BWrapper requestWrapper) {
		logger.info("[SudokuControler] -- resolveSudoku POST");
		
		BWrapper responseWrapper = sService.solveSudoku(requestWrapper);
		
		return new ResponseEntity<BWrapper>(responseWrapper, HttpStatus.OK);
    }
	
	@RequestMapping(value = "/resolveAsyncSudoku", method = RequestMethod.POST)
	public DeferredResult<BWrapper> resolveAsyncSudoku(@RequestBody BWrapper requestWrapper) {
		logger.info("[SudokuControlerAsync] -- resolveAsyncSudoku POST");
		
		DeferredResult<BWrapper> dr = new DeferredResult<BWrapper>();
		
		CompletableFuture.supplyAsync(
		()->{return sService.solveSudoku(requestWrapper);},ex).
		thenAccept((BWrapper responseWrapper)->{dr.setResult(responseWrapper);});
		
		return dr;
	} 
	
	
	
}
