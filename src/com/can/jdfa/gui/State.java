package com.can.jdfa.gui;

public class State implements Target,Labeled{
	private int x;
	private int y;
	private String label;
	private int rings;
	public State(int x,int y,String label,int rings){
		this.x=x;
		this.y=y;
		this.label=label;
		this.rings=rings;
	}
	public int getX(){
		return x;
	}
	public void setX(int x){
		this.x=x;
	}
	public int getY(){
		return y;
	}
	public void setY(int y){
		this.y=y;
	}
	public String getLabel(){
		return label;
	}
	public void setLabel(String label){
		if(label==null){
			throw new NullPointerException();
		}
		this.label=label;
	}
	public int getRings(){
		return rings;
	}
	public String toString(){
		return "State[("+x+","+y+") '"+label+"' rings="+rings+"]";
	}
	public void setRings(int rings){
		this.rings=rings;
	}
}
