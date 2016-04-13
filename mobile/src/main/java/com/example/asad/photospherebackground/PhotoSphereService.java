package com.example.asad.photospherebackground;


import android.content.res.AssetManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;

import com.google.vrtoolkit.cardboard.widgets.common.VrWidgetRenderer;
import com.google.vrtoolkit.cardboard.widgets.pano.PhotoSphereRenderer;
import com.google.vrtoolkit.cardboard.widgets.pano.VrPanoramaView;

import net.rbgrn.android.glwallpaperservice.GLWallpaperService;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by asad on 4/11/16.
 */
public class PhotoSphereService  extends GLWallpaperService {

    private static final String TAG = "PhotoSphereService";

    public PhotoSphereService() {
        super();
    }

    public Engine onCreateEngine() {
        MyEngine engine = new MyEngine();
        return engine;
    }

    class MyEngine extends GLEngine {

        private VrViewHelper panoWidgetView;

        /**
         * Tracks the file to be loaded across the lifetime of this app.
         **/
        private Uri fileUri = null; //use defult
        /**
         * Configuration information for the panorama.
         **/
        private VrPanoramaView.Options panoOptions = new VrPanoramaView.Options();
        private ImageLoaderTask backgroundImageLoaderTask;
        PhotoSphereRenderer renderer;

        public MyEngine() {
            super();

            panoOptions.inputType = VrPanoramaView.Options.TYPE_MONO;



            setEGLContextClientVersion(2);
            setEGLConfigChooser(8, 8, 8, 8, 16, 8);

            VrWidgetRenderer.GLThreadScheduler scheduler = new VrWidgetRenderer.GLThreadScheduler() {
                public void queueGlThreadEvent(Runnable runnable) {
                    queueEvent(runnable);
                }

            };

            panoWidgetView = new VrViewHelper(PhotoSphereService.this,scheduler);

            renderer = panoWidgetView.getRenderer();



            setRenderer(renderer);

            // Load the bitmap in a background thread to avoid blocking the UI thread. This operation can
            // take 100s of milliseconds.
            if (backgroundImageLoaderTask != null) {
                // Cancel any task from a previous intent sent to this activity.
                backgroundImageLoaderTask.cancel(true);
            }
            backgroundImageLoaderTask = new ImageLoaderTask();
            backgroundImageLoaderTask.execute(Pair.create(fileUri, panoOptions));
        }

        @Override
        public void onPause() {
            renderer.onPause();
            super.onPause();

        }

        @Override
        public void onResume() {
            super.onResume();
            renderer.onResume();;
        }

        public void onDestroy() {
            super.onDestroy();
            if (panoWidgetView != null) {
                panoWidgetView.shutdown();
            }
            renderer = null;

            // The background task has a 5 second timeout so it can potentially stay alive for 5 seconds
            // after the activity is destroyed unless it is explicitly cancelled.
            if (backgroundImageLoaderTask != null) {
                backgroundImageLoaderTask.cancel(true);
            }
        }

        /**
         * Helper class to manage threading.
         */
        class ImageLoaderTask extends AsyncTask<Pair<Uri, VrPanoramaView.Options>, Void, Boolean> {

            /**
             * Reads the bitmap from disk in the background and waits until it's loaded by pano widget.
             */
            @Override
            protected Boolean doInBackground(Pair<Uri, VrPanoramaView.Options>... fileInformation) {
                VrPanoramaView.Options panoOptions = null;  // It's safe to use null VrPanoramaView.Options.
                InputStream istr = null;
                if (fileInformation == null || fileInformation.length < 1
                        || fileInformation[0] == null || fileInformation[0].first == null) {
                    AssetManager assetManager = getAssets();
                    try {
                        istr = assetManager.open("andes.jpg");
                        panoOptions = new VrPanoramaView.Options();
                        panoOptions.inputType = VrPanoramaView.Options.TYPE_STEREO_OVER_UNDER;
                    } catch (IOException e) {
                        Log.e(TAG, "Could not decode default bitmap: " + e);
                        return false;
                    }
                } else {
                    try {
                        istr = new FileInputStream(new File(fileInformation[0].first.getPath()));
                        panoOptions = fileInformation[0].second;
                    } catch (IOException e) {
                        Log.e(TAG, "Could not load file: " + e);
                        return false;
                    }
                }

                panoWidgetView.loadImageFromBitmap(BitmapFactory.decodeStream(istr), panoOptions);
                try {
                    istr.close();
                } catch (IOException e) {
                    Log.e(TAG, "Could not close input stream: " + e);
                }

                return true;
            }
        }
    }
}

class MyRenderer implements GLWallpaperService.Renderer {
    public void onDrawFrame(GL10 gl) {
        // Your rendering code goes here

        gl.glClearColor(0.2f, 0.4f, 0.2f, 1f);
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
    }

    public void onSurfaceChanged(GL10 gl, int width, int height) {
    }

    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
    }

    /**
     * Called when the engine is destroyed. Do any necessary clean up because
     * at this point your renderer instance is now done for.
     */
    public void release() {
    }
}


