package com.example.punto2;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class Clientes extends AppCompatActivity {
    TextView Nombre, telefono;
    FirebaseUser user;
    FirebaseAuth auth;
    Button button7;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clientes);
        // Inicialización de las vistas
        Nombre = findViewById(R.id.Nombre);
        telefono = findViewById(R.id.telefono);
         button7 = findViewById(R.id.button7);

        // Inicialización de Firebase
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        // Referencia a la base de datos
        FirebaseDatabase database1 = FirebaseDatabase.getInstance();
        DatabaseReference reference1 = database1.getReference("/MI data base Usuarios/" + user.getUid() + "/Cliente");

        // Configuración del OnClickListener del botón
        button7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Obtener los valores de los TextView
                String nom = Nombre.getText().toString();
                String tel = telefono.getText().toString();

                // Crear un HashMap con los datos del usuario
                HashMap<String, String> DatosUsuario = new HashMap<>();
                DatosUsuario.put("Nombre", nom);
                DatosUsuario.put("telefono", tel);

                // Generar una nueva entrada con un ID único
                reference1.push().setValue(DatosUsuario).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Los datos se enviaron correctamente
                            Toast.makeText(Clientes.this, "Cliente Registrado correctamente", Toast.LENGTH_SHORT).show();

                        } else {
                            // Ocurrió un error
                        }
                    }
                });
            }
        });
    }
}