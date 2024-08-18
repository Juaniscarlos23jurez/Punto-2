package com.example.punto2.adaptadores

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.punto2.R
import com.example.punto2.datos.Diario
import com.example.punto2.datos.producto

class AdapDiario (private val pedido: ArrayList<Diario>, private val context: Context) : RecyclerView.Adapter<AdapDiario.Myholder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Myholder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.listadeldia,parent,false)
        return Myholder(itemView)
    }
    override fun onBindViewHolder(holder: Myholder, position: Int) {
        val producto = pedido[position]
        holder.nombre.text = producto.nombre
        //val enlace: String? = formulariof1.enlace1
        val precio = producto.precio_venta ?: 0 // Asigna 0 si el precio_venta es nulo
        val cantidad = producto.cantidad ?: 0 // Asigna 0 si la cantidad es nula
        val total = precio * cantidad // Calcula el total

        //   texto
        holder.fecha.text=producto.fecha
        holder.precio.text  = "$"+precio.toString()
        holder.cantidad.text = "Cantidad :"+cantidad.toString()
        holder.total.text  ="Total :"+ total.toString()

    }
    override fun getItemCount(): Int {
        return  pedido.size
    }
    class Myholder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val nombre: TextView =itemView.findViewById(R.id.nobre1)
        val precio: TextView =itemView.findViewById(R.id.precio1)
        val cantidad: TextView =itemView.findViewById(R.id.cantidad)
        val total: TextView =itemView.findViewById(R.id.total)
        val fecha: TextView =itemView.findViewById(R.id.fecha)




    }
}