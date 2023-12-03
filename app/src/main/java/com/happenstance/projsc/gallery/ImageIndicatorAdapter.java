package com.happenstance.projsc.gallery;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.request.RequestOptions;
import com.happenstance.projsc.R;
import com.happenstance.projsc.models.ImageAsset;
import com.happenstance.projsc.utils.Utilities;

import java.util.ArrayList;

/**
 * Author CodeBoy722
 */
public class ImageIndicatorAdapter extends RecyclerView.Adapter<ImageIndicatorAdapter.IndicatorViewHolder> {

    private Context context;
    private ArrayList<ImageAsset> imageAssets;
    private int currentPosition;
    private OnIndicatorClickListener onIndicatorClickListener;

    public void setCurrentPosition(int currentPosition) {
        this.currentPosition = currentPosition;
    }
//    private final ImageIndicatorListener imageIndicatorListener;
    public ImageIndicatorAdapter(Context context, ArrayList<ImageAsset> imageAssets,
                                 int currentPosition, OnIndicatorClickListener onIndicatorClickListener) {
        this.context = context;
        this.imageAssets = imageAssets;
        this.currentPosition = currentPosition;
        this.onIndicatorClickListener = onIndicatorClickListener;
    }

    @NonNull
    @Override
    public IndicatorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View cell = inflater.inflate(R.layout.layout_indicator_viewholder, parent, false);
        return new IndicatorViewHolder(cell);
    }

    @Override
    public void onBindViewHolder(@NonNull IndicatorViewHolder holder, final int position) {
        final ImageAsset imageAsset = imageAssets.get(position);

        int intColor;
        if (position == currentPosition) {
            intColor = Color.parseColor("#00000000");
        } else {
            intColor = Color.parseColor("#8c000000");
        }
        holder.viewShade.setBackgroundColor(intColor);

//        Glide.with(context)
//                .load(imageAsset.getFilePath())
//                .apply(new RequestOptions().centerCrop())
//                .into(holder.image);
        Utilities.loadGlideImagePath(context, imageAsset.getFilePath(), holder.ivImageAsset,
                new RequestOptions().centerCrop());

        holder.ivImageAsset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                onIndicatorClickListener.onIndicatorClick(position);
            }
        });

    }

    @Override
    public int getItemCount() {
        return imageAssets.size();
    }

    public class IndicatorViewHolder extends RecyclerView.ViewHolder {

        public ImageView ivImageAsset;
//        private CardView card;
        View viewShade;

        IndicatorViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImageAsset = itemView.findViewById(R.id.ivIndicatorImage);
//            card = itemView.findViewById(R.id.cvIndicatorImage);
            viewShade = itemView.findViewById(R.id.viewShade);
        }
    }

    public interface OnIndicatorClickListener {

        /**
         *
         * @param position position of an item in the RecyclerView Adapter
         */
        void onIndicatorClick(int position);
    }
}
