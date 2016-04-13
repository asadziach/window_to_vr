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


    private Context context;

    VrWidgetRenderer.GLThreadScheduler scheduler;

    public VrViewHelper(Context context, VrWidgetRenderer.GLThreadScheduler scheduler) {
        this.context = context;
        this.scheduler = scheduler;
        init();
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


        float xMetersPerPixel = 0.0254F / this.displayMetrics.xdpi;
        float yMetersPerPixel = 0.0254F / this.displayMetrics.ydpi;


        this.renderer = new PhotoSphereRenderer(context, scheduler, xMetersPerPixel, yMetersPerPixel, 0);
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


