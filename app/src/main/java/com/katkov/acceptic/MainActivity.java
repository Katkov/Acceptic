package com.katkov.acceptic;


import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import java.io.IOException;


public class MainActivity extends AppCompatActivity implements FileLoader.ProgressListener, FileLoader.ErrorListener{

    private VideoView videoView;
    private ProgressBar progressBar;
    private EditText editText;
    private Button button;
    private Button button2;

    private FileLoader fileLoader;

    private static final int PERMISSION_REQUEST_CODE = 444;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fileLoader = new FileLoader();
        setContentView(R.layout.activity_main);
        videoView = findViewById(R.id.video);
        videoView.setMediaController(new MediaController(this));
        progressBar = findViewById(R.id.progress);
        editText = findViewById(R.id.edit);
        button = findViewById(R.id.button);
        button2 = findViewById(R.id.button2);
        setListeners();
    }


    @Override
    public void onProgress(int progress) {
        if (progress < DownloadService.PROGRESS_LENGTH) {
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setProgress(progress);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }


    @Override
    public void onFailed(String error) {
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
        case PERMISSION_REQUEST_CODE:
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                download();
            }
            break;
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        fileLoader.setProgressListener(MainActivity.this);
        fileLoader.setErrorListener(MainActivity.this);
    }


    @Override
    protected void onStop() {
        super.onStop();
        fileLoader.setProgressListener(null);
        fileLoader.setErrorListener(null);
    }


    private void download() {
        fileLoader.download(MainActivity.this, editText.getText().toString().trim());
    }


    private void setListeners() {
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                //check permissions
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !checkPermission()) requestPermission();
                //else download
                else download();
            }
        });
        button2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                fileLoader.setUrl(editText.getText().toString().trim());
                show();
            }
        });
    }


    private void show() {
        try {
            Uri url = fileLoader.getUri();
            videoView.setVideoURI(url);
            videoView.requestFocus();
            videoView.start();
        } catch (IOException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }


    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                PERMISSION_REQUEST_CODE);
    }

}
