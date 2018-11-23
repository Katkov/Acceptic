package com.katkov.acceptic;


import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;


public class DownloadService extends IntentService {

    public static final int PROGRESS = 3117;
    public static final int ERROR  = 666;
    public static final int PROGRESS_LENGTH = 100;

    private static final int CHUNK_SIZE = 1024;

    public static final String PROGRESS_KEY = "progress_key";
    public static final String ERROR_KEY = "error_key";
    public static final String URL_KEY = "url";
    public static final String RECEIVER_KEY = "receiver";

    public DownloadService() {
        super("DownloadService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        String urlToDownload = intent.getStringExtra(URL_KEY);
        ResultReceiver receiver = intent.getParcelableExtra(RECEIVER_KEY);
        try {
            //notify start
            notifyProgress(receiver, 0);
            //open connection
            URLConnection connection = openConnection(urlToDownload);
            //get file length to show progress
            int fileLength = connection.getContentLength();
            File directory = getFilesDir();
            File file = new File(directory, FileLoader.FILE_NAME);
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();

            // download the file
            InputStream input = new BufferedInputStream(connection.getInputStream());
            OutputStream output = openFileOutput(FileLoader.FILE_NAME, Context.MODE_PRIVATE);

            byte data[] = new byte[CHUNK_SIZE];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                total += count;
                output.write(data, 0, count);
                //notify about progress
                notifyProgress(receiver, (int) (total * PROGRESS_LENGTH / fileLength));
            }
            //close connection
            output.flush();
            output.close();
            input.close();

        } catch (IOException e) {
            notifyError(receiver, e.getMessage());
        }
        //notify about finish
        notifyProgress(receiver, PROGRESS_LENGTH);
    }

    private URLConnection openConnection(String urlToDownload) throws IOException {
        URL url = new URL(urlToDownload);
        URLConnection connection = url.openConnection();
        connection.connect();
        return connection;
    }

    private void notifyProgress(ResultReceiver receiver, int progress) {
        Bundle resultData = new Bundle();
        resultData.putInt(PROGRESS_KEY , progress);
        receiver.send(PROGRESS, resultData);
    }


    private void notifyError(ResultReceiver receiver, String message) {
        Bundle resultData = new Bundle();
        resultData.putString(ERROR_KEY, message);
        receiver.send(ERROR, resultData);
    }

}
