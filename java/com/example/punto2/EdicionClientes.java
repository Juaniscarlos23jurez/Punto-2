package com.example.punto2;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.punto2.adaptadores.adaptadorCliente;
import com.example.punto2.datos.provedor;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class EdicionClientes extends AppCompatActivity {
    RecyclerView recyclerView;
    adaptadorCliente adaptadorProbedor1;
    DatabaseReference base;
    ArrayList<provedor> provedor1;

    FirebaseUser user;
    FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edicion_clientes);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        recyclerView = findViewById(R.id.Almacen);
        base = FirebaseDatabase.getInstance().getReference("/MI data base Usuarios/" + user.getUid() + "/Cliente");

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        provedor1 = new ArrayList<>();
        adaptadorProbedor1 = new adaptadorCliente(this, provedor1);
        recyclerView.setAdapter(adaptadorProbedor1);

        base.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                provedor1.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    provedor carrito2 = dataSnapshot.getValue(provedor.class);
                    provedor1.add(carrito2);
                }
                adaptadorProbedor1.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Manejo de error
            }
        });
    }
}
