package com.blend.mediamarkt.utils;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.blend.mediamarkt.ExRoomSession;
import com.blend.mediamarkt.MainActivity;
import com.blend.mediamarkt.R;
import com.threed.jpct.Loader;
import com.threed.jpct.Matrix;
import com.vuforia.CameraCalibration;
import com.vuforia.Matrix44F;
import com.vuforia.Renderer;
import com.vuforia.State;
import com.vuforia.Tool;
import com.vuforia.Trackable;
import com.vuforia.TrackableResult;
import com.vuforia.Vec2F;
import com.vuforia.Vuforia;

import com.threed.jpct.Camera;
import com.threed.jpct.Config;
import com.threed.jpct.FrameBuffer;
import com.threed.jpct.Light;
import com.threed.jpct.Object3D;
import com.threed.jpct.Primitives;
import com.threed.jpct.SimpleVector;
//import com.threed.jpct.Texture;
import com.threed.jpct.TextureManager;
import com.threed.jpct.World;
import com.threed.jpct.util.BitmapHelper;
import com.threed.jpct.util.MemoryHelper;
import com.blend.mediamarkt.utils.Texture;


import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Vector;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by geddy on 21/05/16.
 */
public class exampleObject implements GLSurfaceView.Renderer {


    private static final String LOGTAG = "ImageTargetRenderer";
    private ExRoomSession vuforiaAppSession;
    private MainActivity mActivity;
    private Vector<com.blend.mediamarkt.utils.Texture> mTextures;

    private int texSampler2DHandle;
    private int shaderProgramID;

    private Renderer mRenderer;
    public boolean mIsActive = false;

    private World world;
    private Light sun;
    private Object3D cylinder;
    private Camera cam;
    private FrameBuffer fb;
    private float[] modelViewMat;
    private float fov;
    private float fovy;


    public exampleObject(MainActivity activity, ExRoomSession session,Vector<com.blend.mediamarkt.utils.Texture> texture) {
        mActivity = activity;
        vuforiaAppSession = session;

        mTextures = texture;

        world = new World();
        world.setAmbientLight(20, 20, 20);
        // set the following value according to your need, so the object won't be disappeared.
        world.setClippingPlanes(2.0f, 3000.0f);

        sun = new Light(world);
        sun.setIntensity(250, 250, 250);
        shaderProgramID = SampleUtils.createProgramFromShaderSrc(
                CubeShaders.CUBE_MESH_VERTEX_SHADER,
                CubeShaders.CUBE_MESH_FRAGMENT_SHADER);

        texSampler2DHandle = GLES20.glGetUniformLocation(shaderProgramID,
                "texSampler2D");


        try {

           for (Texture t : mTextures)
            {
                GLES20.glGenTextures(1, t.mTextureID, 0);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, t.mTextureID[0]);
                GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                        GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
                GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                        GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
                GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA,
                        t.mWidth, t.mHeight, 0, GLES20.GL_RGBA,
                        GLES20.GL_UNSIGNED_BYTE, t.mData);
            }

            // activate texture 0, bind it, and pass to shader
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,
                    mTextures.get(4).mTextureID[0]);
            GLES20.glUniform1i(texSampler2DHandle, 0);




            System.out.print("We are here");
            InputStream stream = mActivity.getAssets().open("Coconut Tree.3ds");

//            InputStream stream = new ByteArrayInputStream("coconut_tree".getBytes(StandardCharsets.UTF_8));
            Object3D[] objects = Loader.load3DS(stream, 1.0f);
            Object3D mModel3d = new Object3D(0);
            Object3D temp = null;
            for (int i = 0; i < objects.length; i++) {
                temp = objects[i];
                temp.rotateX(90.0f);
                temp.rotateMesh();
                temp.setCenter(SimpleVector.ORIGIN);
                temp.setRotationMatrix(new Matrix());
                mModel3d = Object3D.mergeObjects(mModel3d, temp);
//                mModel3d.setTexture();
//                mModel3d.setTexture("texture");
                mModel3d.build();

                mModel3d.scale(10.0f);

                world.addObject(mModel3d);

                // activate texture 0, bind it, and pass to shader
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,
                        mTextures.get(4).mTextureID[0]);
                GLES20.glUniform1i(texSampler2DHandle, 0);

                SimpleVector sv = new SimpleVector();
                sv.set(mModel3d.getTransformedCenter());
                sv.y += 100;
                sv.z += 100;
                sun.setPosition(sv);
            }

        }catch (Exception e){
            // Create a texture out of the icon...:-)
//            if ( !TextureManager.getInstance().containsTexture("texture") ) {
//                Texture texture = new Texture(BitmapHelper.rescale(BitmapHelper.convert(
//                        mActivity.getResources().getDrawable(R.mipmap.ic_launcher)), 64, 64));
//                TextureManager.getInstance().addTexture("texture", texture);
//            }
            System.out.print(e);

            cylinder = Primitives.getCylinder(20, 40);
            cylinder.calcTextureWrapSpherical();
            cylinder.setTexture("texture");
            cylinder.strip();
            cylinder.build();

            // Transform (scale, rotate, translate) the object: Depends on your need.
//    	cylinder.scale(scale);
            cylinder.rotateX(90.0f);
//    	cylinder.rotateY(w); cylinder.rotateZ(w);
//    	cylinder.translate(x, y, z);

            world.addObject(cylinder);

        }
        cam = world.getCamera();

        // for older Android versions, which had massive problems with garbage collection
        MemoryHelper.compact();

    }


    // Called to draw the current frame.
    @Override
    public void onDrawFrame(GL10 gl) {
        if (!mIsActive)
            return;

        // Call our function to render content
        renderFrame();

        updateCamera();
        world.renderScene(fb);
        world.draw(fb);
        fb.display();

    }


    // Called when the surface is created or recreated.
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.d(LOGTAG, "GLRenderer.onSurfaceCreated");

        initRendering(); // NOTE: Cocokin sama cpp - DONE

        // Call Vuforia function to (re)initialize rendering after first use
        // or after OpenGL ES context was lost (e.g. after onPause/onResume):
        vuforiaAppSession.onSurfaceCreated();
    }


    // Called when the surface changed size.
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.d(LOGTAG, "GLRenderer.onSurfaceChanged");

        if (fb != null) {
            fb.dispose();
        }
        fb = new FrameBuffer(width, height);
        Config.viewportOffsetAffectsRenderTarget = true;

        updateRendering(width, height);

        // Call Vuforia function to handle render surface size changes:
        vuforiaAppSession.onSurfaceChanged(width, height);
    }


    // Function for initializing the renderer.
    private void initRendering() {
        mRenderer = Renderer.getInstance();

        // Define clear color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, Vuforia.requiresAlpha() ? 0.0f : 1.0f);

//        // Hide the Loading Dialog
//        mActivity.loadingDialogHandler
//                .sendEmptyMessage(LoadingDialogHandler.HIDE_LOADING_DIALOG);
    }

    private void updateRendering(int width, int height) {

        // Update screen dimensions
//        vuforiaAppSession.setmScreenWidth(width);
//        vuforiaAppSession.setmScreenHeight(height);

        // Reconfigure the video background
        vuforiaAppSession.configureVideoBackground();

        CameraCalibration camCalibration = com.vuforia.CameraDevice.getInstance().getCameraCalibration();
        Vec2F size = camCalibration.getSize();
        Vec2F focalLength = camCalibration.getFocalLength();
        float fovyRadians = (float) (2 * Math.atan(0.5f * size.getData()[1] / focalLength.getData()[1]));
        float fovRadians = (float) (2 * Math.atan(0.5f * size.getData()[0] / focalLength.getData()[0]));

//        if (vuforiaAppSession.mIsPortrait) {
            setFovy(fovRadians);
            setFov(fovyRadians);
//        } else {
//            setFov(fovRadians);
//            setFovy(fovyRadians);
//        }

    }

    // The render function.
    private void renderFrame() {
        // clear color and depth buffer
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        // get the state, and mark the beginning of a rendering section
        State state = mRenderer.begin();
        // explicitly render the video background
        mRenderer.drawVideoBackground();

        float[] modelviewArray = new float[16];
        // did we find any trackables this frame?
        for (int tIdx = 0; tIdx < state.getNumTrackableResults(); tIdx++) {
            // get the trackable
            TrackableResult result = state.getTrackableResult(tIdx);
            Trackable trackable = result.getTrackable();
            printUserData(trackable);

            Matrix44F modelViewMatrix = Tool.convertPose2GLMatrix(result.getPose());
            Matrix44F inverseMV = SampleMath.Matrix44FInverse(modelViewMatrix);
            Matrix44F invTranspMV = SampleMath.Matrix44FTranspose(inverseMV);

            modelviewArray = invTranspMV.getData();
            updateModelviewMatrix(modelviewArray);

        }
        // hide the objects when the targets are not detected
        if (state.getNumTrackableResults() == 0) {
            float m [] = {
                    1,0,0,0,
                    0,1,0,0,
                    0,0,1,0,
                    0,0,-10000,1
            };
            modelviewArray = m;
            updateModelviewMatrix(modelviewArray);
        }

        mRenderer.end();
    }


    private void printUserData(Trackable trackable) {
        String userData = (String) trackable.getUserData();
        Log.d(LOGTAG, "UserData:Retreived User Data	\"" + userData + "\"");
    }

    private void updateModelviewMatrix(float mat[]) {
        modelViewMat = mat;
    }

    private void updateCamera() {
        if (modelViewMat != null) {
            float[] m = modelViewMat;

            final SimpleVector camUp;
//            if (vuforiaAppSession.mIsPortrait) {
                camUp = new SimpleVector(-m[0], -m[1], -m[2]);
//            } else {
//                camUp = new SimpleVector(-m[4], -m[5], -m[6]);
//            }

            final SimpleVector camDirection = new SimpleVector(m[8], m[9], m[10]);
            final SimpleVector camPosition = new SimpleVector(m[12], m[13], m[14]);

            cam.setOrientation(camDirection, camUp);
            cam.setPosition(camPosition);

            cam.setFOV(fov);
            cam.setYFOV(fovy);
        }
    }

    private void setFov(float fov) {
        this.fov = fov;
    }

    private void setFovy(float fovy) {
        this.fovy = fovy;
    }
}
