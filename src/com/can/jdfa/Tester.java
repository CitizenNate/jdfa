package com.can.jdfa;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Tester{
	private int letterCount;
	public Tester(int letterCount){
		this.letterCount=letterCount;
	}
	public void enumerate(int length,Consumer<int[]> cons){
		for(int i=0;i<=length;i++){
			enumerate(new int[i],0,cons);
		}
	}
	private void enumerate(int[] word,int index,Consumer<int[]> cons){
		if(index==word.length){
			cons.consume(word);
		}else{
			for(int letter=0;letter<letterCount;letter++){
				word[index]=letter;
				enumerate(word,index+1,cons);
			}
		}
	}
	public String wordToString(int[] word){
		char[] c=new char[word.length];
		for(int i=0;i<word.length;i++){
			c[i]=toChar(word[i]);
		}
		return new String(c);
	}
	private char toChar(int i){
		if(0<=i&&i<=9){
			return (char) (i+'0');
		}else if(10<=i&&i<=35){
			return (char) (i+'a'-10);
		}else{
			return '?';
		}
	}
	public static void test1(){
		Tester tester=new Tester(2);
		int n=15;
		Auto a=Auto.prefix(repeat(0,n),2);
		Auto b=Auto.prefix(repeat(1,n),2);
		Auto c=Auto.cross(a,b,CrossRule.OR);
		c=c.simplify().canonical();
		Language d=new CrossLanguage(a,b,CrossRule.OR);
		
		tester.testThrough(a,b,n+5);
		
	}
	private void testThrough(final Language a,final Language b,int length){
		final List<String> errors=new ArrayList<String>();
		enumerate(length,new Consumer<int[]>(){
			@Override
			public void consume(int[] input){
				if(a.accepts(input)!=b.accepts(input)){
					errors.add(wordToString(input));
				}
			}
		});
	}
	/*public static void main(String[] args){
		test2();
	}*/
	private static void test2(){
		Tester tester=new Tester(2);
		Auto a=Auto.length(2,3,Order.EQUAL);
		System.out.println(a);
		Language b=new KleeneLanguage(a);
		Auto c=Auto.kleene(a);
		tester.printLanguage(c,2);
	}
	private void printLanguage(final Language b,int length){
		enumerate(length,new Consumer<int[]>(){
			@Override
			public void consume(int[] t){
				if(b.accepts(t)){
					System.out.println(wordToString(t));
				}
			}
		});
	}
	private static int[] repeat(int i,int n){
		int[] ret=new int[n];
		Arrays.fill(ret,i);
		return ret;
	}
}
