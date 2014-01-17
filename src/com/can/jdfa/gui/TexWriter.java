package com.can.jdfa.gui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Map;

public class TexWriter{
	private PrintWriter writer;
	public TexWriter(Writer writer){
		this.writer=new PrintWriter(writer);
	}
	
	public static void store(File file,Model model) throws IOException{
		TexWriter w=new TexWriter(new OutputStreamWriter(new FileOutputStream(
			file),"UTF-8"));
		w.write(model);
		w.close();
	}
	
	private void close() throws IOException{
		writer.close();
	}
	
	private void write(Model model){
		writer.println("\\begin{tikzpicture}["
			+"->,>=stealth',shorten >=1pt,auto,node distance=2.8cm,semithick"
			+",y=-1cm,x=1cm]");
		// writer.println("\tikzstyle{every state}=[fill=red,draw=none,text=white]");
		Map<State,Integer> stateMap=ModelWriter.getStateMap(model);
		for(State state:model.getStates()){
			String param="state";
			if(state.getRings()==0){
				param+=",draw=none";
			}else if(state.getRings()==2){
				param+=",accepting";
			}
			writer.println("\\node["+param+"] ("+stateMap.get(state)+") at ("
				+state.getX()+","+state.getY()+") {$"+state.getLabel()+"$};");
		}
		writer.println("\\path");
		for(Transition t:model.getTransitions()){
			Integer from=stateMap.get(t.getFrom());
			Integer to=stateMap.get(t.getTo());
			if(to!=null){
				String param;
				int bendDeg=(int) (t.getBend()/Math.PI*180);
				if(t.isSelf()){
					int rounded=(bendDeg/90)%4;
					if(rounded<0){
						rounded+=4;
					}
					param="[loop ";
					switch(rounded){
						case 0:
							param+="right";
							break;
						case 1:
							param+="below";
							break;
						case 2:
							param+="left";
							break;
						case 3:
							param+="above";
							break;
						default:
							throw new IllegalArgumentException();
					}
				}else{
					param="[bend left="+bendDeg;
				}
				if(t.isSwap()){
					param+=",swap";
				}
				param+="]";
				writer.println("("+from+") edge "+param+" node {$"+t.getLabel()
					+"$} ("+to+")");
			}
		}
		writer.println(";");
		writer.print("\\end{tikzpicture}");
		
	}
	public static void main(String[] args) throws IOException{
		LanguageWriter
			.store(
				new File(
					"/Users/nathan/Documents/cmu/15453-flac/homework1/prob1a.tex"),
				ModelReader
					.readModel(new File(
						"/Users/nathan/Documents/cmu/15453-flac/homework1/prob1a.fsm")));
	}
}
