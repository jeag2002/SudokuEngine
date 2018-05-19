package es.sudokusolver.engine;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.core.IQueue;

import es.sudokusolver.bean.NodeWrapper;

public class DistTask implements Callable<ArrayList<NodeWrapper>>, Serializable, HazelcastInstanceAware{
	
	private static final long serialVersionUID = 1L;

	private transient HazelcastInstance hazelcastInstance;
	
	
	public DistTask(){
		System.out.println("[DistTask]--Created!");
	}
	
	public DistTask(HazelcastInstance _hazelcastInstance){
		
		System.out.println("[DistTask]--Created!");
		hazelcastInstance = _hazelcastInstance;

	}
	
	
	@Override
	public ArrayList<NodeWrapper> call() throws Exception {
		
		IQueue<NodeWrapper> queue_i = hazelcastInstance.getQueue(Constants.SUDOKU_QUEUE_1);
		//IQueue<NodeWrapper> queue_o = hazelcastInstance.getQueue(Constants.SUDOKU_QUEUE_2);
		
		NodeWrapper nW = null;
		while (nW == null){nW = queue_i.poll();}
		
		NodeWrapper nW_1 = new NodeWrapper();
		nW_1.setIdNode(nW.getIdNode());
		nW_1.setData(nW.getData());
		
		int profundity = numProfundity(nW);
		System.out.println("[DistTask ("+hazelcastInstance.getCluster().getLocalMember().toString()+")] -- proccesing node ID (" + nW.getIdNode() + ") data (" + ArrayToString(nW.getData()) + ") profundity (" + profundity + ")");
		int size = nW.getData()[0].length;
		
		ArrayList<NodeWrapper> buffer = new ArrayList<NodeWrapper>();
		recursiveCall(nW, nW_1, 0,0, size, profundity, buffer);
		
		System.out.println("[DistTask ("+hazelcastInstance.getCluster().getLocalMember().toString()+")] -- node ID (" + nW.getIdNode() + ") - (" + buffer.size() + ") possible solutions");
		
		return buffer;
		/*
		for(int i=0; i<buffer.size(); i++){
			queue_o.put(buffer.get(i));
		}
		*/
		//return nW.getIdNode();
	}
	
	private void recursiveCall(NodeWrapper nW, NodeWrapper nW_1, int i, int j, int size, int profundity, ArrayList<NodeWrapper> queue_o) throws Exception{
		
		if (j >= size){j=0;i=i+1;}
		
		if (profundity == 0){
			//System.out.println("[DistTask ("+hazelcastInstance.getCluster().getLocalMember().toString()+")] -- possible solution found node ID: (" + nW_1.getIdNode() + ") data: (" + ArrayToString(nW_1.getData()) + ")");
			NodeWrapper nWClone = new NodeWrapper();
			nWClone.setIdNode(nW_1.getIdNode());
			nWClone.setData(nW_1.getData());
			queue_o.add(nWClone);
		}else{
			
			if (nW.getData()[i][j] != 0){
				recursiveCall(nW, nW_1, i, j+1, size, profundity, queue_o);
			}else{
				
				for(int posValue=1; posValue<=(size*size); posValue++){
					
					nW_1.getData()[i][j] = posValue;
					
					if (isValid(nW_1, i,j,size)){
						recursiveCall(nW, nW_1, i,j+1, size, profundity-1, queue_o);
					}
					
					nW_1.getData()[i][j] = 0;
				}
			}
		}
	}
	
	private boolean isValid(NodeWrapper nW, int i, int j, int size){
		boolean res = true;
		int value = nW.getData()[i][j];
		boolean found = false;
		
		for(int k =0; (k<size && !found); k++){
			for(int l=0; (l<size && !found); l++){
				if ((nW.getData()[k][l] != 0) &&
				   (nW.getData()[k][l] == value) &&
				   ((i!=k) || (j!=l))){
					found = true;
					res = false;
				}
			}
		}	
		return res;
	}
	
	
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
	 * look for max profundity of recursive tree.
	 * @param nW
	 * @return
	 */
	private int numProfundity(NodeWrapper nW){
		int res = 0;
		int[][]matrix = nW.getData();
		int size = matrix[0].length;
		
		for(int i=0;i<size;i++){
			for(int j=0; j<size; j++){
				if (matrix[i][j]==0){res++;}
			}
		}
		return res;
	}
	
	
	@Override
	public void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
		this.hazelcastInstance = hazelcastInstance;
		
	}

}
