package com.example.punto2.adaptadores;


import static android.view.View.GONE;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.punto2.R;
import com.example.punto2.datos.stok;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class adaptadorStok  extends RecyclerView.Adapter<adaptadorStok.Myholder>{
    FirebaseUser user;
    FirebaseAuth auth;
    Context context;
    ArrayList<stok> stockP;
    public adaptadorStok(Context context, ArrayList<stok> stockP) {
        this.context = context;
        this.stockP = stockP;
    }

    @NonNull
    @Override
    public adaptadorStok.Myholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.edidempleado,parent,false);
        auth= FirebaseAuth.getInstance();
        user= auth.getCurrentUser();
        return new Myholder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull adaptadorStok.Myholder holder, int position) {
        stok almacen= stockP.get(position);

        holder.id.setText("id: "+almacen.getCodigo());
        holder.nombre.setText(almacen.getNombre());
        int can = almacen.getCantidad();
        int PrecioC = almacen.getPrecio_compra();
        int Preciov = almacen.getPrecio_venta();

        holder.cantidad.setText(String.valueOf("Cantidad: "+can));

        holder.precioC.setText(String.valueOf("precio De compra: "+PrecioC));
        if (PrecioC  == 0){
            holder.precioC.setVisibility(GONE);
            holder.cantidad.setVisibility(GONE);

        }
        holder.preciov.setText(String.valueOf("precio De Venta: "+Preciov)); // Asegúrate de que `precioV` es el TextView correcto

        Glide.with(context).load(almacen.getImage_url())
                .into(holder.foto);
        FirebaseDatabase database1= FirebaseDatabase.getInstance();

        DatabaseReference reference1 = database1.getReference("/MI data base Usuarios/" + user.getUid()+"/stok"); //nombre de data de base

        holder.eliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nam= holder.id.getText().toString();
                DatabaseReference  r2 =reference1.child(nam);
                r2.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context, "producto eliminado correctamente", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Error al eliminar producto", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        holder.actualizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Inflar el layout del diálogo
                LayoutInflater inflater = LayoutInflater.from(context);
                View dialogView = inflater.inflate(R.layout.edit_dialog, null);

                // Crear el diálogo
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setView(dialogView);

                // Obtener referencias a los EditText del diálogo
                TextView editTextNombre = dialogView.findViewById(R.id.textView9);
                TextView textView1 = dialogView.findViewById(R.id.textView1);
                TextView textView3 = dialogView.findViewById(R.id.textView3);

                EditText editTextPrecioCompra = dialogView.findViewById(R.id.editTextPrecioCompra);
                EditText editTextPrecioVenta = dialogView.findViewById(R.id.editTextPrecioVenta);
                EditText editTextCantidad = dialogView.findViewById(R.id.editTextCantidad);
                @SuppressLint({"MissingInflatedId", "LocalSuppress"}) Button Guardad = dialogView.findViewById(R.id.button7);
            // Poner los valores actuales en los EditText
                editTextNombre.setText(almacen.getNombre());
                if (PrecioC  == 0){
                    editTextPrecioCompra.setVisibility(GONE);
                    editTextCantidad.setVisibility(GONE);
                    textView3.setVisibility(GONE);
                    textView1.setVisibility(GONE);
                }
                editTextPrecioCompra.setText(String.valueOf(almacen.getPrecio_compra()));
                editTextPrecioVenta.setText(String.valueOf(almacen.getPrecio_venta()));
                editTextCantidad.setText(String.valueOf(almacen.getCantidad()));

                // Configurar el botón de guardar del diálogo
                // Configurar el botón de guardar del diálogo
                AlertDialog dialog = builder.create();

                Guardad.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Obtener los nuevos valores de los EditText
                        String nuevoNombre = editTextNombre.getText().toString();
                        double nuevoPrecioCompra = Double.parseDouble(editTextPrecioCompra.getText().toString());
                        double nuevoPrecioVenta = Double.parseDouble(editTextPrecioVenta.getText().toString());
                        int nuevaCantidad = Integer.parseInt(editTextCantidad.getText().toString());

                        // Obtener la referencia a la base de datos
                        String horaCambio = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());

                        // Leer los datos actuales antes de actualizar
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("/MI data base Usuarios/" + user.getUid() + "/stok/" + almacen.getCodigo());

                         ref.child("precio_compra").setValue(nuevoPrecioCompra);
                        ref.child("precio_venta").setValue(nuevoPrecioVenta);
                        ref.child("cantidad").setValue(nuevaCantidad);
                        // No actualizar image_url y codigo
                        ref.child("image_url").setValue(almacen.getImage_url());
                        ref.child("codigo").setValue(almacen.getCodigo());
                        ref.child("nombre").setValue(almacen.getNombre());

                        DatabaseReference cambiosRef = FirebaseDatabase.getInstance().getReference("/MI data base Usuarios/" + user.getUid() +"/cambioDeProductos").push();
                         int actualCantidad = almacen.getCantidad();
                        int actuaPrecio_venta = almacen.getPrecio_venta();
                        int actuaPrecio_compra = almacen.getPrecio_venta();

                        Map<String, Object> cambioData = new HashMap<>();
                        cambioData.put("codigo", almacen.getCodigo());
                        cambioData.put("nombre", nuevoNombre);
                        cambioData.put("Cambio", nuevoPrecioCompra);
                        StringBuilder cambios = new StringBuilder();
                        if (nuevaCantidad != actualCantidad) {
                            cambios.append("Cantidad,");
                        }
                        if (nuevoPrecioVenta != actuaPrecio_venta) {
                            cambios.append("PrecioV,");
                        }
                        if (nuevoPrecioCompra != actuaPrecio_compra) {
                            cambios.append("PrecioC,");
                        }
                        if (cambios.length() > 0) {
                            // Elimina la última coma
                            cambios.setLength(cambios.length() - 1);
                            cambioData.put("Cambio", cambios.toString());
                        }
                        cambioData.put("precio_compra", nuevoPrecioCompra);
                        cambioData.put("precio_venta", nuevoPrecioVenta);
                        cambioData.put("cantidad", nuevaCantidad);
                        cambioData.put("hora", horaCambio);
                        cambioData.put("AnterCantidad", actualCantidad);
                        cambioData.put("AnterPrecio_venta", actuaPrecio_venta);
                        cambioData.put("AnterPrecio_compra", actuaPrecio_compra);

                        cambioData.put("ResultCantidad", nuevaCantidad - actualCantidad);
                        cambioData.put("ResultPrecio_venta", nuevoPrecioVenta - actuaPrecio_venta);
                        cambioData.put("ResultPrecio_compra", nuevoPrecioCompra - actuaPrecio_compra);
                        cambiosRef.setValue(cambioData);
                        // Notificar al adaptador que los datos han cambiado
                        notifyDataSetChanged();
                        // Cerrar el diálogo
                        dialog.dismiss();

                       }
                });

                 dialog.show();

            }
        });
    }


    @Override
    public int getItemCount() {
        return stockP.size();
    }

    public class Myholder extends RecyclerView.ViewHolder {
        TextView id,nombre,precioC,preciov,cantidad;
        ImageView foto;
        Button eliminar, actualizar;
        public Myholder(@NonNull View itemView) {
            super(itemView);
            id = itemView.findViewById(R.id.nobre2);
            nombre = itemView.findViewById(R.id.precio2);
            precioC = itemView.findViewById(R.id.Pcompra);
            preciov = itemView.findViewById(R.id.Pventa);
            cantidad = itemView.findViewById(R.id.Proceso);
            foto=itemView.findViewById(R.id.imagen1);
            eliminar=itemView.findViewById(R.id.eliminar);
            actualizar=itemView.findViewById(R.id.editar);


        }
    }
}
