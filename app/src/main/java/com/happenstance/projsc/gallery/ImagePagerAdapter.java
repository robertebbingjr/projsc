package com.happenstance.projsc.gallery;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.request.RequestOptions;
import com.happenstance.projsc.R;
import com.happenstance.projsc.models.ImageAsset;
import com.happenstance.projsc.utils.Utilities;
import com.ortiz.touchview.TouchImageView;

import java.util.ArrayList;

/**
 * the imageViewPager's adapter
 */
public class ImagePagerAdapter extends RecyclerView.Adapter<ImagePagerAdapter.ImagePagerViewHolder> {
    private Context context;
    private ArrayList<ImageAsset> imageAssets = new ArrayList<>();
    private RecyclerView rvIndicator;
//    private View toolbarGallery;
    private ViewPager2 viewPager2;

    public ImagePagerAdapter(Context context, ArrayList<ImageAsset> imageAssets,
                             RecyclerView rvIndicator,
                             ViewPager2 viewPager2) {
        this.context = context;
        this.imageAssets = imageAssets;
        this.rvIndicator = rvIndicator;
//        this.toolbarGallery = toolbarGallery;
        this.viewPager2 = viewPager2;
    }

    @NonNull
    @Override
    public ImagePagerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.vp_image_viewer, parent, false);
        return new ImagePagerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImagePagerViewHolder holder, int position) {
        ImageAsset imageAsset = imageAssets.get(position);

//        Glide.with(context)
//                .load(imageAsset.getFilePath())
//                .apply(new RequestOptions().fitCenter())
//                .into(holder.ivImageAsset);
        Utilities.loadGlideImagePath(context, imageAsset.getFilePath(), holder.ivImageAsset,
                new RequestOptions().fitCenter());

        holder.ivImageAsset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (rvIndicator.getVisibility() == View.GONE) {
                    rvIndicator.setVisibility(View.VISIBLE);
//                    toolbarGallery.setVisibility(View.VISIBLE);
                } else {
                    rvIndicator.setVisibility(View.GONE);
//                    toolbarGallery.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return imageAssets.size();
    }

    public class ImagePagerViewHolder extends RecyclerView.ViewHolder {
        private TouchImageView ivImageAsset;

        public ImagePagerViewHolder(@NonNull View itemView) {
            super(itemView);

            ivImageAsset = itemView.findViewById(R.id.ivImageAsset);
            ivImageAsset.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (ivImageAsset.isZoomed()) {
                        viewPager2.setUserInputEnabled(false);
                    } else {
                        viewPager2.setUserInputEnabled(true);
                    }
                    return false;
                }
            });
        }
    }
}