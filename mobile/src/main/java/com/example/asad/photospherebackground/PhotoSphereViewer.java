package com.example.asad.photospherebackground;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

public class PhotoSphereViewer extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GLSurfaceView glSurface = new GLSurfaceView(this);
        glSurface.setEGLContextClientVersion(2);
        glSurface.setRenderer( new MyGLRenderer());

        setContentView(glSurface);
    }
}
