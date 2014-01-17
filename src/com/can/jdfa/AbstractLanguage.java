package com.can.jdfa;

public abstract class AbstractLanguage implements Language{
	private Auto autoCache;
	private Auto autoCacheSimplified;
	private final int letterCount;
	public AbstractLanguage(int letterCount){
		this.letterCount=letterCount;
	}
	@Override
	public int getLetterCount(){
		return letterCount;
	}

	@Override
	public Auto getAuto(boolean simplify){
		if(autoCache==null){
			autoCache=getAutoImpl(simplify);
		}
		if(simplify){
			autoCacheSimplified=autoCache.simplify();
			return autoCacheSimplified;
		}else{
			return autoCache;
		}
	}

	protected abstract Auto getAutoImpl(boolean innerSimplify);
	
}
