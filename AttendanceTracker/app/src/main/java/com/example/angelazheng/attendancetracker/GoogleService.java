package com.example.angelazheng.attendancetracker;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;


public class GoogleService extends Service implements LocationListener {
    private LocationManager mLocation;
    public Intent here;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        // The service is starting, due to a call to startService()
        super.onCreate();
        Log.d("Location Service", "Service started!");

        // referenced teamtreehouse.com
        mLocation = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // referenced github android_guides by Roberto Robson
        try {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                Log.d("securityCheck", "security check did not pass RIP");
                return;
            }
            if (mLocation.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                Log.d("GPS:", "using GPS location");
                mLocation.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        10 * 1000,
                        1000f,
                        this
                );
            }
            if (mLocation.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
                Log.d("Network: ", "using Network_provider location");
                mLocation.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        10 * 1000,
                        100000f,
                        this
                );
            }
        } catch (java.lang.SecurityException ex) {
            Log.d("exception", "didn't get location updates due to SecurityException");
        } catch (java.lang.IllegalArgumentException ex) {
            Log.d("illegalArgument", "no network providers RIP");
        }
    }


    @Override
    public void onLocationChanged(Location location) {
        // create doubles array of latitude, longitude coordinates
        double[] latlong = new double[] {location.getLatitude(),location.getLongitude()};
        here = new Intent(this, MainActivity.class);
        Bundle b = new Bundle();
        b.putDoubleArray("location", latlong);
        here.putExtra("location", b);
        here.setAction("BROADCAST_ACTION");
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(here);
        Log.d("Broadcast", "broadcast sent");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
