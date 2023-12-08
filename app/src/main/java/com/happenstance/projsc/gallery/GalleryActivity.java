package com.happenstance.projsc.gallery;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.transition.Fade;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.happenstance.projsc.R;
import com.happenstance.projsc.constants.Extras;
import com.happenstance.projsc.exception_handler.ExceptionHandler;
import com.happenstance.projsc.models.ImageAsset;
import com.happenstance.projsc.models.InterstitialAdObject;
import com.happenstance.projsc.preferences.SettingsActivity;
import com.happenstance.projsc.utils.AdsUtil;
import com.happenstance.projsc.utils.Utilities;

import java.util.ArrayList;

public class GalleryActivity extends AppCompatActivity {

//    private String folderName = "ScreenSnip";
    private ArrayList<ImageAsset> imageAssets = new ArrayList<>();
    private ImageAssetAdapter imageAssetAdapter;
    private SharedPreferences sharedPreferences;
    private InterstitialAdObject interstitialAdObject = new InterstitialAdObject();
    private TextView tvEmptyDirectory;
    private RecyclerView rvGallery;
    private ActionBar actionBar;

    private ImageAssetAdapter.OnImageAssetClickListener onImageAssetClickListener =
            new ImageAssetAdapter.OnImageAssetClickListener() {
                @Override
                public void onClick(ImageAssetAdapter.ImageAssetViewHolder holder, int position, ArrayList<ImageAsset> pics) {
                    ImageBrowserFragment imageBrowserFragment = ImageBrowserFragment.newInstance(
                            imageAssetAdapter,
                            position,
                            actionBar,
                            runOnDelete,
                            runOnFragmentClose
                    );

                    imageBrowserFragment.setEnterTransition(new Fade());
                    imageBrowserFragment.setExitTransition(new Fade());

//                    fabImport.setVisibility(View.GONE);

                    getSupportFragmentManager()
                            .beginTransaction()
                            .addSharedElement(holder.ivImageAsset, position + "picture")
                            .add(R.id.flGalleryActivityContainer, imageBrowserFragment)
                            .addToBackStack(null)
                            .commit();
                }

                @Override
                public void onClick(String pictureFolderPath, String folderName) {

                }
            };

    private Runnable runOnDelete = new Runnable() {
        @Override
        public void run() {
            imageAssetAdapter.notifyDataSetChanged();
            Toast.makeText(GalleryActivity.this,"File deleted", Toast.LENGTH_SHORT).show();

            if (imageAssets == null || imageAssets.size() == 0) {
                tvEmptyDirectory.setVisibility(View.VISIBLE);
            }
        }
    };

    private  Runnable runOnFragmentClose = new Runnable() {
        @Override
        public void run() {
//            fabImport.setVisibility(View.VISIBLE);
            if (actionBar != null) {
                actionBar.show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        setContentView(R.layout.activity_gallery);

        AdView avBrowse = findViewById(R.id.adView);
        FrameLayout flBanner = findViewById(R.id.flBanner);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                AdsUtil.loadInterstitial(GalleryActivity.this,
                        getString(R.string.interstitial_ad_id),
                        interstitialAdObject, null);

                AdsUtil.runBannerAd(GalleryActivity.this, avBrowse, flBanner);
            }
        });

        String folderName = getString(R.string.directory_name);
        if (folderName == null || folderName.isEmpty()) {
            Utilities.showErrorMessage(this, "Error: Folder not selected");
            finish();
        }

        Toolbar tbCustom = (Toolbar) findViewById(R.id.tbCustom);
        Utilities.initializeActionBar(this, tbCustom, "Gallery");
        actionBar = getSupportActionBar();
//        setSupportActionBar(tbCustom);
//        actionBar = getSupportActionBar();
//        if (actionBar != null) {
//            actionBar.setTitle("Gallery");
//            actionBar.setDisplayHomeAsUpEnabled(true);
//        }

        tvEmptyDirectory = findViewById(R.id.tvEmptyDirectory);
        rvGallery = findViewById(R.id.rvFolders);

        imageAssets = Utilities.getImagesAssetsInFolder(this, folderName, false);
        imageAssetAdapter = new ImageAssetAdapter(this, imageAssets,
                onImageAssetClickListener, runOnDelete, folderName, 3);
        rvGallery.setAdapter(imageAssetAdapter);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
        rvGallery.setLayoutManager(gridLayoutManager);

        sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        showGallery(); //imageAssets);
    }

    private void showGallery() { //ArrayList<ImageAsset> imageAssets) {
//        tvGalleryLocked.setVisibility(View.GONE);
        rvGallery.setVisibility(View.VISIBLE);

        if (imageAssets == null || imageAssets.size() == 0) {
            tvEmptyDirectory.setVisibility(View.VISIBLE);
        } else {
            tvEmptyDirectory.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.gallery, menu);
//        return super.onCreateOptionsMenu(menu);
//    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if (interstitialAdObject.getInterstitialAd() != null) {
            interstitialAdObject.getInterstitialAd().show(this);
        }
    }
}