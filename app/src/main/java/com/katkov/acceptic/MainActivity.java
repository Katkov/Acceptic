package com.katkov.acceptic;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import java.io.IOException;


public class MainActivity extends AppCompatActivity implements FileLoader.ProgressListener, FileLoader.ErrorListener {

    public static final String URL_EDIT_TEXT = "URL_EDIT_TEXT";

    private VideoView   videoView;
    private ProgressBar progressBar;
    private EditText    addYourUrlTv;
    private Button      downloadBtn;
    private Button      playBtn;
    private TextView    urlToTryTv;

    private FileLoader fileLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fileLoader = FileLoader.getInstance();
        setContentView(R.layout.activity_main);
        videoView = findViewById(R.id.video);
        videoView.setMediaController(new MediaController(this));
        progressBar = findViewById(R.id.progress);
        addYourUrlTv = findViewById(R.id.edit);
        downloadBtn = findViewById(R.id.downloadBtn);
        playBtn = findViewById(R.id.playBtn);
        urlToTryTv = findViewById(R.id.linkTv);
        setListeners();
    }


    @Override
    public void onProgress(int progress) {
        if (progress < DownloadService.PROGRESS_LENGTH) {
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setProgress(progress);
            playBtn.setText(getString(R.string.play_from_host));
        } else {
            progressBar.setVisibility(View.GONE);
            playBtn.setText(getString(R.string.play_from_local));
        }
    }


    @Override
    public void onFailed(String error) {
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
    }


    @Override
    protected void onStart() {
        super.onStart();
        onProgress(fileLoader.readProgress(this));
        fileLoader.setProgressListener(MainActivity.this);
        fileLoader.setErrorListener(MainActivity.this);
    }


    @Override
    protected void onStop() {
        super.onStop();
        fileLoader.setProgressListener(null);
        fileLoader.setErrorListener(null);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(URL_EDIT_TEXT, addYourUrlTv.getText().toString());
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        String urlText = savedInstanceState.getString(URL_EDIT_TEXT);
        addYourUrlTv.setText(urlText);
    }


    private void download() {
        fileLoader.download(MainActivity.this, addYourUrlTv.getText().toString().trim());
    }


    private void setListeners() {
        downloadBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                download();
            }
        });
        playBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                fileLoader.setUrl(addYourUrlTv.getText().toString().trim());
                show();
            }
        });
        urlToTryTv.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                addYourUrlTv.setText(urlToTryTv.getText());
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
