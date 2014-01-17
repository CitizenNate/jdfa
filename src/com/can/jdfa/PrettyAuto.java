package com.can.jdfa;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public abstract class PrettyAuto<State,Letter>{
	private final State[] states;
	private final Letter[] alphabet;
	private final Auto auto;
	private final State starting;
	public PrettyAuto(State[] states,Letter[] alphabet,State starting){
		this.states=states;
		this.alphabet=alphabet;
		this.starting=starting;
		auto=constructAuto();
	}
	private Auto constructAuto(){
		Map<State,Integer> codes=new HashMap<State,Integer>();
		boolean[] accepting=new boolean[states.length];
		for(int i=0;i<states.length;i++){
			accepting[i]=isAccepting(states[i]);
			codes.put(states[i],i);
		}
		int[][] transitions=new int[states.length][alphabet.length];
		for(int i=0;i<states.length;i++){
			for(int j=0;j<alphabet.length;j++){
				transitions[i][j]=codes.get(getNext(states[i],alphabet[j]));
			}
		}
		Auto ret=new Auto(states.length,alphabet.length,codes.get(starting), accepting, transitions);
		return ret;
	}
	public abstract boolean isAccepting(State state);
	public abstract State getNext(State state,Letter letter);
	public void getLanguage(int maxSize,final Consumer<String> cons){
		Tester tester=new Tester(auto.getLetterCount());
		tester.enumerate(maxSize,new Consumer<int[]>(){
			@Override
			public void consume(int[] t){
				if(auto.accepts(t)){
					StringBuffer buffer=new StringBuffer();
					for(int i=0;i<t.length;i++){
						buffer.append(alphabet[t[i]]);
					}
					cons.consume(buffer.toString());
				}
			}
		});
	}
}
