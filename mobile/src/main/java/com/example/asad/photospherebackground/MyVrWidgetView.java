/*     */ package com.example.asad.photospherebackground;
/*     */ 
/*     */

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.google.vrtoolkit.cardboard.ScreenOnFlagHelper;
import com.google.vrtoolkit.cardboard.widgets.common.VrEventListener;
import com.google.vrtoolkit.cardboard.widgets.common.VrWidgetRenderer;
import com.google.vrtoolkit.cardboard.widgets.pano.PhotoSphereRenderer;
import com.google.vrtoolkit.cardboard.widgets.pano.VrPanoramaEventListener;
import com.google.vrtoolkit.cardboard.widgets.pano.VrPanoramaView;


/*     */ 
/*     */ 
/*     */ public class MyVrWidgetView
/*     */   extends FrameLayout
/*     */ {
/*     */ 
/*     */
/*     */   
/*     */   private PhotoSphereRenderer renderer;
/*     */
/*  74 */   private VrEventListener eventListener = new VrPanoramaEventListener();// Needed by renderere
/*     */   
/*     */ 
/*     */ 
/*     */   private DisplayMetrics displayMetrics;
/*     */   
/*     */ 
/*     */ 
/*     */   private boolean isPaused;
/*     */ 
/*     */ 
/*     */   private GLSurfaceView renderingView;

/*     */   

/*     */   
/*     */ 
/*     */ 
/*     */   public MyVrWidgetView(Context context, AttributeSet attrs)
/*     */   {
/* 158 */     super(context, attrs);

/* 160 */     init();
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */   public MyVrWidgetView(Context context)
/*     */   {
/* 168 */     super(context);
/* 170 */     init();
/*     */   }
/*     */   

/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   private void init()
/*     */   {
/*     */     
/*     */ 
/* 196 */     WindowManager windowManager = (WindowManager)getContext().getSystemService("window");
/* 197 */     Display display = windowManager.getDefaultDisplay();
/* 198 */     this.displayMetrics = new DisplayMetrics();
/* 199 */     if (Build.VERSION.SDK_INT >= 17) {
/* 200 */       display.getRealMetrics(this.displayMetrics);
/*     */     } else {
/* 202 */       display.getMetrics(this.displayMetrics);
/*     */     }
/*     */     
/*     */ 
/*     */ 
/* 207 */     initializeRenderingView();
/*     */
/* 215 */     addView(this.renderingView);
/*     */
/*     */
/*     */
/*     */   }
/*     */
/*     */   
/*     */   private void initializeRenderingView() {
/* 280 */     this.renderingView = new GLSurfaceView(getContext());
/* 281 */     this.renderingView.setEGLContextClientVersion(2);
/* 282 */     this.renderingView.setEGLConfigChooser(8, 8, 8, 8, 16, 8);
/* 283 */     this.renderingView.setPreserveEGLContextOnPause(true);
/*     */     
/* 285 */     float xMetersPerPixel = 0.0254F / this.displayMetrics.xdpi;
/* 286 */     float yMetersPerPixel = 0.0254F / this.displayMetrics.ydpi;
/*     */     
/* 288 */     VrWidgetRenderer.GLThreadScheduler scheduler = new VrWidgetRenderer.GLThreadScheduler()
/*     */     {
/*     */       public void queueGlThreadEvent(Runnable runnable) {
/* 291 */         MyVrWidgetView.this.renderingView.queueEvent(runnable);
/*     */       }
/*     */       
/* 294 */     };
/* 295 */     this.renderer = createRenderer(getContext(), scheduler, xMetersPerPixel, yMetersPerPixel, 
/* 296 */       0);
/* 297 */     this.renderingView.setRenderer(this.renderer);
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */   protected PhotoSphereRenderer createRenderer(Context paramContext, VrWidgetRenderer.GLThreadScheduler glThreadScheduler, float xMetersPerPixel, float yMetersPerPixel, int screenRotation){
               this.renderer = new PhotoSphereRenderer(getContext(), glThreadScheduler, xMetersPerPixel, yMetersPerPixel, screenRotation);

              return this.renderer;
            }
/*     */   

/*     */ 
/*     */   public void pauseRendering()
/*     */   {
/* 506 */     this.renderingView.onPause();
/* 507 */     this.renderer.onPause();
/* 509 */     this.isPaused = true;
/*     */   }
/*     */   

/*     */ 
/*     */   public void resumeRendering()
/*     */   {
/* 522 */     this.renderingView.onResume();
/* 523 */     this.renderer.onResume();

/*     */
/* 530 */     this.isPaused = false;
/*     */   }
/*     */   
/*     */ 

/*     */   public void shutdown()
/*     */   {
/* 546 */     if (!this.isPaused) {
/* 547 */       throw new IllegalStateException("pauseRendering() must be called before calling shutdown().");
/*     */     }
/*     */     
/* 550 */     this.renderer.shutdown();
/*     */   }
/*     */   
/*     */ 


    public void loadImageFromBitmap(Bitmap bitmap, VrPanoramaView.Options panoOptions)
    {

        if (panoOptions == null) {
            panoOptions = new VrPanoramaView.Options();
        } else {
            if ((panoOptions.inputType <= 0) || (panoOptions.inputType >= 3))
            {
                int i = panoOptions.inputType;Log.e(this.getClass().toString(), 38 + "Invalid Options.inputType: " + i);
                panoOptions.inputType = 1;
            }
        }
        this.renderer.loadImageFromBitmap(bitmap, panoOptions, this.eventListener);
    }
/*     */ }


