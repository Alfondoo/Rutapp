package com.master.javi.rutapp.data;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface RouteService {
    @GET("rutas")
    Call<List<Route>> listaRutas(@Query("device") String device);
    @POST("rutas/")
    Call<Route> crearRuta(@Body Route ruta);
    @GET("rutas/{id}")
    Call<Route> rutaDetalle(@Path("id") int id, @Query("device") String device);
}


