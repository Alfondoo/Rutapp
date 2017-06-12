package com.master.javi.rutapp;

import android.Manifest;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.master.javi.rutapp.data.APIClient;
import com.master.javi.rutapp.data.Point;
import com.master.javi.rutapp.data.Route;
import com.master.javi.rutapp.data.RouteService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String EXTRA_ID = "EXTRA_ID";
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 99;
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE_2 = 98;
    private static final String TAG = "MapsActivity";
    private static final int STATE_RECORDING = 1;
    private static final int STATE_STOP = 2;
    private int currentState = STATE_STOP;
    private final int NO_ID = -1;
    private GoogleMap mMap;
    private Route route;
    private List<Point> pointsOfRoute;
    private FusedLocationProviderClient mFusedLocationClient;
    private Location mLastLocation;
    private FloatingActionButton fab;
    private RouteService rs;
    private Retrofit api;
    private Polyline polyline;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Intent i = getIntent();
        int id = i.getIntExtra(EXTRA_ID, NO_ID);
        api = APIClient.getClient();
        rs = api.create(RouteService.class);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fab = (FloatingActionButton) findViewById(R.id.fab_map);
        fab.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorRec)));

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentState == STATE_STOP) {
                    pointsOfRoute = new ArrayList<>();
                    getLastLocation();
                    currentState = STATE_RECORDING;
                    fab.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_stop));
                    fab.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.colorPause)));
                } else if (currentState == STATE_RECORDING) {
                    // Show dialog save
                    createDialog();
                    currentState = STATE_STOP;
                    fab.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_rec));
                    fab.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.colorRec)));
                }
            }
        });

        if (id != NO_ID) {
            rs.rutaDetalle(id, "IMEI").enqueue(new Callback<Route>() {
                @Override
                public void onResponse(Call<Route> call, Response<Route> response) {
                    route = response.body();
                }

                @Override
                public void onFailure(Call<Route> call, Throwable t) {
                    Log.e("ERROR", "Recuperando detalle de ruta");
                }
            });
        }
    }

    private void createDialog() {
        DialogFragment dialog = new RouteDialog();
        dialog.show(getFragmentManager(), "RouteDialog");
    }

    public void createRoute(String name, String description) {
        Log.i("RouteDialog Result", name + "/" + description);
        Log.v("createRoute", pointsOfRoute.toString());
        if (checkPermissionsPhone()) {
            TelephonyManager tManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            String uuid = tManager.getDeviceId();
            Log.d("UUID", uuid);
            Route r = new Route(name, description, pointsOfRoute, uuid);
            rs.crearRuta(r).enqueue(new Callback<Route>() {
                @Override
                public void onResponse(Call<Route> call, Response<Route> response) {
                    if (response.code() == 201) {
                        Snackbar.make(findViewById(android.R.id.content),
                                "Ruta creada",
                                Snackbar.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<Route> call, Throwable t) {
                    Snackbar.make(findViewById(android.R.id.content),
                            "Imposible conectar con el servidor",
                            Snackbar.LENGTH_LONG).show();
                }
            });
        } else {
            requestPermissionsPhone();
        }
    }

    private void addPointToPolyline(Point p){
        if(polyline == null){
            polyline = mMap.addPolyline(new PolylineOptions()
                    .width(5)
                    .color(Color.RED));
        }
        List<LatLng> latLngList = polyline.getPoints();
        latLngList.add(new LatLng(p.getLatitude(), p.getLongitude()));
        polyline.setPoints(latLngList);
    }

    @Override
    public void onStart() {
        super.onStart();

        if (!checkPermissionsLocation()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissionsLocation();
            }
        } else {
            getLastLocation();
        }

        if (!checkPermissionsPhone()) {
            requestPermissionsPhone();
        }
    }

    @Override
    @SuppressWarnings("MissingPermission")
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (!checkPermissionsLocation()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissionsLocation();
            }
        } else {
            mMap.setMyLocationEnabled(true);
        }
    }

    private void requestPermissionsLocation() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        if (shouldProvideRationale) {
            Log.v(TAG, "Mostrando mensaje relacionado con permisos");

            showSnackbar(R.string.permission_location_rationale, android.R.string.ok,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            startLocationPermissionRequest();
                        }
                    });

        } else {
            Log.i(TAG, "Solicitando permisos");
            startLocationPermissionRequest();
        }
    }

    private boolean checkPermissionsLocation() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(MapsActivity.this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_PERMISSIONS_REQUEST_CODE);
    }

    private void requestPermissionsPhone() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.READ_PHONE_STATE);

        if (shouldProvideRationale) {
            Log.v(TAG, "Mostrando mensaje relacionado con permisos");

            showSnackbar(R.string.permission_telefono_rationale, android.R.string.ok,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            startPhonePermissionRequest();
                        }
                    });

        } else {
            Log.i(TAG, "Solicitando permisos");
            startPhonePermissionRequest();
        }
    }

    private boolean checkPermissionsPhone() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.READ_PHONE_STATE);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void startPhonePermissionRequest() {
        ActivityCompat.requestPermissions(MapsActivity.this,
                new String[]{Manifest.permission.READ_PHONE_STATE},
                REQUEST_PERMISSIONS_REQUEST_CODE_2);
    }

    private void showSnackbar(final int mainTextStringId, final int actionStringId,
                              View.OnClickListener listener) {
        Snackbar.make(findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }

    @SuppressWarnings("MissingPermission")
    private void getLastLocation() {
        mFusedLocationClient.getLastLocation()
                .addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            mLastLocation = task.getResult();
                            if (pointsOfRoute != null) {
                                Point p = new Point(mLastLocation.getLongitude(), mLastLocation.getLatitude(), mLastLocation.getAltitude());
                                pointsOfRoute.add(p);
                                addPointToPolyline(p);
                            }
                            Log.v(TAG, mLastLocation.getLatitude() + "," + mLastLocation.getLongitude() + " at " + mLastLocation.getAltitude());
                            LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
                        } else {
                            Log.w(TAG, "getLastLocation:exception", task.getException());
                        }
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                Log.i(TAG, "El usuario ha cancelado.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            } else {
                showSnackbar(R.string.permission_denied_explanation, R.string.settings,
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        });
            }

        } else if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE_2) {
            if (grantResults.length <= 0) {
                Log.i(TAG, "El usuario ha cancelado.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Nada
            } else {
                showSnackbar(R.string.permission_denied_explanation, R.string.settings,
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        });
            }

        }
    }
}
