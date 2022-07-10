package com.happenstance.projsc.exception_handler;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.happenstance.projsc.R;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionHandler implements Thread.UncaughtExceptionHandler {

    private final Activity myContext;
    private final String LINE_SEPARATOR = "\n";

    public ExceptionHandler(Activity myContext) {
        this.myContext = myContext;
    }

    @Override
    public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
        StringWriter stackTrace = new StringWriter();
        e.printStackTrace(new PrintWriter(stackTrace));
        StringBuilder errorReport = new StringBuilder();
        errorReport.append(myContext.getResources().getString(R.string.app_name) + " Error Report\n\n");
        errorReport.append("------------- CAUSE OF ERROR -------------\n\n");
        errorReport.append(stackTrace.toString());

        errorReport.append("\n------------- DEVICE INFORMATION -------------\n");
        errorReport.append("Brand: ");
        errorReport.append(Build.BRAND);
        errorReport.append(LINE_SEPARATOR);
        errorReport.append("Device: ");
        errorReport.append(Build.DEVICE);
        errorReport.append(LINE_SEPARATOR);
        errorReport.append("Model: ");
        errorReport.append(Build.MODEL);
        errorReport.append(LINE_SEPARATOR);
        errorReport.append("Id: ");
        errorReport.append(Build.ID);
        errorReport.append(LINE_SEPARATOR);
        errorReport.append("Product: ");
        errorReport.append(Build.PRODUCT);
        errorReport.append(LINE_SEPARATOR);
        errorReport.append("\n------------- SOFTWARE -------------\n");
        String version = "";
        int versionCode = 0;
        String versionDisplay = "";
        try {
            PackageInfo packageInfo = myContext.getPackageManager().getPackageInfo(myContext.getPackageName(), 0);
            version = packageInfo.versionName;
            versionCode = packageInfo.versionCode;
            versionDisplay = "Version " + version + " (" + String.valueOf(versionCode) + ")";
        } catch (PackageManager.NameNotFoundException nnfe) {
            Toast.makeText(myContext, "Error retrieving version info : " + nnfe.getMessage(), Toast.LENGTH_LONG).show();
        }
        errorReport.append(myContext.getString(R.string.app_name));
        errorReport.append(LINE_SEPARATOR);
        errorReport.append(versionDisplay);
        errorReport.append(LINE_SEPARATOR);
        errorReport.append("\n------------- FIRMWARE -------------\n");
        errorReport.append("SDK: ");
        errorReport.append(Build.VERSION.SDK);
        errorReport.append(LINE_SEPARATOR);
        errorReport.append("Release: ");
        errorReport.append(Build.VERSION.RELEASE);
        errorReport.append(LINE_SEPARATOR);
        errorReport.append("Incremental: ");
        errorReport.append(Build.VERSION.INCREMENTAL);
        errorReport.append(LINE_SEPARATOR);

        Intent intent = new Intent(myContext, ExceptionHandlerActivity.class);
        intent.putExtra("error", errorReport.toString());
        myContext.startActivity(intent);

        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(10);
    }
}
