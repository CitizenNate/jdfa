package com.can.jdfa.gui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.can.jdfa.PrettyAuto;

public class Model{
	private List<State> states=new ArrayList<State>();
	private List<Transition> transitions=new ArrayList<Transition>();
	public Collection<State> getStates(){
		return states;
	}
	public void addState(State state){
		this.states.add(state);
	}
	public Collection<Transition> getTransitions(){
		return transitions;
	}
	public void addTransition(Transition transition){
		transitions.add(transition);
	}
	public Collection<Transition> getTransitions(State start){
		List<Transition> ret=new ArrayList<Transition>();
		for(Transition t:transitions){
			if(t.getFrom()==start){
				ret.add(t);
			}
		}
		return ret;
	}
	public void removeTransition(Transition t){
		transitions.remove(t);
	}
	public void removeState(State s){
		Iterator<Transition> it=transitions.iterator();
		while(it.hasNext()){
			Transition t=it.next();
			if(t.getFrom()==s||t.getTo()==s){
				it.remove();
			}
		}
		states.remove(s);
	}
	public PrettyAuto<State,Character> getAutomaton(){
		Set<Character> characters=new HashSet<Character>();
		for(Transition t:transitions){
			for(int i=0;i<t.getLabel().length();i++){
				char c=t.getLabel().charAt(i);
				if(Character.isAlphabetic(c)||Character.isDigit(c)){
					characters.add(c);
				}
			}
		}
		ArrayList<State> properStates=new ArrayList<State>();
		State start=null;
		for(State state:states){
			if(state.getRings()==0){
				start=(State) getTransitions(state).iterator().next().getTo();
			}else{
				properStates.add(state);
			}
		}
		return new PrettyAuto<State,Character>(properStates.toArray(new State[0]),
			characters.toArray(new Character[0]),start){
			@Override
			public boolean isAccepting(State state){
				return state.getRings()==2;
			}
			@Override
			public State getNext(State state,Character letter){
				for(Transition t:getTransitions(state)){
					if(t.getFrom()==state
						&&t.getLabel().indexOf(letter.charValue())!=-1){
						return (State) t.getTo();
					}
				}
				throw new IllegalArgumentException(state.getLabel()+":"+letter);
			}
		};
	}
}
