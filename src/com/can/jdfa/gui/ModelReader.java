package com.can.jdfa.gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

public class ModelReader{
	public static Model readModel(File file) throws IOException{
		JsonParser parser=new JsonParser();
		JsonObject root=parser.parse(
			new InputStreamReader(new FileInputStream(file),"UTF-8"))
			.getAsJsonObject();
		JsonArray stateArray=root.get("states").getAsJsonArray();
		Model model=new Model();
		State[] states=new State[stateArray.size()];
		for(int i=0;i<stateArray.size();i++){
			JsonObject stateObject=stateArray.get(i).getAsJsonObject();
			State state=new State(stateObject.get("x").getAsInt(),stateObject
				.get("y").getAsInt(),stateObject.get("label").getAsString(),
				getAsInt(stateObject.get("rings"),1));
			model.addState(state);
			states[i]=state;
		}
		JsonArray transArray=root.get("transitions").getAsJsonArray();
		for(int i=0;i<transArray.size();i++){
			JsonObject transObject=transArray.get(i).getAsJsonObject();
			State from=states[transObject.get("from").getAsInt()];
			Target to;
			JsonElement toElement=transObject.get("to");
			if(toElement.isJsonObject()){
				JsonObject toObject=toElement.getAsJsonObject();
				to=new EmptyTarget(toObject.get("x").getAsInt(),toObject.get("y").getAsInt());
			}else{
				to=states[toElement.getAsInt()];
			}
			String label=transObject.get("label").getAsString();
			boolean swap=transObject.get("swap").getAsBoolean();
			double bend=transObject.get("bend").getAsDouble();
			Transition trans=new Transition(from,to,label,swap,bend);
			model.addTransition(trans);
		}
		return model;
	}

	private static int getAsInt(JsonElement e,int def){
		if(e==null){
			return def;
		}else{
			return e.getAsInt();
		}
	}
}
