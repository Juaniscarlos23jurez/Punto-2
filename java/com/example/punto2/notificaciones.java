package com.example.punto2;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.punto2.adaptadores.historial_insumo;
import com.example.punto2.datos.isumos_datos;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class notificaciones extends AppCompatActivity {
    RecyclerView aler;
    historial_insumo aler12;
    DatabaseReference daabase;
    ArrayList<isumos_datos> arrayaler;
    FirebaseUser user;
    FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notificaciones);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        aler = findViewById(R.id.productos);
        daabase = FirebaseDatabase.getInstance().getReference("/MI data base Usuarios/"+ user.getUid()+ "/insumos_verificados");
        aler.setHasFixedSize(false);
        //cobiar el layaout por el gridlayaout y colocarle las 2 columnas
        aler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        arrayaler = new ArrayList<>();
        aler12 = new historial_insumo(this, arrayaler);
        aler.setAdapter(aler12);
        daabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                arrayaler.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    isumos_datos refere = dataSnapshot.getValue(isumos_datos.class);
                    arrayaler.add(refere);
                }
                aler12.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
}