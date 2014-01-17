package com.can.jdfa.gui;

public class Transition implements Labeled{
	private State from;
	private Target to;
	private String label;
	private boolean swap;
	private double bend;
	public Transition(State from,Target to,String label,boolean swap,double bend){
		this.from=from;
		this.to=to;
		this.label=label;
		this.swap=swap;
		this.bend=bend;
	}
	public State getFrom(){
		return from;
	}
	public void setFrom(State from){
		this.from=from;
	}
	public Target getTo(){
		return to;
	}
	public void setTo(Target to){
		this.to=to;
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
	public boolean isSwap(){
		return swap;
	}
	public void setSwap(boolean swap){
		this.swap=swap;
	}
	public double getBend(){
		return bend;
	}
	public void setBend(double bend){
		this.bend=bend;
	}
	public boolean isSelf(){
		return to==from;
	}
	public void swap(){
		this.swap=!swap;
	}
	
}
