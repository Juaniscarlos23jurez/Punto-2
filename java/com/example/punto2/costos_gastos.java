package com.example.punto2;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class costos_gastos extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private EditText nombreEditText, descripcionEditText, montoEditText;
    private TimePicker timePicker;
    private CalendarView calendarView;
    AutoCompleteTextView autoCompleteTextView;
    ArrayAdapter<String> adapterItems;
    String[] items111 = {
            "Alquiler",          // Alquiler de oficina, local o espacio
            "Servicios Públicos", // Electricidad, agua, gas
            "Internet y Teléfono", // Costos de comunicación
            "Suministros de Oficina", // Papelería, material de oficina
            "Salarios",          // Sueldos y salarios de empleados
            "Capacitación",      // Cursos, talleres y formación
            "Mantenimiento",     // Reparaciones y mantenimiento de equipo
            "Publicidad",        // Marketing y campañas publicitarias
            "Viajes y Transporte", // Gastos de desplazamiento
            "Servicios Profesionales", // Honorarios de abogados, contadores, etc.
            "Seguros",           // Seguros de negocio y propiedad
            "Inventario",        // Compra de productos o materiales
            "Gastos Bancarios",  // Comisiones y cargos bancarios
            "Impuestos",         // Impuestos a pagar
            "Equipo de Oficina", // Compra o arrendamiento de equipos
            "Costos de Producción", // Costos asociados a la producción de bienes
            "Otros"              // Categoría para gastos diversos no listados
    };
    AutoCompleteTextView autoCompleteTextView1;
    ArrayAdapter<String> adapterItems1;
    String[] items  = {
            "Unico",          // Alquiler de oficina, local o espacio
            "Diario", // Electricidad, agua, gas
            "Semana", // Costos de comunicación
            "Mensual", // Papelería, material de oficina
            "Trimestral",          // Sueldos y salarios de empleados
            "Semestral",      // Cursos, talleres y formación
            "Anual"

    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_costos_gastos);

        // Inicializa Firebase
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Referencias a los campos del formulario
        //  nombreEditText = findViewById(R.id.Nombre);
        descripcionEditText = findViewById(R.id.telefono);
        montoEditText = findViewById(R.id.empresa);
        timePicker = findViewById(R.id.timePickerInicio);
        calendarView = findViewById(R.id.calendarView1);

        // Configura el botón de subir
        Button subirButton = findViewById(R.id.button7);
        subirButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guardarDatos();
            }
        });
        autoCompleteTextView = findViewById(R.id.Nombre);
          adapterItems = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, items111);
        autoCompleteTextView.setAdapter(adapterItems);
        autoCompleteTextView1 = findViewById(R.id.Categoia1);
        adapterItems1 = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, items );
        autoCompleteTextView1.setAdapter(adapterItems1) ;
    }

    private void guardarDatos() {
        // Obtén los valores del formulario
         String descripcion = descripcionEditText.getText().toString();
        int monto = Integer.parseInt(montoEditText.getText().toString());
        long fechaMillis = calendarView.getDate();
        String hora = String.valueOf(timePicker.getCurrentHour());
        String minuto = String.valueOf(timePicker.getCurrentMinute());
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String fechaFormateada = dateFormat.format(new Date(fechaMillis));
         String categoriaSeleccionada = autoCompleteTextView.getText().toString();
        String tiempo = autoCompleteTextView1.getText().toString();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference gastosRef = mDatabase.child("MI data base Usuarios").child(userId).child("gastos_costos");

        String idUnico = gastosRef.push().getKey();

        // Crea un objeto para almacenar los datos
        Map<String, Object> gastoData = new HashMap<>();
        gastoData.put("nombre", categoriaSeleccionada);
        gastoData.put("descripcion", descripcion);
        gastoData.put("monto", monto);
        gastoData.put("fecha", fechaFormateada);
        gastoData.put("hora", hora+":"+minuto);
         gastoData.put("tiempo", tiempo);
        gastoData.put("id",idUnico );

        // Guarda los datos en la base de datos
         mDatabase.child("MI data base Usuarios").child(userId).child("gastos_costos").push().setValue(gastoData)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(costos_gastos.this, "Datos guardados exitosamente", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(costos_gastos.this, "Error al guardar datos", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
