package com.example.hiking;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.example.hiking.MainActivity.arrayAdapter;
import static com.example.hiking.MainActivity.coordinates;
import static com.example.hiking.MainActivity.places;

import static com.example.hiking.MainActivity.viewSavedLoc;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;
    LatLng currLoc,tapLoc,lastLoc,showLoc;
    Marker currLocMarker,lastLocMarker;



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0, locationListener);
                }
            }
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    if(currLocMarker != null) {
                        currLocMarker.remove();
                    }
                    if(lastLocMarker != null) {
                        lastLocMarker.remove();
                    }
                    currLoc = new LatLng(location.getLatitude(), location.getLongitude());
                    currLocMarker = mMap.addMarker(new MarkerOptions().position(currLoc).title("Your Current Location"));
                    if (viewSavedLoc == -1) {
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currLoc, 15));
                    }
                }
                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {}
                @Override
                public void onProviderEnabled(String provider) {}
                @Override
                public void onProviderDisabled(String provider) {}
            };

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0, locationListener);
            }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        final SharedPreferences sharedPreferences = this.getSharedPreferences("com.example.hiking",Context.MODE_PRIVATE);

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener(){
            @Override
            public void onMapLongClick(LatLng latLng) {
                tapLoc = new LatLng(latLng.latitude, latLng.longitude);
                Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                List<Address> addressList = null;
                try {
                    addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                places.add(addressList.get(0).getAddressLine(0));
                coordinates.add(latLng);
                arrayAdapter.notifyDataSetChanged();
                mMap.addMarker(new MarkerOptions().position(tapLoc).title(addressList.get(0).getAddressLine(0)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
                Toast.makeText(MapsActivity.this, "Location Saved", Toast.LENGTH_SHORT).show();

                try {

                    sharedPreferences.edit().putString("places",ObjectSerializer.serialize(places)).apply();

                    ArrayList<String> latitudes  = new ArrayList<String>();
                    ArrayList<String> longitudes = new ArrayList<String>();

                    for(LatLng coor : coordinates) {
                        latitudes.add(Double.toString(coor.latitude));
                        longitudes.add(Double.toString(coor.longitude));
                    }

                    sharedPreferences.edit().putString("lats",ObjectSerializer.serialize(latitudes)).apply();
                    sharedPreferences.edit().putString("lons",ObjectSerializer.serialize(longitudes)).apply();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && viewSavedLoc == -1) {
            mMap.clear();
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(lastKnownLocation != null) {
                lastLoc = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                lastLocMarker = mMap.addMarker(new MarkerOptions().position(lastLoc).title("Your Last Location"));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastLoc, 13));
            }
        }

        if(viewSavedLoc != -1) {
            showLoc = coordinates.get(viewSavedLoc);
            mMap.addMarker(new MarkerOptions().position(showLoc).title(places.get(viewSavedLoc)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(showLoc,13));
        }

    }

}