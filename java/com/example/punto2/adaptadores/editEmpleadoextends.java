package com.example.punto2.adaptadores;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.punto2.R;
import com.example.punto2.datos.empleadojava;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class editEmpleadoextends extends RecyclerView.Adapter<editEmpleadoextends.Myholder>{
    FirebaseUser user;
    FirebaseAuth auth;
    Context context;
    String[] items = {
            "Unico", "Diario", "Semana", "Mensual", "Trimestral", "Semestral", "Anual"
    };
    ArrayAdapter<String> adapterItems;

    ArrayList<empleadojava> stockP;
    AutoCompleteTextView autoCompleteTextView;


    public editEmpleadoextends(Context context, ArrayList<empleadojava> stockP) {
        this.context = context;
        this.stockP = stockP;
    }

    @NonNull
    @Override
    public editEmpleadoextends.Myholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.empleadoedicion,parent,false);
        auth= FirebaseAuth.getInstance();
        user= auth.getCurrentUser();
        return new editEmpleadoextends.Myholder(v);
    }

    @SuppressLint("RecyclerView")
    @Override
    public void onBindViewHolder(@NonNull editEmpleadoextends.Myholder holder, int position) {
        empleadojava empleado = stockP.get(position);

        holder.nombre.setText(empleado.getNombre());
        holder.HORARIO.setText("HORARIO: " + empleado.getHoraEntrada() + " - " + empleado.getHoraSalida());

        holder.sueldo.setText("Sueldo: $" + empleado.getSueldo());
        holder.Timpo.setText(empleado.getTiempo());
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("/MI data base Usuarios/" + user.getUid() + "/Empleados");
        holder.actualizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Inflar el layout del diálogo
                LayoutInflater inflater = LayoutInflater.from(context);
                View dialogView = inflater.inflate(R.layout.prueeba, null);

                // Crear el diálogo
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setView(dialogView);

                // Obtener referencias a los EditText del diálogo
                TextView editTextNombre = dialogView.findViewById(R.id.textView9);
                EditText editTextPrecioCompra = dialogView.findViewById(R.id.editTextPrecioCompra);
                Button Guardad = dialogView.findViewById(R.id.button7);

                editTextNombre.setText(empleado.getNombre());
                editTextPrecioCompra.setText(String.valueOf(empleado.getSueldo()));

                AlertDialog dialog = builder.create();

                Guardad.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Obtener los nuevos valores de los EditText
                        String nuevoNombre = editTextNombre.getText().toString();
                        double nuevoSueldo = Double.parseDouble(editTextPrecioCompra.getText().toString());

                        // Consultar Firebase para obtener la referencia del empleado por nombre
                        reference.orderByChild("nombre").equalTo(empleado.getNombre()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    for (DataSnapshot empleadoSnapshot : dataSnapshot.getChildren()) {
                                        // Obtener la referencia del empleado
                                        DatabaseReference empleadoRef = empleadoSnapshot.getRef();

                                        // Obtener valores anteriores
                                        String nombreAnterior = empleado.getNombre();
                                        double sueldoAnterior = empleado.getSueldo();

                                        // Actualizar los datos del empleado
                                        empleadoRef.child("nombre").setValue(nuevoNombre);
                                        empleadoRef.child("sueldo").setValue(nuevoSueldo);

                                        // Guardar el historial de cambios
                                        DatabaseReference cambiosRef = FirebaseDatabase.getInstance().getReference("/MI data base Usuarios/" + user.getUid() + "/HistorialCambios").push();

                                        Map<String, Object> cambioData = new HashMap<>();
                                        cambioData.put("nombreAnterior", nombreAnterior);
                                        cambioData.put("sueldoAnterior", sueldoAnterior);
                                         cambioData.put("sueldoNuevo", nuevoSueldo);
                                        String horaCambio = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
                                        cambioData.put("horaCambio", horaCambio);

                                        cambiosRef.setValue(cambioData);

                                        // Notificar al adaptador que los datos han cambiado
                                        Toast.makeText(context, "Cambiado correctamente", Toast.LENGTH_SHORT).show();

                                        notifyDataSetChanged();

                                        // Cerrar el diálogo
                                        dialog.dismiss();
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                // Manejar errores de la consulta a Firebase
                                Log.e("Firebase", "Error al buscar empleado por nombre: ", databaseError.toException());
                            }
                        });
                    }
                });

                dialog.show();
            }
        });
        holder.eliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nombreProveedor = holder.nombre.getText().toString().trim();
                buscarYEliminarProveedor(nombreProveedor);
            }
        });
    }

    private void buscarYEliminarProveedor(String nombreProveedor) {
        DatabaseReference proveedoresRef = FirebaseDatabase.getInstance().getReference("/MI data base Usuarios/" + user.getUid() + "/Empleados");

        proveedoresRef.orderByChild("nombre").equalTo(nombreProveedor).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean eliminado = false;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    snapshot.getRef().removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(context, "  eliminado correctamente", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, "Error al eliminar  ", Toast.LENGTH_SHORT).show();
                        }
                    });
                    eliminado = true;
                }
                if (!eliminado) {
                    Toast.makeText(context, "No encontrado", Toast.LENGTH_SHORT).show();
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
        return stockP.size();
    }

    public class Myholder extends RecyclerView.ViewHolder {
        TextView HORARIO,nombre,sueldo,Corre,Contraseña,Timpo;
        ImageView foto;
        Button eliminar, actualizar,copiar;
        public Myholder(@NonNull View itemView) {
            super(itemView);
            HORARIO = itemView.findViewById(R.id.nobre2);
            HORARIO.setTextColor(Color.parseColor("#000000"));

             nombre = itemView.findViewById(R.id.precio2);

            sueldo = itemView.findViewById(R.id.Proceso);
            sueldo.setTextColor(Color.parseColor("#000000"));
            configureTextView(HORARIO);
            configureTextView(nombre);

            configureTextView(sueldo);


    }
        // Método para configurar TextView
        private void configureTextView(TextView textView) {
            textView.setTextColor(Color.parseColor("#000000"));
            textView.setTypeface(Typeface.create("sans-serif-black", Typeface.NORMAL));
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15); // 15 dp
             eliminar=itemView.findViewById(R.id.eliminar);
            actualizar=itemView.findViewById(R.id.editar);
            Timpo=itemView.findViewById(R.id.Timpo);

        }
    }}
