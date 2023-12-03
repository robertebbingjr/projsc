package com.happenstance.projsc.gallery;

import android.annotation.SuppressLint;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.happenstance.projsc.R;
import com.happenstance.projsc.models.ImageAsset;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ImageBrowserFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ImageBrowserFragment extends Fragment {
    private ArrayList<ImageAsset> imageAssets = new ArrayList<>();
    private ImageAssetAdapter imageAssetAdapter;
    private ImagePagerAdapter imagePagerAdapter;
    private ViewPager2 imageGalleryPager;
    private ImageIndicatorAdapter imageIndicatorAdapter;
    private RecyclerView rvIndicator;
    private int startPosition;
    private ActionBar actionBar;
    private Runnable runOnDelete;
    private Runnable runOnClose;

    private ImageIndicatorAdapter.OnIndicatorClickListener onIndicatorClickListener =
            new ImageIndicatorAdapter.OnIndicatorClickListener() {
                @Override
                public void onIndicatorClick(int position) {
                    selectIndicatorItem(position, true);
                }
            };

    public ImageBrowserFragment() {
        // Required empty public constructor
    }

    public static ImageBrowserFragment newInstance(
            ImageAssetAdapter imageAssetAdapter, int imagePosition,
            ActionBar actionBar, Runnable runOnDelete, Runnable runOnClose) {
        ImageBrowserFragment fragment = new ImageBrowserFragment();

        fragment.imageAssetAdapter = imageAssetAdapter;
        fragment.startPosition = imagePosition;
        fragment.actionBar = actionBar  ;
        fragment.runOnDelete = runOnDelete;
        fragment.imageAssets = imageAssetAdapter.getImageAssets();
//        fragment.ivMenu = fragment.toolbarGallery.findViewById(R.id.ivMenu);
//        fragment.ivMenuViewer = fragment.toolbarGallery.findViewById(R.id.ivMenuViewer);
        fragment.runOnClose = runOnClose;

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_image_browser, container, false);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        actionBar.hide();

        imageGalleryPager = view.findViewById(R.id.imagePager);
        rvIndicator = view.findViewById(R.id.rvIndicator);

        imagePagerAdapter = new ImagePagerAdapter(getActivity(), imageAssets, rvIndicator,
                imageGalleryPager);
        imageGalleryPager.setAdapter(imagePagerAdapter);
        imageGalleryPager.setOffscreenPageLimit(3);
        imageGalleryPager.setCurrentItem(startPosition);//displaying the image at the current position passed by the ImageDisplay Activity

        rvIndicator.hasFixedSize();
        rvIndicator.setLayoutManager(new GridLayoutManager(getContext(), 1,
                RecyclerView.HORIZONTAL, false));
        imageIndicatorAdapter = new ImageIndicatorAdapter(getContext(), imageAssets, startPosition,
                onIndicatorClickListener); //,this);
        rvIndicator.setAdapter(imageIndicatorAdapter);

        //imageGalleryPager.addOnPageChangeListener(onPageChangeListener);
        imageGalleryPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);

                selectIndicatorItem(position, false);
            }
        });

        selectIndicatorItem(startPosition, true);

//        ivMenuViewer.setOnClickListener(oclMenuClick);
//        ivMenu.setVisibility(View.GONE);
//        ivMenuViewer.setVisibility(View.VISIBLE);
    }

    private void selectIndicatorItem(int position, boolean updatePager) {
        imageIndicatorAdapter.setCurrentPosition(position);
        imageIndicatorAdapter.notifyDataSetChanged();
        rvIndicator.scrollToPosition(position);
        if (updatePager) {
            imageGalleryPager.setCurrentItem(position, false);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (runOnClose != null) {
            runOnClose.run();
        }
    }
}