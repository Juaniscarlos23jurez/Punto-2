package com.example.punto2.adaptadores;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class data1Adaptador extends RecyclerView.Adapter<data1Adaptador.Myholder>{
    FirebaseUser user;
    FirebaseAuth auth;
    Context context;
    private DatabaseReference databaseReference;

    ArrayList<stok> arrayList;
    private View.OnClickListener carritp;

    public data1Adaptador(Context context, ArrayList<stok> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }



    @NonNull
    @Override
    public data1Adaptador.Myholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.diseno1,parent,false);
        auth= FirebaseAuth.getInstance();
        user= auth.getCurrentUser();
        this.databaseReference = FirebaseDatabase.getInstance().getReference("/MI data base Usuarios/" + user.getUid() + "/ventas");

        return new data1Adaptador.Myholder(v);
    }
    @Override
    public void onBindViewHolder(@NonNull data1Adaptador.Myholder holder, int position) {
        stok stok=arrayList.get(position);
         holder.nobre.setText(stok.getNombre());
        holder.precio.setText( String.valueOf("$"+stok.getPrecio_venta()));
        Glide.with(context).load(stok.getImage_url())
                .into(holder.imagen);
        holder.cantidad.addTextChangedListener(new TextWatcher() {
            private boolean isUpdating = false; // Flag to avoid recursion

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isUpdating) return; // Avoid recursion
                try {
                    double cantidad = Double.parseDouble(s.toString());
                    double precioVenta = stok.getPrecio_venta();
                    double dinero = cantidad * precioVenta;
                    isUpdating = true; // Set flag to true before updating
                    holder.dinero.setText(String.format("%.2f", dinero)); // Format to 2 decimal places
                    isUpdating = false; // Reset flag after updating
                } catch (NumberFormatException e) {
                    holder.dinero.setText("0.00");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        holder.mas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveDataToVentaVerificada(stok, Integer.parseInt(holder.cantidad.getText().toString()));
            }
        });


    }
    @Override
    public int getItemCount() {
        return arrayList.size();
    }
    public class Myholder extends RecyclerView.ViewHolder {
        TextView nobre,precio;
        ImageView imagen;
        EditText  cantidad,dinero;
        Button mas;
        public Myholder(@NonNull View itemView) {
            super(itemView);
            imagen=itemView.findViewById(R.id.imagen);
            nobre=itemView.findViewById(R.id.nobre);
            precio=itemView.findViewById(R.id.precio);
            cantidad=itemView.findViewById(R.id.cantidad);
            dinero=itemView.findViewById(R.id.dinero);
            mas=itemView.findViewById(R.id.mas);

        }
    }
    private void moveDataToVentaVerificada(stok stok, int nuevaCantidad) {
        String key = databaseReference.push().getKey();
        if (key == null) return;

        Map<String, Object> ventaData = new HashMap<>();
        ventaData.put("codigo", stok.getCodigo());
        ventaData.put("fecha", stok.getHora());
        ventaData.put("image_url", stok.getImage_url());
        ventaData.put("nombre", stok.getNombre());
        ventaData.put("precio_compra", stok.getPrecio_compra());
        ventaData.put("precio_venta", stok.getPrecio_venta());
        ventaData.put("cantidad", nuevaCantidad);

        databaseReference.child(key).setValue(ventaData)
                .addOnSuccessListener(aVoid -> {
                    // Actualización exitosa
                    Toast.makeText(context, "Datos movidos con éxito", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Error en la actualización
                    Toast.makeText(context, "Error al mover los datos", Toast.LENGTH_SHORT).show();
                });
    }
}
