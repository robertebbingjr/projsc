package com.happenstance.projsc.utils;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.happenstance.projsc.constants.SharedPrefVariables;
import com.happenstance.projsc.models.InterstitialAdObject;

public class AdsUtil {
    private static final String TAG = "AdsUtil";

    public static boolean adSwitch(Context context) {
        SharedPrefUtil sharedPrefUtil = new SharedPrefUtil(context);
        return sharedPrefUtil.getBoolean(SharedPrefVariables.AD_SWITCH, false);
    }

    public static void runBannerAd(Context context, AdView avMain, FrameLayout flBanner) {
        if (adSwitch(context)) {
            AdRequest adRequest = new AdRequest.Builder().build();
            avMain.loadAd(adRequest);
            avMain.setAdListener(new AdListener() {
                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    super.onAdFailedToLoad(loadAdError);
                    Log.e(TAG, "Failed to load Admob banner: " + loadAdError.getMessage());
                }

                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                    flBanner.setVisibility(View.VISIBLE);
                    Log.i(TAG, "Successfully loaded Admob banner");
                }
            });
        }
    }

    public static void loadInterstitial(Activity activity, String adUnitId, @Nullable InterstitialAdObject interstitialAdObject, @Nullable View pbLoadingAd) {
        if (adSwitch(activity)) {
            AdRequest adRequest = new AdRequest.Builder().build();

            InterstitialAd.load(activity, adUnitId, adRequest,
                    new InterstitialAdLoadCallback() {
                        @Override
                        public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                            Log.i(TAG, "onAdLoaded");
                            if (interstitialAd != null) {
                                attachInterstitialCallback(interstitialAd, pbLoadingAd);
                                if (interstitialAdObject == null) {
                                    interstitialAd.show(activity);
                                } else {
                                    interstitialAdObject.setInterstitialAd(interstitialAd);
                                }

                            } else {
                                Log.d("TAG", "The interstitial ad wasn't ready yet.");
                            }
                        }

                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            Log.i(TAG, loadAdError.getMessage());
                            if (pbLoadingAd != null)
                                pbLoadingAd.setVisibility(View.GONE);
                        }
                    });
        }
    }

    private static void attachInterstitialCallback(@NonNull InterstitialAd interstitialAd, @Nullable View pbLoadingAd) {
        interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdDismissedFullScreenContent() {
                // Called when fullscreen content is dismissed.
                Log.d("TAG", "The ad was dismissed.");
                if (pbLoadingAd != null)
                    pbLoadingAd.setVisibility(View.GONE);
            }

            @Override
            public void onAdFailedToShowFullScreenContent(AdError adError) {
                // Called when fullscreen content failed to show.
                Log.d("TAG", "The ad failed to show.");
                if (pbLoadingAd != null)
                    pbLoadingAd.setVisibility(View.GONE);
            }

            @Override
            public void onAdShowedFullScreenContent() {
                // Called when fullscreen content is shown.
                // Make sure to set your reference to null so you don't
                // show it a second time.
            }
        });
    }
}
