package com.can.jdfa.gui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;

import com.can.jdfa.Consumer;

public class LanguageWriter{
	private PrintWriter writer;
	public LanguageWriter(Writer writer){
		this.writer=new PrintWriter(writer);
	}
	
	public static void store(File file,Model model) throws IOException{
		LanguageWriter writer=new LanguageWriter(new OutputStreamWriter(
			new FileOutputStream(file),"UTF-8"));
		writer.write(model);
		writer.close();
	}
	
	public void close() throws IOException{
		writer.close();
	}
	
	public void write(Model model){
		int max=10;
		writer.println("Language (accepted words less than "+max+" letters)");
		try{
			model.getAutomaton().getLanguage(max,new Consumer<String>(){
				@Override
				public void consume(String t){
					writer.println(t);
				}
			});
		}catch(RuntimeException e){
			e.printStackTrace(writer);
		}
	}
}
