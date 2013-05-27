package JOGL2DLinePlotSample;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;

public class JOGL2DLinePlot extends JOGL2DPlot{
	
	private ArrayList<Points> data = new ArrayList<Points>();

	public JOGL2DLinePlot(String name, float xmin, float xmax, float ymin,
			float ymax, String xlabel, String ylabel, String format) {
		super(name, xmin, xmax, ymin, ymax, xlabel, ylabel, format);
		// TODO Auto-generated constructor stub
	}

	abstract public class Points{
		
		protected class Mesh{
			public int x;
			public float dx;

			public Mesh(int xn){
				x=xn;
				dx = (rect.xMax-rect.xMin)/(float)x;
			}
		}
		
		public float[] points;
		public float[] color = new float[]{0.0f,0.0f,0.0f,1.0f};
		
		public Mesh mesh;
		public boolean isShown=true;

		public Points(int xn, float[] col){
			mesh = new Mesh(xn-1);
			points = new float[xn];
			color = col;
			setData();
		}
		
		public Points(int xn){
			mesh = new Mesh(xn-1);
			points = new float[xn];
			setData();
		}
		
		public void setData(){
			for(int i=0; i<mesh.x+1; ++i){
				points[i]=func(i*mesh.dx+rect.xMin);
			}
		}
		
		abstract protected float func(float x);
	}
	
	public void addPoints(Points pts){
		data.add(pts);
	}

	@Override
	protected void GLinitialize(GLAutoDrawable drawable) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void render(GLAutoDrawable drawable) {
		// TODO Auto-generated method stub
		gl.glLineWidth(2f);
        for (Points pts : data){
        	if (pts.isShown && pts.points.length>1){
        		for(int i = 0; i < pts.points.length-1; ++i){
        			gl.glBegin (GL.GL_LINES);
        			gl.glColor4f(pts.color[0],pts.color[1],pts.color[2],pts.color[3]);
        			gl.glVertex2f(pts.mesh.dx*i+rect.xMin,pts.points[i]);
        			gl.glVertex2f(pts.mesh.dx*(i+1)+rect.xMin,pts.points[i+1]);
        			gl.glEnd();
        		}	
        	}
        }
        gl.glLineWidth(1f);
	}

	@Override
	protected void fileOpenHandler(ActionEvent arg0, File file) {
		// TODO Auto-generated method stub
		
	}
}
