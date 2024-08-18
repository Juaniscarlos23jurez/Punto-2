package com.example.punto2.adaptadores;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.punto2.R;
import com.example.punto2.datos.isumos_datos;

import java.util.ArrayList;

public class historial_insumo extends RecyclerView.Adapter<historial_insumo.Myholder>{
    Context context;
    ArrayList<isumos_datos> refere;
    public historial_insumo(Context context, ArrayList<isumos_datos> refere) {
        this.context = context;
        this.refere = refere;
    }

    @NonNull
    @Override
    public historial_insumo.Myholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.insumo_hist,parent,false);
        return new historial_insumo.Myholder(v);
    }

    @SuppressLint("RecyclerView")
    @Override
    public void onBindViewHolder(@NonNull historial_insumo.Myholder holder, int position) {
        isumos_datos empleado = refere.get(position);
        int can = empleado.getCantidad();
        int kilos = empleado.getKilos();
        int precio = empleado.getPrecioCompra();
        holder.nombre.setText(empleado.getFecha());
        holder.precio1.setText(String.valueOf("Cantidad: "+can));
        holder.cantidad.setText(String.valueOf("kilos: "+kilos));
        holder.total.setText(String.valueOf("precio: "+precio));
        holder.tipo.setText(empleado.getNombre());
        holder.provedor.setText(empleado.getProveedor());
        holder.tipor.setText(empleado.getTipo());

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
            tipor= itemView.findViewById(R.id.tipo);
        }

    }}
