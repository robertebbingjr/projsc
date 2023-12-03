package com.happenstance.projsc.gallery;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.happenstance.projsc.R;
import com.happenstance.projsc.models.ImageAsset;

import java.text.DateFormat;
import java.util.Date;

public class BottomSheetDialogFragment extends com.google.android.material.bottomsheet.BottomSheetDialogFragment {
    private ImageAsset imageAsset;

    public static BottomSheetDialogFragment newInstance(ImageAsset imageAsset) {
        BottomSheetDialogFragment fragment = new BottomSheetDialogFragment();
        fragment.imageAsset = imageAsset;

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable
            ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.bottom_sheet_file_info,
                container, false);

        TextView tvFileName = v.findViewById(R.id.tvFileName);
        TextView tvLastModified = v.findViewById(R.id.tvLastModified);
        TextView tvLocalPath = v.findViewById(R.id.tvLocalPath);

        String fileName = imageAsset.getFileName();
        String filePath = imageAsset.getFilePath();
        long longDateModified = imageAsset.getLongDateModified();

        tvFileName.setText(fileName);
        tvLocalPath.setText(filePath);

        Date date = new Date(longDateModified);
        String stringTimeModified = DateFormat.getDateTimeInstance().format(date);
        tvLastModified.setText(stringTimeModified);

        return v;
    }
}