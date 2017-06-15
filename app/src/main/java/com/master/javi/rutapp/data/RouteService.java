package com.master.javi.rutapp.data;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Servicios para consumir con nuestra API de Retrofit. En este caso se han empleado tres puesto que son las rutas disponibles en nuestro servidor.
 */

public interface RouteService {
    // Query representa a la parte ?key=value en una URL.
    @GET("rutas")
    Call<List<Route>> listaRutas(@Query("device") String device);
    // Body representa al clásico diccionario JSON.
    @POST("rutas/")
    Call<Route> crearRuta(@Body Route ruta);
    // Path nos servirá para dinamizar la petición gracias al parámetro introducido.
    @GET("rutas/{id}")
    Call<Route> rutaDetalle(@Path("id") int id, @Query("device") String device);
}


