package com.parse.starter;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;


public class MapActivity extends ActionBarActivity implements OnMapReadyCallback {

    private Double Longitude;
    private Double Latitude;
    private ArrayList<String> longitudeList;
    private ArrayList<String> latitudeList;
    private ArrayList<String> titleList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Intent i = getIntent();
        if (i.getExtras() != null) {
            if (i.getStringArrayListExtra("Longitudes") != null) {
                longitudeList = i.getStringArrayListExtra("Longitudes");
                latitudeList = i.getStringArrayListExtra("Latitudes");
                titleList = i.getStringArrayListExtra("Titles");

            } else {
                Longitude = i.getDoubleExtra("Longitude", 0);
                Latitude = i.getDoubleExtra("Latitude", 0);
            }
        }

    }

    @Override
    public void onMapReady(GoogleMap map) {
        if (Longitude != null) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(Latitude, Longitude), 16));

            // You can customize the marker image using images bundled with
            // your app, or dynamically generated bitmaps.
            map.addMarker(new MarkerOptions()
                    .anchor(0.0f, 1.0f) // Anchors the marker on the bottom left
                    .position(new LatLng(Latitude, Longitude)));
        } else {
            for (int i = 0; i < latitudeList.size(); i++) {
                map.addMarker(new MarkerOptions()
                        .anchor(0.0f, 1.0f) // Anchors the marker on the bottom left
                        .position(new LatLng(Double.valueOf(latitudeList.get(i)), Double.valueOf(longitudeList.get(i)))).title(titleList.get(i)));

            }

        }
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_map, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
}
