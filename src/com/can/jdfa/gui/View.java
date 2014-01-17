package com.can.jdfa.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D.Double;
import java.io.File;
import java.util.Iterator;

import javax.swing.JComponent;

public class View extends JComponent{
	private Model model;
	private final static int radius=20;
	private final static int cellSize=40;
	private final static Dimension arrowSize=new Dimension(10,10);
	private final static int border=2;
	private State selectedState;
	private Transition selectedTransition;
	private File file;
	public View(Model model,File file){
		this.setFile(file);
		this.model=model;
		Iterator<State> it=model.getStates().iterator();
		if(it.hasNext()){
			selectedState=it.next();
		}
		setPreferredSize(new Dimension(500,500));
	}
	public void paintComponent(Graphics g){
		Graphics2D g2=(Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_RENDERING,
			RenderingHints.VALUE_RENDER_QUALITY);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
			RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(Color.black);
		g2.setStroke(new BasicStroke(2));
		for(State state:model.getStates()){
			Color color;
			if(state==selectedState){
				color=Color.green.darker().darker();
			}else{
				color=Color.black;
			}
			
			int x=state.getX()*cellSize;
			int y=state.getY()*cellSize;
			
			g.setColor(color);
			int ringCount=state.getRings();
			if(ringCount==0&&state==selectedState){
				g.drawRect(x-radius,y-radius,radius*2,radius*2);
			}
			for(int i=0;i<ringCount;i++){
				int f=4;
				g.drawOval(x-radius+i*f,y-radius+i*f,radius*2-i*2*f,radius*2-i
					*2*f);
			}
			String label=state.getLabel();
			g.setColor(Color.white);
			drawLabel(g2,label,null,color,x,y,false);
		}
		for(Transition transition:model.getTransitions()){
			Color color;
			if(transition==selectedTransition){
				color=Color.green.darker().darker();
			}else{
				color=Color.black;
			}
			g.setColor(color);
			double x0=transition.getFrom().getX()*cellSize;
			double y0=transition.getFrom().getY()*cellSize;
			double x3=transition.getTo().getX()*cellSize;
			double y3=transition.getTo().getY()*cellSize;
			double dx=x3-x0;
			
			double dy=y3-y0;
			double dis=Math.sqrt(dx*dx+dy*dy);
			double theta;
			double bend;
			if(dis==0){
				theta=transition.getBend();
				bend=Math.PI/180*(90-30);
			}else{
				theta=Math.atan2(dy,dx)+Math.PI;
				bend=transition.getBend();
			}
			
			double rx0=-Math.cos(theta-bend);
			double ry0=-Math.sin(theta-bend);
			double rx1=Math.cos(theta+bend);
			double ry1=Math.sin(theta+bend);
			x0+=radius*rx0;
			y0+=radius*ry0;
			x3+=(radius+arrowSize.width)*rx1;
			y3+=(radius+arrowSize.width)*ry1;
			// draw curve
			double factor;
			if(dis==0){
				factor=radius*2;
			}else{
				double dx2=x0-x3;
				double dy2=y0-y3;
				factor=Math.sqrt(dx2*dx2+dy2*dy2)/2;
			}
			Path2D path=new Path2D.Double();
			double x1=x0+factor*rx0;
			double y1=y0+factor*ry0;
			double x2=x3+factor*rx1;
			double y2=y3+factor*ry1;
			path.moveTo(x0,y0);
			path.curveTo(x1,y1,x2,y2,x3,y3);
			g2.draw(path);
			// draw label
			// double centerX=path.getBounds().getCenterX();
			// double centerY=path.getBounds().getCenterY();
			Point2D p0=new Point2D.Double(x0,y0);
			Point2D p1=new Point2D.Double(x1,y1);
			Point2D p2=new Point2D.Double(x2,y2);
			Point2D p3=new Point2D.Double(x3,y3);
			Point2D p11=midpoint(p0,p1);
			Point2D p21=midpoint(p1,p2);
			Point2D p31=midpoint(p2,p3);
			Point2D p22=midpoint(p11,p21);
			Point2D p32=midpoint(p21,p31);
			Point2D center=midpoint(p22,p32);
			double shift=transition.isSwap()?-10:10;
			center=new Point2D.Double(center.getX()+shift*Math.cos(theta+Math.PI/2),center.getY()+shift*Math.sin(theta+Math.PI/2));
			// draw arrowhead
			Graphics2D arrow=(Graphics2D) g2.create();
			arrow.translate((int) x3,(int) y3);
			arrow.rotate(theta+bend);
			
			arrow.fillPolygon(new int[]{-arrowSize.width,0,0},new int[]{0,
				-arrowSize.height/2,arrowSize.height/2},3);
			drawLabel(g2,transition.getLabel(),color,Color.white,center.getX(),
				center.getY(),true);
		}
	}
	private void drawLabel(Graphics2D g,String label,Color fill,Color text,
		double x,double y,boolean hasBorder){
		FontMetrics m=g.getFontMetrics();
		Rectangle2D bounds=m.getStringBounds(label,g);
		if(hasBorder){
			g.setColor(fill);
			Rectangle2D rect=new Rectangle2D.Double(x-bounds.getWidth()/2
				-border,y-border-bounds.getHeight()/2,bounds.getWidth()+border
				*2,bounds.getHeight()+border*2);
			g.fill(rect);
			g.draw(rect);
		}
		g.setColor(text);
		g.drawString(label,(int) (x-bounds.getX()-bounds.getWidth()/2),(int) (y
			-bounds.getY()-bounds.getHeight()/2));
	}
	private Point2D midpoint(Point2D p0,Point2D p1){
		return new Point2D.Double((p0.getX()+p1.getX())/2,
			(p0.getY()+p1.getY())/2);
	}
	public Model getModel(){
		return model;
	}
	public State getSelectedState(){
		return selectedState;
	}
	public void setSelectedState(State selectedState){
		this.selectedState=selectedState;
		selectedTransition=null;
	}
	public Transition getSelectedTransition(){
		return selectedTransition;
	}
	public void setSelectedTransition(Transition selectedTransition){
		if(selectedTransition==null){
			this.selectedTransition=null;
		}else{
			this.selectedState=selectedTransition.getFrom();
			this.selectedTransition=selectedTransition;
		}
	}
	public File getFile(){
		return file;
	}
	public void setFile(File file){
		this.file=file;
	}
}
