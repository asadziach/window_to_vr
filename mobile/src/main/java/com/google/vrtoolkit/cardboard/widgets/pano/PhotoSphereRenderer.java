package com.google.vrtoolkit.cardboard.widgets.pano;


import android.content.Context;

import com.google.vrtoolkit.cardboard.widgets.common.VrWidgetRenderer;

import java.lang.reflect.Field;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by asad on 4/13/16.
 */
public class PhotoSphereRenderer extends VrPanoramaRenderer{

    public PhotoSphereRenderer(Context context, VrWidgetRenderer.GLThreadScheduler glThreadScheduler, float xMetersPerPixel, float yMetersPerPixel, int screenRotation)
    {
        super(context, glThreadScheduler, xMetersPerPixel, yMetersPerPixel, screenRotation, null);
    }

    @Override
    public void onDrawFrame(GL10 gl) {

        Field field = null;
        long id = 0;
        try {
            field = VrWidgetRenderer.class.getDeclaredField("nativeRenderer");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        field.setAccessible(true);
        try {
            id = field.getLong(this);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        if(id != 0L) {
            this.nativeRenderFrame(id, 0, 0);
        }

    }
}
