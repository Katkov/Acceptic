package com.katkov.acceptic;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.*;
import android.text.TextUtils;

import java.io.File;
import java.io.IOException;

import static android.content.Context.MODE_PRIVATE;
import static com.katkov.acceptic.DownloadService.PROGRESS_KEY;


class FileLoader {

    static final String PREF_NAME = "prefAcceptic";
    static final String FILE_NAME = "videoA.mp4";

    private ProgressListener progressListener;
    private ErrorListener errorListener;

    private String url;

    public interface ProgressListener {
        void onProgress(int progress);
    }

    public interface ErrorListener {
        void onFailed(String error);
    }

    void setProgressListener(ProgressListener listener) {
        this.progressListener = listener;
    }

    void setErrorListener(ErrorListener listener) {
        this.errorListener = listener;
    }

    private static FileLoader instance;

    private FileLoader() {}

    static FileLoader getInstance() {
        if(instance == null) {
            instance = new FileLoader();
        }
        return instance;
    }

    void download(Context context, String url) {
        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra(DownloadService.URL_KEY, url);
        intent.putExtra(DownloadService.RECEIVER_KEY, new DownloadReceiver(new Handler()));
        context.startService(intent);
    }

    void setUrl(String url) {
        this.url = url;
    }

    Uri getUri(Context context) throws IOException {
        File directory = context.getFilesDir();
        File file = new File(directory, FileLoader.FILE_NAME);
        if (file.exists() && readProgress(context) == DownloadService.PROGRESS_LENGTH) {
            return Uri.parse(file.getPath());
        } else if (!TextUtils.isEmpty(url)) {
            return Uri.parse(url);
        }
        throw new IOException("You didn't provide any url");
    }

    int readProgress(Context context) {
        SharedPreferences pref = context.getSharedPreferences(FileLoader.PREF_NAME, MODE_PRIVATE);
        return pref.getInt(PROGRESS_KEY, DownloadService.PROGRESS_LENGTH);
    }

    private class DownloadReceiver extends ResultReceiver {

        DownloadReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);
            switch (resultCode) {
            case DownloadService.PROGRESS:
                int progress = resultData.getInt(PROGRESS_KEY);
                if (progressListener != null) progressListener.onProgress(progress);
                break;
            case DownloadService.ERROR:
                String error = resultData.getString(DownloadService.ERROR_KEY);
                if (errorListener != null) errorListener.onFailed(error);
                break;
            }
        }
    }

}
