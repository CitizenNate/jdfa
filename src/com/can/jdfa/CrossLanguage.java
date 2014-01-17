package com.can.jdfa;

public class CrossLanguage extends AbstractLanguage{
	private final Language a;
	private final Language b;
	private final CrossRule rule;
	public CrossLanguage(Language a,Language b,CrossRule rule){
		super(a.getLetterCount());
		this.a=a;
		this.b=b;
		this.rule=rule;
		if(a.getLetterCount()!=b.getLetterCount()){
			throw new IllegalArgumentException();
		}
	}
	@Override
	public boolean accepts(int[] input){
		return rule.apply(a.accepts(input),b.accepts(input));
	}
	@Override
	public int getLetterCount(){
		return a.getLetterCount();
	}
	@Override
	protected Auto getAutoImpl(boolean innerSimplify){
		return Auto.cross(a.getAuto(innerSimplify),b.getAuto(innerSimplify),rule);
	}
	
}
