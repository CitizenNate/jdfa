package com.can.jdfa.gui;

import java.awt.Component;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

import com.can.jdfa.Util;

public class Controller{
	
	private static final String kMoveLeft="kMoveLeft";
	private static final String kMoveRight="kMoveRight";
	private static final String kMoveUp="kMoveUp";
	private static final String kMoveDown="kMoveDown";
	
	private static final String kNewState="kNewState";
	private static final String kCycleState="kCycleState";
	private static final String kCycleTransition="kCycleTransition";
	private static final String kNewTransition="kNewTransition";
	private static final String kDelete="kDelete";
	private static final String kLabel="kLabel";
	private static final String kSwap="kSwap";
	private static final String kBendRight="kBendRight";
	private static final String kBendLeft="kBendLeft";
	private static final String kRingPlus="kRingPlus";
	private static final String kRingMinus="kRingMinus";

	private final View view;
	private final Model model;
	private JFrame frame;
	private JMenuItem undoItem;
	private JMenuItem redoItem;
	private UndoManager undoManager;
	private static Set<Controller> controllers=Collections
		.synchronizedSet(new HashSet<Controller>());
	private boolean modified;
	public Controller(View view){
		controllers.add(this);
		this.view=view;
		this.model=view.getModel();
		this.undoManager=new UndoManager();
		frame=new JFrame();
		frame.getContentPane().add(view);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener();
		setTitle(view.getFile());
		setupKeys();
		setupMenu();
		setupActions();
		
		frame.pack();
		frame.setVisible(true);
	}
	public static Set<Controller> getControllers(){
		synchronized(controllers){
			return new HashSet<Controller>(controllers);
		}
	}
	private void addWindowListener(){
		frame.addWindowListener(new WindowAdapter(){
			@Override
			public void windowClosing(WindowEvent e){
				close(null);
			}
		});
	}
	private void setTitle(File file){
		if(file==null){
			frame.setTitle("Untitled");
		}else{
			frame.setTitle(file.getName());
		}
	}
	private void setupKeys(){
		InputMap input=view.getInputMap();
		
		input.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT,0),kMoveLeft);
		input.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT,0),kMoveRight);
		input.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP,0),kMoveUp);
		input.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN,0),kMoveDown);
		input.put(KeyStroke.getKeyStroke(KeyEvent.VK_1,0),kCycleState);
		input.put(KeyStroke.getKeyStroke(KeyEvent.VK_2,0),kCycleTransition);
		input.put(KeyStroke.getKeyStroke(KeyEvent.VK_Q,0),kNewState);
		input.put(KeyStroke.getKeyStroke(KeyEvent.VK_W,0),kNewTransition);
		input.put(KeyStroke.getKeyStroke(KeyEvent.VK_E,0),kDelete);
		input.put(KeyStroke.getKeyStroke(KeyEvent.VK_A,0),kLabel);
		input.put(KeyStroke.getKeyStroke(KeyEvent.VK_S,0),kBendLeft);
		input.put(KeyStroke.getKeyStroke(KeyEvent.VK_D,0),kBendRight);
		input.put(KeyStroke.getKeyStroke(KeyEvent.VK_X,0),kRingPlus);
		input.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z,0),kRingMinus);
		input.put(KeyStroke.getKeyStroke(KeyEvent.VK_C,0),kSwap);
	}
	private void setupMenu(){
		
		JMenuBar bar=new JMenuBar();
		
		JMenu file=new JMenu("File");
		addMenuItem(file,"New",KeyEvent.VK_N,new FileNew());
		addMenuItem(file,"Open...",KeyEvent.VK_O,new FileOpen());
		JMenu openRecent=new JMenu("Open Recent");
		file.add(openRecent);
		file.addSeparator();
		addMenuItem(file,"Close",KeyEvent.VK_W,new FileClose());
		addMenuItem(file,"Save",KeyEvent.VK_S,new FileSave());
		addMenuItem(file,"Properties...",KeyEvent.VK_I,new FileProperties());
		bar.add(file);
		
		JMenu edit=new JMenu("Edit");
		undoItem=addMenuItem(edit,"Undo",KeyEvent.VK_Z,new Undo());
		redoItem=addMenuItem(edit,"Redo",KeyEvent.VK_Y,new Redo());
		updateButtons();
		if(System.getProperty("os.name").equals("Mac OS X")){
			redoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z,
				KeyEvent.META_DOWN_MASK|KeyEvent.SHIFT_DOWN_MASK));
		}
		bar.add(edit);
		
		frame.setJMenuBar(bar);
	}
	private JMenuItem addMenuItem(JMenu menu,String label,int key,
		ActionListener listener){
		JMenuItem menuItem=new JMenuItem(label);
		menuItem.addActionListener(listener);
		if(key!=-1){
			int command=Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
			menuItem.setAccelerator(KeyStroke.getKeyStroke(key,command));
		}
		menu.add(menuItem);
		return menuItem;
	}
	private void setupActions(){
		ActionMap action=view.getActionMap();
		action.put(kMoveLeft,new MoveAction(-1,0));
		action.put(kMoveRight,new MoveAction(1,0));
		action.put(kMoveUp,new MoveAction(0,-1));
		action.put(kMoveDown,new MoveAction(0,1));
		action.put(kNewState,new NewState());
		action.put(kCycleState,new Cycle(true));
		action.put(kCycleTransition,new Cycle(false));
		action.put(kNewTransition,new NewTransition());
		action.put(kDelete,new Delete());
		action.put(kLabel,new Label());
		action.put(kBendLeft,new Bend(1));
		action.put(kBendRight,new Bend(-1));
		action.put(kRingMinus,new RingCountChange(-1));
		action.put(kRingPlus,new RingCountChange(1));
		action.put(kSwap,new Swap());
	}
	public boolean save(){
		if(view.getFile()==null){
			FileDialog dialog=new FileDialog(frame,"Save",FileDialog.SAVE);
			dialog.setVisible(true);
			if(dialog.getFile()==null){
				return false;
			}
			File file=dialog.getFiles()[0];
			setTitle(file);
			view.setFile(file);
		}
		try{
			ModelWriter.store(view.getFile(),model);
			modified=false;
			TexWriter.store(changeExtension(view.getFile(),".tex"),model);
			LanguageWriter.store(changeExtension(view.getFile(),".txt"),model);
			return true;
		}catch(IOException e){
			displayException(e);
			return false;
		}
	}
	private File changeExtension(File file,String newExtension){
		File directory=file.getParentFile();
		String name=file.getName();
		int point=name.lastIndexOf('.');
		if(point==-1){
			name+=newExtension;
		}else{
			name=name.substring(0,point)+newExtension;
		}
		return new File(directory,name);
	}
	public void edit(ModelEdit edit){
		modified=true;
		edit._redo();
		undoManager.addEdit(edit);
		updateButtons();
	}
	private void updateButtons(){
		undoItem.setEnabled(undoManager.canUndo());
		redoItem.setEnabled(undoManager.canRedo());
	}
	private void addState(State s){
		model.addState(s);
		view.setSelectedState(s);
		view.repaint();
	}
	private void removeState(State s){
		selectNext(true);
		if(view.getSelectedState()==s){
			view.setSelectedState(null);
		}
		model.removeState(s);
		view.repaint();
	}
	public class NewState extends AbstractAction{
		@Override
		public void actionPerformed(ActionEvent e){
			final State s=new State(1,1," ",1);
			edit(new ModelEdit(){
				@Override
				public void _undo(){
					removeState(s);
				}
				@Override
				public void _redo(){
					addState(s);
				}
			});
			
		}
	}
	public class MoveAction extends AbstractAction{
		private final int dx, dy;
		public MoveAction(int dx,int dy){
			this.dx=dx;
			this.dy=dy;
		}
		@Override
		public void actionPerformed(ActionEvent e){
			final Transition t=view.getSelectedTransition();
			final State state=view.getSelectedState();
			if(t!=null){
				edit(new ModelEdit(){
					@Override
					public void _undo(){
						editTo(t,t.getTo().getX()-dx,t.getTo().getY()-dy);
						view.repaint();
					}
					@Override
					public void _redo(){
						editTo(t,t.getTo().getX()+dx,t.getTo().getY()+dy);
						view.repaint();
					}
				});
				
			}else if(state!=null){
				edit(new ModelEdit(){
					@Override
					public void _undo(){
						state.setX(state.getX()-dx);
						state.setY(state.getY()-dy);
						view.repaint();
					}
					@Override
					public void _redo(){
						state.setX(state.getX()+dx);
						state.setY(state.getY()+dy);
						view.repaint();
					}
				});
				
			}
			
		}
	}
	public class Cycle extends AbstractAction{
		private boolean main;
		public Cycle(boolean main){
			this.main=main;
		}
		@Override
		public void actionPerformed(ActionEvent e){
			selectNext(main);
			view.repaint();
		}
		
	}
	private <T>T getNext(Collection<T> states,T s,boolean empty){
		T ret;
		Iterator<T> it=states.iterator();
		if(s==null){
			if(it.hasNext()){
				ret=it.next();
			}else{
				ret=null;
			}
		}else{
			while(true){
				if(!it.hasNext()){
					throw new IllegalArgumentException("Not found "+s+" in "
						+states);
				}
				if(it.next()==s){
					if(it.hasNext()){
						ret=it.next();
					}else{
						if(empty){
							ret=null;
						}else{
							it=states.iterator();
							if(it.hasNext()){
								ret=it.next();
							}else{
								ret=null;
							}
						}
						
					}
					break;
				}
			}
			
		}
		return ret;
	}
	
	public void editTo(Transition t,int x,int y){
		boolean updated=false;
		// TODO should this be done in reverse?
		for(State s:model.getStates()){
			if(s.getX()==x&&s.getY()==y){
				t.setTo(s);
				updated=true;
				break;
			}
		}
		if(!updated){
			t.setTo(new EmptyTarget(x,y));
		}
	}
	public void selectNext(boolean main){
		if(main){
			view.setSelectedState(getNext(model.getStates(),
				view.getSelectedState(),false));
		}else{
			State s=view.getSelectedState();
			if(s!=null){
				view.setSelectedTransition(getNext(model.getTransitions(s),
					view.getSelectedTransition(),true));
			}
		}
	}
	public class NewTransition extends AbstractAction{
		@Override
		public void actionPerformed(ActionEvent e){
			final State s=view.getSelectedState();
			if(s!=null){
				final Transition t=new Transition(s,s," ",false,0);
				edit(new ModelEdit(){
					@Override
					public void _undo(){
						removeTransition(t);
					}
					@Override
					public void _redo(){
						addTransition(t);
					}
				});
				
			}
			
		}
	}
	protected void addTransition(Transition t){
		view.setSelectedTransition(t);
		model.addTransition(t);
		view.repaint();
	}
	
	protected void removeTransition(Transition t){
		selectNext(false);
		if(view.getSelectedTransition()==t){
			selectNext(false);
		}
		model.removeTransition(t);
		view.repaint();
	}
	public class Delete extends AbstractAction{
		@Override
		public void actionPerformed(ActionEvent arg0){
			final Transition t=view.getSelectedTransition();
			final State s=view.getSelectedState();
			if(t!=null){
				edit(new ModelEdit(){
					@Override
					public void _undo(){
						addTransition(t);
					}
					@Override
					public void _redo(){
						removeTransition(t);
					}
				});
			}else if(s!=null){
				final List<Transition> removed=new ArrayList<Transition>();
				for(Transition t2:model.getTransitions()){
					if(t2.getFrom()==s||t2.getTo()==s){
						removed.add(t2);
					}
				}
				edit(new ModelEdit(){
					@Override
					public void _undo(){
						addState(s);
						for(Transition t:removed){
							addTransition(t);
						}
					}
					@Override
					public void _redo(){
						
						removeState(s);
					}
				});
			}
			view.repaint();
		}
	}
	public class Label extends AbstractAction{
		@Override
		public void actionPerformed(ActionEvent e){
			Transition t=view.getSelectedTransition();
			State s=view.getSelectedState();
			if(t!=null){
				getLabel(t);
			}else if(s!=null){
				getLabel(s);
			}
		}
		private void getLabel(final Labeled s){
			new Thread(){
				public void run(){
					final String label=JOptionPane.showInputDialog(
						Controller.this.view,"Select a Label","Select a label",
						JOptionPane.PLAIN_MESSAGE);
					if(label!=null){
						SwingUtilities.invokeLater(new Runnable(){
							@Override
							public void run(){
								final String oldLabel=s.getLabel();
								edit(new ModelEdit(){
									@Override
									public void _undo(){
										s.setLabel(oldLabel);
										view.repaint();
									}
									@Override
									public void _redo(){
										s.setLabel(label);
										view.repaint();
									}
								});
							}
						});
					}
				}
			}.start();
		}
	}
	public class Bend extends AbstractAction{
		private int bend;
		private int factor=10;
		public Bend(int bend){
			this.bend=bend;
		}
		@Override
		public void actionPerformed(ActionEvent e){
			final Transition t=view.getSelectedTransition();
			if(t!=null){
				edit(new ModelEdit(){
					@Override
					public void _undo(){
						t.setBend(t.getBend()-bend*Math.PI/180*factor);
						view.repaint();
					}
					@Override
					public void _redo(){
						t.setBend(t.getBend()+bend*Math.PI/180*factor);
						view.repaint();
					}
				});
			}
		}
	}
	public class FileNew implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e){
			newFile(null);
		}
	}
	public class FileOpen implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e){
			final FileDialog dialog=new FileDialog((Frame) null,"Open",
				FileDialog.LOAD);
			dialog.getFile();
			SwingUtilities.invokeLater(new Runnable(){
				public void run(){
					dialog.setVisible(true);
					if(dialog.getFile()!=null){
						File file=dialog.getFiles()[0];
						newFile(file);
					}
				}
			});
		}
	}
	public class FileClose implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e){
			close(null);
		}
		
	}
	public void close(CloseHandler handlerIn){
		if(handlerIn==null){
			handlerIn=new CloseHandler(){
				@Override
				public void finish(boolean aborted){
				}};
		}
		final CloseHandler handler=handlerIn;
		if(!modified){
			performClose();
			handler.finish(false);
			return;
		}
		new Thread(){
			public void run(){
				int choice=JOptionPane.showOptionDialog(Controller.this.frame,
					"Do you want to save?","",JOptionPane.DEFAULT_OPTION,
					JOptionPane.WARNING_MESSAGE,null,new String[]{"Save",
						"Cancel","Don't Save"},"Save");
				if(choice==0){
					if(save()){
						performClose();
						handler.finish(false);
					}
				}else if(choice==1|| choice==-1){
					handler.finish(true);
					return;
				}else if(choice==2){
					performClose();
					handler.finish(false);
				}else{
					throw new IllegalArgumentException();
				}
			}
		}.start();
	}
	protected void performClose(){
		frame.dispose();
		controllers.remove(this);
	}
	public class FileSave implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e){
			new Thread(){
				public void run(){
					save();
				}
			}.start();
		}
	}
	public class FileProperties implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e){
			// TODO file properties
		}
	}
	public class RingCountChange extends AbstractAction{
		private final int delta;
		public RingCountChange(int delta){
			this.delta=delta;
		}
		@Override
		public void actionPerformed(ActionEvent e){
			final State s=view.getSelectedState();
			if(s!=null){
				final int oldR=s.getRings();
				final int newR=Math.max(oldR+delta,0);
				edit(new ModelEdit(){
					@Override
					public void _undo(){
						s.setRings(oldR);
						view.repaint();
					}
					@Override
					public void _redo(){
						s.setRings(newR);
						view.repaint();
					}});
			}
		}
	}
	public class Swap extends AbstractAction{

		@Override
		public void actionPerformed(ActionEvent e){
			final Transition t=view.getSelectedTransition();
			if(t!=null){
				edit(new ModelEdit(){
					@Override
					public void _undo(){
						t.swap();
						view.repaint();
					}
					@Override
					public void _redo(){
						t.swap();
						view.repaint();
					}
				});
			}
		}
		
	}
	public class Undo implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e){
			undoManager.undo();
			updateButtons();
		}
	}
	public class Redo implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e){
			undoManager.redo();
			updateButtons();
		}
	}
	
	public static void main(String[] args){
		try{
			System.setProperty("apple.laf.useScreenMenuBar","true");
			System.setProperty(
				"com.apple.mrj.application.apple.menu.about.name","Test");
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}catch(ClassNotFoundException e){
			System.out.println("ClassNotFoundException: "+e.getMessage());
		}catch(InstantiationException e){
			System.out.println("InstantiationException: "+e.getMessage());
		}catch(IllegalAccessException e){
			System.out.println("IllegalAccessException: "+e.getMessage());
		}catch(UnsupportedLookAndFeelException e){
			System.out.println("UnsupportedLookAndFeelException: "
				+e.getMessage());
		}
		MacApplication.run();
		newFile(null);
	}
	
	public static void newFile(File file){
		try{
			Model model;
			if(file==null){
				model=new Model();
				State q1=new State(3,3," ",1);
				State q2=new State(10,10," ",1);
				model.addState(q1);
				model.addState(q2);
				for(double i=0;i<Math.PI;i+=Math.PI/3){
					model.addTransition(new Transition(q1,q2,"a",false,i));
				}
			}else{
				model=ModelReader.readModel(file);
			}
			new Controller(new View(model,file));
		}catch(IOException ioe){
			displayException(ioe);
		}
	}
	private static void displayException(IOException ioe){
		JOptionPane.showMessageDialog(null,ioe.getLocalizedMessage());
	}
	private abstract class ModelEdit extends
		javax.swing.undo.AbstractUndoableEdit{
		public final void undo(){
			super.undo();
			_undo();
		}
		public final void redo(){
			super.redo();
			_redo();
		}
		public abstract void _undo();
		public abstract void _redo();
	}
}
