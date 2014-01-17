package com.can.jdfa;
public class KleeneLanguage extends AbstractLanguage{
	
	private final Language repeat;
	
	public KleeneLanguage(Language repeat){
		super(repeat.getLetterCount());
		this.repeat=repeat;
	}
	@Override
	public boolean accepts(int[] input){
		if(input.length==0){
			return true;
		}
		for(int i=1;i<=input.length;i++){
			if(repeat.accepts(subword(input,0,i))){
				if(accepts(subword(input,i,input.length-i))){
					return true;
				}
			}
		}
		return false;
	}
	
	private int[] subword(int[] input,int off,int len){
		int[] ret=new int[len];
		for(int i=0;i<ret.length;i++){
			ret[i]=input[off+i];
		}
		return ret;
	}
	
	@Override
	protected Auto getAutoImpl(boolean innerSimplify){
		return Auto.kleene(repeat.getAuto(innerSimplify));
	}
}
