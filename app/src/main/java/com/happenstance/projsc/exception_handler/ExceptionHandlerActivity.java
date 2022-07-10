package com.happenstance.projsc.exception_handler;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.happenstance.projsc.R;

public class ExceptionHandlerActivity extends AppCompatActivity {
    String exception = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exception_handler);


        exception = getIntent().hasExtra("error")
                ? getIntent().getStringExtra("error")
                : "";

        TextView tvExceptionReport = findViewById(R.id.tvExceptionReport);
        tvExceptionReport.setText(exception);
    }

    public void btnEmailClick(View view) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL  , new String[]{getString(R.string.developer_email)});
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name) + " Error Report");
        intent.putExtra(Intent.EXTRA_TEXT   , exception);
        try {
            startActivity(Intent.createChooser(intent, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
        }
    }
}