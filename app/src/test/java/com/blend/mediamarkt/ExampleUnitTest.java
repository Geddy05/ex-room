package com.blend.mediamarkt;

import android.content.Context;
import android.test.AndroidTestCase;
import android.test.mock.MockContext;

import com.blend.mediamarkt.activities.MainActivity;
import com.blend.mediamarkt.apiHandlers.AudioApiHandler;
import com.blend.mediamarkt.enumerations.Sounds;
import com.blend.mediamarkt.enumerations.AudioOptions;
import com.blend.mediamarkt.activities.VuforiaActivity;
import com.blend.mediamarkt.scenes.WesternScene;
import com.blend.mediamarkt.vuforia.VuforiaController;
import com.threed.jpct.Loader;
import com.threed.jpct.Object3D;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({android.util.Log.class})
public class ExampleUnitTest extends AndroidTestCase {

    VuforiaActivity activity;
    VuforiaController vuforiaController;
    AudioApiHandler apiHandlerPlay;
    AudioApiHandler apiHandlerStop;
    boolean serverIsOnline = false;

    @Before
    public void setUp() {
        activity = new MainActivity();
        Context context = new MockContext();
        setContext(context);

        vuforiaController = new VuforiaController(activity);

        apiHandlerPlay = new AudioApiHandler(activity, AudioOptions.Play,Sounds.forest);
        apiHandlerStop = new AudioApiHandler(activity, AudioOptions.Stop,Sounds.forest);
    }

    @Test
    public void handleResponse_areCorrect() {
        AudioApiHandler apiHandler = new AudioApiHandler(activity, AudioOptions.Play,Sounds.forest);

        boolean result199 = apiHandler.responseIsSucceed(199);
        boolean result200 = apiHandler.responseIsSucceed(200);
        boolean result201 = apiHandler.responseIsSucceed(201);

        boolean result299 = apiHandler.responseIsSucceed(299);
        boolean result300 = apiHandler.responseIsSucceed(300);
        boolean result301 = apiHandler.responseIsSucceed(301);

        assertEquals(false,result199);
        assertEquals(true ,result200);
        assertEquals(true ,result201);

        assertEquals(true ,result299);
        assertEquals(false,result300);
        assertEquals(false,result301);
    }

    @Test
    public void SoundsEnum_isCorrect(){
        assertEquals(1,Sounds.the_good_the_bad_the_ugly.getId());
        assertEquals(2,Sounds.forest.getId());

        assertEquals(R.raw.the_good_the_bad_and_the_ugly,Sounds.the_good_the_bad_the_ugly.getSound());
        assertEquals(R.raw.forest,Sounds.forest.getSound());
    }

    @Test
    public void AudioEnum_isCorrect(){
        assertEquals("/sounds/"     ,    AudioOptions.Play.toString());
        assertEquals("/sounds/stop" ,    AudioOptions.Stop.toString());
    }

    @Test
    public void createURL_isCorrect() throws MalformedURLException {
        Sounds[] sounds =new Sounds[]{Sounds.the_good_the_bad_the_ugly,Sounds.forest};
        String url = Constants.baseUrl +"/sounds/";
        int count = 1;
        for (Sounds sound : sounds) {
            apiHandlerPlay = new AudioApiHandler(activity, AudioOptions.Play, sound);
            URL response = apiHandlerPlay.createURL();
            URL expectedURl =  new URL(url+count);
            assertEquals(expectedURl,response);
            count++;
        }
    }

    @Test
    public void urls_areCorrect(){
        if (serverIsOnline) {

            Sounds[] sounds = new Sounds[]{Sounds.forest,Sounds.the_good_the_bad_the_ugly,null};

        for (Sounds sound : sounds) {
            apiHandlerPlay = new AudioApiHandler(activity, AudioOptions.Play, sound);
            apiHandlerStop = new AudioApiHandler(activity, AudioOptions.Stop, null);

            boolean responsePlay = apiHandlerPlay.doInBackground();
            boolean responseStop = apiHandlerStop.doInBackground();

            assertEquals(true   ,responsePlay);
            assertEquals(true   ,responseStop);
            }
        }
    }

    @Test
    public void loadObjects_Correct() {
        //WesternScene west = new WesternScene(activity);

        try {
            InputStream streamObj = activity.getAssets().open("Snow covered CottageOBJ.obj");
            InputStream streamMtl = activity.getAssets().open("Snow covered CottageOBJ.mtl");
            //Object3D object = WesternScene.loadModel("house", streamObj,streamMtl);
            //assertEquals(object != null, true);
        }
        catch (IOException e){
            System.out.println(e);
        }
    }

    @Test
    public void createObjects_Correct() {
        try {
            InputStream streamObj = activity.getAssets().open("Snow covered CottageOBJ.obj");
            InputStream streamMtl = activity.getAssets().open("Snow covered CottageOBJ.mtl");
            Object3D[] model = Loader.loadOBJ(streamObj,streamMtl, 1.5f);

            Object3D object = WesternScene.createObject("house", model);
            assertEquals(object != null, true);
        }
        catch (IOException e){
            System.out.println(e);
        }
    }
}