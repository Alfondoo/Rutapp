package com.master.javi.rutapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.master.javi.rutapp.data.APIClient;
import com.master.javi.rutapp.data.Route;
import com.master.javi.rutapp.data.RouteService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class RoutesActivity extends AppCompatActivity {

    private static final String EXTRA_ID = "EXTRA_ID";
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE_2 = 98;
    private static final String TAG = "RoutesActivity";
    private RecyclerView recyclerView;
    private FloatingActionButton fab;
    private LayoutInflater layoutInflater;

    private RoutesAdapter routesAdapter;

    private List<Route> routesList;
    private TextView emptyTextView;
    private Retrofit api;
    private RouteService rs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routes);

        recyclerView = (RecyclerView) findViewById(R.id.rv_routes_activity);
        emptyTextView = (TextView) findViewById(R.id.rv_empty_text);

        fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Accediendo a MapsActivity!");
                Intent i = new Intent(getApplicationContext(), MapsActivity.class);
                startActivity(i);
            }
        });

        layoutInflater = getLayoutInflater();
        routesList = new ArrayList<Route>();

        // Instanciamos el servicio de Retrofit
        api = APIClient.getClient();
        rs = api.create(RouteService.class);

        // Creamos el adapter para mostrar nuestras rutas en el RecyclerView
        routesAdapter = new RoutesAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(routesAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Comprobamos si tenemos permisos sobre los datos del teléfono. Necesarios para obtener el IMEI
        if (!checkPermissionsPhone()) {
            requestPermissionsPhone();
        } else {
            solicitarRutas();
        }
    }

    private void solicitarRutas() {
        // Obtenemos el IMEI
        TelephonyManager tManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String uuid = tManager.getDeviceId();
        Log.d(TAG, "IMEI: " + uuid);
        // Lanzamos la petición mediante retrofit
        rs.listaRutas(uuid).enqueue(new Callback<List<Route>>() {
            @Override
            public void onResponse(Call<List<Route>> call, Response<List<Route>> response) {
                if (response.body() != null) {
                    // Cuando ha finalizado y hemos obtenido una lista de rutas, procedemos a poblar el adapter y a efectuar cambios en la interfaz.
                    Log.d(TAG, "Rutas: " + response.body().toString());
                    routesList = response.body();
                    routesAdapter.notifyDataSetChanged();
                    if (routesList.size() > 0) {
                        emptyTextView.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Route>> call, Throwable t) {
                Log.e(TAG, "Error recuperando las rutas");
                Toast.makeText(getApplicationContext(), "No se han podido recuperar las rutas", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void startMapActivityWithRoute(Route item) {
        // Lanzaremos la actividad del mapa pasando la ID de la ruta que queremos que sea representada
        Intent i = new Intent(this, MapsActivity.class);
        i.putExtra(EXTRA_ID, item.getId());
        startActivity(i);
    }

    private void requestPermissionsPhone() {
        // Solicitaremos los permisos de teléfono
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
        ActivityCompat.requestPermissions(RoutesActivity.this,
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE_2) {
            if (grantResults.length <= 0) {
                Log.i(TAG, "El usuario ha cancelado.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                solicitarRutas();
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

    class RoutesAdapter extends RecyclerView.Adapter<RoutesAdapter.RoutesViewHolder> {

        // Adaptador personalizado para mostrar la información de las rutas en el RecyclerView

        @Override
        public RoutesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = layoutInflater.inflate(R.layout.route_item, parent, false);
            return new RoutesViewHolder(v);
        }

        @Override
        public void onBindViewHolder(RoutesViewHolder holder, int position) {
            // Función que se ejecuta cuando el adapter empareja la vista con el el ViewHolder.
            // Aquí por lo tanto será donde necesitamos actualizar la interfaz.
            Route currentItem = routesList.get(position);
            holder.name.setText(currentItem.getName());
            holder.description.setText(currentItem.getDescription());
            holder.created.setText(currentItem.getCreatedAt());
        }

        @Override
        public int getItemCount() {
            return routesList.size();
        }

        class RoutesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            // Vista personalizada para cada "elemento" de nuestro adapter.

            private TextView name;
            private TextView description;
            private TextView created;
            private ViewGroup container;

            public RoutesViewHolder(View itemView) {
                super(itemView);
                this.name = (TextView) itemView.findViewById(R.id.title_list_item);
                this.description = (TextView) itemView.findViewById(R.id.description_list_item);
                this.created = (TextView) itemView.findViewById(R.id.created_list_item);
                this.container = (ViewGroup) itemView.findViewById(R.id.root_list_item);

                this.container.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                // Cuando seleccionemos cualquier celda/fila se disparará este evento.
                startMapActivityWithRoute(routesList.get(this.getAdapterPosition()));
            }
        }
    }
}
