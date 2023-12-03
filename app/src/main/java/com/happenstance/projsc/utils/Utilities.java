package com.happenstance.projsc.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.happenstance.projsc.R;
import com.happenstance.projsc.constants.Constants;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.happenstance.projsc.models.ImageAsset;
import com.happenstance.projsc.models.InterstitialAdObject;
//import com.happenstance.projsc.models.InterstitialAdObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Utilities {
    private static final String TAG = "Utilities";

    public static void showErrorMessage(Context context, String message) {
        Log.e("Error:", message);
        Toast.makeText(context, "Error: " + message, Toast.LENGTH_LONG).show();
    }

    public static String getImageDirectory(Context context) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        return Environment.DIRECTORY_PICTURES + "/" + context.getString(R.string.directory_name);
//        } else {
//            return context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
//                    .toString() + "/" + context.getString(R.string.app_folder);
//        }
    }

    public static String getImageFileName(@NonNull String format) {
        return System.currentTimeMillis() + "." + getImageFileExtension(format);
    }

    public static Bitmap.CompressFormat getImageCompressFormat(@NonNull String format) {
        if (format.equals("JPG")) {
            return Bitmap.CompressFormat.JPEG;
        } else {
            return Bitmap.CompressFormat.PNG;
        }
    }

    public static String getImageFileExtension(@NonNull String format) {
        if (format.equals("JPG")) {
            return Constants.IMAGE_FILE_EXTENSION_JPG;
        } else {
            return Constants.IMAGE_FILE_EXTENSION_PNG;
        }
    }

    public static String getImageMimeType(@NonNull String compressType) {
        if (compressType.equals("JPG")) {
            return Constants.IMAGE_MIME_TYPE_JPG;
        } else {
            return Constants.IMAGE_MIME_TYPE_PNG;
        }
    }

    public static String getFileNameWithoutExtension(String fileName) {
        if (fileName == null || fileName.isEmpty() || fileName.indexOf(".") < 0) {
            return "FileNameError";
        } else {
            return fileName.replaceFirst("[.][^.]+$", "");
        }
    }

    public static Uri getImageFileUri(Context context, String compressType, String fileName) {
        String imageDirectory = Utilities.getImageDirectory(context);
        //String compressType = Preferences.getImageCompressFormat(context);
//        String fileName = Utilities.getImageFileName(compressType);
        Uri uriOutput;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Starting with Q (Android 10 = API 29), ContentResolver manages media content,
            // So you can just pass in the imageDirectory and fileName for it to spit out media Uri
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
            values.put(MediaStore.Images.Media.MIME_TYPE, getImageMimeType(compressType));
            values.put(MediaStore.Images.Media.RELATIVE_PATH, imageDirectory);

            uriOutput =
                    context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            values);
        } else {
            // For earlier versions of Android, you need an absolute path to the directory you are creating
//            String imageDirectoryAbsolutePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
//                    .toString() + "/" + context.getString(R.string.app_folder);
            String imageDirectoryAbsolutePath = getImageDirectoryAbsolutePath(context);

            // Check if the directory exists
            File fileDirectory = new File(imageDirectoryAbsolutePath);
            if (!fileDirectory.exists()) {
                boolean success = fileDirectory.mkdirs();
                if (!success) {
                    Utilities.showErrorMessage(context, "failed to create file storage directory.");
                }
            }

            // And you can use the absolute file path to get media Uri
//            File fileImage = new File(imageDirectoryAbsolutePath, fileName);
//            String filePath = fileImage.getAbsolutePath();
            String filePath = imageDirectoryAbsolutePath + "/" + fileName;
//            uriOutput = Uri.fromFile(fileImage);

            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DATA, filePath);
            return context.getContentResolver().insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        }
        return uriOutput;
    }

    public static String getImageDirectoryAbsolutePath(Context context) {
        String imageDirectoryAbsolutePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                .toString() + "/" + context.getString(R.string.directory_name);

        return imageDirectoryAbsolutePath;
    }


    public static class FileMetaData {
        public String displayName;
        public long size;
        public String mimeType;
        public String path;

        @Override
        public String toString() {
            return "name : " + displayName + " ; size : " + size + " ; path : " + path + " ; mime : " + mimeType;
        }
    }

    @SuppressLint("Range")
    public static FileMetaData getFileMetaData(Context context, Uri uri) {
        FileMetaData fileMetaData = new FileMetaData();

        if ("file".equalsIgnoreCase(uri.getScheme())) {
            File file = new File(uri.getPath());
            fileMetaData.displayName = file.getName();
            fileMetaData.size = file.length();
            fileMetaData.path = file.getPath();

            return fileMetaData;
        } else {
            ContentResolver contentResolver = context.getContentResolver();
            Cursor cursor = contentResolver.query(uri, null, null, null, null);
            fileMetaData.mimeType = contentResolver.getType(uri);

            try {
                if (cursor != null && cursor.moveToFirst()) {
                    int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                    fileMetaData.displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));

                    if (!cursor.isNull(sizeIndex))
                        fileMetaData.size = cursor.getLong(sizeIndex);
                    else
                        fileMetaData.size = -1;

                    try {
                        fileMetaData.path = cursor.getString(cursor.getColumnIndexOrThrow("_data"));
                    } catch (Exception e) {
                        // DO NOTHING, _data does not exist
                    }

                    return fileMetaData;
                }
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            } finally {
                if (cursor != null)
                    cursor.close();
            }

            return null;
        }
    }

    public static boolean isInteger(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            int i = Integer.parseInt(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    public static void showDialogOK(Context context, String title, String msg, @Nullable Runnable runnable) {
        if (context instanceof Activity && !((Activity) context).isFinishing()) {
            new MaterialAlertDialogBuilder(context)
                    .setTitle(title)
                    .setMessage(msg)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (runnable != null)
                                runnable.run();
                        }
                    })
                    .setCancelable(false)
                    .show();
        }
    }

    public static void errorPopup(Context context, String title, String message, boolean clickOKToClose) {
        if (context instanceof Activity && !((Activity) context).isFinishing()) {
            new MaterialAlertDialogBuilder(context)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (context instanceof Activity && clickOKToClose)
                                ((Activity) context).finish();
                        }
                    })
                    .setCancelable(false)
                    .show();
        }
    }

    public static void showDialogPositiveNegative(Context context, String title, String msg, String positive, String negative,
                                                  @Nullable Runnable runOnPositive, @Nullable Runnable runOnNegative) {
        showDialogPositiveNegativeWithImage(context, title, msg, positive, negative, runOnPositive, runOnNegative, 0);
    }

    public static void showDialogPositiveNegativeWithImage(Context context, String title, String msg, String positive, String negative,
                                                  @Nullable Runnable runOnPositive, @Nullable Runnable runOnNegative, int imageResourceLayout) {

        if (context instanceof Activity && !((Activity) context).isFinishing()) {
            MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(context)
                    .setTitle(title)
                    .setMessage(msg)
                    .setPositiveButton(positive, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (runOnPositive != null)
                                runOnPositive.run();
                        }
                    })
                    .setNegativeButton(negative, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (runOnNegative != null)
                                runOnNegative.run();
                        }
                    })
                    .setCancelable(false);
            if (imageResourceLayout != 0) {
////                ImageView ivResource = new ImageView(context);
                LayoutInflater layoutInflater = LayoutInflater.from(context);
//                View ivResource = layoutInflater.inflate(R.layout.layout_sample_overlay_permission, null);
////                ivResource.setImageResource(imageResource);
                View ivResource = layoutInflater.inflate(imageResourceLayout, null);
                materialAlertDialogBuilder.setView(ivResource);
            }
            materialAlertDialogBuilder.show();
        }
    }

    public static int getColorResourceId(Context context, String buttonColor) {
        final int defaultColor = R.color.screensnip_green;

        if (buttonColor.isEmpty()) {
            return defaultColor;
        } else if (buttonColor.equals("White")) {
            return R.color.white;
        } else if (buttonColor.equals("Black")) {
            return R.color.black;
        } else if (buttonColor.equals("Red")) {
            return R.color.red;
        } else if (buttonColor.equals("Orange")) {
            return R.color.star_orange;
        } else if (buttonColor.equals("Yellow")) {
            return R.color.yellow;
        } else if (buttonColor.equals("Green")) {
            return R.color.green;
        } else if (buttonColor.equals("Blue")) {
            return R.color.blue;
        } else if (buttonColor.equals("Navy")) {
            return R.color.navy;
        } else if (buttonColor.equals("Purple")) {
            return R.color.purple;
        } else {
            // default
            return defaultColor;
        }
    }


    public static int getCircleButtonSize(Context context, String buttonSize) {
        final int defaultSize = 100;
        if (buttonSize.isEmpty()) {
            return defaultSize;
        } else if (buttonSize.equals("Extra Large")) {
            return 140;
        } else if (buttonSize.equals("Large")) {
            return 120;
        } else if (buttonSize.equals("Medium")) {
            return defaultSize;
        } else if (buttonSize.equals("Small")) {
            return 80;
        } else if (buttonSize.equals("Extra Small")) {
            return 60;
        } else {
            // default
            return defaultSize;
        }
    }

    public static float getButtonAlpha(Context context, int buttonTransparency) {
        return (float) Integer.valueOf(buttonTransparency) / 100;
    }

//    public static void loadInterstitial(Activity activity, String adUnitId, @Nullable InterstitialAdObject interstitialAdObject, @Nullable View pbLoadingAd) {
//        MobileAds.initialize(activity, new OnInitializationCompleteListener() {
//            @Override
//            public void onInitializationComplete(InitializationStatus initializationStatus) {
//            }
//        });
//        AdRequest adRequest = new AdRequest.Builder().build();
//
//        InterstitialAd.load(activity, adUnitId, adRequest,
//                new InterstitialAdLoadCallback() {
//                    @Override
//                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
//                        Log.i(TAG, "onAdLoaded");
//                        if (interstitialAd != null) {
//                            attachInterstitialCallback(interstitialAd, pbLoadingAd);
//                            if (interstitialAdObject == null) {
//                                interstitialAd.show(activity);
//                            } else {
//                                interstitialAdObject.setInterstitialAd(interstitialAd);
//                            }
//
//                        } else {
//                            Log.d("TAG", "The interstitial ad wasn't ready yet.");
//                        }
//                    }
//
//                    @Override
//                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
//                        Log.i(TAG, loadAdError.getMessage());
//                        if (pbLoadingAd != null)
//                            pbLoadingAd.setVisibility(View.GONE);
//                    }
//                });
//    }
//
//    public static void attachInterstitialCallback(@NonNull InterstitialAd interstitialAd, @Nullable View pbLoadingAd) {
//        interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
//            @Override
//            public void onAdDismissedFullScreenContent() {
//                // Called when fullscreen content is dismissed.
//                Log.d("TAG", "The ad was dismissed.");
//                if (pbLoadingAd != null)
//                    pbLoadingAd.setVisibility(View.GONE);
//            }
//
//            @Override
//            public void onAdFailedToShowFullScreenContent(AdError adError) {
//                // Called when fullscreen content failed to show.
//                Log.d("TAG", "The ad failed to show.");
//                if (pbLoadingAd != null)
//                    pbLoadingAd.setVisibility(View.GONE);
//            }
//
//            @Override
//            public void onAdShowedFullScreenContent() {
//                // Called when fullscreen content is shown.
//                // Make sure to set your reference to null so you don't
//                // show it a second time.
//            }
//        });
//    }

    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static int getNavigationBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static DisplayMetrics getDisplayMetrics(Activity activity) {
        final DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);

        return metrics;
    }

    public static boolean isViewOverlapping(View firstView, View secondView) {
        int[] firstPosition = new int[2];
        int[] secondPosition = new int[2];

        firstView.getLocationOnScreen(firstPosition);
        secondView.getLocationOnScreen(secondPosition);

        // Rect constructor parameters: left, top, right, bottom
        Rect rectFirstView = new Rect(firstPosition[0], firstPosition[1],
                firstPosition[0] + firstView.getMeasuredWidth(), firstPosition[1] + firstView.getMeasuredHeight());
        Rect rectSecondView = new Rect(secondPosition[0], secondPosition[1],
                secondPosition[0] + secondView.getMeasuredWidth(), secondPosition[1] + secondView.getMeasuredHeight());
        return rectFirstView.intersect(rectSecondView);
    }

    public static ArrayList<ImageAsset> getImagesAssetsInFolder(Context context, String folderName, boolean isInternal) {
        ArrayList<ImageAsset> images = new ArrayList<>();

        File directory = isInternal ? getInternalFolder(context, folderName) : getExternalFolder(context);
        File[] files = directory.listFiles();

        try {
            for (File file : files) {
                // If getAllFiles == true, always show every file
                // If getAllFiles == false, then only show image files
                String fileName = file.getName();
                String filePath = file.getPath();
                if (Utilities.isImageFile(fileName)) {
                    boolean isFolder = file.isDirectory();
                    ImageAsset imageAsset = new ImageAsset(fileName, filePath, file.lastModified(),
                            isFolder);
                    images.add(imageAsset);
                }
            }

            // Sort by reverse date modified
            Collections.sort(images, new Comparator<ImageAsset>() {
                @Override
                public int compare(ImageAsset o1, ImageAsset o2) {
                    return (int) -(o1.getLongDateModified() - o2.getLongDateModified());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return images;
    }

    private static File getInternalFolder(Context context, @Nullable String folderName) {
        File fileDirectory;
        if (folderName == null || folderName.isEmpty()) {
            fileDirectory = getInternalRootFolder(context);
        } else {
            fileDirectory = new File(context.getFilesDir(), folderName);
        }
//        File fileDirectory = context.getFilesDir();
        if (!fileDirectory.exists()) {
            fileDirectory.mkdir();
        }
        return fileDirectory;
    }

    private static File getExternalFolder(Context context) {
        String imageDirectoryAbsolutePath = getImageDirectoryAbsolutePath(context);

        // Check if the directory exists
        File fileDirectory = new File(imageDirectoryAbsolutePath);
        if (!fileDirectory.exists()) {
            boolean success = fileDirectory.mkdirs();
            if (!success) {
                Utilities.showErrorMessage(context, "Failed to access captured image directory");
            }
        }

        return fileDirectory;
    }

    private static File getInternalRootFolder(Context context) {
        File fileDirectory = context.getFilesDir();
        return fileDirectory;
    }

    private static boolean isImageFile(String fileName) {
        final String[] okFileExtensions = new String[] {
                "jpg",
                "png",
                "gif",
                "jpeg",
                "bmp",
                "img"
        };

        for (String extension: okFileExtensions) {
            if (fileName.toLowerCase().endsWith("." + extension)) {
                return true;
            }
        }
        return false;
    }

    public static int getImageViewDimension(Context context, int numColumns) {
        if (context instanceof Activity) {
            DisplayMetrics displayMetrics = Utilities.getDisplayMetrics((Activity) context);
            return displayMetrics.widthPixels / numColumns;
        } else {
            return 0;
        }
    }

    public static void loadGlideImageResource(Context context, int res, ImageView ivDestination, RequestOptions requestOptions) {
        Glide.with(context)
                .load(res)
                .apply(requestOptions)
                .placeholder(R.drawable.ic_file)
                .error(R.drawable.ic_file)
                .into(ivDestination);
    }

    public static void loadGlideImagePath(Context context, String filePath, ImageView ivDestination, RequestOptions requestOptions) {
        Glide.with(context)
                .load(filePath)
                .apply(requestOptions)
                .placeholder(R.drawable.ic_file)
                .error(R.drawable.ic_file)
                .into(ivDestination);
    }

    public static void deleteSpecificImageFile(Context context,
                                               ArrayList<ImageAsset> imageAssets,
                                               int position,
                                               @Nullable Runnable runnable) {
        if (imageAssets != null && imageAssets.size() > 0) {
            ImageAsset imageAsset = imageAssets.get(position);
            String filePath = imageAsset.getFilePath();

//            boolean deleted = deleteFileOrDirectory(context, filePath);
            deleteFileOrDirectory(context, filePath);

//            if (deleted) {
            imageAssets.remove(position);
            if (runnable != null) {
                runnable.run();
            }
//            } else {
//                Utilities.showErrorMessage(context, "Failed to delete file");
//            }
        } else {
            Utilities.showErrorMessage(context, "Failed to delete file - there are no deletable images");
        }
    }

    public static void deleteFileOrDirectory(Context context, File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteFileOrDirectory(context, child);

        fileOrDirectory.delete();
    }

    public static void deleteFileOrDirectory(Context context, String filePath) {
        File file = new File(filePath);
        deleteFileOrDirectory(context, file);
    }

    public static boolean deleteUri(Context context, Uri uri) {
        File file = new File(uri.getPath());
        String[] selectionArgs = {file.getAbsolutePath()};
        ContentResolver contentResolver = context.getContentResolver();

        String where;
        Uri filesUri;
        if (android.os.Build.VERSION.SDK_INT >= 29) {
            filesUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            where = MediaStore.Images.Media._ID + "=?";
            selectionArgs = new String[]{file.getName()};
        } else {
            where = MediaStore.MediaColumns.DATA + "=?";
            filesUri = MediaStore.Files.getContentUri("external");
        }
        int result = contentResolver.delete(filesUri, where, selectionArgs);

        return !file.exists();
    }

    public static void initializeActionBar(AppCompatActivity activity, Toolbar tbCustom, String title) {
        activity.setSupportActionBar(tbCustom);
        ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }
}
