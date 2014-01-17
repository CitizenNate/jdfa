package com.can.jdfa;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Auto extends AbstractLanguage{
	private final int stateCount;
	private final int letterCount;
	private final int startingState;
	private final boolean[] accepting;
	private final int[][] transitions;
	public Auto(int stateCount,int letterCount,int startingState,
		boolean[] accepting,int[][] transitions){
		super(letterCount);
		this.stateCount=stateCount;
		this.letterCount=letterCount;
		this.startingState=startingState;
		this.accepting=accepting.clone();
		this.transitions=Util.deepClone(transitions);
		check();
	}
	private void check(){
		if(accepting.length!=stateCount){
			throw new IllegalArgumentException(
				"accepting length is not stateCount");
		}
		if(stateCount<0||letterCount<0){
			throw new IllegalArgumentException(
				"stateCount and letterCount must be non-negative");
		}
		if(startingState<0||startingState>=stateCount){
			throw new IllegalArgumentException("startingState is not in bounds");
		}
		for(int state=0;state<stateCount;state++){
			if(transitions[state].length!=letterCount){
				throw new IllegalArgumentException("Length of transition for "
					+state+" is not letterCount");
			}
			for(int letter=0;letter<letterCount;letter++){
				if(transitions[state][letter]<0
					||transitions[state][letter]>=stateCount){
					throw new IllegalArgumentException(
						"Transition is not in bounds");
				}
			}
		}
	}
	public int step(int state,int letter){
		return transitions[state][letter];
	}
	public boolean acceptsState(int state){
		return accepting[state];
	}
	public boolean accepts(int[] input){
		int state=startingState;
		for(int i=0;i<input.length;i++){
			state=step(state,input[i]);
		}
		return acceptsState(state);
	}
	public Partition getInitialPartition(){
		int acceptCount=0;
		int rejectCount=0;
		for(boolean b:accepting){
			if(b){
				acceptCount++;
			}else{
				rejectCount++;
			}
		}
		int[] acceptClass=new int[acceptCount];
		int[] rejectClass=new int[rejectCount];
		acceptCount=0;
		rejectCount=0;
		for(int i=0;i<stateCount;i++){
			if(acceptsState(i)){
				acceptClass[acceptCount++]=i;
			}else{
				rejectClass[rejectCount++]=i;
			}
		}
		return new Partition(new int[][]{acceptClass,rejectClass});
	}
	public Partition step(Partition start){
		int[][][] classGroups=new int[start.classArray.length][][];
		int total=0;
		for(int i=0;i<classGroups.length;i++){
			int[] clazz=start.classArray[i];
			classGroups[i]=stepClass(start,clazz);
			total+=classGroups[i].length;
		}
		int[][] ret=new int[total][];
		total=0;
		for(int[][] group:classGroups){
			System.arraycopy(group,0,ret,total,group.length);
			total+=group.length;
		}
		return new Partition(ret);
	}
	public int[][] stepClass(Partition start,int[] clazz){
		Map<IntBuffer,IntBuffer> newClasses=new HashMap<IntBuffer,IntBuffer>();
		for(int state:clazz){
			int[] steppedClasses=new int[letterCount];
			for(int i=0;i<letterCount;i++){
				steppedClasses[i]=start.getIndex(step(state,i));
			}
			IntBuffer buf=IntBuffer.wrap(steppedClasses);
			IntBuffer ret=newClasses.get(buf);
			if(ret==null){
				ret=IntBuffer.allocate(clazz.length);
				newClasses.put(buf,ret);
			}
			ret.put(state);
		}
		Collection<IntBuffer> classValues=newClasses.values();
		int[][] ret=new int[classValues.size()][];
		int index=0;
		for(IntBuffer buf:classValues){
			buf.flip();
			int[] classValue=new int[buf.limit()];
			buf.get(classValue);
			ret[index++]=classValue;
		}
		return ret;
	}
	protected Auto simplifyConstant(){
		boolean allAccept=true;
		boolean allReject=true;
		for(boolean b:accepting){
			if(b){
				allReject=false;
			}else{
				allAccept=false;
			}
		}
		if(allAccept||allReject){
			return newConstant(letterCount,allAccept);
		}else{
			return null;
		}
	}
	protected Auto simplify(){
		Auto ret=canonical();
		Auto constant=ret.simplifyConstant();
		if(constant!=null){
			return constant;
		}
		return ret.contract().canonical();
	}
	protected Auto contract(){
		Partition part=getInitialPartition();
		while(true){
			Partition next=step(part);
			if(next.size()>part.size()){
				part=next;
			}else{
				break;
			}
		}
		checkValid(part);
		checkMinimal(part);
		return coallesce(part);
	}
	private Auto coallesce(Partition part){
		int newStateCount=part.size();
		int newLetterCount=letterCount;
		int newStartingState=part.getIndex(startingState);
		boolean[] newAccepting=new boolean[newStateCount];
		for(int i=0;i<newStateCount;i++){
			newAccepting[i]=accepting[part.classArray[i][0]];
		}
		int[][] newTransitions=new int[newStateCount][];
		for(int i=0;i<newStateCount;i++){
			newTransitions[i]=new int[newLetterCount];
			for(int k=0;k<newLetterCount;k++){
				newTransitions[i][k]=part.indexMap[step(part.classArray[i][0],k)];
			}
		}
		return new Auto(newStateCount,newLetterCount,newStartingState,
			newAccepting,newTransitions);
	}
	public static Auto newConstant(int letterCount,boolean accept){
		return new Auto(1,letterCount,0,new boolean[]{accept},
			new int[][]{new int[letterCount]});
	}
	private void checkMinimal(Partition part){
		if(letterCount==0){
			if(part.size()!=1){
				throw new IllegalArgumentException(
					"The DFA on an empty alphabet has one state. ("+part.size()
						+" found).");
			}
		}
		for(int i=0;i<part.size();i++){
			for(int k=i+1;k<part.size();k++){
				int c1=part.classArray[i][0];
				int c2=part.classArray[k][0];
				if(accepting[c1]==accepting[c2]){
					boolean matched=true;
					for(int l=0;l<letterCount;l++){
						if(part.indexMap[step(c1,l)]!=part.indexMap[step(c2,l)]){
							matched=false;
							break;
						}
					}
					if(matched){
						throw new IllegalArgumentException(
							"Not a minimal partition: classes "+i+"->"
								+Arrays.toString(part.classArray[i])+" and "+k
								+"->"+Arrays.toString(part.classArray[k])
								+" are equivalent.");
					}
				}
			}
		}
	}
	private void checkValid(Partition part){
		
		for(int[] clazz:part.classArray){
			for(int i=1;i<clazz.length;i++){
				for(int k=0;k<letterCount;k++){
					if(part.indexMap[step(clazz[0],k)]!=part.indexMap[step(
						clazz[i],k)]){
						throw new IllegalArgumentException("Class "
							+Arrays.toString(clazz));
					}
				}
			}
		}
	}
	private class Partition{
		private final int[][] classMap;
		private final int[][] classArray;
		private final int[] indexMap;
		public Partition(int[][] classArray){
			this.classArray=Util.deepClone(classArray);
			
			boolean[] visited=new boolean[stateCount];
			for(int[] clazz:classArray){
				if(clazz.length==0){
					throw new IllegalArgumentException(
						"Not a partition (empty class)");
				}
				for(int i:clazz){
					if(visited[i]){
						throw new IllegalArgumentException(
							"Not a partition (duplicate)");
					}
					visited[i]=true;
				}
			}
			for(int i=0;i<visited.length;i++){
				if(!visited[i]){
					throw new IllegalArgumentException(
						"Not a partition (missing)");
				}
			}
			
			classMap=new int[stateCount][];
			indexMap=new int[stateCount];
			for(int i=0;i<classArray.length;i++){
				for(int state:classArray[i]){
					indexMap[state]=i;
					classMap[state]=classArray[i];
				}
			}
		}
		public int size(){
			return classArray.length;
		}
		public int[] getClass(int state){
			return classMap[state];
		}
		public int[][] getClasses(){
			return classMap;
		}
		public int getIndex(int state){
			return indexMap[state];
		}
		public String toString(){
			StringBuffer ret=new StringBuffer();
			ret.append("{");
			for(int i=0;i<classArray.length;i++){
				ret.append("{");
				for(int k:classArray[i]){
					ret.append(k);
					ret.append(",");
				}
				ret.deleteCharAt(ret.length()-1);
				ret.append("},");
			}
			ret.deleteCharAt(ret.length()-1);
			ret.append("}");
			return ret.toString();
		}
	}
	public String toString(){
		StringBuffer ret=new StringBuffer();
		ret.append("Auto[start=");
		ret.append(startingState);
		ret.append(", accepting=[");
		for(int i=0;i<stateCount;i++){
			if(accepting[i]){
				ret.append(i+",");
			}
		}
		ret.deleteCharAt(ret.length()-1);
		ret.append("], transitions={");
		for(int state=0;state<stateCount;state++){
			for(int letter=0;letter<letterCount;letter++){
				ret.append("("+state+","+letter+")->"+step(state,letter)+", ");
			}
		}
		ret.append("}]");
		return ret.toString();
	}
	public static int getCrossState(Auto a,Auto b,int sa,int sb){
		return sa*b.stateCount+sb;
	}
	private int[] permutation;
	private int name;
	protected Auto canonical(){
		permutation=new int[stateCount];
		name=0;
		Arrays.fill(permutation,-1);
		fill(startingState);
		return permuteStates(permutation,name);
	}
	private void fill(int current){
		if(permutation[current]>=0){
			return;
		}else{
			permutation[current]=name++;
			for(int i=0;i<letterCount;i++){
				fill(step(current,i));
			}
		}
	}
	public Auto permuteStates(int[] perm,int newStateCount){
		boolean[] newAccepting=new boolean[newStateCount];
		for(int i=0;i<stateCount;i++){
			if(perm[i]>=0){
				newAccepting[perm[i]]=accepting[i];
			}
		}
		int[][] newTransitions=new int[newStateCount][letterCount];
		for(int i=0;i<stateCount;i++){
			if(perm[i]>=0){
				for(int k=0;k<letterCount;k++){
					newTransitions[perm[i]][k]=perm[transitions[i][k]];
				}
			}
		}
		return new Auto(newStateCount,letterCount,perm[startingState],
			newAccepting,newTransitions);
	}
	public static Auto cross(Auto a,Auto b,CrossRule rule){
		int stateCount=a.stateCount*b.stateCount;
		if(a.letterCount!=b.letterCount){
			throw new IllegalArgumentException();
		}
		int letterCount=a.letterCount;
		int startingState=getCrossState(a,b,a.startingState,b.startingState);
		boolean[] accepting=new boolean[stateCount];
		int[][] transitions=new int[stateCount][];
		for(int sa=0;sa<a.stateCount;sa++){
			for(int sb=0;sb<b.stateCount;sb++){
				int state=getCrossState(a,b,sa,sb);
				accepting[state]=rule.apply(a.acceptsState(sa),
					b.acceptsState(sb));
				int[] nextStates=new int[letterCount];
				for(int i=0;i<letterCount;i++){
					nextStates[i]=getCrossState(a,b,a.step(sa,i),b.step(sb,i));
				}
				transitions[state]=nextStates;
			}
		}
		return new Auto(stateCount,letterCount,startingState,accepting,
			transitions);
	}
	public static Auto prefix(int[] prefix,int letterCount){
		int stateCount=prefix.length+2;
		int startingState=0;
		boolean[] accepting=new boolean[stateCount];
		
		int[][] transitions=new int[stateCount][letterCount];
		int fail=prefix.length+1;
		int win=prefix.length;
		accepting[win]=true;
		for(int state=0;state<stateCount;state++){
			for(int letter=0;letter<letterCount;letter++){
				int newState;
				if(state<prefix.length){
					if(letter==prefix[state]){
						newState=state+1;
					}else{
						newState=fail;
					}
				}else if(state==win){
					newState=win;
				}else{// fail
					newState=fail;
				}
				transitions[state][letter]=newState;
			}
		}
		return new Auto(stateCount,letterCount,startingState,accepting,
			transitions);
	}
	@Override
	protected Auto getAutoImpl(boolean innerSimplify){
		return this;
	}
	/*
	 * private boolean[] alive; public Auto removeDeadStates(){ alive=new
	 * boolean[stateCount];
	 * 
	 * }
	 */
	public void latex(){
		System.out.println("\\begin{tikzpicture}");
		for(int i=0;i<stateCount;i++){
			double theta=i*Math.PI*2/stateCount;
			double radius=3;
			System.out.println("\\node["+(i==startingState?"initial,":"")
				+"state"+(accepting[i]?",accepting":"")+"] ("+i+") at ("+radius
				*Math.cos(theta)+","+radius*Math.sin(theta)+") {"+i+"};");
		}
		for(int i=0;i<stateCount;i++){
			for(int k=0;k<stateCount;k++){
				StringBuffer label=new StringBuffer();
				int found=0;
				label.append("{");
				for(int j=0;j<letterCount;j++){
					int next=transitions[i][j];
					if(next==k){
						label.append(j);
						label.append(",");
						found++;
					}
				}
				label.deleteCharAt(label.length()-1);
				label.append("}");
				if(found>0){
					System.out.println("\\path[->] ("+i+") edge"
						+(i==k?"[loop above]":"")+" node {"+label.toString()
						+"} ("+k+");");
				}
			}
		}
		System.out.println("\\end{tikzpicture}");
	}
	public static boolean has(int meta,int base){
		return ((meta>>base)&1)==1;
	}
	public static Auto kleene(Auto a){
		if(a.stateCount>=30){
			throw new IllegalArgumentException("Overflow");
		}
		int stateCount=1<<(a.stateCount+1);
		int letterCount=a.letterCount;
		int startingState=1<<a.startingState;
		boolean[] accepting=new boolean[stateCount];
		for(int i=0;i<a.stateCount;i++){
			System.out.println(i+" "+a.accepting[i]);
		}
		for(int i=0;i<stateCount;i++){
			boolean accept=has(i,a.startingState);
			for(int j=0;j<a.stateCount;j++){
				if(a.accepting[j]&&has(i,j)){
					accept=true;
					break;
				}
			}
			System.out.println(Util.binary(i,a.stateCount)+" "+accept);
			accepting[i]=accept;
		}
		int[][] transitions=new int[stateCount][letterCount];
		for(int i=0;i<stateCount;i++){
			for(int j=0;j<letterCount;j++){
				int newState=0;
				for(int k=0;k<a.stateCount;k++){
					if(has(i,k)){
						int next=a.transitions[k][j];
						newState|=1<<next;
					}
					if(a.accepting[k]){
						newState|=startingState;
					}
				}
				transitions[i][j]=newState;
			}
		}
		return new Auto(stateCount,letterCount,startingState,accepting,
			transitions);
	}
	
	public static Auto length(int letterCount,int length,Order order){
		int stateCount=length+2;//one exact state, and one state for larger
		int startingState=0;
		boolean[] accepting=new boolean[stateCount];
		int[][] transitions=new int[stateCount][letterCount];
		for(int i=0;i<stateCount;i++){
			accepting[i]=order.applies(i,length);
		}
		for(int i=0;i<stateCount;i++){
			int nextState=i+1;
			if(nextState==stateCount){
				nextState--;
			}
			for(int j=0;j<letterCount;j++){
				transitions[i][j]=nextState;
			}
		}
		return new Auto(stateCount,letterCount,startingState, accepting, transitions);
	}
}
