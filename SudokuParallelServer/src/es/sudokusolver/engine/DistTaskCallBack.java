package es.sudokusolver.engine;

import java.util.ArrayList;

import com.hazelcast.core.ExecutionCallback;

import es.sudokusolver.bean.NodeWrapper;

public class DistTaskCallBack implements ExecutionCallback<ArrayList<NodeWrapper>> {
	
	private int flagFinish = 0;
	private ArrayList<ArrayList<int[][]>> buffers = new ArrayList<ArrayList<int[][]>>();
	
	public DistTaskCallBack(ArrayList<ArrayList<int[][]>> _buffers){
		buffers = _buffers;
	}
	
	@Override
	public void onResponse(ArrayList<NodeWrapper> response) {
		
		ArrayList<NodeWrapper> nodes = response;
		for(int i=0; i<nodes.size(); i++){
			NodeWrapper nW = nodes.get(i);
			String node = nW.getIdNode();
			String index = node.substring(node.indexOf("_")+1, node.length());
			int indexInt = Integer.parseInt(index);
			buffers.get(indexInt-1).add(nW.getData());	
		}
		flagFinish = 1;
	}

	@Override
	public void onFailure(Throwable t) {
		
	}
	
	public int getFlag(){
		return flagFinish;
	}
	
	public ArrayList<ArrayList<int[][]>> getBuffers(){
		return buffers;
	}
	

}
