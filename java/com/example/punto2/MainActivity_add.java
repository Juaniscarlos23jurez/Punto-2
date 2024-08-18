package com.example.punto2;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity_add extends AppCompatActivity {

    Button Producto,Proveedor,Cliente,edicion;
    Button editarprod,editarprove,editarclient,empleadob,agreagargastos,edicostos;
    TextView textView3,textView4,empleado,gastos;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_add);

        Producto=findViewById(R.id.Producto);
        Proveedor=findViewById(R.id.Proveedor);
        Cliente=findViewById(R.id.Cliente);
        editarprod=findViewById(R.id.editarprod);
        editarprove=findViewById(R.id.editarprove);
        editarclient=findViewById(R.id.editarclient);
        textView3=findViewById(R.id.textView3);
        textView4=findViewById(R.id.textView4);
        empleado=findViewById(R.id.empleado);
        gastos=findViewById(R.id.gastos);
        empleadob= findViewById(R.id.empleadob);
        agreagargastos= findViewById(R.id.agreagargastos);
        edicostos= findViewById(R.id.edicostos);
        editarclient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity_add.this,  EdicionClientes.class));

            }
        });
        edicostos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity_add.this,  edicioncostos.class));

            }
        });
        agreagargastos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity_add.this,  costos_gastos.class));

            }
        });
        empleadob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity_add.this,  emplados.class));

            }
        });
        //agregar productos
        Producto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity_add.this,  newproductos.class));
            }
        });
        editarprod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity_add.this,  edProducto.class));
            }
        });
        Proveedor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity_add.this,  newprovedor.class));
            }
        });
        editarprove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity_add.this,  edProvedor.class));
            }
        });
        edicion=findViewById(R.id.edicion);
        edicion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity_add.this,  EmpleadoED.class));

            }
        });
 }
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = null;
        FirebaseAuth auth = null;
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        FirebaseDatabase Tipo = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = Tipo.getReference("/MI data base Usuarios/" + user.getUid() + "/selectedOptionGroup2");
        String userId = user.getUid();

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String selectedOption = dataSnapshot.getValue(String.class);
                if ("individual".equals(selectedOption)) {
                     textView3.setVisibility(View.GONE);
                    textView4.setVisibility(View.GONE);
                } else if ("negocio".equals(selectedOption)) {
                    //mr1.setVisibility(View.GONE);
                    handleNegocio(userId);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Maneja el error
            }
        });

        // logueoUsuarios();

    }
    private void handleNegocio(String userId) {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w("FCM", "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        String token = task.getResult();
                        Log.d("FCM", "FCM Registration Token: " + token);

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("MI data base Usuarios/" + userId + "/not");

                        checkAndHandleToken(reference, token);
                    }
                });
    }
    private void checkAndHandleToken(DatabaseReference reference, String token) {
        reference.orderByChild("key").equalTo(token).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        String tipo = child.child("tipo").getValue(String.class);
                        if ("jefe".equals(tipo)) {

                            Log.d("Firebase", "El token pertenece al jefe.");
                            // Muestra información del jefe
                        } else if ("empleado".equals(tipo)) {
                            Log.d("Firebase", "El token pertenece a un empleado.");
                            // Oculta botones y muestra el AlertDialog para elegir empleado
                            empleadob.setVisibility(View.GONE);
                            edicion.setVisibility(View.GONE);
                            agreagargastos.setVisibility(View.GONE);
                            edicostos.setVisibility(View.GONE);
                            Proveedor.setVisibility(View.GONE);
                            editarprove.setVisibility(View.GONE);
                            Cliente.setVisibility(View.GONE);
                            editarclient.setVisibility(View.GONE);
                            textView3.setVisibility(View.GONE);
                            textView4.setVisibility(View.GONE);
                            empleado.setVisibility(View.GONE);
                            gastos.setVisibility(View.GONE);
                         }
                    }
                } else {
                    Log.d("Firebase", "Token no registrado.");
                    // Lógica para registrar un nuevo token si es necesario
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("Firebase", "Error al acceder a la base de datos.", error.toException());
            }
        });

    }
 }/*
        editarprove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity_add.this,  MainActivityedicionprov.class));

            }
        });
    }
}*/