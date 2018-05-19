package es.sudokusolver.bean;

import java.io.Serializable;

public class NodeWrapper implements Serializable{
	
	private static final int DEF_SIZE = 3;
	private static final long serialVersionUID = 1L;
	
	private String idNode;
	private int[][]data;
	
	public NodeWrapper(){
		idNode = "";
		data = new int[DEF_SIZE][DEF_SIZE];
	}
	
	public NodeWrapper(String _idNode, int[][] _data){
		idNode = _idNode;
		data = _data;
	}
	
	public String getIdNode() {
		return idNode;
	}

	public void setIdNode(String idNode) {
		this.idNode = idNode;
	}

	public int[][] getData() {
		return data;
	}

	public void setData(int[][] _data) {
		
		this.data = null;
		this.data = new int[_data[0].length][_data[0].length];
		
		for(int i=0; i<_data[0].length; i++){
			for(int j=0; j<_data[0].length; j++){
				data[i][j] = _data[i][j];
			}
		}
		
		
	}


}
