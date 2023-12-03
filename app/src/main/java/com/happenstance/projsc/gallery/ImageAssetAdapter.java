package com.happenstance.projsc.gallery;

import static androidx.core.view.ViewCompat.setTransitionName;

import android.content.Context;
import android.content.DialogInterface;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.happenstance.projsc.R;
import com.happenstance.projsc.models.ImageAsset;
import com.happenstance.projsc.utils.Utilities;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ImageAssetAdapter extends RecyclerView.Adapter<ImageAssetAdapter.ImageAssetViewHolder> {
    private final ArrayList<ImageAsset> imageAssets;
    private final Context context;
    private final OnImageAssetClickListener onImageAssetClickListener;
    private final Runnable runOnDelete;
    private final String folderName;

    private int columnWidth;

    public ArrayList<ImageAsset> getImageAssets() {
        return imageAssets;
    }

    public ImageAssetAdapter(Context context, ArrayList<ImageAsset> imageAssets,
                             OnImageAssetClickListener onImageAssetClickListener,
                             Runnable runOnDelete, String folderName, int numColumns) {
//                             int columnWidth) {
        this.context = context;
        this.imageAssets = imageAssets;
        this.onImageAssetClickListener = onImageAssetClickListener;
        this.runOnDelete = runOnDelete;
//        this.columnWidth = columnWidth;
        this.folderName = folderName;

//        DisplayMetrics displayMetrics = Utilities.getDisplayMetrics((Activity) context);
//        columnWidth = displayMetrics.widthPixels / 3;
        columnWidth = Utilities.getImageViewDimension(context, numColumns);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageAssetViewHolder holder, int position, @NonNull List<Object> payloads) {
        final ImageAsset imageAsset = imageAssets.get(position);

        if (imageAsset.isFolder()) {
            Utilities.loadGlideImageResource(context, R.drawable.ic_folder, holder.ivImageAsset,
                    new RequestOptions().centerCrop());
        } else {
            Utilities.loadGlideImagePath(context, imageAsset.getFilePath(), holder.ivImageAsset,
                    new RequestOptions().centerCrop());
        }

        setTransitionName(holder.ivImageAsset, String.valueOf(position) + "_image");

        holder.ivImageAsset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                onImageAssetClickListener.onClick(holder, position, imageAssets);
            }
        });

        holder.ivImageAsset.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                int position = holder.getAdapterPosition();
                showLongClickOptions(position, null);

//                return true means that the event is consumed. It is handled. No other click events will be notified.
//                return false means the event is not consumed. Any other click events will continue to receive notifications.
                return true;
            }
        });
    }

    public void showLongClickOptions(int position, @Nullable Runnable
            runnableUpdateIndicatorOnDelete) {
//        int position = holder.getAdapterPosition();
        final ImageAsset imageAsset = imageAssets.get(position);
        ArrayList<String> items = new ArrayList();
        items.add("Share");
        items.add("Delete");
        items.add("Info");

        new MaterialAlertDialogBuilder(context)
                .setTitle("Select action")
                .setItems(items.toArray(new String[0]), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if ("Delete".equals(items.get(which))) {
                            Runnable runnableCombined = new Runnable() {
                                @Override
                                public void run() {
                                    if (runOnDelete != null) {
                                        runOnDelete.run();
                                    }
                                    if (runnableUpdateIndicatorOnDelete != null) {
                                        runnableUpdateIndicatorOnDelete.run();
                                    }

                                }
                            };
                            Utilities.deleteSpecificImageFile(context, imageAssets, position,
                                    runnableCombined);
                        } else if ("Info".equals(items.get(which))) {
                            BottomSheetDialogFragment bottomSheetDialogFragment =
                                    BottomSheetDialogFragment.newInstance(imageAsset);

                            if (context instanceof AppCompatActivity) {
                                bottomSheetDialogFragment.show(((AppCompatActivity) context).
                                        getSupportFragmentManager(), "ModalBottomSheet");
                            }
                        }
                    }
                })
                .show();
    }

    @NonNull
    @Override
    public ImageAssetViewHolder onCreateViewHolder(@NonNull ViewGroup container, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(container.getContext());
        View view = inflater.inflate(R.layout.layout_gallery_viewholder, container, false);
        return new ImageAssetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageAssetViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return imageAssets.size();
    }

    public class ImageAssetViewHolder extends RecyclerView.ViewHolder {

        public ImageView ivImageAsset;

        public ImageAssetViewHolder(@NonNull View itemView) {
            super(itemView);

            ivImageAsset = itemView.findViewById(R.id.ivImageAsset);

            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(columnWidth, columnWidth);
            ivImageAsset.setLayoutParams(layoutParams);
        }
    }

    public interface OnImageAssetClickListener {

        /**
         * Called when a picture is clicked
         * @param holder The ViewHolder for the clicked picture
         * @param position The position in the grid of the picture that was clicked
         */
        void onClick(ImageAssetAdapter.ImageAssetViewHolder holder, int position, ArrayList<ImageAsset> pics);
        void onClick(String pictureFolderPath, String folderName);
    }
}
