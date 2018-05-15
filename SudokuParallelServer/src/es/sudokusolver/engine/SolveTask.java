package es.sudokusolver.engine;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;

import es.sudokusolver.bean.BWrapper;
import es.sudokusolver.bean.NodeWrapper;

public class SolveTask implements Callable<BWrapper>, Serializable, HazelcastInstanceAware{
	
	private static final long serialVersionUID = 1L;
	private transient HazelcastInstance hazelcastInstance;
	
	private int[][] seed;
	private ArrayList<ArrayList<int[][]>> buffers;
	private int nodes;
	private int res_int;
	
	
	public SolveTask(int[][] seed_1, ArrayList<ArrayList<int[][]>> buffers_1, int nodes_1, int res_int_1){	
		seed = seed_1;
		buffers = buffers_1;
		nodes = nodes_1;
		res_int = res_int_1;
	}
	
	@Override
	public BWrapper call() throws Exception {		
		BWrapper bw = new BWrapper();
		
		ArrayList<int[][]> stack = new ArrayList<int[][]>();
		ArrayList<BWrapper> resStack = new ArrayList<BWrapper>();
		
		System.out.println("[SolveTask ("+hazelcastInstance.getCluster().getLocalMember().toString()+")] node seed (" + ArrayToString(seed) + ") nodes (" + nodes + ") res_int (" + res_int + ")");
		
		stack.add(seed);
		processRecursiveNode(buffers,stack, resStack, 0,nodes,res_int);	
		
		if (resStack.size() > 0){
			System.out.println("[SolveTask ("+hazelcastInstance.getCluster().getLocalMember().toString()+")] node seed (" + ArrayToString(seed) + ") resStack (" + resStack.size() + ") solve (" + resStack.get(0) + ")");
			return resStack.get(0);
		}else{
			System.out.println("[SolveTask ("+hazelcastInstance.getCluster().getLocalMember().toString()+")] node seed (" + ArrayToString(seed) + ") resStack (" + resStack.size() + ") empty (" + bw.toString() + ")");
			return new BWrapper();
		}
		
	}
	
	
	
	@Override
	public void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
		this.hazelcastInstance = hazelcastInstance;
		
	}
	
	
	/**
	 * Process recursive obtained nodes
	 */
	private void processRecursiveNode(List<ArrayList<int[][]>> buffers, ArrayList<int[][]>stack, ArrayList<BWrapper> resStack, int level, int nodes, int res_int) throws Exception{
		if (level == (nodes-1)){
			
			int[][] matrix = stackToMatrix(stack, nodes, res_int);
			
			
			if (checkSudoku(matrix,nodes)){
				System.out.println("[SolveTask ("+hazelcastInstance.getCluster().getLocalMember().toString()+")] Solution FOUND! result (" + ArrayToString(matrix) + ")");
			
				BWrapper bw = new BWrapper();	
				bw.setSizeX(String.valueOf(nodes));
				bw.setSizeY(String.valueOf(nodes));
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
					processRecursiveNode(buffers,stack, resStack,level+1,nodes, res_int);
				}else{
					//System.out.println("[SolveTask] Solution NOT valid (" + ArrayToString(matrix) + ")");
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
	
	
	

}
