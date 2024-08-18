package com.example.punto2.adaptadores;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.punto2.R;
import com.example.punto2.datos.isumos_datos;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class insumosadaptador extends RecyclerView.Adapter<insumosadaptador.Myholder>{
    Context context;
    ArrayList<isumos_datos> refere;
    public insumosadaptador(Context context, ArrayList<isumos_datos> refere) {
        this.context = context;
        this.refere = refere;
    }

    @NonNull
    @Override
    public insumosadaptador.Myholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.insumos_chequeo,parent,false);

        return new  Myholder(v);
    }

    @SuppressLint("RecyclerView")
    @Override
    public void onBindViewHolder(@NonNull insumosadaptador.Myholder holder, int position) {
        isumos_datos empleado = refere.get(position);
        int can = empleado.getCantidad();
        int kilos = empleado.getKilos();
        int precio = empleado.getPrecioCompra();
        holder.nombre.setText(empleado.getFecha());
        holder.precio1.setText(String.valueOf( can));
        holder.cantidad.setText(String.valueOf( kilos));
        holder.total.setText(String.valueOf( precio));
        holder.tipo.setText(empleado.getNombre());
        holder.tipor.setText(empleado.getTipo());
        holder.provedor.setText(empleado.getProveedor());

        FirebaseDatabase firebaseDatabase;
        DatabaseReference daabase;
        FirebaseUser user;
        FirebaseAuth auth;
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        daabase = FirebaseDatabase.getInstance().getReference("/MI data base Usuarios/"+ user.getUid()+ "/insumos_verificados");

        holder.ordenes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Crear un nuevo objeto con los datos del insumo
                Map<String, Object> verifiedInsumo = new HashMap<>();
                verifiedInsumo.put("fecha", empleado.getFecha());
                verifiedInsumo.put("nombre", empleado.getNombre());
                verifiedInsumo.put("cantidad", can);
                verifiedInsumo.put("kilos", kilos);
                verifiedInsumo.put("precioCompra", precio);
                verifiedInsumo.put("Tipo", empleado.getTipo());
                verifiedInsumo.put("hora", empleado.getHora());
                verifiedInsumo.put("mes", empleado.getMes());
                verifiedInsumo.put("proveedor", empleado.getProveedor());


                // Referencia a la nueva ruta para guardar los datos
                DatabaseReference daabaseVerificados = FirebaseDatabase.getInstance().getReference("/MI data base Usuarios/" + user.getUid() + "/insumos_verificados");

                // Guarda los datos en la nueva ruta
                daabaseVerificados.push().setValue(verifiedInsumo)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    // Datos guardados exitosamente, ahora eliminar de la ruta original

                                    DatabaseReference daabaseOriginal = FirebaseDatabase.getInstance().getReference("/MI data base Usuarios/" + user.getUid() + "/insumos");

                                    // Buscar y eliminar el insumo basado en la fecha de registro
                                    daabaseOriginal.orderByChild("hora").equalTo(empleado.getHora()).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                                dataSnapshot.getRef().removeValue();
                                            }
                                            Toast.makeText(context, " Correcto", Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Toast.makeText(context, "Error al eliminar el insumo original", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                } else {
                                    Toast.makeText(context, "Error al guardar los datos", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

            }
        });
        holder.cancelr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Referencia a la base de datos, apuntando al insumo específico
                DatabaseReference daabaseOriginal = FirebaseDatabase.getInstance().getReference("/MI data base Usuarios/" + user.getUid() + "/insumos");

                // Buscar el insumo en la base de datos usando la fecha de registro o cualquier otro identificador único
                daabaseOriginal.orderByChild("fechaRegistro").equalTo(empleado.getFecha()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            // Eliminar el insumo
                            dataSnapshot.getRef().removeValue();
                            Toast.makeText(context, "Insumo eliminado correctamente", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(context, "Error al eliminar el insumo", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }


    @Override
    public int getItemCount() {
        return refere.size();
    }

    public class Myholder extends RecyclerView.ViewHolder {
        TextView  nombre,provedor,tipo,tipor ;
        EditText  precio1,cantidad,total ;
        Button ordenes,cancelr ;
        public Myholder(@NonNull View itemView) {
            super(itemView);
            nombre = itemView.findViewById(R.id.nobre1);
            precio1 = itemView.findViewById(R.id.precio1);
            cantidad = itemView.findViewById(R.id.cantidad);
            total = itemView.findViewById(R.id.total);
            ordenes = itemView.findViewById(R.id.ordenes);
            cancelr = itemView.findViewById(R.id.cancelr);
            tipo = itemView.findViewById(R.id.nombre);
            provedor = itemView.findViewById(R.id.provedor);
            tipor = itemView.findViewById(R.id.tipo);

        }

    }}
