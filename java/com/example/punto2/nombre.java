package com.example.punto2;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class nombre extends AppCompatActivity {
    private EditText nombreEditText;
    private Button subirButton;
    private DatabaseReference nombresRef;
    FirebaseUser user;
    FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nombre);
        // Inicializar las vistas y la referencia a Firebase
        nombreEditText = findViewById(R.id.Nombre);
        subirButton = findViewById(R.id.button7);
         auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        nombresRef = FirebaseDatabase.getInstance().getReference("/MI data base Usuarios/"+ user.getUid() +"/nombres" );

        // Configurar el botón para subir el nombre
        subirButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String nombre = nombreEditText.getText().toString().trim().toLowerCase();

                // Verificar que el campo no esté vacío
                if (TextUtils.isEmpty(nombre)) {
                    Toast.makeText(nombre.this, "Por favor, ingresa un nombre", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Comprobar si el nombre ya existe
                nombresRef.orderByValue().equalTo(nombre).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                            // Si no existe, guardar el nombre
                            nombresRef.push().setValue(nombre)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(nombre.this, "Nombre registrado con éxito", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(nombre.this, "Error al registrar nombre: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(nombre.this, "Error de base de datos: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}