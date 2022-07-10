package com.happenstance.projsc;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.canhub.cropper.CropImage;
import com.canhub.cropper.CropImageContract;
import com.canhub.cropper.CropImageContractOptions;
import com.canhub.cropper.CropImageOptions;
import com.canhub.cropper.CropImageView;
import com.happenstance.projsc.constants.Extras;
import com.happenstance.projsc.exception_handler.ExceptionHandler;
import com.happenstance.projsc.preferences.Preferences;
import com.happenstance.projsc.utils.Utilities;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Objects;

public class CropActivity extends AppCompatActivity {
    private static final String TAG = "CropActivity";

    private final ActivityResultLauncher<CropImageContractOptions> cropImage = registerForActivityResult(new CropImageContract(), this::onCropImageResult);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        setContentView(R.layout.activity_crop);

        String stringUriCapturedImage = getIntent().getStringExtra(Extras.CAPTURED_IMAGE_URI);
        if (stringUriCapturedImage == null || stringUriCapturedImage.isEmpty()) {
            Utilities.showErrorMessage(this, "Failed to load captured image to crop");
            FloatingButtonService.showUI();
            finish();
        } else {
            Uri uriCapturedImage = Uri.parse(stringUriCapturedImage);
            startCameraWithUri(uriCapturedImage);
        }
    }

    private void onCropImageResult(@NonNull CropImageView.CropResult result) {
        if (result.isSuccessful()) {
            handleCropImageResult(Objects.requireNonNull(result.getUriContent())
                    .toString()
                    .replace("file:", ""));
        } else {
            if (result.equals(CropImage.CancelledResult.INSTANCE)) {
                Utilities.showErrorMessage(this, "Snipping image was cancelled by the user");
            } else {
                Utilities.showErrorMessage(this,"Snipping image failed");
            }
//            if (result != null && result.getUriContent() != null) {
//                File fileFailed = new File(result.getUriContent().getPath());
//                fileFailed.delete();
//            }
            FloatingButtonService.showUI();
            finish();
        }
    }

    private void handleCropImageResult(@NotNull String uri) {
        //SampleResultScreen.Companion.start(this, null, Uri.parse(uri), null);
        Toast.makeText(this, "Snipped image saved successfully", Toast.LENGTH_SHORT).show();
        FloatingButtonService.showUI();
        finish();
    }

    private void startCameraWithUri(Uri uriInput) {
        Utilities.FileMetaData fileMetaDataSource = Utilities.getFileMetaData(this, uriInput);
        Log.e(TAG, fileMetaDataSource.toString());
        String compressType = Preferences.getImageCompressFormat(this);
        String croppedFileName = Utilities.getFileNameWithoutExtension(fileMetaDataSource.displayName)
                + "_snipped." + Utilities.getImageFileExtension(compressType);
        Uri uriOutput = Utilities.getImageFileUri(this, compressType, croppedFileName);
        Utilities.FileMetaData fileMetaDataDestination = Utilities.getFileMetaData(this, uriOutput);
        Log.e(TAG, fileMetaDataDestination.toString());

        CropImageContractOptions options = new CropImageContractOptions(uriInput, new CropImageOptions())
                .setScaleType(CropImageView.ScaleType.FIT_CENTER)
                .setCropShape(CropImageView.CropShape.RECTANGLE)
                .setGuidelines(CropImageView.Guidelines.ON_TOUCH)
                .setAspectRatio(1, 1)
                .setMaxZoom(4)
                .setAutoZoomEnabled(true)
                .setMultiTouchEnabled(true)
                .setCenterMoveEnabled(true)
                .setShowCropOverlay(true)
                .setAllowFlipping(true)
                .setSnapRadius(3f)
                .setTouchRadius(48f)
                .setImageSource(true, false)
                .setInitialCropWindowPaddingRatio(0f)
                .setBorderLineThickness(8f)
                .setBorderLineColor(getColor(R.color.red))
                .setBorderCornerThickness(6f)
                .setBorderCornerOffset(15f)
                .setBorderCornerLength(40f)
                .setBorderCornerColor(getColor(R.color.red))
                .setGuidelinesThickness(6f)
                .setGuidelinesColor(getColor(R.color.red))
                .setBackgroundColor(Color.argb(119, 0, 0, 0))
                .setMinCropWindowSize(24, 24)
                .setMinCropResultSize(20, 20)
                .setMaxCropResultSize(99999, 99999)
                .setActivityTitle("Snip Image")
                .setActivityMenuIconColor(0)
                .setOutputUri(uriOutput)
                .setOutputCompressFormat(Utilities.getImageCompressFormat(compressType))
                .setOutputCompressQuality(Preferences.getImageQuality(this))
                .setRequestedSize(0, 0)
                .setRequestedSize(0, 0, CropImageView.RequestSizeOptions.RESIZE_INSIDE)
                .setInitialCropWindowRectangle(null)
                .setInitialRotation(0)
                .setAllowCounterRotation(false)
                .setFlipHorizontally(false)
                .setFlipVertically(false)
                .setCropMenuCropButtonTitle(null)
                .setCropMenuCropButtonIcon(0)
                .setAllowRotation(true)
                .setNoOutputImage(false)
                .setFixAspectRatio(false);

        cropImage.launch(options);
    }
}

