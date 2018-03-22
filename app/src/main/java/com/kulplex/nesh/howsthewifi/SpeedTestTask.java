package com.kulplex.nesh.howsthewifi;

import android.os.AsyncTask;
import android.util.Log;

import java.util.Random;

import fr.bmartel.speedtest.SpeedTestReport;
import fr.bmartel.speedtest.SpeedTestSocket;
import fr.bmartel.speedtest.inter.ISpeedTestListener;
import fr.bmartel.speedtest.model.SpeedTestError;

public class SpeedTestTask extends AsyncTask<SpeedTestReport, SpeedTestReport, SpeedTestReport>
{

    private MainFragment mainFragment;
    private ReportType reportType;
    private int maxDuration;
    private Random rng;

    public SpeedTestTask(MainFragment mainFragment, ReportType rt, int maxDuration)
    {
        reportType = rt;
        this.mainFragment = mainFragment;
        this.maxDuration = maxDuration;
        rng = new Random();
    }

    @Override
    protected SpeedTestReport doInBackground(SpeedTestReport... params)
    {

        SpeedTestSocket speedTestSocket = new SpeedTestSocket();

        // add a listener to wait for speedtest completion and progress
        speedTestSocket.addSpeedTestListener(new ISpeedTestListener()
        {

            @Override
            public void onCompletion(SpeedTestReport report)
            {
                mainFragment.setReportStatus(reportType, ReportStatus.COMPLETED);
            }

            @Override
            public void onError(SpeedTestError speedTestError, String errorMessage)
            {
                Log.e("speedtest", errorMessage);
                mainFragment.setReportStatus(reportType, ReportStatus.FAILED);
            }

            @Override
            public void onProgress(float percent, SpeedTestReport report)
            {
                // called to notify download/upload progress
                publishProgress(report);
            }
        });

        if (reportType == ReportType.DOWNLOAD)
        {
            speedTestSocket.setDownloadSetupTime(1000);
            if (rng.nextBoolean())
            {
                speedTestSocket.startFixedDownload("http://ipv4.ikoula.testdebit.info/100M.iso",
                                                   maxDuration);
            } else
            {
                speedTestSocket.startFixedDownload("ftp://speedtest.tele2.net/100MB.zip",
                                                   maxDuration);
            }
        } else
        {
            speedTestSocket.setUploadSetupTime(1000);
            if (rng.nextBoolean())
            {
                speedTestSocket.startFixedUpload("http://ipv4.ikoula.testdebit.info/", 100000000,
                                                 maxDuration);
            } else
            {
                speedTestSocket.startFixedUpload("http://2.testdebit.info/", 100000000,
                                                 maxDuration);
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(SpeedTestReport report)
    {

    }

    @Override
    protected void onProgressUpdate(SpeedTestReport... reports)
    {
        if (reportType == ReportType.DOWNLOAD)
        {
            mainFragment.setDownloadText(reports[0].getTransferRateBit().floatValue());
        } else
        {
            mainFragment.setUploadText(reports[0].getTransferRateBit().floatValue());
        }
    }


}