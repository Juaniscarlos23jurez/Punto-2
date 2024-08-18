package com.example.punto2;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.punto2.adaptadores.adaptadorStok;
import com.example.punto2.datos.stok;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class edProducto extends AppCompatActivity {
    RecyclerView recyclerView;
    adaptadorStok stokAdaptador;
    DatabaseReference base;
    ArrayList<stok> stockP;
    ///
    FirebaseUser user;
    FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ed_producto);

        ///
        auth = FirebaseAuth.getInstance();
        user= auth.getCurrentUser();
        recyclerView = findViewById(R.id.Almacen);
        base= FirebaseDatabase.getInstance().getReference("/MI data base Usuarios/" + user.getUid()+"/stok");
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager( this, LinearLayoutManager.VERTICAL,false));
        stockP =new ArrayList<>();
        stokAdaptador=new adaptadorStok(this,stockP);
        recyclerView.setAdapter(stokAdaptador);
        base.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                stockP.clear();
                for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                    stok carrito2 = dataSnapshot.getValue(stok.class);
                    stockP.add(carrito2);
                }
                stokAdaptador.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}