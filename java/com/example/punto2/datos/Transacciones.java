package com.example.punto2.datos;

public class Transacciones {
    int cantidad;
   String fecha;
   String tipo;
    int anterior;
    int   finalTotal;
    public int getCantidad() {
        return cantidad;
    }
    public int getAnterior() {
        return anterior;
    }
    public int getFinalTotal() {
        return finalTotal;
    }
    public String getTipo() {
        return tipo;
    }
    public String getFecha() {
        return fecha;
    }

}
