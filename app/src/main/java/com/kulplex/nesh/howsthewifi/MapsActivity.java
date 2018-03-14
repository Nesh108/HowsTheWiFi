package com.kulplex.nesh.howsthewifi;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback
{

    private GoogleMap mMap;
    private double mLatitude;
    private double mLongitude;
    private String addressName;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(
                R.id.map);
        mapFragment.getMapAsync(this);

        Bundle extras = getIntent().getExtras();
        mLatitude = extras.getDouble("lat");
        mLongitude = extras.getDouble("long");
        addressName = extras.getString("address");
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;

        LatLng pin = new LatLng(mLatitude, mLongitude);
        mMap.addMarker(new MarkerOptions().position(pin).title(addressName));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(pin));
    }
}
