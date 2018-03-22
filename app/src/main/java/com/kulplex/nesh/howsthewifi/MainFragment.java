package com.kulplex.nesh.howsthewifi;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class MainFragment extends Fragment
{

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private Context rootContext;
    private View rootView;
    private TextView pingTextView;
    private TextView packetLossTextView;
    private TextView downloadTextView;
    private TextView uploadTextView;
    private TextView addressTextView;
    private TextView pingLabel;
    private TextView packetLossLabel;
    private TextView downloadLabel;
    private TextView uploadLabel;
    private TextView addressLabel;
    private Button checkConnectionBtn;
    private Button sendBtn;
    private ReportStatus pingTaskStatus;
    private ReportStatus downloadTaskStatus;
    private ReportStatus uploadTaskStatus;
    private ReportStatus addressTaskStatus;
    private LocationRequest mLocationRequest;
    // Form data
    private float downloadValue;
    private float uploadValue;
    private float pingValue;
    private int packetLossValue;
    private Location mLastLocation;
    private String locationName;
    private String locationComments;
    private FusedLocationProviderClient mFusedLocationClient;
    LocationCallback mLocationCallback = new LocationCallback()
    {
        @Override
        public void onLocationResult(LocationResult locationResult)
        {
            for (Location location : locationResult.getLocations())
            {
                Log.e("MapsActivity",
                      "Location: " + location.getLatitude() + "," + location.getLongitude());
                Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
                try
                {
                    List<Address> addresses = geocoder.getFromLocation(location.getLatitude(),
                                                                       location.getLongitude(), 1);
                    if (addresses.size() > 0)
                    {
                        setTextViewAddress(addresses.get(0));
                    } else
                    {
                        setTextViewAddress(location);
                    }
                    Log.e("speedtest", "" + new Date(location.getTime()).toString());
                    Log.e("speedtest", "" + location.getAccuracy() + "m");

                    setReportStatus(ReportType.GPS, ReportStatus.COMPLETED);
                } catch (IOException e)
                {
                    e.printStackTrace();
                    setReportStatus(ReportType.GPS, ReportStatus.FAILED);
                }
                mLastLocation = location;
            }
        }
    };

    public void onClickAddress()
    {
        if (addressTaskStatus.equals(ReportStatus.COMPLETED))
        {
            Intent intent = new Intent(rootContext, MapsActivity.class);
            intent.putExtra("lat", mLastLocation.getLatitude());
            intent.putExtra("long", mLastLocation.getLongitude());
            intent.putExtra("address", addressTextView.getText());
            startActivity(intent);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        rootContext = getContext();
        rootView = inflater.inflate(R.layout.main_fragment, container, false);

        pingTextView = rootView.findViewById(R.id.pingTextView);
        packetLossTextView = rootView.findViewById(R.id.packetLossTextView);
        downloadTextView = rootView.findViewById(R.id.downloadTextView);
        uploadTextView = rootView.findViewById(R.id.uploadTextView);
        addressTextView = rootView.findViewById(R.id.addressTextView);
        addressTextView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onClickAddress();
            }
        });

        checkConnectionBtn = rootView.findViewById(R.id.checkConnectionButton);
        checkConnectionBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                onCheckConnection();
            }
        });
        sendBtn = rootView.findViewById(R.id.sendBtn);
        sendBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                onSend();
            }
        });

        pingLabel = rootView.findViewById(R.id.pingLabel);
        packetLossLabel = rootView.findViewById(R.id.packetLossLabel);
        uploadLabel = rootView.findViewById(R.id.uploadLabel);
        downloadLabel = rootView.findViewById(R.id.downloadLabel);
        addressLabel = rootView.findViewById(R.id.addressLabel);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(rootContext);

        return rootView;
    }

    @Override
    public void onPause()
    {
        super.onPause();
    }

    public void onSend()
    {
        if (pingValue == -1)
        {
            pingValue = 0;
        }
        if (packetLossValue == -1)
        {
            packetLossValue = 100;
        }

        if (downloadValue >= 0 && uploadValue >= 0)
        {
            showNotesDialog();
        } else
        {
            Toast.makeText(getContext(), "Something is not correct", Toast.LENGTH_SHORT).show();
        }
    }

    public void onCheckConnection()
    {
        checkConnectionBtn.setEnabled(false);
        sendBtn.setEnabled(false);

        pingLabel.setTextColor(rootContext.getColor(R.color.colorTextDefault));
        packetLossLabel.setTextColor(rootContext.getColor(R.color.colorTextDefault));
        downloadLabel.setTextColor(rootContext.getColor(R.color.colorTextDefault));
        uploadLabel.setTextColor(rootContext.getColor(R.color.colorTextDefault));
        addressLabel.setTextColor(rootContext.getColor(R.color.colorTextDefault));
        addressTextView.setPaintFlags(
                addressTextView.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));

        // Setting tasks flags
        pingTaskStatus = ReportStatus.IN_PROGRESS;
        downloadTaskStatus = ReportStatus.IN_PROGRESS;
        uploadTaskStatus = ReportStatus.IN_PROGRESS;
        addressTaskStatus = ReportStatus.IN_PROGRESS;

        // Clear the textViews
        pingTextView.setText("-");
        packetLossTextView.setText("-");
        downloadTextView.setText("-");
        uploadTextView.setText("-");
        addressTextView.setText("-");

        PingTask pingTask = new PingTask(5, 5);
        SpeedTestTask downloadTask = new SpeedTestTask(this, ReportType.DOWNLOAD, 8000);
        SpeedTestTask uploadTask = new SpeedTestTask(this, ReportType.UPLOAD, 8000);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
        {
            pingTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            downloadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            uploadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else
        {
            pingTask.execute();
            downloadTask.execute();
            uploadTask.execute();
        }
        if (mLocationRequest == null)
        {
            setupLocationManager(1000);
        }
    }

    public Float[] getPing(String url, int amount, int maxDuration)
    {
        float delay = -1;
        float packetLoss = -1;
        StringBuffer output = new StringBuffer();
        try
        {
            java.lang.Process process = Runtime.getRuntime().exec(
                    "ping -w " + maxDuration + " -c " + amount + " " + url);
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            int idx;
            char[] buffer = new char[4096];
            while ((idx = reader.read(buffer)) > 0)
            {
                output.append(buffer, 0, idx);
            }
            reader.close();
            String op[] = output.toString().split("\n");
            for (int j = 0; j < op.length; j++)
            {
                if (op[j].contains("time="))
                {
                    delay += Float.parseFloat(op[j].split("time=")[1].split(" ms")[0]);
                } else if (op[j].contains("received, "))
                {
                    packetLoss = Float.parseFloat(op[j].split("received, ")[1].split("%")[0]);
                }
            }
        } catch (Exception e)
        {
            return new Float[]{delay / amount, packetLoss};
        }
        return new Float[]{delay / amount, packetLoss};
    }

    public void setDownloadText(float f)
    {
        downloadValue = f;
        downloadTextView.setText(round(f * 0.001f, 1) + "Kb/s");
    }

    public void setUploadText(float f)
    {
        uploadValue = f;
        uploadTextView.setText(round(f * 0.001f, 1) + "Kb/s");
    }

    protected float round(float value, int precision)
    {
        float prec = 10 * precision;
        return (int) (value * prec) / (prec);
    }

    public void setReportStatus(ReportType reportType, ReportStatus reportStatus)
    {
        switch (reportType)
        {
            case PING:
                pingTaskStatus = reportStatus;
                break;
            case UPLOAD:
                uploadTaskStatus = reportStatus;
                break;
            case DOWNLOAD:
                downloadTaskStatus = reportStatus;
                break;
            case GPS:
                addressTaskStatus = reportStatus;
                break;
        }

        getActivity().runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                checkSpeedTestCompleted();
            }
        });
    }

    private void checkSpeedTestCompleted()
    {
        if (pingTaskStatus != ReportStatus.IN_PROGRESS)
        {
            pingLabel.setTextColor(pingTaskStatus == ReportStatus.COMPLETED ? rootContext.getColor(
                    R.color.colorTaskSuccess) : rootContext.getColor(
                    R.color.colorTaskAllowedFailure));
            packetLossLabel.setTextColor(
                    pingTaskStatus == ReportStatus.COMPLETED ? rootContext.getColor(
                            R.color.colorTaskSuccess) : rootContext.getColor(
                            R.color.colorTaskAllowedFailure));
        }

        if (downloadTaskStatus != ReportStatus.IN_PROGRESS)
        {
            downloadLabel.setTextColor(
                    downloadTaskStatus == ReportStatus.COMPLETED ? rootContext.getColor(
                            R.color.colorTaskSuccess) : rootContext.getColor(
                            R.color.colorTaskFailure));
        }

        if (uploadTaskStatus != ReportStatus.IN_PROGRESS)
        {
            uploadLabel.setTextColor(
                    uploadTaskStatus == ReportStatus.COMPLETED ? rootContext.getColor(
                            R.color.colorTaskSuccess) : rootContext.getColor(
                            R.color.colorTaskFailure));
        }

        if (addressTaskStatus != ReportStatus.IN_PROGRESS)
        {
            addressLabel.setTextColor(
                    addressTaskStatus == ReportStatus.COMPLETED ? rootContext.getColor(
                            R.color.colorTaskSuccess) : rootContext.getColor(
                            R.color.colorTaskFailure));
        }

        if (pingTaskStatus != ReportStatus.IN_PROGRESS && downloadTaskStatus != ReportStatus.IN_PROGRESS && uploadTaskStatus != ReportStatus.IN_PROGRESS && addressTaskStatus != ReportStatus.IN_PROGRESS)
        {
            checkConnectionBtn.setEnabled(true);
            if (downloadTaskStatus == ReportStatus.COMPLETED && uploadTaskStatus == ReportStatus.COMPLETED && addressTaskStatus == ReportStatus.COMPLETED)
            {
                sendBtn.setEnabled(true);
            }
        }
    }

    private void setupLocationManager(int intervalUpdate)
    {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(intervalUpdate); // two minute interval
        mLocationRequest.setFastestInterval(intervalUpdate);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if (ContextCompat.checkSelfPermission(rootContext,
                                                  Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            {
                //Location Permission already granted
                mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback,
                                                            Looper.myLooper());
            } else
            {
                //Request Location Permission
                checkLocationPermission();
            }
        } else
        {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback,
                                                        Looper.myLooper());
        }
    }

    private void checkLocationPermission()
    {
        if (ContextCompat.checkSelfPermission(rootContext,
                                              Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                                                                    Manifest.permission.ACCESS_FINE_LOCATION))
            {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(rootContext).setTitle(
                        "Location Permission Needed").setMessage(
                        "This app needs the Location permission, please accept to use location functionality").setPositiveButton(
                        "OK", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(getActivity(), new String[]{
                                                                          Manifest.permission.ACCESS_FINE_LOCATION},
                                                                  MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        }).create().show();
            } else
            {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(getActivity(), new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        switch (requestCode)
        {
            case MY_PERMISSIONS_REQUEST_LOCATION:
            {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(rootContext,
                                                          Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                    {

                        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                                                                    mLocationCallback,
                                                                    Looper.myLooper());
                    }
                } else
                {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(rootContext, "permission denied", Toast.LENGTH_LONG).show();
                    mLocationRequest = null;
                    setReportStatus(ReportType.GPS, ReportStatus.FAILED);
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void setTextViewAddress(Address address)
    {
        addressTextView.setPaintFlags(
                addressTextView.getPaintFlags() | (Paint.UNDERLINE_TEXT_FLAG));
        addressTextView.setTextSize(16);
        addressTextView.setText(address.getAddressLine(0) + ", " + address.getCountryName());
    }

    private void setTextViewAddress(Location location)
    {
        addressTextView.setPaintFlags(
                addressTextView.getPaintFlags() | (Paint.UNDERLINE_TEXT_FLAG));
        addressTextView.setTextSize(24);
        addressTextView.setText("(" + location.getLatitude() + "," + location.getLongitude() + ")");
    }

    private class PingTask extends AsyncTask<Float, Void, Float[]>
    {
        TextView pingTextView;
        int maxDuration;
        int amountPing;

        PingTask(int amountPing, int maxDuration)
        {
            pingTextView = rootView.findViewById(R.id.pingTextView);
            packetLossTextView = rootView.findViewById(R.id.packetLossTextView);
            this.maxDuration = maxDuration;
            this.amountPing = amountPing;
        }

        @Override
        protected Float[] doInBackground(Float... params)
        {
            return getPing("google.com", amountPing, maxDuration);
        }

        @Override
        protected void onPostExecute(Float[] result)
        {
            if (result[0] >= 0)
            {
                pingTextView.setText(result[0] + "ms");
                pingValue = result[0];
                setReportStatus(ReportType.PING, ReportStatus.COMPLETED);
            } else
            {
                pingTextView.setText("N/A");
                pingValue = -1f;
                setReportStatus(ReportType.PING, ReportStatus.FAILED);
            }

            if (result[1] >= 0)
            {
                packetLossTextView.setText(result[1].intValue() + "%");
                packetLossValue = result[1].intValue();
            } else
            {
                packetLossTextView.setText("N/A");
                packetLossValue = -1;
            }
        }

        @Override
        protected void onPreExecute()
        {

        }
    }

    void showNotesDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(rootContext);
        builder.setTitle("Location Information");
        builder.setMessage("Enter location information:");
        Context context = rootContext;
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);

        // Set up the input
        final EditText nameEditText = new EditText(rootContext);
        nameEditText.setHint("Name (e.g. 'Hotel California')");
        final EditText commentsEditText = new EditText(rootContext);
        commentsEditText.setHint("Comment (e.g. 'good WiFi, noisy cafe')");
        nameEditText.setInputType(InputType.TYPE_CLASS_TEXT);
        layout.addView(nameEditText);

        commentsEditText.setInputType(InputType.TYPE_CLASS_TEXT);
        layout.addView(commentsEditText);

        builder.setView(layout);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                locationName = nameEditText.getText().toString();
                locationComments = commentsEditText.getText().toString();

                if (!locationName.isEmpty() && !locationComments.isEmpty())
                {
                    uploadWifiSpeed();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.cancel();
            }
        });

        final AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener()
        {

            @Override
            public void onShow(DialogInterface dialog)
            {
                ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            }
        });

        // Now set the textchange listener for edittext
        nameEditText.addTextChangedListener(new TextWatcher()
        {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {

            }

            @Override
            public void afterTextChanged(Editable editable)
            {
                // Check if edittext is empty
                if (nameEditText.getText().toString().isEmpty() || commentsEditText.getText().toString().isEmpty())
                {
                    // Disable ok button
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                } else
                {
                    // Something into edit text. Enable the button.
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                }
            }
        });

        // Now set the textchange listener for edittext
        commentsEditText.addTextChangedListener(new TextWatcher()
        {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {

            }

            @Override
            public void afterTextChanged(Editable editable)
            {
                // Check if edittext is empty
                if (nameEditText.getText().toString().isEmpty() || commentsEditText.getText().toString().isEmpty())
                {
                    // Disable ok button
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                } else
                {
                    // Something into edit text. Enable the button.
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                }
            }
        });

        dialog.show();

    }

    void uploadWifiSpeed()
    {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("latitude", mLastLocation.getLatitude());
        params.put("longitude", mLastLocation.getLongitude());
        params.put("accuracy", mLastLocation.getAccuracy());
        params.put("download", downloadValue);
        params.put("upload", uploadValue);
        params.put("ping", pingValue);
        params.put("packet_loss", packetLossValue);
        params.put("name", locationName);
        params.put("comments", locationComments);

        client.post(getString(R.string.post_api_url), params, new JsonHttpResponseHandler()
        {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response)
            {
                // If the response is JSONObject instead of expected JSONArray
                Toast.makeText(rootContext, "Successful!", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse)
            {
                Toast.makeText(rootContext, "Failure :( : " + throwable.toString(),
                               Toast.LENGTH_LONG).show();
                Log.d("wifispeeds", throwable.toString());
            }
        });
    }
}

