package com.kulplex.nesh.howsthewifi;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

public class MainActivity extends AppCompatActivity
{
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private LocationRequest mLocationRequest;
    private Location mLastLocation;
    private MainFragment mainFragment;
    private MapFragment mapFragment;
    PagerAdapter adapter;
    private FusedLocationProviderClient mFusedLocationClient;
    LocationCallback mLocationCallback = new LocationCallback()
    {
        @Override
        public void onLocationResult(LocationResult locationResult)
        {
            if(mainFragment == null)
            {
                mainFragment = adapter.getMainFrag();
                mapFragment = adapter.getMapFrag();
            }
            if(mainFragment != null)
            {
                mainFragment.locationHandler(locationResult);
                mapFragment.setUserLocationPin(locationResult);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TabLayout tabLayout = findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.title_activity_main)));
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.title_activity_maps)));
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.title_activity_settings)));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final ViewPager viewPager = findViewById(R.id.pager);
        adapter = new PagerAdapter(getSupportFragmentManager(),
                                                      tabLayout.getTabCount());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener()
        {
            @Override
            public void onTabSelected(TabLayout.Tab tab)
            {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab)
            {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab)
            {

            }
        });

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getBaseContext());
        if (mLocationRequest == null)
        {
            setupLocationManager(1000);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        return super.onOptionsItemSelected(item);
    }

    private void setupLocationManager(int intervalUpdate)
    {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(intervalUpdate); // two minute interval
        mLocationRequest.setFastestInterval(intervalUpdate);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if (ContextCompat.checkSelfPermission(getBaseContext(),
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
        if (ContextCompat.checkSelfPermission(getBaseContext(),
                                              Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                                                                    Manifest.permission.ACCESS_FINE_LOCATION))
            {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(getBaseContext()).setTitle(
                        "Location Permission Needed").setMessage(
                        "This app needs the Location permission, please accept to use location functionality").setPositiveButton(
                        "OK", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                                                                          Manifest.permission.ACCESS_FINE_LOCATION},
                                                                  MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        }).create().show();
            } else
            {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this, new String[]{
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
                    if (ContextCompat.checkSelfPermission(getBaseContext(),
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
                    Toast.makeText(getBaseContext(), "We really need your location!",
                                   Toast.LENGTH_LONG).show();
                    mLocationRequest = null;
                    mainFragment.locationHandler(null);
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}