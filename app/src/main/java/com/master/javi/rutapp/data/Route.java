package com.master.javi.rutapp.data;

import java.util.List;

public class Route {

    private int id;
    private String name;
    private String description;
    private String created_at;
    private String device;
    private List<Point> puntos;

    public Route(String title, String description, String created_at){
        this.name = title;
        this.description = description;
        this.created_at = created_at;
    }

    public Route(String title, String description, List<Point> puntos, String device){
        this.name = title;
        this.description = description;
        this.puntos = puntos;
        this.device = device;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreatedAt() {
        return created_at;
    }

    public void setCreatedAt(String created_at) {
        this.created_at = created_at;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public List<Point> getPuntos() {
        return puntos;
    }

    public void setPuntos(List<Point> puntos) {
        this.puntos = puntos;
    }
}

