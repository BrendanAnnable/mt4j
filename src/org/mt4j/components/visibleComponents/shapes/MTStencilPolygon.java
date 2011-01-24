/***********************************************************************
 * mt4j Copyright (c) 2008 - 2009, C.Ruff, Fraunhofer-Gesellschaft All rights reserved.
 *  
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 ***********************************************************************/
package org.mt4j.components.visibleComponents.shapes;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GL;

import org.mt4j.util.GraphicsUtil;
import org.mt4j.util.math.Tools3D;
import org.mt4j.util.math.ToolsBuffers;
import org.mt4j.util.math.ToolsGeometry;
import org.mt4j.util.math.Vertex;
import org.mt4j.util.opengl.GL10;
import org.mt4j.util.opengl.GL11;
import org.mt4j.util.opengl.GL11Plus;
import org.mt4j.util.opengl.GLStencilUtil;
import org.mt4j.util.opengl.JoglGL20Plus;

import processing.core.PApplet;
import processing.core.PGraphics;

/**
 * This class can only be used with OpenGL!
 * <br>This class can be used to draw concave, non-simple
 * polygons using the stencil buffer.
 * 
 * @author Christopher Ruff
 */
public class MTStencilPolygon extends MTPolygon {
	
	/** The pa. */
	private PApplet pa;
	
	/** The min max. */
	private float[] minMax;
	
	/** The min x. */
	private float minX;
	
	/** The min y. */
	private float minY;
	
	/** The max x. */
	private float maxX;
	
	/** The max y. */
	private float maxY;
    
	/** The contours. */
	private List<Vertex[]> contours;
	
	
	
	
	/**
	 * Instantiates a new mT stencil polygon.
	 *
	 * @param vertices the vertices
	 * @param pApplet the applet
	 * @deprecated constructor will be deleted! Please use the constructor with the PApplet instance as the first parameter.
	 */
	public MTStencilPolygon(Vertex[] vertices, PApplet pApplet) {
		this(pApplet, vertices);
	}
	
	/**
	 * Instantiates a new mT stencil polygon.
	 * @param pApplet the applet
	 * @param vertices the vertices
	 */
	public MTStencilPolygon(PApplet pApplet, Vertex[] vertices) {
		super(pApplet, vertices);
		
		ArrayList<Vertex[]> contours = new ArrayList<Vertex[]>();
		contours.add(vertices);
		this.contours = contours;
		this.init(pa, vertices, contours);
	}
	
	
	/**
	 * Instantiates a new mT stencil polygon.
	 *
	 * @param innerVertices the inner vertices
	 * @param contours the contours
	 * @param pApplet the applet
	 * @deprecated constructor will be deleted! Please use the constructor with the PApplet instance as the first parameter.
	 */
	public MTStencilPolygon(Vertex[] innerVertices, ArrayList<Vertex[]> contours, PApplet pApplet) {
		this(pApplet, innerVertices, contours);
	}
	
	/**
	 * The Constructor.
	 * @param pApplet the applet
	 * @param innerVertices the vertices used for picking and other calculations, its best to use the biggest contour for this
	 * @param contours the contour(s) of the shape
	 */
	public MTStencilPolygon(PApplet pApplet, Vertex[] innerVertices, ArrayList<Vertex[]> contours) {
		super(pApplet, innerVertices);
		this.init(pApplet, innerVertices, contours);
	}
	
	/**
	 * Inits the.
	 * @param pApplet the applet
	 * @param innerVertices the inner vertices
	 * @param contours the contours
	 */
	private void init(PApplet pApplet, Vertex[] innerVertices, ArrayList<Vertex[]> contours){
		this.pa = this.getRenderer();
		this.contours = contours;
		
		//This class may only be used with opengl anyway!
		this.setUseDirectGL(true);
		this.setDrawSmooth(true);
		
		//Convert BezierVertices to regular Vertices
		//SEGMENTS CONRTOLS THE DETAIL OF THE BEZIER CURVE APPROXIMATION
		int segments = 12; 
		//TODO let the user do that beforehand?
		Vertex[] allVerts = ToolsGeometry.createVertexArrFromBezierArr(innerVertices, segments);
		this.setVertices(allVerts);
		
		//Replace beziervertices in the SUB-PATHS (outlines) of the glyphs with many calculated regular vertices
		//The subpaths are used to draw the outline
		this.contours = ToolsGeometry.createVertexArrFromBezierVertexArrays(contours, segments);
		
	    reCalcMinMax();
	    
		this.setStrokeWeight(1.0f);
		
		// use?
//		this.setEnableTesselation(true);
		
		this.createContourAndStencilQuadBuffers();
	}
	
	private ArrayList<FloatBuffer> contoursInfos;
	private FloatBuffer stencilQuad;
	
	private void createContourAndStencilQuadBuffers(){
		contoursInfos = new ArrayList<FloatBuffer>();
		for (Vertex[] v : this.contours) {
			FloatBuffer buff = ToolsBuffers.generateVertexBuffer(v);
			contoursInfos.add(buff);
		}
		Vertex[] quadVertices = new Vertex[]{
				new Vertex(minX, minY, 0.0f), 
				new Vertex(maxX, minY, 0.0f), 
				new Vertex(maxX, maxY, 0.0f),
				new Vertex(minX, maxY, 0.0f) 
		};
		this.stencilQuad = ToolsBuffers.generateVertexBuffer(quadVertices);
	}
	
	/**
	 * Re calc min max.
	 */
	private void reCalcMinMax(){
		minMax = ToolsGeometry.getMinXYMaxXY(this.getVerticesLocal());
		minX = minMax[0]-5;
	    minY = minMax[1]-5;
	    maxX = minMax[2]+5;
	    maxY = minMax[3]+5;
	}
	
	/**
	 * NOTE: this also sets the contours to one and uses the new
	 * vertices as the only contour! If you want to set other countours
	 * use the setContours or setVerticesAndContours method!.
	 * 
	 * @param vertices the vertices
	 */
	@Override
	public void setVertices(Vertex[] vertices) {
		super.setVertices(vertices);
		reCalcMinMax();
		createContourAndStencilQuadBuffers();
	}
	
	/**
	 * Sets new outlines for this stencil polygon.
	 * This is a separate method, because when you want
	 * to ouline polygons with holes, you have to have separate,
	 * not connected outline arrays.
	 * 
	 * @param contours the contours
	 */
	public void setNewContours(ArrayList<Vertex[]> contours){
		this.contours = contours;
		this.setMatricesDirty(true);
		createContourAndStencilQuadBuffers();
	}
	
	/**
	 * Sets the new vertices and contours.
	 * 
	 * @param vertices the vertices
	 * @param contours the contours
	 */
	public void setNewVerticesAndContours(Vertex[] vertices, ArrayList<Vertex[]> contours){
		this.contours = contours;
		setVertices(vertices);
		reCalcMinMax();
		createContourAndStencilQuadBuffers();
	}
	
	

	/**
	 * Just draws the character without applying its own local matrix,
	 * useful when another components want to draw this component.
	 * 
	 * @param gl the gl
	 */
	public void drawComponent(GL10 gl) {
		if (isUseDirectGL()){
			if (isUseDisplayList()){
				int[] displayListIDs = this.getGeometryInfo().getDisplayListIDs();
				if (!this.isNoFill() && displayListIDs[0] != -1)
//					gl.glCallList(displayListIDs[0]); //Draw fill
					((GL11Plus)gl).glCallList(displayListIDs[0]); //Draw fill
				if (!this.isNoStroke()  && displayListIDs[1] != -1)
//					gl.glCallList(displayListIDs[1]); //Draw outline
					((GL11Plus)gl).glCallList(displayListIDs[1]); //Draw outline
			}else{
				drawPureGL(gl);
			}
		}
	}
	
	@Override
	public void drawComponent(PGraphics g) {
		if (isUseDirectGL()){
//			GL gl=((PGraphicsOpenGL)this.getRenderer().g).beginGL();
			GL10 gl = GraphicsUtil.beginGL();
			drawComponent(gl);
//			((PGraphicsOpenGL)this.getRenderer().g).endGL();
			GraphicsUtil.endGL();
		}
	}
	
	/**
	 * Draw pure gl.
	 * 
	 * @param gl the gl
	 */
	private void drawPureGL(GL10 gl){
		FloatBuffer vertBuff = this.getGeometryInfo().getVertBuff();
		FloatBuffer colorBuff = this.getGeometryInfo().getColorBuff();
		FloatBuffer strokeColBuff = this.getGeometryInfo().getStrokeColBuff();
		
	    gl.glEnableClientState(GL.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL.GL_COLOR_ARRAY);
		
		GL11 gl11 = GraphicsUtil.getGL11(); //TODO check if 1.1 available?
		
		if (this.isUseVBOs()){
			gl11.glBindBuffer(GL.GL_ARRAY_BUFFER, this.getGeometryInfo().getVBOVerticesName());
			gl11.glVertexPointer(3, GL.GL_FLOAT, 0, 0);
			
			gl11.glBindBuffer(GL.GL_ARRAY_BUFFER, this.getGeometryInfo().getVBOColorName());
			gl11.glColorPointer(4, GL.GL_FLOAT, 0, 0);
		}else{
			gl.glVertexPointer(3, GL.GL_FLOAT, 0, vertBuff);
			gl.glColorPointer(4, GL.GL_FLOAT, 0, colorBuff);
		}
		
		//Normals
		if (this.getGeometryInfo().isContainsNormals()){
			gl.glEnableClientState(GL.GL_NORMAL_ARRAY);
			if (this.isUseVBOs()){
				gl11.glBindBuffer(GL.GL_ARRAY_BUFFER, this.getGeometryInfo().getVBONormalsName());
				gl11.glNormalPointer(GL.GL_FLOAT, 0, 0); 
			}else{
				gl.glNormalPointer(GL.GL_FLOAT, 0, this.getGeometryInfo().getNormalsBuff());
			}
		}
		
//	    /*
	    if (!this.isNoFill()){
	    	
	    	/*
			///////////////////////
			// Draw Into Stencil //
		    ///////////////////////
			gl.glClearStencil(0);
			gl.glColorMask(false,false,false,false);
			gl.glDisable(GL.GL_BLEND);
			gl.glDepthMask(false);//remove..?
			
			//Enable stencilbuffer
			gl.glEnable(GL.GL_STENCIL_TEST);
//		    gl.glStencilMask (0x01);
		    gl.glStencilOp(GL.GL_KEEP, GL.GL_KEEP, GL.GL_INVERT);
		    gl.glStencilFunc (GL.GL_ALWAYS, 0, ~0);
		    
		    //Draw into stencil
		    gl.glDrawArrays(GL.GL_TRIANGLE_FAN, 0, vertBuff.capacity()/3); 
		    
		    if (this.getGeometryInfo().isContainsNormals()){
				gl.glDisableClientState(GL.GL_NORMAL_ARRAY);
			}
		    
			//////////////////////
			// Draw fill Overlay//
		    ////////////////////// 
		    gl.glDepthMask(true);
			gl.glColorMask(true, true, true, true);
			gl.glEnable (GL.GL_BLEND);
			
		    gl.glStencilOp (GL.GL_ZERO, GL.GL_ZERO, GL.GL_ZERO);
		    gl.glStencilFunc(GL.GL_EQUAL, 0x01, 0x01);
		    
		    if (useGradient){
			    gl.glBegin (GL.GL_QUADS);
				    gl.glColor4f(x1R, x1G, x1B, x1A);
				    gl.glVertex3d (minX, minY, 0.0);
				    gl.glColor4f(x2R, x2G, x2B, x2A);
				    gl.glVertex3d (maxX, minY, 0.0); 
				    gl.glColor4f(x3R, x3G, x3B, x3A);
				    gl.glVertex3d (maxX, maxY, 0.0); 
				    gl.glColor4f(x4R, x4G, x4B, x4A);
				    gl.glVertex3d (minX, maxY, 0.0); 
			    gl.glEnd ();
		    }else{
		    	gl.glColor4d (colorBuff.get(0), colorBuff.get(1), colorBuff.get(2), colorBuff.get(3));
			    gl.glBegin (GL.GL_QUADS);
				    gl.glVertex3d (minX, minY, 0.0); 
				    gl.glVertex3d (maxX, minY, 0.0); 
				    gl.glVertex3d (maxX, maxY, 0.0); 
				    gl.glVertex3d (minX, maxY, 0.0); 
			    gl.glEnd ();
		    }
		    
		    gl.glDisable (GL.GL_STENCIL_TEST);
		    */	
	    	
			///////////////////////
			// Draw Into Stencil //
		    ///////////////////////
//	    	GLStencilUtil.getInstance().beginDrawClipShape(gl);
	    	
	    	if (GLStencilUtil.getInstance().isClipActive() && GraphicsUtil.getGL11Plus() != null){
//	    		gl.glPushAttrib(GL.GL_STENCIL_BUFFER_BIT);
	    		((GraphicsUtil.getGL11Plus())).glPushAttrib(GL.GL_STENCIL_BUFFER_BIT);
	    	}else{
	    		//Enable stencilbuffer
				gl.glEnable(GL.GL_STENCIL_TEST);
		    	gl.glClearStencil(GLStencilUtil.stencilValueStack.peek());
		    	gl.glClear(GL.GL_STENCIL_BUFFER_BIT);
	    	}
//	    	gl.glPushAttrib(GL.GL_STENCIL_TEST);
//	    	gl.glDisable(GL.GL_STENCIL_TEST);
//	    	gl.glClearStencil(GLStencilUtil.getInstance().stencilValueStack.peek());
//			gl.glClearStencil(0);
//	    	gl.glClear(GL.GL_STENCIL_BUFFER_BIT);
			gl.glColorMask(false,false,false,false);
			gl.glDisable(GL.GL_BLEND);
			gl.glDepthMask(false);//remove..?
			
			
//		    gl.glStencilMask (0x01);
		    gl.glStencilOp(GL.GL_KEEP, GL.GL_KEEP, GL.GL_INVERT);
		    gl.glStencilFunc (GL.GL_ALWAYS, 0, ~0);
//		    gl.glStencilFunc (GL.GL_ALWAYS, GLStencilUtil.getInstance().stencilValueStack.peek(), ~GLStencilUtil.getInstance().stencilValueStack.peek());
		    
		    //Draw into stencil
		    gl.glDrawArrays(GL.GL_TRIANGLE_FAN, 0, vertBuff.capacity()/3); 
		    
		    if (this.getGeometryInfo().isContainsNormals()){
				gl.glDisableClientState(GL.GL_NORMAL_ARRAY);
			}
		    
			//////////////////////
			// Draw fill Overlay//
		    ////////////////////// 
		    gl.glDepthMask(true);
			gl.glColorMask(true, true, true, true);
			gl.glEnable (GL.GL_BLEND);
			
			
			gl.glStencilOp (GL.GL_KEEP, GL.GL_KEEP, GL.GL_REPLACE);
//		    gl.glStencilOp (GL.GL_ZERO, GL.GL_REPLACE, GL.GL_REPLACE);
//			gl.glStencilOp (GL.GL_KEEP, GL.GL_KEEP, GL.GL_ZERO); 
//		     gl.glStencilOp (GL.GL_ZERO, GL.GL_ZERO, GL.GL_ZERO); //Org
//		    gl.glStencilFunc(GL.GL_EQUAL, 0x01, 0x01); //org
//			if (GLStencilUtil.getInstance().isClipActive()){
				gl.glStencilFunc(GL.GL_NOTEQUAL, GLStencilUtil.stencilValueStack.peek(), GLStencilUtil.stencilValueStack.peek());
//				gl.glStencilFunc(GL.GL_NOTEQUAL, 0x01, 0x01);
//			}else{
//				gl.glStencilFunc(GL.GL_EQUAL, GLStencilUtil.getInstance().stencilValueStack.peek(), GLStencilUtil.getInstance().stencilValueStack.peek());
//			}

				//Draw quad over everything -> only where the stencil value is right, it will be drawn
//				gl.glColor4f (colorBuff.get(0), colorBuff.get(1), colorBuff.get(2), colorBuff.get(3));
//				gl.glBegin (GL.GL_QUADS);
//				gl.glVertex3d (minX, minY, 0.0); 
//				gl.glVertex3d (maxX, minY, 0.0); 
//				gl.glVertex3d (maxX, maxY, 0.0); 
//				gl.glVertex3d (minX, maxY, 0.0); 
//				gl.glEnd ();
				
				gl.glVertexPointer(3, GL.GL_FLOAT, 0, stencilQuad);
		    	gl.glDrawArrays(GL.GL_TRIANGLE_FAN, 0, stencilQuad.capacity()/3); 

		    if (GLStencilUtil.getInstance().isClipActive()  && GraphicsUtil.getGL11Plus() != null){
//			    gl.glPopAttrib();
		    	GraphicsUtil.getGL11Plus().glPopAttrib();
	    	}else{
	    		 gl.glDisable (GL.GL_STENCIL_TEST);
	    	}

	    }
	    
	    //////////////////////////////
		// Draw aliased outlines	//
		//////////////////////////////
	    if (!isNoStroke()){
	    	if (this.isUseVBOs()){
	    		gl11.glBindBuffer(GL.GL_ARRAY_BUFFER, this.getGeometryInfo().getVBOStrokeColorName());
	    		gl11.glColorPointer(4, GL.GL_FLOAT, 0, 0);
			}else{
				gl.glColorPointer(4, GL.GL_FLOAT, 0, strokeColBuff);
			}
			
//		  	gl.glDepthMask(false); //FIXME enable? disable?
//		    // Draw aliased off-pixels to real
//		    gl.glEnable (GL.GL_BLEND);
//		    gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
//			
//		    gl.glStencilOp (GL.GL_KEEP, GL.GL_KEEP, GL.GL_KEEP);
//		    gl.glStencilFunc (GL.GL_EQUAL, 0x00, 0x01); //THIS IS THE ORIGINAL!
		    
//		    gl.glEnable(GL.GL_LINE_SMOOTH);
	    	//FIXME TEST
			Tools3D.setLineSmoothEnabled(gl, true);
	    	
		    gl.glLineWidth(this.getStrokeWeight());
		    
		    short lineStipple = this.getLineStipple();
			if (lineStipple != 0 && GraphicsUtil.getGL20() instanceof JoglGL20Plus){
//				gl.glLineStipple(1, lineStipple);
				((JoglGL20Plus)gl).glLineStipple(1, lineStipple);
				gl.glEnable(GL.GL_LINE_STIPPLE);
			}
		    
		    //DRAW 
//			gl.glDrawElements(GL.GL_LINE_STRIP, indexBuff.capacity(), GL.GL_UNSIGNED_INT, indexBuff);
//			gl.glDrawArrays(GL.GL_LINE_STRIP, 0, vertexArr.length);
		    
		    /////TEST/// //TODO make vertex pointer arrays?
		    gl.glColor4f (strokeColBuff.get(0), strokeColBuff.get(1), strokeColBuff.get(2), strokeColBuff.get(3));
//		    for (Vertex[] outline : contours){
//				 gl.glBegin (GL.GL_LINE_STRIP);
//				 	for (Vertex vertex : outline)
//				 		gl.glVertex3f (vertex.getX(), vertex.getY(), vertex.getZ());
//			    gl.glEnd();
//			}
		    for (FloatBuffer outline : contoursInfos){
		    	gl.glVertexPointer(3, GL.GL_FLOAT, 0, outline);
		    	gl.glDrawArrays(GL.GL_TRIANGLE_FAN, 0, outline.capacity()/3); 
		    }
		    
//			gl.glDisable (GL.GL_LINE_SMOOTH);
		    //FIXME TEST
			Tools3D.setLineSmoothEnabled(gl, false);
		    
			gl.glDisable(GL.GL_LINE_STIPPLE); 
	    }
	    
//		gl.glDisable (GL.GL_STENCIL_TEST);	
//		gl.glDepthMask(true);
		    
		//Disable client states
		gl.glDisableClientState(GL.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL.GL_COLOR_ARRAY);
		
		if (this.isUseVBOs()){
			gl11.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
			gl11.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, 0);
		}
	}
	
	

	@Override //TODO JUST COMPILE DrawPureGL() into a list!?
	public void generateDisplayLists() {
		this.getGeometryInfo().setDisplayListIDs(Tools3D.generateStencilDisplayList(
								pa, this.getGeometryInfo().getVertBuff(), this.getGeometryInfo().getTexBuff(), this.getGeometryInfo().getColorBuff(), this.getGeometryInfo().getStrokeColBuff(),
								this.getGeometryInfo().getIndexBuff(),true, this.getStrokeWeight(), this.getVerticesLocal(), contours));
	}

	/**
	 * returns the vertex arrays which shape the outline of the character.
	 * 
	 * @return the contours
	 */
	public List<Vertex[]> getContours(){
		return this.contours;
	}
	
	/**
	 * Gets the max x.
	 * 
	 * @return the max x
	 */
	public float getMaxX() {
		return maxX;
	}

	/**
	 * Gets the max y.
	 * 
	 * @return the max y
	 */
	public float getMaxY() {
		return maxY;
	}

	/**
	 * Gets the min x.
	 * 
	 * @return the min x
	 */
	public float getMinX() {
		return minX;
	}

	/**
	 * Gets the min y.
	 * 
	 * @return the min y
	 */
	public float getMinY() {
		return minY;
	}
	

	
	
	
	
}
