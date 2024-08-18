package com.example.punto2.adaptadores;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
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
import com.example.punto2.datos.provedor;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class adaptadorCliente extends RecyclerView.Adapter<adaptadorCliente.Myholder>{
    FirebaseUser user;
    FirebaseAuth auth;
    Context context;
    ArrayList<provedor> provedor1;
    public adaptadorCliente(Context context, ArrayList<provedor> provedor1) {
        this.context = context;
        this.provedor1 = provedor1;
    }

    @NonNull
    @Override
    public adaptadorCliente.Myholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.e_d_provedor,parent,false);
        auth= FirebaseAuth.getInstance();
        user= auth.getCurrentUser();
        return new adaptadorCliente.Myholder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull adaptadorCliente.Myholder holder, int position) {
        provedor almacen= provedor1.get(position);
        holder.nombre.setText(almacen.getNombre());
        holder.Telefono.setText(almacen.getTelefono());
        holder.empresa.setText(almacen.getEmpresa());
        FirebaseDatabase database1= FirebaseDatabase.getInstance();
        DatabaseReference reference1 = database1.getReference("/MI data base Usuarios/" + user.getUid()+"/Cliente"); //nombre de data de base
        holder.eliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nombreProveedor = holder.nombre.getText().toString().trim();
                buscarYEliminarProveedor(nombreProveedor);
            }
        });

// Método para buscar y eliminar el proveedor por nombre

        holder.actualizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Inflar el layout del diálogo
                LayoutInflater inflater = LayoutInflater.from(context);
                View dialogView = inflater.inflate(R.layout.edid_prove, null);

                // Crear el diálogo
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setView(dialogView);

                // Obtener referencias a los EditText del diálogo
                EditText editTextnombre = dialogView.findViewById(R.id.editTextPrecioCompra);
                EditText editTextenoresa = dialogView.findViewById(R.id.editTextCantidad);
                EditText editTexttelefono = dialogView.findViewById(R.id.editTextPrecioVenta);
                @SuppressLint({"MissingInflatedId", "LocalSuppress"}) Button Guardad = dialogView.findViewById(R.id.button7);
                // Poner los valores actuales en los EditText getNombre
                editTexttelefono.setText(String.valueOf(almacen.getTelefono()));
                editTextenoresa.setText(String.valueOf(almacen.getEmpresa()));
                editTextnombre.setText(String.valueOf(almacen.getNombre()));
                // Configurar el botón de guardar del diálogo
                // Configurar el botón de guardar del diálogo
                AlertDialog dialog = builder.create();

                Guardad.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Obtener los nuevos valores de los EditText
                        String Nombre = editTextnombre.getText().toString();
                        String empresa = editTextenoresa.getText().toString();
                        int telefono = Integer.parseInt(editTexttelefono.getText().toString());
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("/MI data base Usuarios/" + user.getUid()+"/Provedor");


                    }
                });

                dialog.show();

            }
        });

    }
    private void buscarYEliminarProveedor(String nombreProveedor) {
        DatabaseReference proveedoresRef = FirebaseDatabase.getInstance().getReference("/MI data base Usuarios/" + user.getUid() + "/Provedor");

        proveedoresRef.orderByChild("Nombre").equalTo(nombreProveedor).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean eliminado = false;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    snapshot.getRef().removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(context, "Proveedor eliminado correctamente", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, "Error al eliminar proveedor", Toast.LENGTH_SHORT).show();
                        }
                    });
                    eliminado = true;
                }
                if (!eliminado) {
                    Toast.makeText(context, "Proveedor no encontrado", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(context, "Error en la consulta: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return provedor1.size();
    }

    public class Myholder extends RecyclerView.ViewHolder {
        TextView nombre,Telefono,empresa;
        Button eliminar, actualizar;
        public Myholder(@NonNull View itemView) {
            super(itemView);
            nombre = itemView.findViewById(R.id.precio2);
            Telefono = itemView.findViewById(R.id.Pventa);
            empresa = itemView.findViewById(R.id.Proceso);
            eliminar=itemView.findViewById(R.id.eliminar);
            actualizar=itemView.findViewById(R.id.editar);
            actualizar.setVisibility(View.GONE);

        }
    }
}
