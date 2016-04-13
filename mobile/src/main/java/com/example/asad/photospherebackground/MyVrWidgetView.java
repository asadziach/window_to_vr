/*     */ package com.example.asad.photospherebackground;
/*     */ 
/*     */

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.google.vr.cardboard.UiLayer;
import com.google.vr.cardboard.VrParamsProvider;
import com.google.vr.cardboard.VrParamsProviderFactory;
import com.google.vrtoolkit.cardboard.ScreenOnFlagHelper;
import com.google.vrtoolkit.cardboard.proto.nano.CardboardDevice;
import com.google.vrtoolkit.cardboard.widgets.common.FullScreenDialog;
import com.google.vrtoolkit.cardboard.widgets.common.TrackingSensorsHelper;
import com.google.vrtoolkit.cardboard.widgets.common.VrEventListener;
import com.google.vrtoolkit.cardboard.widgets.common.VrWidgetRenderer;
import com.google.vrtoolkit.cardboard.widgets.common.VrWidgetView;
import com.google.vrtoolkit.cardboard.widgets.pano.PhtoSphereRenderer;
import com.google.vrtoolkit.cardboard.widgets.pano.VrPanoramaEventListener;
import com.google.vrtoolkit.cardboard.widgets.pano.VrPanoramaView;

/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */

/*     */
/*     */ 
/*     */ 
/*     */ 
/*     */ public class MyVrWidgetView
/*     */   extends VrWidgetView
/*     */ {
/*  41 */   private static final String TAG = MyVrWidgetView.class.getSimpleName();
/*     */   
/*     */   private static final boolean DEBUG = false;
/*     */   
/*  45 */   private static final Uri INFO_BUTTON_URL = Uri.parse("https://g.co/vr/view");
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */   private static final String STATE_KEY_SUPER_CLASS = "superClassState";
/*     */   
/*     */ 
/*     */ 
/*     */   private static final String STATE_KEY_ORIENTATION_HELPER = "orientationHelperState";
/*     */   
/*     */ 
/*     */ 
/*     */   private static final String STATE_KEY_IS_FULL_SCREEN = "isFullScreen";
/*     */   
/*     */ 
/*     */ 
/*     */   private static final String STATE_KEY_IS_VR_MODE = "isVrMode";
/*     */   
/*     */ 
/*     */ 
/*     */   private static final float METERS_PER_INCH = 0.0254F;
/*     */   
/*     */ 
/*     */ 
/*  70 */   private PointF offsetDegrees = new PointF();
/*     */   
/*     */   private PhtoSphereRenderer renderer;
/*     */   
/*  74 */   private VrEventListener eventListener = new VrPanoramaEventListener();
/*     */   
/*     */ 
/*     */ 
/*     */   private DisplayMetrics displayMetrics;
/*     */   
/*     */ 
/*     */ 
/*     */   private Activity activity;
/*     */   
/*     */ 
/*     */ 
/*     */   private boolean isPaused;
/*     */   
/*     */ 
/*     */ 
/*     */   private VrParamsProvider viewerParamsProvider;
/*     */   
/*     */ 
/*     */ 
/*     */   private ViewGroup innerWidgetView;
/*     */   
/*     */ 
/*     */ 
/*     */   private GLSurfaceView renderingView;
/*     */   
/*     */ 
/*     */ 
/*     */   private View uiView;
/*     */   
/*     */ 
/*     */ 
/*     */   private ImageButton enterCardboardButton;
/*     */   
/*     */ 
/*     */ 
/*     */   private ImageButton enterFullscreenButton;
/*     */   
/*     */ 
/*     */ 
/*     */   private ImageButton fullscreenBackButton;
/*     */   
/*     */ 
/*     */ 
/*     */   private ImageButton infoButton;
/*     */   
/*     */ 
/*     */ 
/*     */   private boolean isCardboardButtonEnabled;
/*     */   
/*     */ 
/*     */   private boolean isFullscreenButtonEnabled;
/*     */   
/*     */ 
/*     */   private boolean isInfoButtonEnabled;
/*     */   
/*     */ 
/*     */   public FullScreenDialog fullScreenDialog;
/*     */   
/*     */ 
/*     */   private TrackingSensorsHelper sensorsHelper;
/*     */   
/*     */ 
/*     */   private ScreenOnFlagHelper screenOnFlagHelper;
/*     */   
/*     */ 
/*     */   private OrientationHelper orientationHelper;
/*     */   
/*     */ 
/*     */   private ViewRotator viewRotator;
/*     */   
/*     */ 
/*     */   private boolean isFullScreen;
/*     */   
/*     */ 
/*     */   UiLayer vrUiLayer;
/*     */   
/*     */ 
/*     */   private boolean isVrMode;
/*     */   
/*     */ 
/*     */ 
/*     */   public MyVrWidgetView(Context context, AttributeSet attrs)
/*     */   {
/* 158 */     super(context, attrs);
/* 159 */     checkContextIsActivity(context);
/* 160 */     init();
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */   public MyVrWidgetView(Context context)
/*     */   {
/* 168 */     super(context);
/* 169 */     checkContextIsActivity(context);
/* 170 */     init();
/*     */   }
/*     */   
/*     */   private void checkContextIsActivity(Context context) {
/* 174 */     if (!(context instanceof Activity)) {
/* 175 */       throw new RuntimeException("Context must be an instance of activity");
/*     */     }
/* 177 */     this.activity = ((Activity)context);
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   private void init()
/*     */   {
/* 186 */     this.viewerParamsProvider = VrParamsProviderFactory.create(getContext());
/*     */     
/* 188 */     this.sensorsHelper = new TrackingSensorsHelper(getContext().getPackageManager());
              isFullScreen = true;
/* 189 */     this.isCardboardButtonEnabled = false;// this.sensorsHelper.areTrackingSensorsAvailable();
/* 190 */     this.isFullscreenButtonEnabled = true;
/* 191 */     this.isInfoButtonEnabled = false;
/*     */     
/* 193 */     this.screenOnFlagHelper = new ScreenOnFlagHelper(this.activity);
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
/* 207 */     initializeRenderingView(display.getRotation());
/* 208 */     this.innerWidgetView = new FrameLayout(getContext());
/*     */     
/* 210 */     this.innerWidgetView.setId(R.id.vrwidget_inner_view);
/* 211 */     this.innerWidgetView.addView(this.renderingView);
/*     */     
/*     */ 
/* 214 */     setPadding(0, 0, 0, 0);
/* 215 */     addView(this.innerWidgetView);
/*     */     
/* 217 */     this.orientationHelper = new OrientationHelper(this.activity);
/*     */     
/* 219 */     this.fullScreenDialog = new FullScreenDialog(getContext(), this.innerWidgetView, this.renderer);
/* 220 */     this.fullScreenDialog.setOnCancelListener(new OnCancelListener()
/*     */     {
/*     */       public void onCancel(DialogInterface dialog) {
/* 223 */         MyVrWidgetView.this.isFullScreen = false;
/* 224 */         MyVrWidgetView.this.toggleFullScreen();
/*     */       }
/*     */       
/* 227 */     });
/* 228 */     this.uiView = inflate(getContext(), R.layout.ui_view_embed, null);
/*     */     
/*     */ 
/*     */ 
/* 232 */     this.viewRotator = new ViewRotator(getContext(), this.uiView, getScreenRotationInDegrees(display.getRotation()), this.sensorsHelper.areTrackingSensorsAvailable());
/* 233 */     this.innerWidgetView.addView(this.uiView);
/*     */     
/*     */ 
/* 236 */     this.innerWidgetView.addView(new View(getContext()));
/*     */     
/* 238 */     this.vrUiLayer = new UiLayer(getContext());
/*     */     
/*     */ 
/* 241 */     this.vrUiLayer.setPortraitSupportEnabled(true);
/* 242 */     this.vrUiLayer.setEnabled(true);
/* 243 */     this.innerWidgetView.addView(this.vrUiLayer.getView());
/*     */     
/* 245 */     initializeUiButtons();
/* 246 */     initializeTouchTracker();
/*     */   }
/*     */   
/*     */   private void initializeTouchTracker() {
/* 250 */     TouchTracker touchTracker = new TouchTracker(getContext(), new TouchTracker.TouchEnabledVrView()
/*     */     {
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */       public void setYawPitchOffset(float yaw, float pitch)
/*     */       {
/*     */ 
/*     */ 
/*     */ 
/* 261 */         MyVrWidgetView.this.offsetDegrees.set(yaw, pitch);
/*     */       }
/*     */       
/*     */       public VrEventListener getEventListener()
/*     */       {
/* 266 */         return MyVrWidgetView.this.eventListener;
/*     */       }
/*     */     });
/*     */     
/*     */ 
/* 271 */     if (this.sensorsHelper.areTrackingSensorsAvailable())
/*     */     {
/* 273 */       touchTracker.setTouchSpeed(0.0F, 0.0F);
/*     */     }
/*     */     
/* 276 */     setOnTouchListener(touchTracker);
/*     */   }
/*     */   
/*     */   private void initializeRenderingView(int rotation) {
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
/* 296 */       getScreenRotationInDegrees(rotation));
/* 297 */     this.renderingView.setRenderer(this.renderer);
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */   protected  PhtoSphereRenderer createRenderer(Context paramContext, VrWidgetRenderer.GLThreadScheduler glThreadScheduler, float xMetersPerPixel, float yMetersPerPixel, int screenRotation){
               this.renderer = new PhtoSphereRenderer(getContext(), glThreadScheduler, xMetersPerPixel, yMetersPerPixel, screenRotation, this);

              return this.renderer;
            }
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */   private void initializeUiButtons()
/*     */   {
/* 310 */     this.enterFullscreenButton = ((ImageButton)this.uiView.findViewById(R.id.fullscreen_button));
/* 311 */     this.enterFullscreenButton.setOnClickListener(new OnClickListener()
/*     */     {
/*     */       public void onClick(View v) {
/* 314 */         MyVrWidgetView.this.isVrMode = false;
/* 315 */         MyVrWidgetView.this.isFullScreen = true;
/* 316 */         MyVrWidgetView.this.toggleFullScreen();
/*     */       }
/*     */       
/* 319 */     });
/* 320 */     this.enterCardboardButton = ((ImageButton)this.uiView.findViewById(R.id.cardboard_button));
/* 321 */     this.enterCardboardButton.setOnClickListener(new OnClickListener()
/*     */     {
/*     */       public void onClick(View v) {
/* 324 */         MyVrWidgetView.this.isVrMode = true;
/* 325 */         MyVrWidgetView.this.isFullScreen = true;
/* 326 */         MyVrWidgetView.this.toggleFullScreen();
/*     */       }
/*     */       
/* 329 */     });
/* 330 */     this.fullscreenBackButton = ((ImageButton)this.uiView.findViewById(R.id.fullscreen_back_button));
/* 331 */     this.fullscreenBackButton.setOnClickListener(new OnClickListener()
/*     */     {
/*     */       public void onClick(View v) {
/* 334 */         MyVrWidgetView.this.isVrMode = false;
/* 335 */         MyVrWidgetView.this.isFullScreen = false;
/* 336 */         MyVrWidgetView.this.toggleFullScreen();
/*     */       }
/*     */       
/* 339 */     });
/* 340 */     this.infoButton = ((ImageButton)this.uiView.findViewById(R.id.info_button));
/* 341 */     this.infoButton.setOnClickListener(new OnClickListener()
/*     */     {
/*     */       public void onClick(View v) {
/* 344 */         MyVrWidgetView.this.activity.startActivity(MyVrWidgetView.getInfoButtonIntent());
/*     */       }
/*     */       
/* 347 */     });
/* 348 */     updateButtonVisibility();
/*     */   }
/*     */   
/*     */   private void toggleFullScreen()
/*     */   {
/* 353 */     if (!this.isFullScreen)
/*     */     {
/* 355 */       this.isVrMode = false;
/*     */     }
/*     */     
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/* 363 */     updateVrMode();
/*     */     
/* 365 */     if (this.isFullScreen)
/*     */     {
/* 367 */       this.orientationHelper.lockOrientation();
/*     */       
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/* 374 */       this.fullScreenDialog.show();
/*     */     } else {
/* 376 */       this.fullScreenDialog.dismiss();
/* 377 */       this.orientationHelper.restoreOriginalOrientation();
/*     */     }
/*     */     
/* 380 */     updateControlsLayout();
/*     */     
/* 382 */     this.eventListener.onDisplayModeChanged(getDisplayMode());
/*     */   }
/*     */   
/*     */   private void updateControlsLayout() {
/* 386 */     LinearLayout controlLayout = (LinearLayout)this.innerWidgetView.findViewById(R.id.control_layout);
/*     */     
/* 388 */     RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)controlLayout.getLayoutParams();
/*     */     
/*     */ 
/*     */ 
/*     */ 
/* 393 */     if ((this.isFullScreen) && (this.isVrMode) && (this.orientationHelper.isInPortraitOrientation())) {
/* 394 */       layoutParams.addRule(9);
/*     */       
/* 396 */       layoutParams.addRule(11, 0);
/*     */     } else {
/* 398 */       layoutParams.addRule(9, 0);
/* 399 */       layoutParams.addRule(11);
/*     */     }
/* 401 */     controlLayout.setLayoutParams(layoutParams);
/*     */     
/*     */ 
/* 404 */     if ((this.isFullScreen) && (!this.isVrMode)) {
/* 405 */       this.viewRotator.enable();
/*     */     } else {
/* 407 */       this.viewRotator.disable();
/*     */     }
/*     */   }
/*     */   
/*     */   private void updateVrMode() {
/* 412 */     this.renderer.setVrMode(this.isVrMode);
/*     */     
/* 414 */     if (this.isVrMode) {
/* 415 */       this.screenOnFlagHelper.start();
/*     */     } else {
/* 417 */       this.screenOnFlagHelper.stop();
/*     */     }
/*     */     
/* 420 */     updateButtonVisibility();
/* 421 */     updateViewerName();
/*     */   }
/*     */   
/*     */   private void updateButtonVisibility() {
/* 425 */     if ((this.isFullscreenButtonEnabled) && ((this.isVrMode) || (!this.isFullScreen))) {
/* 426 */       this.enterFullscreenButton.setVisibility(0);
/*     */     } else {
/* 428 */       this.enterFullscreenButton.setVisibility(8);
/*     */     }
/*     */     
/* 431 */     if ((this.isCardboardButtonEnabled) && (!this.isVrMode)) {
/* 432 */       this.enterCardboardButton.setVisibility(0);
/*     */     } else {
/* 434 */       this.enterCardboardButton.setVisibility(8);
/*     */     }
/*     */     
/* 437 */     this.vrUiLayer.setSettingsButtonEnabled(this.isVrMode);
/* 438 */     this.vrUiLayer.setAlignmentMarkerEnabled(this.isVrMode);
/* 439 */     this.vrUiLayer.setTransitionViewEnabled(this.isVrMode);
/*     */     
/* 441 */     if (!this.isFullScreen)
/*     */     {
/* 443 */       this.fullscreenBackButton.setVisibility(8);
/* 444 */       this.vrUiLayer.setBackButtonListener(null);
/*     */     }
/* 446 */     else if (this.isVrMode)
/*     */     {
/*     */ 
/*     */ 
/*     */ 
/* 451 */       this.fullscreenBackButton.setVisibility(8);
/* 452 */       this.vrUiLayer.setBackButtonListener(new Runnable()
/*     */       {
/*     */         public void run() {
/* 455 */           MyVrWidgetView.this.isVrMode = false;
/* 456 */           MyVrWidgetView.this.isFullScreen = false;
/* 457 */           MyVrWidgetView.this.toggleFullScreen();
/*     */         }
/*     */       });
/*     */     }
/*     */     else {
/* 462 */       this.fullscreenBackButton.setVisibility(0);
/* 463 */       this.vrUiLayer.setBackButtonListener(null);
/*     */     }
/*     */     
/*     */ 
/* 467 */     this.infoButton.setVisibility((this.isInfoButtonEnabled) && (!this.isVrMode) ? 0 : 8);
/*     */   }
/*     */   
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
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   public void setEventListener(VrEventListener eventListener)
/*     */   {
/* 494 */         super.setEventListener(eventListener);
                this.eventListener = eventListener;
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   public void pauseRendering()
/*     */   {
/* 506 */     this.renderingView.onPause();
/* 507 */     this.renderer.onPause();
/* 508 */     this.screenOnFlagHelper.stop();
/* 509 */     this.isPaused = true;
/* 510 */     this.viewRotator.disable();
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   public void resumeRendering()
/*     */   {
/* 522 */     this.renderingView.onResume();
/* 523 */     this.renderer.onResume();
/* 524 */     updateVrMode();
/* 525 */     if (this.isFullScreen) {
/* 526 */       this.fullScreenDialog.show();
/*     */     }
/* 528 */     updateButtonVisibility();
/* 529 */     updateControlsLayout();
/* 530 */     this.isPaused = false;
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
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
/*     */ 
/*     */   PointF getYawPitchOffset()
/*     */   {
/* 557 */     return this.offsetDegrees;
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   public void setCardboardButtonEnabled(boolean enabled)
/*     */   {
/* 569 */     boolean sensorsAvailable = this.sensorsHelper.areTrackingSensorsAvailable();
/* 570 */     if ((enabled) && (!sensorsAvailable)) {
/* 571 */       Log.w(TAG, "This phone doesn't have the necessary sensors for head tracking, Cardboard button will be disabled.");
/*     */     }
/*     */     
/* 574 */     this.isCardboardButtonEnabled = ((enabled) && (sensorsAvailable));
/* 575 */     updateButtonVisibility();
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   public void setFullscreenButtonEnabled(boolean enabled)
/*     */   {
/* 584 */     this.isFullscreenButtonEnabled = enabled;
/* 585 */     updateButtonVisibility();
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   public void setInfoButtonEnabled(boolean enabled)
/*     */   {
/* 595 */     this.isInfoButtonEnabled = enabled;
/* 596 */     updateButtonVisibility();
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   public void setOnTouchListener(OnTouchListener touchListener)
/*     */   {
/* 605 */     super.setOnTouchListener(touchListener);
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   protected Parcelable onSaveInstanceState()
/*     */   {
/* 617 */     Bundle bundle = new Bundle();
/* 618 */     bundle.putParcelable("superClassState", super.onSaveInstanceState());
/* 619 */     bundle.putBundle("orientationHelperState", this.orientationHelper.onSaveInstanceState());
/* 620 */     bundle.putBoolean("isFullScreen", this.isFullScreen);
/* 621 */     bundle.putBoolean("isVrMode", this.isVrMode);
/* 622 */     return bundle;
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */   protected void onRestoreInstanceState(Parcelable state)
/*     */   {
/* 630 */     if ((state instanceof Bundle)) {
/* 631 */       Bundle bundle = (Bundle)state;
/* 632 */       this.orientationHelper.onRestoreInstanceState(bundle.getBundle("orientationHelperState"));
/* 633 */       this.isFullScreen = bundle.getBoolean("isFullScreen");
/* 634 */       this.isVrMode = bundle.getBoolean("isVrMode");
/* 635 */       state = bundle.getParcelable("superClassState");
/*     */     }
/* 637 */     super.onRestoreInstanceState(state);
/*     */   }
/*     */   
/*     */   private int getDisplayMode() {
/* 641 */     if (!this.isFullScreen) {
/* 642 */       return 1;
/*     */     }
/* 644 */     return this.isVrMode ? 3 : 2;
/*     */   }
/*     */   
/*     */   static Intent getInfoButtonIntent() {
/* 648 */     return new Intent("android.intent.action.VIEW", INFO_BUTTON_URL);
/*     */   }
/*     */   
/*     */   private void updateViewerName() {
/* 652 */     CardboardDevice.DeviceParams deviceProto = this.viewerParamsProvider.readDeviceParams();
/* 653 */     this.vrUiLayer.setViewerName(deviceProto == null ? null : deviceProto.getModel());
/*     */   }
/*     */   
/*     */   public static abstract class DisplayMode
/*     */   {
/*     */     public static final int EMBEDDED = 1;
/*     */     public static final int FULLSCREEN_MONO = 2;
/*     */     public static final int FULLSCREEN_VR = 3;
/*     */   }
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


/* Location:              /work/hobby/android_experiments/temp/widgets/commonwidget.aar_FILES/classes.jar!/com/google/vrtoolkit/cardboard/widgets/common/VrWidgetView.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */

/*     */ class ViewRotator
/*     */ {
    /*     */   private static final int ORIENTATION_DELTA_THRESHOLD_DEGREES = 70;
    /*     */   private final View view;
    /*     */   private int currentViewOrientation90Inc;
    /*     */   private final int initialRotationDegrees;
    /*     */   private int originalViewWidth;
    /*     */   private int originalViewHeight;
    /*     */   private OrientationEventListener orientationEventListener;
    /*     */
/*     */   public ViewRotator(Context context, View view, int initialRotationDegrees, final boolean trackingSensorsAvailable)
/*     */   {
/*  53 */     if (!isViewProperlyConfigured(view)) {
/*  54 */       throw new IllegalArgumentException("View should have MATCH_PARENT layout and no translation.");
/*     */     }
/*     */
/*     */
/*  58 */     if (initialRotationDegrees < 180) {
/*  59 */       this.initialRotationDegrees = initialRotationDegrees;
/*     */     }
/*     */     else {
/*  62 */       this.initialRotationDegrees = (initialRotationDegrees - 180);
/*     */     }
/*     */
/*  65 */     this.view = view;
/*  66 */     this.orientationEventListener = new OrientationEventListener(context)
/*     */     {
            /*     */       public void onOrientationChanged(int orientation) {
/*  69 */         if (!trackingSensorsAvailable) {
/*  70 */           return;
/*     */         }
/*     */
/*  73 */         if (orientation == -1) {
/*  74 */           return;
/*     */         }
/*     */
/*  77 */         orientation += ViewRotator.this.initialRotationDegrees;
/*     */
/*     */
/*  80 */         if (orientation > 180) {
/*  81 */           orientation -= 360;
/*     */         }
/*     */
/*     */
/*     */
/*  86 */         int orientationDelta = orientation - ViewRotator.this.currentViewOrientation90Inc;
/*     */
/*     */
/*     */
/*  90 */         if (orientationDelta > 180) {
/*  91 */           orientationDelta = 360 - orientationDelta;
/*     */         }
/*  93 */         if (orientationDelta < 65356) {
/*  94 */           orientationDelta = 360 + orientationDelta;
/*     */         }
/*     */
/*     */
/*     */
/*  99 */         if (Math.abs(orientationDelta) > 70) {
/* 100 */           ViewRotator.this.rotateView(orientation);
/*     */         }
/*     */       }
/*     */     };
/*     */   }
    /*     */
/*     */
/*     */
/*     */   public void enable()
/*     */   {
/* 110 */     this.orientationEventListener.enable();
/*     */   }
    /*     */
/*     */
/*     */
/*     */   public void disable()
/*     */   {
/* 117 */     this.orientationEventListener.disable();
/*     */
/* 119 */     ViewGroup.LayoutParams layoutParams = this.view.getLayoutParams();
/* 120 */     layoutParams.height = -1;
/* 121 */     layoutParams.width = -1;
/* 122 */     this.view.setTranslationY(0.0F);
/* 123 */     this.view.setTranslationX(0.0F);
/* 124 */     this.view.setRotation(0.0F);
/* 125 */     this.originalViewWidth = 0;
/* 126 */     this.originalViewHeight = 0;
/*     */   }
    /*     */
/*     */   private void rotateView(int newPhoneOrientation)
/*     */   {
/* 131 */     if (this.view.getParent() == null) {
/* 132 */       return;
/*     */     }
/* 134 */     if ((this.originalViewWidth == 0) || (this.originalViewHeight == 0)) {
/* 135 */       this.originalViewWidth = this.view.getWidth();
/* 136 */       this.originalViewHeight = this.view.getHeight();
/* 137 */       if ((this.originalViewWidth == 0) || (this.originalViewHeight == 0)) {
/* 138 */         return;
/*     */       }
/*     */     }
/*     */
/*     */
/*     */
/* 144 */     this.currentViewOrientation90Inc = getNearestOrientationWith90Inc(newPhoneOrientation);
/* 145 */     this.view.setRotation(-this.currentViewOrientation90Inc);
/*     */
/* 147 */     ViewGroup.LayoutParams layoutParams = this.view.getLayoutParams();
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/* 161 */     if (this.currentViewOrientation90Inc % 180 != 0) {
/* 162 */       layoutParams.height = this.originalViewWidth;
/* 163 */       layoutParams.width = this.originalViewHeight;
/* 164 */       this.view.setTranslationX((this.originalViewWidth - this.originalViewHeight) / 2);
/* 165 */       this.view.setTranslationY((this.originalViewHeight - this.originalViewWidth) / 2);
/*     */     }
/*     */     else {
/* 168 */       layoutParams.height = this.originalViewHeight;
/* 169 */       layoutParams.width = this.originalViewWidth;
/* 170 */       this.view.setTranslationY(0.0F);
/* 171 */       this.view.setTranslationX(0.0F);
/*     */     }
/*     */
/* 174 */     this.view.requestLayout();
/*     */   }
    /*     */
/*     */   static int getNearestOrientationWith90Inc(int orientation)
/*     */   {
/* 179 */     double orientationSign = Math.signum(orientation);
/* 180 */     return (int)(orientationSign * Math.round(Math.abs(orientation) / 90.0D) * 90.0D);
/*     */   }
    /*     */
/*     */   private static boolean isViewProperlyConfigured(View view) {
/* 184 */     ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
/* 185 */     if ((layoutParams != null) && ((layoutParams.height != -1) || (layoutParams.width != -1)))
/*     */     {
/*     */
/* 188 */       return false;
/*     */     }
/* 190 */     return (view.getTranslationX() == 0.0F) && (view.getTranslationY() == 0.0F);
/*     */   }
/*     */ }

/*    */ class OrientationHelper
/*    */ {
    /*    */   private static final String STATE_KEY_IS_ORIENTATION_LOCKED = "isOrientationLocked";
    /*    */   private static final String STATE_KEY_ORIGINAL_REQUESTED_ORIENTATION = "originalRequestedOrientation";
    /*    */   private Activity activity;
    /*    */   private boolean isOrientationLocked;
    /*    */   private int originalRequestedOrientation;
    /*    */
/*    */   public OrientationHelper(Activity activity)
/*    */   {
/* 42 */     this.activity = activity;
/*    */   }
    /*    */
/*    */   public boolean isInPortraitOrientation() {
/* 46 */     return this.activity.getResources().getConfiguration().orientation == 1;
/*    */   }
    /*    */
/*    */
/*    */
/*    */
/*    */   public void lockOrientation()
/*    */   {
/* 54 */     if (this.isOrientationLocked) {
/* 55 */       return;
/*    */     }
/*    */
/* 58 */     this.originalRequestedOrientation = this.activity.getRequestedOrientation();
/*    */
/* 60 */     this.activity.setRequestedOrientation(isInPortraitOrientation() ?
/* 61 */       1 : 0);
/*    */
/* 63 */     this.isOrientationLocked = true;
/*    */   }
    /*    */
/*    */
/*    */
/*    */   public void restoreOriginalOrientation()
/*    */   {
/* 70 */     this.isOrientationLocked = false;
/* 71 */     this.activity.setRequestedOrientation(this.originalRequestedOrientation);
/*    */   }
    /*    */
/*    */
/*    */
/*    */   public Bundle onSaveInstanceState()
/*    */   {
/* 78 */     Bundle bundle = new Bundle();
/* 79 */     bundle.putBoolean("isOrientationLocked", this.isOrientationLocked);
/* 80 */     bundle.putInt("originalRequestedOrientation", this.originalRequestedOrientation);
/* 81 */     return bundle;
/*    */   }
    /*    */
/*    */
/*    */
/*    */   public void onRestoreInstanceState(Bundle state)
/*    */   {
/* 88 */     this.originalRequestedOrientation = state.getInt("originalRequestedOrientation");
/* 89 */     this.isOrientationLocked = state.getBoolean("isOrientationLocked");
/*    */   }
/*    */ }

/*     */ class TouchTracker
/*     */   implements View.OnTouchListener
/*     */ {
    /*     */   public static final float DEFAULT_PITCH_BOUNDS_DEGREES = 15.0F;
    /*     */   public static final float DEFAULT_DEGREES_PER_DPI_FACTOR = 0.033333335F;
    /*     */   private final TouchEnabledVrView target;
    /*  30 */   private PointF offsetDegrees = new PointF();
    /*     */
/*     */
/*  33 */   private PointF pxToDegreesFactor = new PointF();
    /*     */
/*     */
/*     */   private float pitchOffsetBoundsDegrees;
    /*     */
/*     */
/*  39 */   private PointF lastTouchPointPx = new PointF();
    /*     */
/*     */
/*     */
/*  43 */   private PointF startTouchPointPx = new PointF();
    /*     */
/*     */
/*     */   private boolean isYawing;
    /*     */
/*     */
/*     */   private final float dipToPx;
    /*     */
/*     */
/*     */   private final float scrollSlopPx;
    /*     */
/*     */
/*     */   public TouchTracker(Context context, TouchEnabledVrView target)
/*     */   {
/*  57 */     this.target = target;
/*  58 */     this.dipToPx = TypedValue.applyDimension(1, 1.0F, context
/*  59 */       .getResources().getDisplayMetrics());
/*  60 */     this.scrollSlopPx = ViewConfiguration.get(context).getScaledTouchSlop();
/*  61 */     setTouchSpeed(0.033333335F, 0.033333335F);
/*  62 */     setTouchPitchBounds(15.0F);
/*     */   }
    /*     */
/*     */
/*     */
/*     */   TouchTracker(TouchEnabledVrView target, float dipToPx, float scrollSlopPx)
/*     */   {
/*  69 */     this.target = target;
/*  70 */     this.dipToPx = dipToPx;
/*  71 */     this.scrollSlopPx = scrollSlopPx;
/*  72 */     setTouchSpeed(0.033333335F, 0.033333335F);
/*  73 */     setTouchPitchBounds(15.0F);
/*     */   }
    /*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */   public void setTouchSpeed(float xSpeed, float ySpeed)
/*     */   {
/*  84 */     this.pxToDegreesFactor.set(this.dipToPx * xSpeed, this.dipToPx * ySpeed);
/*     */   }
    /*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */   public void setTouchPitchBounds(float pitchBoundsDegrees)
/*     */   {
/*  99 */     this.pitchOffsetBoundsDegrees = Math.min(Math.max(0.0F, pitchBoundsDegrees), 45.0F);
/*     */   }
    /*     */
/*     */   public boolean onTouch(View view, MotionEvent event)
/*     */   {
/* 104 */     switch (event.getAction()) {
/*     */     case 0:
/* 106 */       this.startTouchPointPx.set(event.getX(), event.getY());
/* 107 */       this.lastTouchPointPx.set(event.getX(), event.getY());
/* 108 */       view.getParent().requestDisallowInterceptTouchEvent(true);
/* 109 */       this.isYawing = false;
/* 110 */       return true;
/*     */     case 2:
/* 112 */       if (!this.isYawing)
/*     */       {
/*     */
/* 115 */         if ((this.pxToDegreesFactor.x == 0.0F) && (this.pxToDegreesFactor.y == 0.0F))
/*     */         {
/*     */
/* 118 */           view.getParent().requestDisallowInterceptTouchEvent(false);
/* 119 */           return false; }
/* 120 */         if ((this.pitchOffsetBoundsDegrees == 0.0F) &&
/* 121 */           (Math.abs(event.getY() - this.startTouchPointPx.y) > this.scrollSlopPx))
/*     */         {
/*     */
/*     */
/* 125 */           view.getParent().requestDisallowInterceptTouchEvent(false);
/* 126 */           return false; }
/* 127 */         if (Math.abs(event.getX() - this.startTouchPointPx.x) > this.scrollSlopPx)
/*     */         {
/*     */
/* 130 */           this.isYawing = true;
/*     */         }
/*     */       }
/*     */
/*     */
/* 135 */       this.offsetDegrees.x += this.pxToDegreesFactor.x * (event.getX() - this.lastTouchPointPx.x);
/* 136 */       this.offsetDegrees.y += this.pxToDegreesFactor.y * (event.getY() - this.lastTouchPointPx.y);
/* 137 */       this.offsetDegrees.y = Math.max(-this.pitchOffsetBoundsDegrees,
/* 138 */         Math.min(this.offsetDegrees.y, this.pitchOffsetBoundsDegrees));
/* 139 */       this.target.setYawPitchOffset(this.offsetDegrees.x, this.offsetDegrees.y);
/* 140 */       this.lastTouchPointPx.set(event.getX(), event.getY());
/* 141 */       return true;
/*     */     case 1:
/* 143 */       if ((Math.abs(event.getX() - this.startTouchPointPx.x) < this.scrollSlopPx) &&
/* 144 */         (Math.abs(event.getY() - this.startTouchPointPx.y) < this.scrollSlopPx)) {
/* 145 */         this.target.getEventListener().onClick();
/*     */       }
/* 147 */       view.getParent().requestDisallowInterceptTouchEvent(false);
/* 148 */       return true;
/*     */     }
/*     */
/* 151 */     return false;
/*     */   }
    /*     */
/*     */   static abstract interface TouchEnabledVrView
/*     */   {
        /*     */     public abstract void setYawPitchOffset(float paramFloat1, float paramFloat2);
        /*     */
/*     */     public abstract VrEventListener getEventListener();
/*     */   }
/*     */ }
