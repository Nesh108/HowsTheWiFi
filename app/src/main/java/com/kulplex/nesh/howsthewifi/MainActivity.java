package com.kulplex.nesh.howsthewifi;

import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {

    private TextView pingTextView;
    private TextView downloadTextView;
    private TextView uploadTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pingTextView = findViewById(R.id.pingTextView);
        downloadTextView = findViewById(R.id.downloadTextView);
        uploadTextView = findViewById(R.id.uploadTextView);
    }

    public void onCheckConnection(View view) {
        // Clear the textViews
        pingTextView.setText("-");
        downloadTextView.setText("-");
        uploadTextView.setText("-");

        PingTask pingTask = new PingTask();
        SpeedTestTask downloadTask = new SpeedTestTask(this, ReportType.DOWNLOAD, 5000);
        SpeedTestTask uploadTask = new SpeedTestTask(this, ReportType.UPLOAD, 5000);

        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB) {
            pingTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            downloadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            uploadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        else {
            pingTask.execute();
            downloadTask.execute();
            uploadTask.execute();
        }
    }

    public int getPing(String url, int amount) {
        int delay = 0;

        try {
            java.lang.Process process = Runtime.getRuntime().exec(
                    "ping -c " + amount + " " + url);
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    process.getInputStream()));
            int i;
            char[] buffer = new char[4096];
            StringBuffer output = new StringBuffer();
            String op[];
            while ((i = reader.read(buffer)) > 0) {
                output.append(buffer, 0, i);
            }
            reader.close();

            op = output.toString().split("\n");
            for(int j=1; j<= amount; j++) {
                delay += Integer.parseInt(op[j].split("time=")[1].split(" ms")[0]);
            }
        } catch (Exception e) {
        }
        return delay / amount;
    }

    private class PingTask extends AsyncTask<Integer, Void, Integer> {
        TextView pingTextView;

        PingTask() {
            pingTextView = findViewById(R.id.pingTextView);
        }
        @Override
        protected Integer doInBackground(Integer... params) {
            return getPing("google.com", 5);
        }

        @Override
        protected void onPostExecute(Integer result) {
            pingTextView.setText(result + "ms");
        }

        @Override
        protected void onPreExecute() {}
    }

    public void setDownloadText(String s) {
        downloadTextView.setText(s);
    }

    public void setUploadText(String s) {
        uploadTextView.setText(s);
    }
}
