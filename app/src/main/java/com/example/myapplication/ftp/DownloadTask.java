package com.example.myapplication.ftp;

import android.content.Context;
import android.os.AsyncTask;
import android.os.PowerManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

// usually, subclasses of AsyncTask are declared inside the activity class.
// that way, you can easily modify the UI thread from here
public class DownloadTask extends AsyncTask<String, Integer, String> implements DownLoadProgressListener {
    private Context context;
    private String fileName;
    private PowerManager.WakeLock mWakeLock;

    public DownloadTask(Context context) {
        this.context = context;
    }

    @Override
    protected String doInBackground(String... sUrl) {

        FtpUtil.startDownloadFtpFileProgress(fileName,this);

        return null;
    }

    @Override
    public void onDownLoadProgress(long max, long current) {
        publishProgress((int) current);
    }

    @Override
    protected void onProgressUpdate(Integer... progresses) {

//        progressBar.setProgress(progresses[0]);
//        text.setText("loading..." + progresses[0] + "%");

    }


}

interface DownLoadProgressListener{

    public void onDownLoadProgress(long max, long current);
}
