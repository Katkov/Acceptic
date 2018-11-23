package com.katkov.acceptic;

import android.net.Uri;
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
                download();
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
            Uri url = fileLoader.getUri(this);
            videoView.setVideoURI(url);
            videoView.requestFocus();
            videoView.start();
        } catch (IOException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

}
