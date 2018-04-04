package com.kulplex.nesh.howsthewifi;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class MapFragment extends Fragment implements OnMapReadyCallback
{
    private GoogleMap mMap;
    private View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        rootView = inflater.inflate(R.layout.map_fragment, container, false);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(
                R.id.fragment_map);
        mapFragment.getMapAsync(this);

        return rootView;
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;
        mMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener()
        {
            @Override
            public void onCameraMoveStarted(int reason)
            {
                if (reason == REASON_GESTURE)
                {
                    GetAllLocationsPins(mMap.getProjection().getVisibleRegion().latLngBounds);
                }
            }
        });
        GetAllLocationsPins(mMap.getProjection().getVisibleRegion().latLngBounds);
    }

    void GetAllLocationsPins(LatLngBounds curScreen)
    {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("min_latitude", curScreen.southwest.latitude);
        params.put("max_latitude", curScreen.northeast.latitude);
        params.put("min_longitude", curScreen.southwest.longitude);
        params.put("max_longitude", curScreen.northeast.longitude);

        client.get(getString(R.string.get_api_url), params, new JsonHttpResponseHandler()
        {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response)
            {
                AddPinsToMap(response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse)
            {
                Log.d("wifispeeds", throwable.toString());
            }
        });
    }

    void AddPinsToMap(JSONArray locationsJson)
    {
        mMap.clear();
        for (int i = 0; i < locationsJson.length(); i++)
        {
            try
            {
                JSONObject loc = locationsJson.getJSONObject(i);
                LatLng pin = new LatLng(loc.getDouble("latitude"), loc.getDouble("longitude"));
                mMap.addMarker(
                        new MarkerOptions().position(pin).title(loc.getString("name")).snippet(
                                loc.getString("comments")));
            } catch (JSONException e)
            {
                e.printStackTrace();
            }
        }
    }
}
