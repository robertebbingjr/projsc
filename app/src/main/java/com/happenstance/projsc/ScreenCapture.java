package com.happenstance.projsc;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.OrientationEventListener;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.happenstance.projsc.constants.Broadcast;
import com.happenstance.projsc.constants.Extras;
import com.happenstance.projsc.preferences.Preferences;
import com.happenstance.projsc.utils.Utilities;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class ScreenCapture {
    private static final String TAG = "ScreenCapture";
    private static final String SCREENCAP_NAME = "screencap";

    private static CallbackInterface callbackCropImage;
//    private static UpdateUIInteface updateUIInteface;

    private Context context;
    private Intent intentConsentToken;


    private MediaProjection mediaProjection;
    private ImageReader imageReader;
    private Handler handler;
    private Display display;
    private VirtualDisplay virtualDisplay;
    private int density, width, height, rotation;
    private OrientationChangeCallback mOrientationChangeCallback;

    public ScreenCapture(Context context, Intent intentConsentToken) {
        this.context = context;
        this.intentConsentToken = intentConsentToken;
    }

    public void takeScreenshot() {
        // start capture handling thread
        //Looper.prepare();
        //Looper.loop();

        //updateUIInteface.updateUI(false);
        // Force delay for updateUI to be executed (which is not immediate b/c not in main thread)
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startProjection();
            }
        }, 100);
    }

    private void startProjection() {
        MediaProjectionManager mpManager =
                (MediaProjectionManager) context.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        if (mediaProjection == null) {
            mediaProjection = mpManager.getMediaProjection(Activity.RESULT_OK, intentConsentToken);
        }

        if (mediaProjection != null) {
            // display metrics
            WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            display = windowManager.getDefaultDisplay();

            // register orientation change callback
            mOrientationChangeCallback = new OrientationChangeCallback(context);
            if (mOrientationChangeCallback.canDetectOrientation()) {
                mOrientationChangeCallback.enable();
            }

            // create virtual display depending on device width / height
            createVirtualDisplay();
        } else {
            Toast.makeText(context, "Media projection failed to launch, please check permission", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Media projection is null value!!!");
        }

    }

    @SuppressLint("WrongConstant")
    private void createVirtualDisplay() {
        // get width and height

        DisplayMetrics displayMetrics = FloatingButtonService.getDisplayMetrics();
        if (displayMetrics == null) {
            width = Resources.getSystem().getDisplayMetrics().widthPixels;
            height = Resources.getSystem().getDisplayMetrics().heightPixels;
            density = Resources.getSystem().getDisplayMetrics().densityDpi;
        } else {
            width = displayMetrics.widthPixels;
            height = displayMetrics.heightPixels;
            density = displayMetrics.densityDpi;
        }

        // start capture reader
        imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2);
        virtualDisplay = mediaProjection.createVirtualDisplay(SCREENCAP_NAME, width, height,
                density, getVirtualDisplayFlags(), imageReader.getSurface(), null, handler);
        imageReader.setOnImageAvailableListener(new CaptureOnImageAvailableListener(), handler);
    }



    private static int getVirtualDisplayFlags() {
        return DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY | DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC;
//        return DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR;
    }

    // IMPORTANT: onImageAvailable callback is fired every time a new frame is available (something
    // changes on screen UI)
    private class CaptureOnImageAvailableListener implements ImageReader.OnImageAvailableListener {
        @Override
        public void onImageAvailable(ImageReader reader) {
            // reader = imageReader here

            stopMediaProjection();

            OutputStream fos = null;
            Bitmap bitmap = null;
            Image image = null;
            try {
                image = imageReader.acquireLatestImage();
                if (image != null) {
                    Image.Plane[] planes = image.getPlanes();
                    ByteBuffer buffer = planes[0].getBuffer();
                    int pixelStride = planes[0].getPixelStride();
                    int rowStride = planes[0].getRowStride();
                    int rowPadding = rowStride - pixelStride * width;

                    // create bitmap
                    bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
//                    bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    bitmap.copyPixelsFromBuffer(buffer);

                    boolean showStatusBar = Preferences.showStatusBar(context);
                    boolean showNavigationBar = Preferences.showNavigationBar(context);

                    if (!showStatusBar || !showNavigationBar) {
                        int statusBarHeight = showStatusBar ? 0 : Utilities.getStatusBarHeight(context);
                        int navigationBarHeight = showNavigationBar ? 0 : Utilities.getNavigationBarHeight(context);
                        int adjustedHeight = height - statusBarHeight - navigationBarHeight;
                        int startY = showStatusBar ? 0 : statusBarHeight;

                        bitmap = Bitmap.createBitmap(bitmap, 0, startY, bitmap.getWidth(), adjustedHeight);
                    }

                    String compressType = Preferences.getImageCompressFormat(context);
                    String fileName = Utilities.getImageFileName(compressType);
                    Uri uriOutput = Utilities.getImageFileUri(context, compressType, fileName);
                    fos = context.getContentResolver().openOutputStream(uriOutput);
                    bitmap.compress(Utilities.getImageCompressFormat(compressType), Preferences.getImageQuality(context), fos);

                    handleNextStep(uriOutput);
                }

            } catch (Exception e) {
                Utilities.showErrorMessage(context, e.getMessage());
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.flush();
                        fos.close();
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                }
                if (bitmap != null) {
                    bitmap.recycle();
                }
                if (image != null) {
                    image.close();
                }
                imageReader.close(); // imageReader must be closed here at the end
            }
        }
    }

    private void stopMediaProjection() {
        if (virtualDisplay != null)
            virtualDisplay.release();
        if (imageReader != null) {
            imageReader.setOnImageAvailableListener(null, null);
        }
        if (mOrientationChangeCallback != null)
            mOrientationChangeCallback.disable();
        mediaProjection.stop();
        virtualDisplay.release();
    }

    private void handleNextStep(Uri uriOutput) throws IOException {
        if (Preferences.isSnippingTool(context)) {
            FloatingButtonService.hideUI();

            // Need to close MainActivity otherwise CropActivity->back press will revert to Main
            Intent intentCloseMainActivity = new Intent(Broadcast.CLOSE_MAIN_ACTIVITY);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intentCloseMainActivity);

            Intent intentStartCropActivity = new Intent(context, CropActivity.class);
            intentStartCropActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intentStartCropActivity.putExtra(Extras.CAPTURED_IMAGE_URI, uriOutput.toString());
            context.startActivity(intentStartCropActivity);
        } else {
            Toast.makeText(context, "Screen captured and saved in gallery", Toast.LENGTH_SHORT).show();
            FloatingButtonService.showUI();
        }
    }

    private class OrientationChangeCallback extends OrientationEventListener {

        OrientationChangeCallback(Context context) {
            super(context);
        }

        @Override
        public void onOrientationChanged(int orientation) {
            final int rotation = display.getRotation();
            if (rotation != ScreenCapture.this.rotation) {
                ScreenCapture.this.rotation = rotation;
                try {
                    // clean up
                    if (virtualDisplay != null) virtualDisplay.release();
                    if (imageReader != null) imageReader.setOnImageAvailableListener(null, null);

                    // re-create virtual display depending on device width / height
                    createVirtualDisplay();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public interface CallbackInterface {
        void onComplete(Uri uri);
    }
}