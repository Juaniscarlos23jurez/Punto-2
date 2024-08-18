package com.example.punto2.datos;

public class actualizacion {

    int cantidad  ;
    String codigo  ;
    String fecha  ;
    String imageUrl  ;
    String nombre  ;
    double precioCompra  ;
    double precioVenta ;


    // Getters y Setters
    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }


    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }


    public void setFecha(String fecha) {
        this.fecha = fecha;
    }


    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public double getPrecioCompra() {
        return precioCompra;
    }


    public double getPrecioVenta() {
        return precioVenta;
    }


    // Método para obtener el ID si es necesario
    public String getId() {
        return codigo; // Suponiendo que el código es único y puede actuar como ID
    }
}