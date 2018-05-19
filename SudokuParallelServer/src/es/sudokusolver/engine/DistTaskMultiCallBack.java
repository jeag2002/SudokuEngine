package es.sudokusolver.engine;

import java.util.ArrayList;
import java.util.Map;

import com.hazelcast.core.Member;
import com.hazelcast.core.MultiExecutionCallback;

import es.sudokusolver.bean.NodeWrapper;

public class DistTaskMultiCallBack implements MultiExecutionCallback {
	
	private int flagFinish = 0;
	private ArrayList<ArrayList<int[][]>> buffers = new ArrayList<ArrayList<int[][]>>();
	
	public DistTaskMultiCallBack(ArrayList<ArrayList<int[][]>> _buffers){
		buffers = _buffers;
	}

	@Override
	public void onResponse(Member member, Object value) {
		
		ArrayList<NodeWrapper> nodes = (ArrayList<NodeWrapper>)value;
		for(int i=0; i<nodes.size(); i++){
			NodeWrapper nW = nodes.get(i);
			String node = nW.getIdNode();
			String index = node.substring(node.indexOf("_")+1, node.length());
			int indexInt = Integer.parseInt(index);
			buffers.get(indexInt-1).add(nW.getData());
			
		}
		
	}

	@Override
	public void onComplete(Map<Member, Object> values) {
		flagFinish = 1;

	}
	
	public int getFlag(){
		return flagFinish;
	}
	
	public ArrayList<ArrayList<int[][]>> getBuffers(){
		return buffers;
	}

}
