package es.sudokusolver.bean;

import java.io.Serializable;

public class BWrapper implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public static final String OK = "OK";
	public static final String KO = "KO";
	
	private String sizeX;
	private String sizeY;
	private String data;
	private String res;
	private String errMsg;
	
	public BWrapper(){
		sizeX = "";
		sizeY = "";
		data = "";
		res = "";
		errMsg = "";
	}
	
	public BWrapper(BWrapper copy){
		this.sizeX = copy.getSizeX();
		this.sizeY = copy.getSizeY();
		this.data = copy.getData();
		this.res = copy.getRes();
		this.errMsg = copy.getErrMsg();
	}
	
	public void setClone(BWrapper copy){
		this.sizeX = copy.getSizeX();
		this.sizeY = copy.getSizeY();
		this.data = copy.getData();
		this.res = copy.getRes();
		this.errMsg = copy.getErrMsg();
	}
	
	
	public String getSizeX() {
		return sizeX;
	}

	public void setSizeX(String sizeX) {
		this.sizeX = sizeX;
	}

	public String getSizeY() {
		return sizeY;
	}

	public void setSizeY(String sizeY) {
		this.sizeY = sizeY;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public String getRes() {
		return res;
	}

	public void setRes(String res) {
		this.res = res;
	}

	public String getErrMsg() {
		return errMsg;
	}

	public void setErrMsg(String errMsg) {
		this.errMsg = errMsg;
	}
	
	
	
	@Override
	public String toString(){
		return "X:"+this.sizeX+"Y:"+this.sizeY+"data:("+this.data+")res:"+this.res+"errMsg:"+this.errMsg;
	}
	
	

}
