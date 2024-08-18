package com.example.punto2

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.CalendarView
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.punto2.datos.Empleado
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.Locale

class emplados : AppCompatActivity() {
     private lateinit var auth: FirebaseAuth
     private lateinit var databaseReference: DatabaseReference
    private lateinit var autoCompleteTextView: AutoCompleteTextView
    private lateinit var calendarView: CalendarView
    private var fechaSeleccionada: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emplados)
        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference

        // Configura AutoCompleteTextView
        autoCompleteTextView = findViewById(R.id.contenedor1)
        val items = arrayOf(
            "Unico", "Diario", "Semana", "Mensual", "Trimestral", "Semestral", "Anual"
        )
        val adapterItems = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, items)
        autoCompleteTextView.setAdapter(adapterItems)

        // Inicializa CalendarView y captura la fecha seleccionada
        calendarView = findViewById(R.id.calendarView1)
        calendarView.setOnDateChangeListener { _, dayOfMonth, month, year ->
            // Convierte la fecha seleccionada en formato "yyyy-MM-dd"
            fechaSeleccionada = String.format(Locale.getDefault(), "%02d/%02d/%04d" , year, month + 1, dayOfMonth)
        }

        // Obtén las referencias de los elementos UI
        val nombreEditText = findViewById<TextInputEditText>(R.id.Nombre)
        val sueldoEditText = findViewById<TextInputEditText>(R.id.precioc)
        val timePickerInicio = findViewById<TimePicker>(R.id.timePickerInicio)
        val timePickerFin = findViewById<TimePicker>(R.id.timePickerFin)
        val subirButton = findViewById<Button>(R.id.button7)

        // Configura el botón de subida
        subirButton.setOnClickListener {
            val nombre = nombreEditText.text.toString().trim()
            val sueldo = sueldoEditText.text.toString().toDoubleOrNull() ?: 0.0
            val tiempo = autoCompleteTextView.text.toString().trim()
            if (nombre.isEmpty()) {
                nombreEditText.error = "El nombre es obligatorio"
                return@setOnClickListener
            }
            if (sueldo == null || sueldo <= 0) {
                sueldoEditText.error = "Ingresa un sueldo válido"
                return@setOnClickListener
            }
            if (tiempo.isEmpty()) {
                autoCompleteTextView.error = "Selecciona una frecuencia de pago"
                return@setOnClickListener
            }
            if (fechaSeleccionada.isEmpty()) {
                Toast.makeText(this, "Selecciona una fecha de primer pago", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // Obtén la hora de entrada y salida del TimePicker
            val horaEntrada = String.format(Locale.getDefault(), "%02d:%02d", timePickerInicio.hour, timePickerInicio.minute)
            val horaSalida = String.format(Locale.getDefault(), "%02d:%02d", timePickerFin.hour, timePickerFin.minute)

            // Crear objeto Empleado incluyendo la fecha seleccionada
            val empleado = Empleado(nombre, sueldo, horaEntrada, horaSalida, tiempo, fechaSeleccionada)

            // Guardar en Firebase
            guardarEmpleadoEnFirebase(empleado)
        }
    }

    private fun guardarEmpleadoEnFirebase(empleado: Empleado) {
        val user = auth.currentUser
        user?.let {
            val userId = it.uid
            val empleadoId = databaseReference.child("MI data base Usuarios").child(userId).child("Empleados").push().key
            empleadoId?.let { id ->
                databaseReference.child("MI data base Usuarios").child(userId).child("Empleados").child(id).setValue(empleado)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Empleado registrado exitosamente", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Error al registrar empleado", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
    }
}