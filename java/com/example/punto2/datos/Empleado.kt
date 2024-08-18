package com.example.punto2.datos

data class Empleado(
    val nombre: String = "",
    val sueldo: Double = 0.0,
    val horaEntrada: String = "",
    val horaSalida: String = "",
    val tiempo :String =  "",
    val primer_pago: String = ""  // Se agrega la fecha seleccionada como primer_pago

)
