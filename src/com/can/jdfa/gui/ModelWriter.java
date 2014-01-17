package com.can.jdfa.gui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.stream.JsonWriter;

public class ModelWriter{
	private JsonWriter writer;
	public ModelWriter(JsonWriter writer) {
		this.writer=writer;
	}
	public static Map<State,Integer> getStateMap(Model model){
		Map<State,Integer> map=new HashMap<State,Integer>();
		int counter=0;
		for(State state:model.getStates()){
			map.put(state,counter);
			counter++;
		}
		return map;
	}
	public void writeModel(Model model) throws IOException{
		Map<State,Integer> map=getStateMap(model);
		writer.beginObject();
		writer.name("states");
		writer.beginArray();
		for(State s:model.getStates()){
			writer.beginObject();
			writer.name("id").value(map.get(s));
			writer.name("x").value(s.getX());
			writer.name("y").value(s.getY());
			writer.name("label").value(s.getLabel());
			writer.name("rings").value(s.getRings());
			writer.endObject();
		}
		writer.endArray();
		writer.name("transitions");
		writer.beginArray();
		for(Transition t:model.getTransitions()){
			writer.beginObject();
			writer.name("from").value(map.get(t.getFrom()));
			Integer id=map.get(t.getTo());
			if(id==null){
				writer.name("to");
				writer.beginObject();
				writer.name("x").value(t.getTo().getX());
				writer.name("y").value(t.getTo().getY());
				writer.endObject();
			}else{
				writer.name("to").value(id);
			}
			writer.name("label").value(t.getLabel());
			writer.name("bend").value(t.getBend());
			writer.name("swap").value(t.isSwap());
			writer.endObject();
		}
		writer.endArray();
		writer.endObject();
	}
	public static void store(File file,Model model) throws IOException{
		ModelWriter writer=new ModelWriter(new JsonWriter(new OutputStreamWriter(
			new FileOutputStream(file),"UTF-8")));
		
		writer.writeModel(model);
		writer.close();
	}
	private void close() throws IOException{
		writer.close();
	}
}
