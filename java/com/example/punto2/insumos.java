package com.example.punto2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.punto2.adaptadores.NotificationHelper;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class insumos extends AppCompatActivity {
    FirebaseUser user;
    FirebaseAuth auth;
    Button Agregar;
    private AutoCompleteTextView autoNombre, proveedores;
    private DatabaseReference nombresRef,rpovedoresRef;
     AutoCompleteTextView autoCompleteTextView1;
    ArrayAdapter<String> adapterItems1;
    String[] items  = {
            "Contado",          // Alquiler de oficina, local o espacio
            "Credito", // Electricidad, agua, gas


    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insumos);
        autoCompleteTextView1 = findViewById(R.id.tipo);
        adapterItems1 = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, items );
        autoCompleteTextView1.setAdapter(adapterItems1) ;
        Button subirButton = findViewById(R.id.button7);
         EditText precioCompraEditText = findViewById(R.id.precioc);
        EditText cantidadEditText = findViewById(R.id.cantidad);
        EditText kilosEditText = findViewById(R.id.kilos);
        proveedores = findViewById(R.id.AutoCompleteProveedor);
          Agregar = findViewById(R.id.ordenes);
        Agregar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(insumos.this, nombre.class));

            }
        });
        autoNombre = findViewById(R.id.autoNombre);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        nombresRef = FirebaseDatabase.getInstance().getReference("/MI data base Usuarios/"+ user.getUid() +"/nombres" );
        cargarNombres();
          rpovedoresRef = FirebaseDatabase.getInstance().getReference("/MI data base Usuarios/"+ user.getUid() +"/Provedor" );
        cargarProvedores();
        subirButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nombre = autoNombre.getText().toString().trim();
                int precioCompra = Integer.parseInt(precioCompraEditText.getText().toString().trim());
                int cantidad = Integer.parseInt(cantidadEditText.getText().toString().trim());
                int kilos = Integer.parseInt(kilosEditText.getText().toString().trim());
                String proveedor = proveedores.getText().toString().trim();
                Date fechaActual = new Date();
                // Formatear la fecha y la hora
                String fecha = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(fechaActual);
                String hora = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(fechaActual);
                // Obtener el número del mes
                int mes = Calendar.getInstance().get(Calendar.MONTH) + 1; // +1 porque los meses en Calendar empiezan en 0
                // Crear el mapa con los datos
                Map<String, Object> insumoData = new HashMap<>();
                insumoData.put("nombre", nombre);
                insumoData.put("precioCompra", precioCompra);
                insumoData.put("cantidad", cantidad);
                insumoData.put("kilos", kilos);
                insumoData.put("proveedor", proveedor);
                insumoData.put("fecha", fecha);
                insumoData.put("hora", hora);
                insumoData.put("mes", mes);
                insumoData.put("Tipo", autoCompleteTextView1.getText().toString().trim());  // Corrección aquí

                DatabaseReference insumosRef = FirebaseDatabase.getInstance().getReference("/MI data base Usuarios/" + user.getUid() + "/insumos");

                insumosRef.push().setValue(insumoData)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(insumos.this, "Insumo registrado con éxito", Toast.LENGTH_SHORT).show();
                                int badgeCount = 1;  // Ajusta este valor según la cantidad de notificaciones no leídas
                                NotificationHelper notificationHelper = new NotificationHelper(insumos.this, badgeCount);
                                notificationHelper.createNotification("Nuevo Insumo", "Revision del insumo añadido ");
                                notifi2( );
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(insumos.this, "Error al registrar insumo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

    }

    private void cargarProvedores() {
        rpovedoresRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<String> nombresList = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Accede al valor de "Nombre" dentro de cada nodo de Provedor
                    String nombre = snapshot.child("Nombre").getValue(String.class);
                    String empresa = snapshot.child("empresa").getValue(String.class);



                    if (nombre != null) {
                        nombresList.add(nombre+" ("+empresa+")");
                    }
                }

                // Crear y asignar el adaptador al AutoCompleteTextView
                ArrayAdapter<String> adapter = new ArrayAdapter<>(insumos.this, android.R.layout.simple_dropdown_item_1line, nombresList);
                proveedores.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(insumos.this, "Error al cargar nombres: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void cargarNombres() {
        nombresRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<String> nombresList = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String nombre = snapshot.getValue(String.class);
                    if (nombre != null) {
                        nombresList.add(nombre);
                    }
                }

                // Crear y asignar el adaptador al AutoCompleteTextView
                ArrayAdapter<String> adapter = new ArrayAdapter<>(insumos.this, android.R.layout.simple_dropdown_item_1line, nombresList);
                autoNombre.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(insumos.this, "Error al cargar nombres: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void notifi2() {
        RequestQueue myreques= Volley.newRequestQueue(getApplicationContext());
        JSONObject json=new JSONObject();
        try {
            json.put("to","cEd0hdIUQzGQMPrmZ2g1S5:APA91bGx8OjDp9Gzp8Zj-s4xFdCHmImaZOS7HHHMvn9OT0y_Z002zHnlz9Ai2VmtznKCDN92zDdacQR-IxLk69pY5WCaY5FPXz0gRqF2h3WD8B_OUxTjmKCjE_B8t-EOanBqOXRggpCN");
            JSONObject notification=new JSONObject();
            notification.put("title","Nuevo insumo agregado");
            notification.put("body"," : Revisión pendiente");
            json.put("notification",notification);
            String URL="https://fcm.googleapis.com/fcm/send";
            JsonObjectRequest request=new JsonObjectRequest( Request.Method.POST,URL,json,null,null){
                @Override
                public Map<String, String> getHeaders()   {
                    Map<String, String>header=new HashMap<>();
                    header.put("Content-Type","application/json");
                    header.put("Authorization","key=AAAAmHBedfQ:APA91bHVkL_EtfqjnAnhL79XWAztBLhfonHer30ckkIuCkI2I0w7gfDdX-Rkd0ZU6_Uw3qLVMywxFHlSnIm7uuZ_OM1KWMdleEAHtQkzeC1Ha4rvijFpvGKfqrdNC1iBsot0nRHksWgP");
                    return header;
                }
            };
            myreques.add(request);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}