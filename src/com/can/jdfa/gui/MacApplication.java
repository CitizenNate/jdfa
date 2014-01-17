package com.can.jdfa.gui;
import java.util.Iterator;

import com.apple.eawt.*;
import com.apple.eawt.AppEvent.QuitEvent;
public class MacApplication implements QuitHandler{
	public static void run(){
		Application a=Application.getApplication();
		MacApplication app=new MacApplication();
		a.setQuitHandler(app);
	}

	@Override
	public void handleQuitRequestWith(final QuitEvent event,final QuitResponse response){
		Iterator<Controller> it=Controller.getControllers().iterator();
		if(it.hasNext()){
			Controller c=it.next();
			c.close(new CloseHandler(){
				@Override
				public void finish(boolean aborted){
					if(!aborted){
						handleQuitRequestWith(event,response);
					}else{
						response.cancelQuit();
					}
				}
			});
		}else{
			response.performQuit();
		}
	}
}
