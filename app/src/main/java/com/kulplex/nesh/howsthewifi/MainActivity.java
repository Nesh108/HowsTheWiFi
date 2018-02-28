package com.kulplex.nesh.howsthewifi;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {

    private TextView downloadTextView;
    private TextView uploadTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        downloadTextView = findViewById(R.id.downloadTextView);
        uploadTextView = findViewById(R.id.uploadTextView);
    }

    public void onCheckConnection(View view) {
        new SpeedTestTask(this, ReportType.DOWNLOAD).execute();
        new SpeedTestTask(this, ReportType.UPLOAD).execute();
        new PingTask().execute();
    }

    public int getPing(String url) {
        String str = "";
        try {
            java.lang.Process process = Runtime.getRuntime().exec(
                    "ping -c 1 " + url);
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    process.getInputStream()));
            int i;
            char[] buffer = new char[4096];
            StringBuffer output = new StringBuffer();
            String op[];
            String delay[];
            while ((i = reader.read(buffer)) > 0)
                output.append(buffer, 0, i);
            reader.close();
            op = output.toString().split("\n");
            Log.d("speedtest", output.toString());
            delay = op[3].split("time ");
            str = delay[1];
        } catch (IOException e) {
            // body.append("Error\n");
            e.printStackTrace();
        }
        return Integer.parseInt(str.split("ms")[0]);
    }

    private class PingTask extends AsyncTask<Integer, Void, Integer> {

        @Override
        protected Integer doInBackground(Integer... params) {
            return getPing("www.bing.com");
        }

        @Override
        protected void onPostExecute(Integer result) {
            TextView pingTextView = findViewById(R.id.pingTextView);
            pingTextView.setText(result + "ms");
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }

    public void setDownloadText(String s) {
        downloadTextView.setText(s);
    }

    public void setUploadText(String s) {
        uploadTextView.setText(s);
    }
}
