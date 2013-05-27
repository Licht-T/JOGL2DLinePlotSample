package JOGL2DLinePlotSample;

import javax.swing.JOptionPane;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.text.DecimalFormat;

import javax.imageio.ImageIO;
import javax.media.opengl.*;
import javax.media.opengl.awt.GLJPanel;
import javax.media.opengl.fixedfunc.GLPointerFunc;
import javax.media.opengl.glu.GLU;


import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.util.gl2.GLUT;
import com.jogamp.opengl.util.awt.Screenshot;

public abstract class JOGL2DPlot implements GLEventListener{
	
    protected class fileOpenSelectorAction extends AbstractAction{
		/**
		 * 
		 */
		private static final long serialVersionUID = 3L;
    	fileOpenSelectorAction(){
    	}
    	
		@Override
		public void actionPerformed(ActionEvent arg0) {
			JFileChooser filechooser = new JFileChooser(){
				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				@Override
				public void approveSelection(){
					File f = getSelectedFile();
					if(!f.canRead() && getDialogType() == OPEN_DIALOG){
						JOptionPane.showMessageDialog(this, 
													"Cannot read from this file.Choose another.", 
													"Can't read.",
													JOptionPane.ERROR_MESSAGE);
					}
					else {
						super.approveSelection();
					}
				}
			};
			int selected = filechooser.showOpenDialog((Component) arg0.getSource());
			if (selected == JFileChooser.APPROVE_OPTION){
				File file = filechooser.getSelectedFile();
				boolean result = false;
				if(file.canRead()){
					fileOpenHandler(arg0, file);
				}
				else {

				}

			}
		    else if (selected == JFileChooser.CANCEL_OPTION){
		    	System.out.println("Canceled.");
		    }
		    else if (selected == JFileChooser.ERROR_OPTION){
		    	System.out.println("Error.");
		    }
		}
    	
    }
	
	protected class fileSaveSelectorAction extends AbstractAction{
		/**
		 * 
		 */
		private static final long serialVersionUID = 3L;
		fileSaveSelectorAction(){
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			JFileChooser filechooser = new JFileChooser(){
				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				@Override
				public void approveSelection(){
					File f = getSelectedFile();
					if(f.exists() && getDialogType() == SAVE_DIALOG){
						int result = JOptionPane.showConfirmDialog(this,"The file exists, overwrite?","Existing file",JOptionPane.YES_NO_CANCEL_OPTION);
						switch(result){
						case JOptionPane.YES_OPTION:
							super.approveSelection();
							return;
						case JOptionPane.NO_OPTION:
							return;
						case JOptionPane.CLOSED_OPTION:
							return;
						case JOptionPane.CANCEL_OPTION:
							cancelSelection();
							return;
						}
					}
					super.approveSelection();
				}
			};
			int selected = filechooser.showSaveDialog((Component) arg0.getSource());
			if (selected == JFileChooser.APPROVE_OPTION){
				File file = filechooser.getSelectedFile();
				System.out.println("Approved."+file.getAbsolutePath());
				boolean result = false;
				if(!file.canWrite()){
					System.out.println("Not Writable.");

					try {
						result=file.createNewFile();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						result = false;
					}
				}

				result = false;

				BufferedImage writeImage = buf;		    			
				try {
					result = ImageIO.write(writeImage, "PNG", file);
				} catch (Exception e) {
					e.printStackTrace();
					result = false;
				}
			}
			else if (selected == JFileChooser.CANCEL_OPTION){
				System.out.println("Canceled.");
			}
			else if (selected == JFileChooser.ERROR_OPTION){
				System.out.println("Error.");
			}
		}
	}

	protected enum GridPlateType {
		XY(true);

		private boolean on;
		private GridPlateType(boolean b){
			this.on=b;
		}
		public boolean getValue(){
			return this.on;
		}
		public void putValue(boolean b){
			this.on=b;
		}
		public void toggle(){
			this.on=!this.on;
		}
	}

	protected enum AxisType {
		X(true),Y(true);

		private boolean on;
		private AxisType(boolean b){
			this.on=b;
		}
		public boolean getValue(){
			return this.on;
		}
		public void putValue(boolean b){
			this.on=b;
		}
		public void toggle(){
			this.on=!this.on;
		}
	}

	protected GL2 gl;
	protected GLU glu;
	protected GLUT glut;

	protected BufferedImage buf;
	protected JMenu select;
	protected JMenuBar menuBar;
	protected JMenu fileMenu;
	private Animator animator;
	
	protected Rect rect;
	
	public class Rect{
		public int width,height;
		public float xMin,xMax,yMin,yMax;
		public float xOff,yOff;
		public String xAxisLabel,yAxisLabel,xMinStr,xMaxStr,yMinStr,yMaxStr;
		
		public Rect(int w,int h,float xmin,float xmax, float ymin,float ymax){
			width=w;
			height=h;
			xMin=xmin;
			xMax=xmax;
			yMin=ymin;
			yMax=ymax;
			
			xMinStr="0.0";
			yMinStr="0.0";
			xMaxStr="0.0";
			yMaxStr="0.0";
			
			xOff=Math.abs(xMax-xMin)*0.1f;
			yOff=Math.abs(yMax-yMin)*0.1f;
		}
		public Rect(int w,int h,float xmin,float xmax, float ymin,float ymax,String xlabel,String ylabel,String format){
			width=w;
			height=h;
			xMin=xmin;
			xMax=xmax;
			yMin=ymin;
			yMax=ymax;
			
			xAxisLabel=xlabel;
			yAxisLabel=ylabel;
			
			DecimalFormat f = new DecimalFormat(format);
			xMinStr=f.format(xMin);
			yMinStr=f.format(yMin);
			xMaxStr=f.format(xMax);
			yMaxStr=f.format(yMax);
			
			xOff=Math.abs(xMax-xMin)*0.1f;
			yOff=Math.abs(yMax-yMin)*0.1f;
		}
	}

	

	public JOGL2DPlot(String name,float xmin,float xmax,float ymin,float ymax,String xlabel,String ylabel,String format) {
		
		JFrame frame = new JFrame(name);
		GLJPanel canvas = new GLJPanel();
		canvas.addGLEventListener(this);
		
		
		rect = new Rect(600,600,xmin,xmax,ymin,ymax,xlabel,ylabel,format);
		
		
		frame.add(canvas);
		frame.setSize(rect.width, rect.height);

		menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);

		fileMenu = new JMenu("File");
		menuBar.add(fileMenu);
    	JMenuItem open = new JMenuItem("Open");
    	open.addActionListener(new fileOpenSelectorAction());
    	fileMenu.add(open);
		JMenuItem save = new JMenuItem("Save");
		save.addActionListener(new fileSaveSelectorAction());
		fileMenu.add(save);


		animator = new Animator(canvas);

		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				animator.stop();
				System.exit(0);
			}
		});
		frame.setVisible(true);
		animator.start();
	}

	@Override
	public void display(GLAutoDrawable drawable) {


		gl = drawable.getGL().getGL2();

		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
		gl.glLoadIdentity();
		
		drawGraphFrame();
		drawLabels();
		
		render(drawable);

		buf = Screenshot.readToBufferedImage(rect.width,rect.height);
	}

	public void displayChanged(GLAutoDrawable drawable,
			boolean modeChanged,
			boolean deviceChanged) {}

	@Override
	public void dispose(GLAutoDrawable arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void init(GLAutoDrawable drawable) {

		glut = new GLUT();
		gl = drawable.getGL().getGL2();
		glu = GLU.createGLU(gl);
		
		gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
		gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
		gl.glEnable(GL2.GL_BLEND);
		gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
		

		gl.glShadeModel(GL2.GL_SMOOTH);
		gl.glEnable(GL2.GL_NORMALIZE);
		gl.glEnable(GL2.GL_BLEND);
		gl.glEnable(GL2.GL_LINE_SMOOTH);
		gl.glLineWidth(1f);
		gl.glHint(GL2.GL_LINE_SMOOTH_HINT, GL2.GL_NICEST); 

		gl.glEnable(GL2.GL_POLYGON_SMOOTH);
		gl.glHint(GL2.GL_POLYGON_SMOOTH_HINT, GL2.GL_NICEST); 

		gl.glPolygonMode(GL.GL_FRONT, GL2GL3.GL_LINES);
		gl.glPolygonMode(GL.GL_BACK, GL2GL3.GL_LINES);


		gl.glEnableClientState(GLPointerFunc.GL_VERTEX_ARRAY);
		//gl.glEnableClientState(GLPointerFunc.GL_NORMAL_ARRAY);
		//gl.glEnableClientState(GLPointerFunc.GL_COLOR_ARRAY); 

		
		GLinitialize(drawable);
	}

	@Override
	public void reshape(GLAutoDrawable drawable,
			int x, int y, 
			int width, int height) {

		rect.width=width;
		rect.height=height;

		gl = drawable.getGL().getGL2();
		gl.glViewport(0, 0, rect.width, rect.height);

		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();

		gl.glOrtho(rect.xMin-rect.xOff, rect.xMax+rect.xOff, rect.yMin-rect.yOff, rect.yMax+rect.yOff, 0.0, -1.0);
		gl.glMatrixMode(GL2.GL_MODELVIEW);

		gl.glLoadIdentity();
	}

	
	private void drawGraphFrame(){
		gl.glPolygonMode(GL2.GL_FRONT_AND_BACK,GL2.GL_LINE);
		gl.glLineWidth(2f);
		gl.glColor4f(0.0f, 0.0f, 0.0f,1.0f);
		gl.glBegin(GL2.GL_QUADS);
		gl.glVertex2f(rect.xMin,rect.yMax);
		gl.glVertex2f(rect.xMin,rect.yMin);
		gl.glVertex2f(rect.xMax,rect.yMin);
		gl.glVertex2f(rect.xMax,rect.yMax);
		gl.glEnd();
		gl.glLineWidth(1f);
		gl.glPolygonMode(GL2.GL_FRONT_AND_BACK,GL2.GL_FILL);
	}
	
	private void drawLabels(){
		gl.glColor3f(0.0f, 0.0f, 0.0f);
		
		float charHeight = 13.0f /(float)rect.height * (rect.yMax-rect.yMin+2f*rect.yOff);
		
		float charWidth = ((float)glut.glutBitmapLength(GLUT.BITMAP_HELVETICA_18, rect.yMinStr))/(float)rect.width*(rect.xMax-rect.xMin+2f*rect.xOff);
		gl.glRasterPos2f(rect.xMin-charWidth*1.1f,rect.yMin);
		glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18,rect.yMinStr);
		
		charWidth = ((float)glut.glutBitmapLength(GLUT.BITMAP_HELVETICA_18, rect.yMaxStr))/(float)rect.width*(rect.xMax-rect.xMin+2f*rect.xOff);
		gl.glRasterPos2f(rect.xMin-charWidth*1.1f,rect.yMax-charHeight);
		glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18,rect.yMaxStr);
		
		gl.glRasterPos2f(rect.xMin,rect.yMin-charHeight*1.1f);
		glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18,rect.xMinStr);
		
		
		charWidth = ((float)glut.glutBitmapLength(GLUT.BITMAP_HELVETICA_18, rect.xMaxStr))/(float)rect.width*(rect.xMax-rect.xMin+2f*rect.xOff);
		gl.glRasterPos2f(rect.xMax-charWidth,rect.yMin-charHeight*1.1f);
		glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18,rect.xMaxStr);
		
		
		gl.glRasterPos2f(rect.xMin,rect.yMax*1.03f);
		glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18,rect.yAxisLabel);
		
		gl.glRasterPos2f(rect.xMax*1.03f,rect.yMin);
		glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18,rect.xAxisLabel);
	}


//	protected void drawAxis(float size){
//		gl.glLineWidth(3f);
//		gl.glColor4f(0.0f, 0.0f, 0.0f,0.5f);
//		gl.glBegin(GL2.GL_LINES);
//		if(AxisType.X.getValue()){
//			gl.glVertex3f(-Math.round(0.1f*size/2.0f)*10f, 0.0f, 0.0f);
//			gl.glVertex3f( Math.round(0.1f*size/2.0f)*10f, 0.0f, 0.0f);
//		}
//		if(AxisType.Y.getValue()){
//			gl.glVertex3f(0.0f, -Math.round(0.1f*size/2.0f)*10f, 0.0f);
//			gl.glVertex3f(0.0f,  Math.round(0.1f*size/2.0f)*10f, 0.0f);
//		}
//		gl.glEnd();
//		gl.glLineWidth(1f);
//	}
//
//	protected void drawGridPlates(float size1, float size2, int len){
//		if(GridPlateType.XY.getValue()){
//			drawGridPlate(GridPlateType.XY, size1, size2, len);
//		}
//	}
//
//	protected void drawGridPlate(GridPlateType type,float size1, float size2, int len)
//	{
//
//		gl.glColor4f(0.0f,0.0f,0.0f,0.05f);
//		gl.glPolygonMode(GL2.GL_FRONT_AND_BACK,GL2.GL_LINE);
//		int i, j;
//		float p[][] = new float[2][2];
//
//		for(j = 0; j < size2/len; j++)
//		{
//
//
//			gl.glBegin(GL2.GL_QUAD_STRIP);
//			for(i = 1; i <= size1/len; i++)
//			{
//
//				p[0][0] = (float) ((float)i*len  - Math.round(0.1f*size1 / 2.0)*10f);
//				p[0][1] = (float) ((float)j*len  - Math.round(0.1f*size2 / 2.0)*10f);
//				p[1][0] = (float) ((float)i*len  - Math.round(0.1f*size1 / 2.0)*10f);
//				p[1][1] = (float) ((float)(j+1)*len  - Math.round(0.1f*size2 / 2.0)*10f);
//				
//				if(type==GridPlateType.XY){
//					gl.glVertex2fv(FloatBuffer.wrap(p[0]));
//					gl.glVertex2fv(FloatBuffer.wrap(p[1]));
//				}
//				
//			}
//			gl.glEnd();
//		}
//		gl.glPolygonMode(GL2.GL_FRONT_AND_BACK,GL2.GL_FILL);
//	}
	
	protected void addToMenuBar(JMenu menu){
		menuBar.add(menu);
	}

	protected void addToFileMenu(JMenuItem menu){
		fileMenu.add(menu);
	}

    protected static final FloatBuffer makeFloatBuffer(float[] arr,int len){
        FloatBuffer fb = Buffers.newDirectFloatBuffer(len);
        fb.put(arr,0,len);
        fb.position(0);
 
        return fb;
    }
	
	protected abstract void GLinitialize(GLAutoDrawable drawable);
	protected abstract void render(GLAutoDrawable drawable);
	protected abstract void fileOpenHandler(ActionEvent arg0,File file);

}
