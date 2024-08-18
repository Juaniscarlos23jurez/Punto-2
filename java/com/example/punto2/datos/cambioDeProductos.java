package com.example.punto2.datos;

public class cambioDeProductos {
    int cantidad;
    int precio_compra;
    int precio_venta;
    int AnterCantidad;
    int AnterPrecio_compra;
    int  AnterPrecio_venta;
    int ResultCantidad;
    int ResultPrecio_venta;
    int ResultPrecio_compra;
    String Cambio;
     String codigo;
    String hora;
    String nombre;

    public int getPrecio_compra() {
        return precio_compra;
    }
    public int getPrecio_venta() {
        return precio_venta;
    }
    public int getCantidad() {
        return cantidad;
    }
    public int getAnterCantidad() {
        return AnterCantidad;
    }
    public int getAnterPrecio_compra() {
        return AnterPrecio_compra;
    }
    public int getAnterPrecio_venta() {
        return AnterPrecio_venta;
    }
    public int getResultCantidad() {
        return ResultCantidad;
    }
    public int getResultPrecio_venta() {
        return ResultPrecio_venta;
    }
    public int getResultPrecio_compra() {
        return ResultPrecio_compra;
    }


    public String getCambio() {
        return Cambio;
    }
    public String getCodigo() {
        return codigo;
    }
    public String getHora() {
        return hora;
    }
    public String getNombre() {
        return nombre;
    }

}
