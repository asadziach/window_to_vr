package com.example.asad.photospherebackground;


import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.google.vrtoolkit.cardboard.widgets.common.VrEventListener;
import com.google.vrtoolkit.cardboard.widgets.common.VrWidgetRenderer;
import com.google.vrtoolkit.cardboard.widgets.pano.PhotoSphereRenderer;
import com.google.vrtoolkit.cardboard.widgets.pano.VrPanoramaEventListener;
import com.google.vrtoolkit.cardboard.widgets.pano.VrPanoramaView;


public class VrViewHelper {


    private PhotoSphereRenderer renderer;

    private VrEventListener eventListener = new VrPanoramaEventListener();// Needed by renderer


    private DisplayMetrics displayMetrics;


    private boolean isPaused;


    private GLSurfaceView renderingView;

    private Context context;


    public VrViewHelper(Context context) {
        this.context = context;
        init();
    }


    GLSurfaceView getRenderingView() {
        return this.renderingView;
    }

    private void init() {


        WindowManager windowManager = (WindowManager) context.getSystemService("window");
        Display display = windowManager.getDefaultDisplay();
        this.displayMetrics = new DisplayMetrics();
        if (Build.VERSION.SDK_INT >= 17) {
            display.getRealMetrics(this.displayMetrics);
        } else {
            display.getMetrics(this.displayMetrics);
        }


        initializeRenderingView();


    }

    PhotoSphereRenderer getRenderer(){
        return this.renderer;
    }

    private void initializeRenderingView() {
        this.renderingView = new GLSurfaceView(context);
        this.renderingView.setEGLContextClientVersion(2);
        this.renderingView.setEGLConfigChooser(8, 8, 8, 8, 16, 8);
        this.renderingView.setPreserveEGLContextOnPause(true);

        float xMetersPerPixel = 0.0254F / this.displayMetrics.xdpi;
        float yMetersPerPixel = 0.0254F / this.displayMetrics.ydpi;

        VrWidgetRenderer.GLThreadScheduler scheduler = new VrWidgetRenderer.GLThreadScheduler() {
            public void queueGlThreadEvent(Runnable runnable) {
                VrViewHelper.this.renderingView.queueEvent(runnable);
            }

        };
        this.renderer = new PhotoSphereRenderer(context, scheduler, xMetersPerPixel, yMetersPerPixel, 0);
    }


    public void pauseRendering() {

        this.renderingView.onPause();

        this.renderer.onPause();

        this.isPaused = true;
    }


    public void resumeRendering() {

        this.renderingView.onResume();

        this.renderer.onResume();

        this.isPaused = false;
    }


    public void shutdown() {

        if (!this.isPaused) {

            throw new IllegalStateException("pauseRendering() must be called before calling shutdown().");
        }

        this.renderer.shutdown();
    }


    public void loadImageFromBitmap(Bitmap bitmap, VrPanoramaView.Options panoOptions) {

        if (panoOptions == null) {
            panoOptions = new VrPanoramaView.Options();
        } else {
            if ((panoOptions.inputType <= 0) || (panoOptions.inputType >= 3)) {
                int i = panoOptions.inputType;
                Log.e(this.getClass().toString(), 38 + "Invalid Options.inputType: " + i);
                panoOptions.inputType = 1;
            }
        }
        this.renderer.loadImageFromBitmap(bitmap, panoOptions, this.eventListener);
    }
}


