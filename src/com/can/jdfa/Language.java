package com.can.jdfa;

public interface Language{
	int getLetterCount();
	boolean accepts(int[] input);
	Auto getAuto(boolean simplify);
}
