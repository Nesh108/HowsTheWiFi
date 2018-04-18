package com.kulplex.nesh.howsthewifi;

import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cz.msebera.android.httpclient.Header;

public class MapFragment extends Fragment implements OnMapReadyCallback
{
    private GoogleMap mMap;
    private Marker userMarker;
    private MarkerOptions userMarkerOptions;
    private View rootView;
    private double[] connectionRanges = {10d, 100d, 1000d, 10000d, Double.MAX_VALUE};
    private float[] connectionColors = {BitmapDescriptorFactory.HUE_RED,
                                        BitmapDescriptorFactory.HUE_ORANGE,
                                        BitmapDescriptorFactory.HUE_YELLOW,
                                        BitmapDescriptorFactory.HUE_GREEN,
                                        BitmapDescriptorFactory.HUE_CYAN};

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
                UpdateOfflinePins(response);
                AddPinsToMap(response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String error, Throwable throwable)
            {
                AddPinsToMap(GetStoredPins());
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject json)
            {
                AddPinsToMap(GetStoredPins());
            }
        });
    }

    void UpdateOfflinePins(JSONArray pins)
    {
        SharedPreferences settings = getContext().getSharedPreferences(
                getString(R.string.user_settings_keystore), 0);

        String storedPinsString = settings.getString(getString(R.string.pins_key), "[]");
        try
        {
            JSONArray storedPins = new JSONArray(storedPinsString);
            JSONArray updatedPins = ConcatArray(storedPins, pins);

            SharedPreferences.Editor editor = settings.edit();
            editor.putString(getString(R.string.pins_key), updatedPins.toString());

            editor.apply();
        } catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    JSONArray GetStoredPins()
    {
        SharedPreferences settings = getContext().getSharedPreferences(
                getString(R.string.user_settings_keystore), 0);

        String storedPinsString = settings.getString(getString(R.string.pins_key), "[]");
        try
        {
            return new JSONArray(storedPinsString);
        } catch (JSONException e)
        {
            e.printStackTrace();
        }
        return new JSONArray();
    }

    private JSONArray ConcatArray(JSONArray... arrs) throws JSONException
    {
        JSONArray result = new JSONArray();
        for (JSONArray arr : arrs)
        {
            for (int i = 0; i < arr.length(); i++)
            {
                JSONObject pin = (JSONObject) arr.get(i);
                if(!PinExists(result, pin.getInt("id"))) {
                    result.put(pin);
                }
            }
        }
        return result;
    }

    private boolean PinExists(JSONArray jsonArray, int id){
        return jsonArray.toString().contains("\"id\":\"" + id + "\"");
    }

    void AddPinsToMap(JSONArray locationsJson)
    {
        mMap.clear();
        if (userMarkerOptions != null)
        {
            mMap.addMarker(userMarkerOptions);
        }

        for (int i = 0; i < locationsJson.length(); i++)
        {
            try
            {
                JSONObject loc = locationsJson.getJSONObject(i);
                LatLng pin = new LatLng(loc.getDouble("latitude"), loc.getDouble("longitude"));
                mMap.addMarker(new MarkerOptions().position(pin).icon(
                        BitmapDescriptorFactory.defaultMarker(
                                GetColourFromSpeed(loc.getDouble("download")))).title(
                        loc.getString("name")).snippet("Download Speed: " + MainFragment.round(
                        (float) (loc.getDouble("download")) * 0.001f,
                        1) + " Kb/s | Upload Speed: " + MainFragment.round(
                        (float) (loc.getDouble("upload")) * 0.001f, 1) + " Kb/s"));
            } catch (JSONException e)
            {
                e.printStackTrace();
            }
        }
    }

    protected void setUserLocationPin(LocationResult locationResult)
    {
        if (locationResult != null && mMap != null)
        {
            Location location = locationResult.getLocations().get(0);
            LatLng userPin = new LatLng(location.getLatitude(), location.getLongitude());

            if (userMarker != null)
            {
                userMarkerOptions = null;
                userMarker.remove();
            }

            userMarkerOptions = new MarkerOptions().position(userPin).icon(
                    BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
            userMarker = mMap.addMarker(userMarkerOptions);
        }

    }

    private float GetColourFromSpeed(double downloadSpeed)
    {
        for (int i = 0; i < connectionRanges.length; i++)
        {
            if (downloadSpeed <= connectionRanges[i])
            {
                return connectionColors[i];
            }
        }
        return connectionColors[connectionColors.length - 1];
    }
}
