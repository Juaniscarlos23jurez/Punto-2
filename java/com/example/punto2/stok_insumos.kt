package com.example.punto2

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.punto2.adaptadores.adaptadorStok
import com.example.punto2.datos.stok
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class stok_insumos : AppCompatActivity() {
    var recyclerView: RecyclerView? = null
    var stokAdaptador: adaptadorStok? = null
    var base: DatabaseReference? = null
    var stockP: ArrayList<stok>? = null

    ///
    var user: FirebaseUser? = null
    var auth: FirebaseAuth? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stok_insumos)
        // Inicializar FirebaseAuth
        auth = FirebaseAuth.getInstance()
        user = auth!!.currentUser!!

        // Inicializar RecyclerView
         val recyclerView : RecyclerView = findViewById(R.id.Almacen)

        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        // Inicializar base de datos
        base = FirebaseDatabase.getInstance().getReference("/MI data base Usuarios/${user!!.uid}/stok")

        // Inicializar ArrayList y Adaptador
        stockP = ArrayList()
        stokAdaptador = adaptadorStok(this, stockP)
        recyclerView.adapter = stokAdaptador

        // Agregar ValueEventListener a la base de datos
        base!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                stockP!!.clear()
                for (dataSnapshot in snapshot.children) {
                    val carrito2 = dataSnapshot.getValue(stok::class.java)
                    carrito2?.let { stockP!!.add(it) }
                }
                stokAdaptador!!.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejar el error si es necesario
            }
        })
    }
}