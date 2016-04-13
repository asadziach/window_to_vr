package com.example.asad.photospherebackground;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.vrtoolkit.cardboard.widgets.common.VrWidgetRenderer;
import com.google.vrtoolkit.cardboard.widgets.pano.PhtoSphereRenderer;
import com.google.vrtoolkit.cardboard.widgets.pano.VrPanoramaEventListener;
import com.google.vrtoolkit.cardboard.widgets.pano.VrPanoramaView;
import com.google.vrtoolkit.cardboard.widgets.pano.VrPanoramaView.Options;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class PhotoSphereViewer extends Activity {

    private static final String TAG = PhotoSphereViewer.class.getSimpleName();
    /** Actual panorama widget. **/
    private MyVrWidgetView panoWidgetView;
    /**
     * Arbitrary variable to track load status. In this example, this variable should only be accessed
     * on the UI thread. In a real app, this variable would be code that performs some UI actions when
     * the panorama is fully loaded.
     */
    private boolean loadImageSuccessful;
    /** Tracks the file to be loaded across the lifetime of this app. **/
    private Uri fileUri;
    /** Configuration information for the panorama. **/
    private VrPanoramaView.Options panoOptions = new Options();
    private ImageLoaderTask backgroundImageLoaderTask;

    private GLSurfaceView glview;
    /**
     * Called when the app is launched via the app icon or an intent using the adb command above. This
     * initializes the app and loads the image to render.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        panoWidgetView = new MyVrWidgetView(this);
//        panoWidgetView.setEventListener(new ActivityEventListener());
//        setContentView(panoWidgetView);

/* 196 */     WindowManager windowManager = (WindowManager)getSystemService("window");
/* 197 */     Display display = windowManager.getDefaultDisplay();
/* 198 */     DisplayMetrics displayMetrics = new DisplayMetrics();
/* 199 */     if (Build.VERSION.SDK_INT >= 17) {
/* 200 */       display.getRealMetrics(displayMetrics);
/*     */     } else {
/* 202 */       display.getMetrics(displayMetrics);
/*     */     }

         glview = new GLSurfaceView(this);
        glview.setEGLContextClientVersion(2);
/* 282 */     glview.setEGLConfigChooser(8, 8, 8, 8, 16, 8);
/* 283 */     glview.setPreserveEGLContextOnPause(true);
/*     */
/* 285 */     float xMetersPerPixel = 0.0254F / displayMetrics.xdpi;
/* 286 */     float yMetersPerPixel = 0.0254F / displayMetrics.ydpi;
/*     */
/* 288 */     VrWidgetRenderer.GLThreadScheduler scheduler = new VrWidgetRenderer.GLThreadScheduler()
/*     */     {
            /*     */       public void queueGlThreadEvent(Runnable runnable) {
/* 291 */         glview.queueEvent(runnable);
/*     */       }
/*     */
/* 294 */     };
        int screenRotation = getScreenRotationInDegrees(display.getRotation());
/* 295 */     PhtoSphereRenderer renderer = new PhtoSphereRenderer(this, scheduler, xMetersPerPixel, yMetersPerPixel, screenRotation, panoWidgetView);

/* 297 */     glview.setRenderer(renderer);

        // Initial launch of the app or an Activity recreation due to rotation.
        handleIntent(getIntent());
    }

    /*
     * Called when the Activity is already running and it's given a new intent.
     */
    @Override
    protected void onNewIntent(Intent intent) {
        Log.i(TAG, this.hashCode() + ".onNewIntent()");
        // Save the intent. This allows the getIntent() call in onCreate() to use this new Intent during
        // future invocations.
        setIntent(intent);
        // Load the new image.
        handleIntent(intent);
    }

    /**
     * Load custom images based on the Intent or load the default image. See the Javadoc for this
     * class for information on generating a custom intent via adb.
     */
    private void handleIntent(Intent intent) {
        // Determine if the Intent contains a file to load.
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            Log.i(TAG, "ACTION_VIEW Intent recieved");

            fileUri = intent.getData();
            if (fileUri == null) {
                Log.w(TAG, "No data uri specified. Use \"-d /path/filename\".");
            } else {
                Log.i(TAG, "Using file " + fileUri.toString());
            }

            panoOptions.inputType = intent.getIntExtra("inputType", Options.TYPE_MONO);
            Log.i(TAG, "Options.inputType = " + panoOptions.inputType);
        } else {
            Log.i(TAG, "Intent is not ACTION_VIEW. Using default pano image.");
            fileUri = null;
            panoOptions.inputType = Options.TYPE_MONO;
        }

        // Load the bitmap in a background thread to avoid blocking the UI thread. This operation can
        // take 100s of milliseconds.
        if (backgroundImageLoaderTask != null) {
            // Cancel any task from a previous intent sent to this activity.
            backgroundImageLoaderTask.cancel(true);
        }
        backgroundImageLoaderTask = new ImageLoaderTask();
        backgroundImageLoaderTask.execute(Pair.create(fileUri, panoOptions));
    }

    public boolean isLoadImageSuccessful() {
        return loadImageSuccessful;
    }

    @Override
    protected void onPause() {
        glview.onPause();
        panoWidgetView.pauseRendering();
        super.onPause();

    }

    @Override
    protected void onResume() {
        super.onResume();
        glview.onResume();
        panoWidgetView.resumeRendering();
    }

    @Override
    protected void onDestroy() {
        // Destroy the widget and free memory.
        panoWidgetView.shutdown();

        // The background task has a 5 second timeout so it can potentially stay alive for 5 seconds
        // after the activity is destroyed unless it is explicitly cancelled.
        if (backgroundImageLoaderTask != null) {
            backgroundImageLoaderTask.cancel(true);
        }
        super.onDestroy();
    }

    /**
     * Helper class to manage threading.
     */
    class ImageLoaderTask extends AsyncTask<Pair<Uri, Options>, Void, Boolean> {

        /**
         * Reads the bitmap from disk in the background and waits until it's loaded by pano widget.
         */
        @Override
        protected Boolean doInBackground(Pair<Uri, Options>... fileInformation) {
            Options panoOptions = null;  // It's safe to use null VrPanoramaView.Options.
            InputStream istr = null;
            if (fileInformation == null || fileInformation.length < 1
                    || fileInformation[0] == null || fileInformation[0].first == null) {
                AssetManager assetManager = getAssets();
                try {
                    istr = assetManager.open("andes.jpg");
                    panoOptions = new Options();
                    panoOptions.inputType = Options.TYPE_STEREO_OVER_UNDER;
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

    /**
     * Listen to the important events from widget.
     */
    private class ActivityEventListener extends VrPanoramaEventListener {
        /**
         * Called by pano widget on the UI thread when it's done loading the image.
         */
        @Override
        public void onLoadSuccess() {
            loadImageSuccessful = true;
        }

        /**
         * Called by pano widget on the UI thread on any asynchronous error.
         */
        @Override
        public void onLoadError(String errorMessage) {
            loadImageSuccessful = false;
            Toast.makeText(
                    PhotoSphereViewer.this, "Error loading pano: " + errorMessage, Toast.LENGTH_LONG)
                    .show();
            Log.e(TAG, "Error loading pano: " + errorMessage);
        }
    }

    /*     */   private int getScreenRotationInDegrees(int rotation) {
/* 471 */     switch (rotation) {
/*     */     case 1:
/* 473 */       return 90;
/*     */     case 2:
/* 475 */       return 180;
/*     */     case 3:
/* 477 */       return 270;
/*     */     }
/*     */
/* 480 */     return 0;
/*     */   }
}
