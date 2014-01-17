package com.can.jdfa;
import java.lang.reflect.Array;


public class Util{

	public static int[][] deepClone(int[][] a){
		int[][] ret=new int[a.length][];
		for(int i=0;i<ret.length;i++){
			ret[i]=a[i].clone();
		}
		return ret;
	}
	public static String toString(Object o){
		return toStringBuffer(o,new StringBuffer()).toString();
	}
	private static StringBuffer toStringBuffer(Object o,StringBuffer input){
		if(o.getClass().isArray()){
			input.append("{");
			for(int i=0;i<Array.getLength(o);i++){
				toStringBuffer(Array.get(o,i),input);
				input.append(",");
			}
			input.deleteCharAt(input.length()-1);
			input.append("}");
		}else{
			input.append(o.toString());
		}
		return input;
	}
	public static String binary(int i,int len){
		StringBuffer ret=new StringBuffer(len);
		String str=Integer.toString(i,2);
		for(int k=str.length();k<len;k++){
			ret.append("0");
		}
		ret.append(str);
		return ret.toString();
	}
	
}
