package advanced.extension3D;

import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;

import javax.media.opengl.GL;

import org.mt4j.MTApplication;
import org.mt4j.components.MTComponent;
import org.mt4j.components.MTLight;
import org.mt4j.components.StateChange;
import org.mt4j.components.TransformSpace;
import org.mt4j.components.visibleComponents.shapes.AbstractShape;
import org.mt4j.components.visibleComponents.shapes.mesh.MTCube;
import org.mt4j.components.visibleComponents.shapes.mesh.MTTriangleMesh;
import org.mt4j.input.IMTEventListener;
import org.mt4j.input.gestureAction.DefaultDragAction;
import org.mt4j.input.gestureAction.DefaultLassoAction;
import org.mt4j.input.gestureAction.DefaultPanAction;
import org.mt4j.input.gestureAction.DefaultRotateAction;
import org.mt4j.input.gestureAction.DefaultZoomAction;
import org.mt4j.input.inputProcessors.IGestureEventListener;
import org.mt4j.input.inputProcessors.MTGestureEvent;



import org.mt4j.input.inputProcessors.componentProcessors.arcballProcessor.ArcBallGestureEvent;
import org.mt4j.input.inputProcessors.componentProcessors.arcballProcessor.ArcballProcessor;
import org.mt4j.input.inputProcessors.componentProcessors.dragProcessor.DragProcessor;
import org.mt4j.input.inputProcessors.componentProcessors.lassoProcessor.LassoProcessor;
import org.mt4j.input.inputProcessors.componentProcessors.panProcessor.PanProcessorTwoFingers;

import org.mt4j.input.inputProcessors.componentProcessors.rotateProcessor.RotateProcessor;
import org.mt4j.input.inputProcessors.componentProcessors.scaleProcessor.ScaleEvent;
import org.mt4j.input.inputProcessors.componentProcessors.scaleProcessor.ScaleProcessor;
import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapProcessor;
import org.mt4j.input.inputProcessors.componentProcessors.zoomProcessor.ZoomProcessor;
import org.mt4j.input.inputProcessors.globalProcessors.CursorTracer;
import org.mt4j.input.inputSources.MouseInputSource;
import org.mt4j.sceneManagement.AbstractScene;
import org.mt4j.util.MTColor;
import org.mt4j.util.camera.Frustum;
import org.mt4j.util.camera.Icamera;
import org.mt4j.util.camera.MTCamera;
import org.mt4j.util.math.Tools3D;
import org.mt4j.util.math.Vector3D;
import org.mt4j.util.modelImporter.ModelImporterFactory;
import org.mt4j.util.opengl.GLMaterial;
import org.mt4jx.input.gestureAction.CreateDragHelperAction;
import org.mt4jx.input.gestureAction.DefaultDepthAction;
import org.mt4jx.input.gestureAction.Rotate3DAction;
import org.mt4jx.input.inputProcessors.componentProcessors.Group3DProcessorNew.ClusterDataManager;
import org.mt4jx.input.inputProcessors.componentProcessors.Group3DProcessorNew.ClusterHub;
import org.mt4jx.input.inputProcessors.componentProcessors.Group3DProcessorNew.IVisualizeMethodProvider;
import org.mt4jx.input.inputProcessors.componentProcessors.Group3DProcessorNew.FingerTapGrouping.FingerTapSelectionManager;
import org.mt4jx.input.inputProcessors.componentProcessors.Group3DProcessorNew.GroupVisualizations.BlinkingLineVisualizationAction;
import org.mt4jx.input.inputProcessors.componentProcessors.Group3DProcessorNew.GroupVisualizations.LineVisualizationAction;
import org.mt4jx.input.inputProcessors.componentProcessors.Group3DProcessorNew.GroupVisualizations.LineVisualizationWithOutlinesAction;
import org.mt4jx.input.inputProcessors.componentProcessors.Rotate3DProcessor.Rotate3DProcessor;
import org.mt4jx.input.inputProcessors.componentProcessors.depthProcessor.DepthProcessor;
import org.mt4jx.util.ComponentHelper;
import org.mt4jx.util.MergeHelper;
import org.mt4jx.util.Collision.CollisionManager;


import processing.core.PGraphics;
import processing.core.PGraphics3D;

public class Extension3DScene extends AbstractScene {
	private MTApplication mtApp;
	
	private CollisionManager collisionManager;
	//TODO switch button/wireframe
	private ClusterHub clusterHub;
	
	private ArrayList<Rotate3DAction> drawAction = new ArrayList<Rotate3DAction>(); //REMOVE
	
	
	private MTComponent comp = null;
	Vector3D grundflaecheTranslation = null;
	public Extension3DScene(MTApplication mtApplication, String name) {
		super(mtApplication, name);
		mtApp = mtApplication;
	
		Icamera cam = this.getSceneCam();
		
		collisionManager = new CollisionManager(this,mtApp);
		
		this.registerGlobalInputProcessor(new CursorTracer(mtApp, this));
		
		//Make canvas zoomable
	//	this.getCanvas().registerInputProcessor(new ZoomProcessor(mtApp));
		//this.getCanvas().addGestureListener(ZoomProcessor.class, new DefaultZoomAction());
		
		//Init light settings
		MTLight.enableLightningAndAmbient(mtApplication, 150, 150, 150, 255);
		//Create a light source //I think GL_LIGHT0 is used by processing!
		MTLight light = new MTLight(mtApplication, GL.GL_LIGHT3, new Vector3D(0,0,0));
		
		//Set up a material to react to the light
		GLMaterial material = new GLMaterial(Tools3D.getGL(mtApplication));
		material.setAmbient(new float[]{ .3f, .3f, .3f, 1f });
		material.setDiffuse(new float[]{ .9f, .9f, .9f, 1f } );
		material.setEmission(new float[]{ .0f, .0f, .0f, 1f });
		material.setSpecular(new float[]{ 1.0f, 1.0f, 1.0f, 1f });  // almost white: very reflective
		material.setShininess(110);// 0=no shine,  127=max shine
		
/*MTComponent spoon1;
		
		spoon1 = getMeshGroup(mtApplication, new Vector3D(0.0f,0.0f,-200.0f),System.getProperty("user.dir")  + File.separator + "examples" +  File.separator +"advanced"+ File.separator+ File.separator + "models3D"  + File.separator + "data" +  File.separator +
				"spoon" + File.separator + "spoon.3ds",light,material,"spoon1");
		
MTComponent spoon2;
		
		spoon2 = getMeshGroup(mtApplication, new Vector3D(-200.0f,0.0f,-200.0f),System.getProperty("user.dir")  + File.separator + "examples" +  File.separator +"advanced"+ File.separator+ File.separator + "models3D"  + File.separator + "data" +  File.separator +
				"spoon" + File.separator + "spoon.3ds",light,material,"spoon2");*/
		
		//Group used to move to the screen center and to put the mesh group in
	MTComponent group1;
		
		group1 = getMeshGroup(mtApplication, new Vector3D(0.0f,0.0f,-200.0f),System.getProperty("user.dir")  + File.separator + "examples" +  File.separator +"advanced"+ File.separator+  "extension3D"  + File.separator + "data" +  File.separator +
				"CWK500" + File.separator + "CWK500_mit_kuehlmittelbehaelter.obj",light,material,"machine1");
		
		MTComponent machine;
		
		machine = getMeshGroup(mtApplication, new Vector3D(-300.0f,0.0f,-200.0f), System.getProperty("user.dir")  + File.separator + "examples" +  File.separator +"advanced"+ File.separator+  "extension3D"  + File.separator + "data" +  File.separator +
				"CWK500" + File.separator + "CWK500_mit_kuehlmittelbehaelter.obj",light,material,"machine2");
	
		MTComponent machine2;
		
		machine2 = getMeshGroup(mtApplication, new Vector3D(-500.0f,500.0f,0.0f), System.getProperty("user.dir")  + File.separator + "examples" +  File.separator +"advanced"+ File.separator+ "extension3D"  + File.separator + "data" +  File.separator +
				"CWK500" + File.separator + "CWK500_mit_kuehlmittelbehaelter.obj",light,material,"machine3");
		
		MTComponent machine3;
		
		machine3 = getMeshGroup(mtApplication, new Vector3D(-300.0f,-350.0f,0.0f), System.getProperty("user.dir")  + File.separator + "examples" +  File.separator +"advanced"+ File.separator+ "extension3D"  + File.separator + "data" +  File.separator +
				"CWK500" + File.separator + "CWK500_mit_kuehlmittelbehaelter.obj",light,material,"machine4");
		
		//this.getCanvas().addChild(machine3);
		
		MTComponent machine4;
		
		machine4 = getMeshGroup(mtApplication, new Vector3D(-400.0f,-700.0f,1200.0f), System.getProperty("user.dir")  + File.separator + "examples" +  File.separator +"advanced"+ File.separator + "extension3D"  + File.separator + "data" +  File.separator +
				"elevtruck" + File.separator + "elev_truck.obj",light,material,"elevTruck");
		
		//machine4.rotateX(MergeHelper.getInstance().getMergedBoundsForComponent(machine4).getCenterPointGlobal(), -90.0f);
				
		machine4.rotateX(ComponentHelper.getCenterPointGlobal(machine4), -90.0f);
		//machine4.scale(0.5f,0.5f,0.5f,MergeHelper.getInstance().getMergedBoundsForComponent(machine4).getCenterPointGlobal());
		machine4.scale(0.5f,0.5f,0.5f,ComponentHelper.getCenterPointGlobal(machine4));
		
		MTComponent robotArm;
		
		robotArm = getMeshGroup(mtApplication, new Vector3D(-450.0f,-150.0f,-200.0f), System.getProperty("user.dir")  + File.separator + "examples" +  File.separator +"advanced"+ File.separator + "extension3D"  + File.separator + "data" +  File.separator +
				"robotArm" + File.separator + "robotArm.obj",light,material,"robotArm");
		
		//robotArm.rotateX(robotArm.getCenterPointGlobal(), -90.0f);
		//robotArm.scale(0.4f,0.4f,0.4f,MergeHelper.getInstance().getMergedBoundsForComponent(robotArm).getCenterPointGlobal());		
		robotArm.scale(0.4f,0.4f,0.4f,ComponentHelper.getCenterPointGlobal(robotArm));
		//this.getCanvas().addChild(machine4);*/
		
		MTComponent dreh;
		
		dreh = getMeshGroup(mtApplication, new Vector3D(-100.0f,-150.0f,-200.0f), System.getProperty("user.dir")  + File.separator + "examples" +  File.separator +"advanced"+ File.separator+ File.separator + "extension3D"  + File.separator + "data" +  File.separator +
				"drehmaschine" + File.separator + "maschine1.obj",light,material,"drehmaschine");
		
		
		MTComponent grundflaecheGroup = getGroundMesh(mtApplication, System.getProperty("user.dir")  + File.separator + "examples" +  File.separator +"advanced"+ File.separator+ File.separator + "extension3D"  + File.separator + "data" +  File.separator +
				"grundflaeche" + File.separator + "grundflaeche2.obj",light,material,cam);
						
		//create Array with the differetn planes which are used for selection on floor
		
		
		//LineGroup3DAction lGroupAct = new LineGroup3DAction(mtApp, this.getCanvas().getClusterManager(),this.getCanvas());
		
		
		//LassoGroupSelectionManager selectionManager = new LassoGroupSelectionManager(mtApp,this.getSceneCam(),planes);
		
		//TapGroupSelectionManager selectionManagerTap = new TapGroupSelectionManager();
		
		
		//Group3DProcessor groupProcessor = new Group3DProcessor(mtApp,this.getSceneCam(),this.getCanvas(),selectionManagerTap);
		//selectionManagerTap.setProcessor(groupProcessor);
		ClusterDataManager clusterManager = new ClusterDataManager(mtApplication,this.getCanvas(),collisionManager);
		clusterHub = new ClusterHub();
		clusterManager.addClusterEventListener(clusterHub);
		//LassoVisualizationAction visAction = new LassoVisualizationAction(mtApplication);		
		//clusterHub.addEventListener(visAction);
		LineVisualizationAction visAction = new LineVisualizationAction(mtApplication);
		clusterHub.addEventListener(visAction);
		
		//BlinkingLineVisualizationAction visAction2 = new BlinkingLineVisualizationAction(mtApplication);
		//clusterHub.addEventListener(visAction2);
		
		//LineVisualizationWithOutlinesAction visAction3 = new LineVisualizationWithOutlinesAction(mtApplication);
		//clusterHub.addEventListener(visAction3);
		
		//LassoGroupSelectionManager selectionManager = new LassoGroupSelectionManager(this.getCanvas(),clusterManager);
		//selectionManager.addSelectionListener(clusterHub);
		
		FingerTapSelectionManager selectionManager = new FingerTapSelectionManager(clusterManager,this.getCanvas());
		selectionManager.addSelectionListener(clusterHub);
		this.registerGlobalInputProcessor(selectionManager);
		
		//this.getCanvas().registerInputProcessor(selectionManager);
		//this.getCanvas().addGestureListener(LassoGroupSelectionManager.class, null);
		
		//this.registerGlobalInputProcessor(groupProcessor);
		
		
		//this.getCanvas().registerInputProcessor(groupProcessor);
		
		//this.getCanvas().registerInputProcessor(groupProcessor);
		//this.getCanvas().addGestureListener(Group3DProcessor.class,lGroupAct);
		//this.getCanvas().setGestureAllowance(Group3DProcessor.class, true);
		
		/*registerGroupProcessor(group1,groupProcessor,lGroupAct);		
		registerGroupProcessor(machine,groupProcessor,lGroupAct);
		registerGroupProcessor(machine2,groupProcessor,lGroupAct);
		registerGroupProcessor(machine3,groupProcessor,lGroupAct);
		registerGroupProcessor(machine4,groupProcessor,lGroupAct);*/
		
		//grundflaecheGroup.registerInputProcessor(groupProcessor);
		//grundflaecheGroup.addGestureListener(Group3DProcessor.class, new Group3DAction(mtApp, this.getCanvas().getClusterManager(),this.getCanvas()));
		//grundflaecheGroup.addGestureListener(Group3DProcessor.class, new LineGroup3DAction(mtApp, this.getCanvas().getClusterManager(),this.getCanvas()));
		//grundflaecheGroup.setGestureAllowance(Group3DProcessor.class,true);
				
		selectionManager.addClusterable(group1);
		selectionManager.addClusterable(machine);
		selectionManager.addClusterable(machine2);
		selectionManager.addClusterable(machine3);
		selectionManager.addClusterable(machine4);
		selectionManager.addClusterable(robotArm);
		selectionManager.addClusterable(dreh);
	//	selectionManager.addClusterable(spoon1);
		//selectionManager.addClusterable(spoon2);
		
		collisionManager.addObjectsToCollisionDomain();
		/*MTTriangleMesh[] meshCube = ModelImporterFactory.loadModel(mtApplication, System.getProperty("user.dir")  + File.separator + "examples" +  File.separator +"advanced"+ File.separator+ File.separator + "extension3D"  + File.separator + "data" +  File.separator +
				"room" + File.separator + "cube.obj", 180, true, false );
		
		MTComponent cubeGroup = new MTComponent(mtApplication);
		cubeGroup.setLight(light);
		
		for (int i = 0; i < meshCube.length; i++) {
			MTTriangleMesh mesh = meshCube[i];
		
			cubeGroup.addChild(mesh);
			mesh.unregisterAllInputProcessors(); //Clear previously registered input processors
			mesh.setPickable(true);
			mesh.setDrawWireframe(true);
			//If the mesh has more than 20 vertices, use a display list for faster rendering
			if (mesh.getVertexCount() > 20)
				mesh.generateAndUseDisplayLists();
			//Set the material to the mesh  (determines the reaction to the lightning)
			//if (mesh.getMaterial() == null)
			mesh.setMaterial(material);
			mesh.setDrawNormals(false);
		}
		
		cubeGroup.setGestureAllowance(DragProcessor.class, false);
		cubeGroup.setGestureAllowance(ScaleProcessor.class,false);
		cubeGroup.setGestureAllowance(RotateProcessor.class,false);
		
		Vector3D cubeDestination = new Vector3D(mtApplication.getWidth()/2f,mtApplication.getHeight()/2f,0.0f);
		cubeGroup.translateGlobal(cubeDestination);
		cubeGroup.scaleGlobal(250.0f, 250.0f, 250.0f,cubeDestination);
		cubeGroup.rotateX(cubeDestination,90.0f);
		cubeGroup.setPickable(false);
		
		this.getCanvas().addChild(cubeGroup);*/
		
	
		
	}

	/*private void registerGroupProcessor(MTComponent comp,Group3DProcessor groupProcessor,LineGroup3DAction lGroupAct)
	{
		//comp.registerInputProcessor(groupProcessor);				
		//comp.addGestureListener(Group3DProcessor.class,lGroupAct);
		//comp.setGestureAllowance(Group3DProcessor.class,true);
	}*/
	
	public MTTriangleMesh getBiggestMesh(MTTriangleMesh[] meshes){
		MTTriangleMesh currentBiggestMesh = null;
		//Get the biggest mesh and extract its width
		float currentBiggestWidth = Float.MIN_VALUE;
		for (int i = 0; i < meshes.length; i++) {
			MTTriangleMesh triangleMesh = meshes[i];
			float width = triangleMesh.getWidthXY(TransformSpace.GLOBAL);
			if (width > currentBiggestWidth){
				currentBiggestWidth = width;
				currentBiggestMesh = triangleMesh;
			}
		}
		return currentBiggestMesh;
	}
	
	
	
	//@Override
	public void init() {
		mtApp.registerKeyEvent(this);
	}

	//@Override
	public void shutDown() {
		mtApp.unregisterKeyEvent(this);
	}
	
	public void keyEvent(KeyEvent e){
		//System.out.println(e.getKeyCode());
		int evtID = e.getID();
		if (evtID != KeyEvent.KEY_PRESSED)
			return;
		switch (e.getKeyCode()){
		case KeyEvent.VK_F:
			System.out.println("FPS: " + mtApp.frameRate);
			break;
		case KeyEvent.VK_PLUS:
			this.getSceneCam().moveCamAndViewCenter(0, 0, -10);
			break;
		case KeyEvent.VK_MINUS:
			this.getSceneCam().moveCamAndViewCenter(0, 0, +10);
			break;
		case KeyEvent.VK_1:
			LineVisualizationAction visAction = new LineVisualizationAction(mtApp);
			removeAllVisualization(clusterHub);
			clusterHub.addEventListener(visAction);
			break;
		case KeyEvent.VK_2:
			BlinkingLineVisualizationAction visAction2 = new BlinkingLineVisualizationAction(this.mtApp);
			removeAllVisualization(clusterHub);
			clusterHub.addEventListener(visAction2);
			break;
		case KeyEvent.VK_3:
			LineVisualizationWithOutlinesAction visAction3 = new LineVisualizationWithOutlinesAction(this.mtApp);
			removeAllVisualization(clusterHub);
			clusterHub.addEventListener(visAction3);
			break;
			default:
				break;
		}
	}
	
	public void removeAllVisualization(ClusterHub cHub)
	{
		ArrayList<IMTEventListener> toRemove = new ArrayList<IMTEventListener>();
		
		for(IMTEventListener listener : cHub.getListeners())
		{
			if(listener instanceof IVisualizeMethodProvider)
			{
				toRemove.add(listener);
			}
		}	
		cHub.getListeners().removeAll(toRemove);
		
	}
	
	public void drawAndUpdate(PGraphics g, long timeDelta) {
        super.drawAndUpdate(g, timeDelta);
        g.pushMatrix();
        Tools3D.beginGL(mtApp);
        GL gl = Tools3D.getGL(mtApp);
        if(drawAction!=null)
        {
        	for(Rotate3DAction act:drawAction)
        	{
        		if(act.isDrawAble())
        		{
        			act.draw();
        		}
        	}
        }
        
        Tools3D.endGL(mtApp);
        g.popMatrix();
    }
	
	private MTComponent getMeshGroup(MTApplication mtApplication,Vector3D translation,String filename,MTLight light,GLMaterial material,String name)
	{		
		
		
		//Create a group and set the light for the whole mesh group ->better for performance than setting light to more comps
		//MTComponent group1 = new MTComponent(mtApplication);
		final MTComponent meshGroup = new MTComponent(mtApplication, "Mesh group");
		meshGroup.addStateChangeListener(StateChange.GLOBAL_TRANSFORM_CHANGED, MergeHelper.getInstance());//necessary for getting matrix changes for updating merged bounds
		
//		meshGroup.setMergedOfChildrenBounds(true);
		meshGroup.setLight(light);
		this.getCanvas().addChild(meshGroup);
		//Desired position for the meshes to appear at
		Vector3D destinationPosition = new Vector3D(mtApplication.width/2+200.0f, mtApplication.height/2, 50);
		//System.out.println("destPos: " + destinationPosition);
		//Desired scale for the meshes
		float destinationScale = mtApplication.width*0.94f;

		//Load the meshes with the ModelImporterFactory (A file can contain more than 1 mesh)
		MTTriangleMesh[] meshes = ModelImporterFactory.loadModel(mtApp,filename, 180, true, false );
		
		//Get the biggest mesh in the group to use as a reference for setting the position/scale
		final MTTriangleMesh biggestMesh = this.getBiggestMesh(meshes);
		
		Vector3D translationToScreenCenter = new Vector3D(destinationPosition);
		translationToScreenCenter.subtractLocal(biggestMesh.getCenterPointGlobal());
		//System.out.println(translationToScreenCenter);
		Vector3D scalingPoint = new Vector3D(biggestMesh.getCenterPointGlobal());
		float biggestWidth = biggestMesh.getWidthXY(TransformSpace.GLOBAL);	
		float scale = destinationScale/biggestWidth;
		
		//Move the group the the desired position
		meshGroup.translateGlobal(translationToScreenCenter.getAdded(translation));
		meshGroup.scale(scale/2, scale/2, scale/2,translationToScreenCenter.getAdded(translation));
	
		meshGroup.setName(name);
					
		//meshGroup.addChild(meshGroup);
		for (int i = 0; i < meshes.length; i++) {
			MTTriangleMesh mesh = meshes[i];
			mesh.setName(name + " " + i);
			meshGroup.addChild(mesh);
			mesh.unregisterAllInputProcessors(); //Clear previously registered input processors
			mesh.setPickable(true);
			//If the mesh has more than 20 vertices, use a display list for faster rendering
			if (mesh.getVertexCount() > 20)
				mesh.generateAndUseDisplayLists();
			//Set the material to the mesh  (determines the reaction to the lightning)
			if (mesh.getMaterial() == null)
				mesh.setMaterial(material);
			//mesh.setCenterPointGlobal(mesh.)
			//mesh.setMass(5.0f);
			mesh.setDrawNormals(false);
			//mesh.transform(mesh.getGlobalInverseMatrix());			
		}
		
		meshGroup.rotateX(translationToScreenCenter.getAdded(translation),90.0f);
		//add to Collision World
		for(int i=0;i<meshes.length;i++)
		{
			collisionManager.addMeshToCollisionGroup(meshGroup, meshes[i], translationToScreenCenter.getAdded(translation));			
		}
		
		settingsForNormalMeshGroup(mtApplication,meshGroup);
		//group1.setComposite(true);
		return meshGroup;
	}
	
	private MTComponent getGroundMesh(MTApplication mtApplication,String filename,MTLight light,GLMaterial material,Icamera cam)
	{
		MTComponent grundflaecheGroup = new MTComponent(mtApplication);
		grundflaecheGroup.addStateChangeListener(StateChange.GLOBAL_TRANSFORM_CHANGED, MergeHelper.getInstance());//necessary for getting matrix changes for updating merged bounds
		
		MTTriangleMesh[] grundflaeche = ModelImporterFactory.loadModel(mtApp,filename, 0, true, false );
		grundflaecheGroup.setLight(light);
		this.getCanvas().addChild(grundflaecheGroup);
		for(int i=0;i<grundflaeche.length;i++)
		{			
			grundflaecheGroup.addChild(grundflaeche[i]);
			grundflaeche[i].unregisterAllInputProcessors(); //Clear previously registered input processors
			grundflaeche[i].setPickable(false);
		
			//If the mesh has more than 20 vertices, use a display list for faster rendering
			if (grundflaeche[i].getVertexCount() > 20)
				grundflaeche[i].generateAndUseDisplayLists();
			//Set the material to the mesh  (determines the reaction to the lightning)
			if (grundflaeche[i].getMaterial() == null)
				grundflaeche[i].setMaterial(material);
			//grundflaeche[i].setMass(5.0f);
			grundflaeche[i].setDrawNormals(false);
		}	
		
		grundflaecheTranslation = new Vector3D(mtApp.getWidth()/2.f,mtApp.getHeight()/2.f,-300.0f);
		
		for(int i=0;i<grundflaeche.length;i++)
		{
			collisionManager.addMeshToCollisionGroup(grundflaecheGroup,grundflaeche[i], grundflaecheTranslation);
		}
		
		final MTTriangleMesh biggestMeshGrundflaeche = this.getBiggestMesh(grundflaeche);
		grundflaecheGroup.translateGlobal(grundflaecheTranslation);
		grundflaecheGroup.rotateXGlobal(grundflaecheTranslation,90.0f);
		
		float biggestWidthGrundflaeche = biggestMeshGrundflaeche.getWidthXY(TransformSpace.GLOBAL);
		float biggestHeightGrundflaeche = biggestMeshGrundflaeche.getHeightXY(TransformSpace.GLOBAL);
				
		grundflaecheGroup.scaleGlobal(cam.getFrustum().getWidthOfPlane(-300.0f)/biggestWidthGrundflaeche,
								      cam.getFrustum().getHeightOfPlane(-300.0f)/biggestHeightGrundflaeche,1.0f,grundflaecheTranslation);
		
		//grundflaecheGroup.setMass(5.0f);
		grundflaecheGroup.setComposite(true);
		grundflaecheGroup.setPickable(false);
		grundflaecheGroup.setName("grundflaeche");
		return grundflaecheGroup;
	}

	private void settingsForNormalMeshGroup(MTApplication mtApplication,final MTComponent meshGroup)
	{
		meshGroup.setComposite(true); //-> Group gets picked instead of its children
		
		
		meshGroup.registerInputProcessor(new ScaleProcessor(mtApplication));
		meshGroup.addGestureListener(ScaleProcessor.class, new IGestureEventListener(){
			//@Override
			public boolean processGestureEvent(MTGestureEvent ge) {
					ScaleEvent se = (ScaleEvent)ge;				
					
					//meshGroup.scaleGlobal(se.getScaleFactorX(), se.getScaleFactorY(), se.getScaleFactorX(), MergeHelper.getInstance().getMergedBoundsForComponent(meshGroup).getCenterPointGlobal());				
					meshGroup.scaleGlobal(se.getScaleFactorX(), se.getScaleFactorY(), se.getScaleFactorX(), ComponentHelper.getCenterPointGlobal(meshGroup));
				return false;
			}
		});
				
		meshGroup.registerInputProcessor(new RotateProcessor(mtApplication));
		meshGroup.addGestureListener(RotateProcessor.class, new DefaultRotateAction());
		
		meshGroup.setGestureAllowance(RotateProcessor.class,true);
		
		meshGroup.registerInputProcessor(new TapProcessor(mtApplication,999999.0f));//high number for correct DragHelper behaviour when dragging a tapped object
		meshGroup.addGestureListener(TapProcessor.class, new CreateDragHelperAction(mtApplication,this.getCanvas(),this.getSceneCam(),meshGroup));
		
		//meshGroup.setGestureAllowance(ScaleProcessor.class, false);
		
		meshGroup.registerInputProcessor(new Rotate3DProcessor(mtApplication,meshGroup));
		 Rotate3DAction act = new Rotate3DAction(meshGroup,mtApplication);
		 drawAction.add(act);
		meshGroup.addGestureListener(Rotate3DProcessor.class,act);
		meshGroup.setGestureAllowance(Rotate3DProcessor.class,true);
		
		meshGroup.registerInputProcessor(new DragProcessor(mtApplication));
		meshGroup.addGestureListener(DragProcessor.class,new DefaultDragAction());
		meshGroup.setGestureAllowance(DragProcessor.class,true);

	}
	


}