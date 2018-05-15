package es.sudokusolver.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.IMap;
import com.hazelcast.core.IQueue;
import com.hazelcast.core.Member;
import com.hazelcast.core.MultiExecutionCallback;


import es.sudokusolver.bean.BWrapper;
import es.sudokusolver.bean.NodeWrapper;
import es.sudokusolver.engine.Constants;
import es.sudokusolver.engine.DistTask;
import es.sudokusolver.engine.DistTaskCallBack;
import es.sudokusolver.engine.SolveTask;

@Service
@CacheConfig(cacheNames = "sudokus")
public class SudokuServiceImpl implements SudokuService {
	
	private final Logger logger = LoggerFactory.getLogger(SudokuService.class);
	
	@Autowired
	HazelcastInstance hInstance;

	private IQueue<NodeWrapper> queue_i;
	private IQueue<NodeWrapper> queue_o;
	
	private IExecutorService executorService;
		
	@Autowired
	DistTask task;
	
	
	@Override
	@Cacheable()
	public BWrapper solveSudoku(BWrapper wrapper) {
		
		BWrapper results = new BWrapper();
		
		long start = 0;
		
		try{
			
			queue_i = hInstance.getQueue(Constants.SUDOKU_QUEUE_1);
			queue_o = hInstance.getQueue(Constants.SUDOKU_QUEUE_2);
		
			logger.info("[SudokuService] -- solveSudoku INI BWrapper ("+wrapper.toString()+")");
			
			start = System.currentTimeMillis();
			
			List<Member> activeMembers = getActiveElements();
			int numberMembers = activeMembers.size();
			
			logger.info("[SudokuService] -- size active cluster members (" + numberMembers + ")");
			
			int rows = Integer.parseInt(wrapper.getSizeY());
			int cols = Integer.parseInt(wrapper.getSizeY());
			
			if (hInstance == null){
				throw new Exception("hInstance NULL");
			}else if (task == null){
				throw new Exception("task NULL");
			}else if (rows != cols ){
				throw new Exception("rows: " + rows + " - cols: " + cols + " not equal");
			}else if ((rows <= 0) || (cols <= 0)){
				throw new Exception("files: " + rows + " or cols " + cols + " values less or equals to zero");
			}else{		
				double res = Math.sqrt(rows);
				int res_int = (int)res;
				double fracc = res - res_int;
				
				if (fracc != 0){
					throw new Exception("cannot divide sudoku matrix in small parts (" + res + ")");
				}
				
				logger.info("[SudokuService] -- is a (" + res + ")x(" + res + ") sudoku");
				
				if (wrapper.getData().length() < (rows * cols)){
					throw new Exception("Not enough data (" + wrapper.getData().length() + ")");
				}
				
				results = sudokuEngine(wrapper, rows, res_int, numberMembers);
			}
			
			logger.info("[SudokuService] -- solveSudoku FIN");
		}catch(Exception es_1){
			logger.warn("[SudokuService] -- error (" + es_1.getMessage() + ")");
			results.setRes(BWrapper.KO);
			results.setErrMsg(es_1.getMessage());
		}finally{
			
			long end = System.currentTimeMillis();
			logger.info("[SudokuControler] -- time spent (" + (end-start) + ") ms");
			
			return results;
		}
		
	}
	
	
	
	/**
	 * Process Nodes and Return
	 */
	private BWrapper sudokuEngine(BWrapper wrapper, int rows, int res_int, int numberMembers) throws Exception{
		
		BWrapper bw = new BWrapper();
		
		executorService = hInstance.getExecutorService("exec");
		
		int matrix[][] = StringToMatrix(wrapper, rows);
		
		//extract subsets of matrix
		ArrayList<int[][]>submatrixlist = new ArrayList<int[][]>();
		
		for(int i=0; i<res_int; i++){
			for (int j=0; j<res_int; j++){
				submatrixlist.add(subSetMatrix(matrix, res_int, i, j));
			}
		}
		
		int rem = 0;
		
		//process members
		if (numberMembers < submatrixlist.size()){
			rem = submatrixlist.size()%numberMembers;
		}
		
		int limInf = 0; 				
		int limSup = numberMembers;
		
		
		//create TAD keep all possible combination possible.
		////////////////////////////////////////////////////////////////////////////
		ArrayList<ArrayList<int[][]>> buffers = new ArrayList<ArrayList<int[][]>>();
		
		for(int i=0; i<rows; i++){
			buffers.add(new ArrayList<int[][]>());
		}
		////////////////////////////////////////////////////////////////////////////
		
		
		int limit = Integer.parseInt(wrapper.getSizeY());
		
		//set matrix to member clusters.
		////////////////////////////////////////////////////////////////////////////
		
		boolean processed = false;
		
		while(!processed){
			
			
			//evaluate in every steps the nodes availables
			List<Member> activeMembers = getActiveElements();
			numberMembers = activeMembers.size();
			logger.info("[SudokuService] -- size active cluster members (" + numberMembers + ")");
			
			
			//set all the data to all active members
			if (numberMembers >= submatrixlist.size()){
				for(int i=0; i<submatrixlist.size(); i++){
					NodeWrapper nW = new NodeWrapper("data_"+(i+1), submatrixlist.get(i));
					queue_i.put(nW);
				}
				
			//set subset data to all active members
			}else{
				
				List<int[][]>subImap = submatrixlist.subList(limInf, limSup);
				
				for(int i=0; i<subImap.size(); i++){
					NodeWrapper nW = new NodeWrapper("data_"+(limInf+i+1), subImap.get(i));
					queue_i.put(nW);
				
				}
				
				limInf = limSup;
				limSup = limInf + numberMembers;
				
				if (limSup > submatrixlist.size()){
					limSup = limInf + rem;
				}
				
			}
			
			
			DistTaskCallBack dTCB = new DistTaskCallBack(buffers);
			executorService.submitToAllMembers(task, dTCB);
			
			while(dTCB.getFlag()==0){Thread.sleep(100);}
			buffers = dTCB.getBuffers();
			
			if (numberMembers >= submatrixlist.size()){
				processed = true;
			}else if (limInf >= rows){
				processed = true;
			}
		}
		
		bw = processResultsFromNodesParallel(executorService, wrapper, buffers, rows, res_int);
		////////////////////////////////////////////////////////////////////////////
		
		return bw;
	}
	
	
	/**
	 * Process results from node parallel way 
	 */
	private BWrapper processResultsFromNodesParallel(
			IExecutorService executorService, 
			BWrapper wrapper, 
			ArrayList<ArrayList<int[][]>> buffers, 
			int nodes, 
			int res_int) throws Exception{
		
	
		
		ArrayList<int[][]> array_seeds = buffers.get(0);
		
		boolean processed = false;
		
		List<Future<BWrapper>> results = new ArrayList<Future<BWrapper>>();
		List<BWrapper> result_data = new ArrayList<BWrapper>();
		
		BWrapper bw = new BWrapper();

		int j = 0;
		
		ArrayList<ArrayList<int[][]>> bufferToSolveTask = new ArrayList<ArrayList<int[][]>>();
	    bufferToSolveTask.addAll(buffers.subList(1, buffers.size()));
		
		while(!processed){
			List<Member> active_cluster = getActiveElements();
			int size_active_cluster = active_cluster.size();
			
			results.clear();
			result_data.clear();
			
			
			for(int i=0; i<size_active_cluster; i++){
				if (j < array_seeds.size()){
					
					logger.info("[SudokuService] res node [" + j + "] node [" + active_cluster.get(i).getUuid() + "]");
					SolveTask sT = new SolveTask(array_seeds.get(j),bufferToSolveTask,nodes,res_int);
					sT.setHazelcastInstance(hInstance);
					results.add(executorService.submitToMember( sT, active_cluster.get(i)));
					j++;
				}
			}
			
			
			for(int z=0; z<results.size(); z++){
				result_data.add(results.get(z).get());
			}
			
			
			
			for(int z=0; z<result_data.size(); z++){
				bw = result_data.get(z);
				
				logger.info("[SudokuService] res node [" + (j-1) + "] result [" + bw.toString() + "]");
				
				
				
				if (bw.getRes().equalsIgnoreCase(BWrapper.OK)){
					wrapper.set(bw);
					break;
				}
			}
			
			if (bw.getRes().equalsIgnoreCase(BWrapper.OK)){
				processed = true;
			}else{
				if (j>=array_seeds.size()){processed = true;}		
			}	
		}
		return wrapper;
	}
	
	
	
	
	
	
	
	/**
	 * Process results from nodes
	 */
	
	private BWrapper processResultsFromNodes(BWrapper wrapper, ArrayList<ArrayList<int[][]>> buffers, int nodes, int res_int) throws Exception{ 
		ArrayList<int[][]> stack = new ArrayList<int[][]>();
		ArrayList<BWrapper> resStack = new ArrayList<BWrapper>();
		processRecursiveNode(wrapper, buffers,stack, resStack, 0,nodes,res_int);	
		if (resStack.size() > 0){
			return resStack.get(0);
		}else{
			return new BWrapper();
		}
	}
	
	/**
	 * Process recursive obtained nodes
	 */
	private void processRecursiveNode(BWrapper wrapper, ArrayList<ArrayList<int[][]>> buffers, ArrayList<int[][]>stack, ArrayList<BWrapper> resStack, int level, int nodes, int res_int) throws Exception{
		

		
		if (level == nodes){
			
			int[][] matrix = stackToMatrix(stack, nodes, res_int);
			
			logger.info("[SudokuService] processing (" + ArrayToString(matrix) + ")");
			Thread.sleep(10);
			
			if (checkSudoku(matrix,nodes)){
				logger.info("[SudokuService] Solution FOUND! for wrapper (" + wrapper + ") result (" + ArrayToString(matrix) + ")");
			
				BWrapper bw = new BWrapper();	
				bw.setSizeX(wrapper.getSizeX());
				bw.setSizeY(wrapper.getSizeY());
				bw.setData(matrixToString(matrix));
				bw.setErrMsg("Solution FOUND!");
				bw.setRes(BWrapper.OK);
				resStack.add(bw);
				
			}
			
		}else{
			//-->get element from stack
			ArrayList<int[][]> stage = buffers.get(level);
			for(int i=0; i<stage.size(); i++){
				stack.add(stage.get(i));
				
				int[][]matrix = stackToMatrix(stack, nodes, res_int);
				
				if (isAValidMatrix(matrix)){
					processRecursiveNode(wrapper, buffers,stack, resStack,level+1,nodes, res_int);
				}else{
					logger.debug("[SudokuService] not a valid matrix (" + ArrayToString(matrix) + ")");
				}
				
				stack.remove(stage.get(i));
			}
		}
	}
	
	
	/**
	 * is a valid matrix?
	 */
	
	private boolean isAValidMatrix(int[][] matrix){
		boolean res = true;
		
		//-->Evaluate rows
		for(int i=0; i< matrix[0].length; i++){
			int a[] = matrix[i];
			for (int j=0; j<a.length; j++){
				for(int z=j+1; z<a.length; z++){
					if ((a[j] == a [z]) && (a[j] != 0)){
						res = res && false;
					}
				}
			}
		}
				
		//-->Evaluate cols
		if (res){
			for(int j=0; j<matrix[0].length; j++){
				int b[] = new int[matrix[0].length];
				for (int l=0; l<matrix[0].length; l++){
					b[l] = matrix[l][j];
				}
				
				for(int m=0; m<matrix[0].length; m++){
					for(int n=m+1; n<matrix[0].length; n++){
						if ((b[m]==b[n]) && (b[n]!=0)){
							res = res && false;
						}
					}
				}
			}
		}			
		
		return res;
	}
	
	
	
	/**
	 * 
	 */
	private String matrixToString(int [][]matrix){
		String data = "";
		
		for(int i=0; i<matrix[0].length; i++){
			for (int j=0; j<matrix[0].length; j++){
				data += String.valueOf(matrix[i][j]);
			}
		}
		
		return data;
	}
	
	
	
	
	/**
	 * Sudoku general evaluation
	 */
	
	private boolean checkSudoku(int [][]matrix, int nodes){
		boolean isValid = true;
		
		//-->Evaluate rows
		for(int i=0; i< matrix[0].length; i++){
			int a[] = matrix[i];
			for(int j=1; (j<=nodes && isValid); j++){
				isValid = false;
				
				for(int z=0; (z<a.length && !isValid); z++){
					isValid = (a[z] == j);
				}
			}
			if (!isValid){break;}
		}
		
		//-->Evaluate cols
		if (isValid){
			for(int j=0; j<matrix[0].length; j++){
				int b[] = new int[nodes];
				for(int l=0; l<matrix[0].length; l++){
					b[l] = matrix[l][j];
				}
				
				for(int m=1;(m<=nodes && isValid); m++){
					isValid = false;
					for(int z=0; (z<b.length && !isValid); z++){
						isValid = (b[z] == m);
					}
				}
				
				if (!isValid){break;}
				
			}
		}
		
		return isValid;
		
	}
	
	
	
	
	/**
	 * Process stack to matrix
	 */
	
	private int[][] stackToMatrix(ArrayList<int[][]>stack, int nodes, int res_int){
		int[][] matrix = new int[nodes][nodes];
		
		for(int i=0; i<stack.size(); i++){
			
			int[][] data = stack.get(i);
			
			int row = i/res_int;
			int col = i%res_int;
			
			int j=  row * res_int;
			int k = col * res_int;
			
			for(int l=0,m=j; l<res_int; l++,m++){
				for(int n=0,p=k; n<res_int; n++,p++){
					matrix[m][p] = data[l][n];
				}
			}
		}
		
		return matrix;
	}
	
	
	
	/**
	 * Matrix int to String
	 * @param intArray
	 * @return
	 */	
	private String ArrayToString(int[][] intArray){
		String res = "[";
		
		for(int i=0; i<intArray[0].length; i++){
			res += "[";
			for (int j=0; j<intArray[0].length; j++){
				res += intArray[i][j] + " ";
			}
			res += "]";
		}
		
		res += "]";
		return res;
	}
	
	
	
	
	/**
	 * Extract Subset of matrix
	 */
	private int[][] subSetMatrix(int[][] iMatrix, int size, int file, int col){
		
		int[][] oMatrix = new int[size][];
		
		int limit_inf = file * size;
		int limit_sup = ((file+1) * size);
		
		for(int i=limit_inf, j=0; i<limit_sup; i++, j++){
			oMatrix[j] = Arrays.copyOfRange(iMatrix[i], col*size, (col+1)*size);
		}
		
		return oMatrix;
	}
	
	
	/**
	 * transform String to matrix of data.
	 * @param bw
	 * @param size
	 * @return
	 */	
	private int[][] StringToMatrix(BWrapper bw, int size){
		int[][] matrix = new int[size][size];
		int limit = size * size;
		
		int col = 0;
		int row = 0;
		
		for(int i=0; i<limit; i++){
			
			char elementAt = bw.getData().charAt(i);
			int iAt = Character.getNumericValue(elementAt);
			matrix[row][col] = iAt;
			col++;
			if (col >= size){
				col = 0;
				row++;		
			}			
		}
		return matrix;
	}
	
	
	/**
	 * Detect active cluster members
	 * @return
	 */
	private List<Member> getActiveElements(){
		
		Set<Member> all = hInstance.getCluster().getMembers();
		List<Member> named = new ArrayList<Member>(all.size());
		for (Member member: all) {
			//logger.info("[SudokuService] -- Active Member of cluster [" + member.toString() + "]");
			named.add(member);
		}
		
		return named;
	}
	

	
}
