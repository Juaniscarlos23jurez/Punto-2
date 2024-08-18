package com.example.punto2;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.punto2.adaptadores.AdapDiario;
import com.example.punto2.adaptadores.data1Adaptador;
import com.example.punto2.adaptadores.insumosadaptador;
import com.example.punto2.datos.Diario;
import com.example.punto2.datos.isumos_datos;
import com.example.punto2.datos.stok;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.inappmessaging.FirebaseInAppMessaging;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;
    private AdView adView;
    RecyclerView mr1;
    data1Adaptador add1888;
    DatabaseReference databas1;
    ArrayList<stok> arrayList;
    FirebaseUser user;
    FirebaseAuth auth;
    private RecyclerView registro;
    private AdapDiario add1;
    private ArrayList<Diario> list1;
    private DatabaseReference myregistro;
    private Dialog loadingDialog;
    Button button, orden,button8,pfg;

    FloatingActionButton floatingactionbutton,alerta;

     private static final int CREATE_FILE_REQUEST_CODE = 101;
    RecyclerView aler;
    insumosadaptador aler12;
    DatabaseReference daabase;
    ArrayList<isumos_datos> arrayaler;

    @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
         aler = findViewById(R.id.Revision);
        daabase = FirebaseDatabase.getInstance().getReference("/MI data base Usuarios/"+ user.getUid()+ "/insumos");
        aler.setHasFixedSize(false);
        //cobiar el layaout por el gridlayaout y colocarle las 2 columnas
        aler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        arrayaler = new ArrayList<>();
        aler12 = new insumosadaptador(this, arrayaler);
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
        FirebaseApp.initializeApp(this);
        FirebaseInAppMessaging.getInstance().setAutomaticDataCollectionEnabled(true);
        mr1 = findViewById(R.id.productos);
        databas1 = FirebaseDatabase.getInstance().getReference("/MI data base Usuarios/"+ user.getUid() +"/stok");
            mr1.setHasFixedSize(false);
            //cobiar el layaout por el gridlayaout y colocarle las 2 columnas
            mr1.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            arrayList = new ArrayList<>();
            add1888 = new data1Adaptador(this, arrayList);
            mr1.setAdapter(add1888);
            databas1.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    arrayList.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        stok datos2 = dataSnapshot.getValue(stok.class);
                        arrayList.add(datos2);
                    }
                    add1888.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
            MobileAds.initialize(this, initializationStatus -> {});
            AdView adView = findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);
            // Initialize the Google Mobile Ads SDK
            adView.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    // Ad successfully loaded
                }

                @Override
                public void onAdFailedToLoad(LoadAdError adError) {
                    // Ad failed to load
                    Log.d("AdMob", "Ad failed to load: " + adError.getMessage());
                }
            });
            registro = findViewById(R.id.Registrodiario);
            registro.setHasFixedSize(true);
            registro.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
            list1 = new ArrayList<>();
            add1 = new AdapDiario(list1, this); // Pass the context of the main activity
            registro.setAdapter(add1);
            myregistro = FirebaseDatabase.getInstance().getReference("/MI data base Usuarios/" + user.getUid() + "/ventas"); // Initialize with your reference
            myregistro.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    list1.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Diario pedido = snapshot.getValue(Diario.class);
                        if (pedido != null) {
                            list1.add(pedido);
                        }
                    }
                    add1.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle possible errors
                }
            });
              button = findViewById(R.id.button6);  // Asegúrate de que el id es correcto y existe en el layout activity_main
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Mostrar una alerta para confirmar el cierre de caja
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Confirmación")
                            .setMessage("¿Estás seguro de que quieres cerrar la caja?")
                            .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // Si el usuario confirma, calcula el total y muestra el total en otra alerta
                                    // calcularTotalVentasYMostrarAlerta();
                                    cierreCaja();
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // Si el usuario cancela, simplemente cierra el diálogo

                                    dialog.dismiss();
                                }
                            })
                            .show();
                }
            });
            FirebaseAuth auth = FirebaseAuth.getInstance();
            FirebaseUser user = auth.getCurrentUser();
             FirebaseDatabase databasesuma = FirebaseDatabase.getInstance();
            DatabaseReference dinerotiempo = databasesuma.getReference("/MI data base Usuarios/" + user.getUid() + "/dinero");
            TextView textView8 = findViewById(R.id.textView8);  // Asegúrate de que el id es correcto y existe en el layout activity_main
            // Añadir el ValueEventListener para escuchar cambios en tiempo real
            dinerotiempo.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Double currentMoney = dataSnapshot.getValue(Double.class);
                    if (currentMoney == null) {
                        currentMoney = 0.0;
                    }

                     textView8.setText("Efectivo en caja: $" + currentMoney.toString());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Manejar el error al obtener el valor inicial
                    Toast.makeText(MainActivity.this, "Error al obtener el efectivo actual", Toast.LENGTH_SHORT).show();
                }
            });
            //   initLoadingScreen();

            orden=findViewById(R.id.ordenes);
            orden.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(MainActivity.this, MainActivityreportes.class));
                }
            });
            // Verifica y solicita permisos de cámara si es necesario
            floatingactionbutton=findViewById(R.id.floatingActionButton2);
            floatingactionbutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    startActivity(new Intent(MainActivity.this, MainActivity4.class));
                }
            });
            button8= findViewById(R.id.button8);
            button8.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Efectivo();
                }
            });
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                checkAndRequestPermissions();
            } else {
                checkAndRequestPermissionsLegacy();
            }
    }
    private void checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this, new String[]{
                        android.Manifest.permission.READ_MEDIA_IMAGES,
                        android.Manifest.permission.READ_MEDIA_VIDEO,
                        android.Manifest.permission.READ_MEDIA_AUDIO
                }, PERMISSION_REQUEST_CODE);
            } else {
                // Permisos ya otorgados
                Toast.makeText(this, "Permisos ya otorgados", Toast.LENGTH_SHORT).show();
            }
        } else {
            checkAndRequestPermissionsLegacy();
        }
    }
    private void checkAndRequestPermissionsLegacy() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
            }, PERMISSION_REQUEST_CODE);
        } else {
            // Permisos ya otorgados
            Toast.makeText(this, "Permisos ya otorgados", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allPermissionsGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }

            if (allPermissionsGranted) {
                // Permisos otorgados
                Toast.makeText(this, "Permisos otorgados", Toast.LENGTH_SHORT).show();
            } else {
                // Permisos denegados
                Toast.makeText(this, "Permisos denegados", Toast.LENGTH_SHORT).show();
            }
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)


    private void cierreCaja() {


        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        String userId = (user != null) ? user.getUid() : null;

        DatabaseReference ventasRef = FirebaseDatabase.getInstance().getReference("/MI data base Usuarios/" + userId + "/ventas");
        DatabaseReference ventaVerificada = FirebaseDatabase.getInstance().getReference("/MI data base Usuarios/" + userId + "/ventaVerificada");

        ventasRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Map<String, Object> ventasMap = new HashMap<>();
                    for (DataSnapshot productSnapshot : dataSnapshot.getChildren()) {
                        ventasMap.put(productSnapshot.getKey(), productSnapshot.getValue());
                    }
                    ventaVerificada.updateChildren(ventasMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                // Borra los datos de "preventa" después de haberlos copiado a "ventas"
                                ventasRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> removeTask) {
                                        if (removeTask.isSuccessful()) {
                                            // Opcional: notificar al usuario que la operación se completó
                                            Toast.makeText(MainActivity.this, "Datos movidos a ventas", Toast.LENGTH_SHORT).show();

                                        } else {
                                            // Manejar el error de borrado
                                            Toast.makeText(MainActivity.this, "Error al borrar datos de preventa", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            } else {
                                // Manejar el error de actualización
                                Toast.makeText(MainActivity.this, "Error al actualizar ventas", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Manejar el error en caso de que la lectura de datos falle
                Toast.makeText(MainActivity.this, "Error al leer datos de ventas", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void Efectivo() {
        FirebaseDatabase databasesuma = FirebaseDatabase.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = (user != null) ? user.getUid() : null;
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_layout, null);
        EditText editTextAddMoney = dialogView.findViewById(R.id.editTextAddMoney);
        DatabaseReference dineroRef = databasesuma.getReference("/MI data base Usuarios/" + userId + "/dinero");
        DatabaseReference transaccionesRef = databasesuma.getReference("/MI data base Usuarios/" + userId + "/transacciones");

        dineroRef.get().addOnSuccessListener(dataSnapshot -> {
            double currentMoney = (dataSnapshot.getValue(Double.class) != null) ? dataSnapshot.getValue(Double.class) : 0.0;

            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Total de Efectivo")
                    .setMessage("El total de efectivo es: " + currentMoney)
                    .setView(dialogView)
                    .setPositiveButton("Agregar dinero", (dialog, which) -> {
                        double addMoney = parseDouble(editTextAddMoney.getText().toString());
                        double finalTotal = currentMoney + addMoney;

                        dineroRef.setValue(finalTotal).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                                String fecha = dateFormatter.format(new Date()); // Fecha en formato dd-MM-yyyy

                                Map<String, Object> transaccion = new HashMap<>();
                                transaccion.put("anterior", currentMoney);
                                transaccion.put("finalTotal", finalTotal);
                                transaccion.put("cantidad", addMoney);
                                transaccion.put("tipo", "Se Agreego Dinero");
                                transaccion.put("fecha", fecha); // Fecha formateada

                                transaccionesRef.push().setValue(transaccion).addOnCompleteListener(transaccionTask -> {
                                    if (transaccionTask.isSuccessful()) {
                                        new AlertDialog.Builder(MainActivity.this)
                                                .setTitle("Efectivo Final")
                                                .setMessage("El efectivo final es: " + finalTotal)
                                                .setPositiveButton("Aceptar", (dialog1, which1) -> dialog1.dismiss())
                                                .show();
                                    } else {
                                        // Manejar el error
                                        Toast.makeText(MainActivity.this, "Error al registrar la transacción", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                // Manejar el error
                                Toast.makeText(MainActivity.this, "Error al actualizar el efectivo", Toast.LENGTH_SHORT).show();
                            }
                        });
                    })
                    .setNegativeButton("Sacar dinero", (dialog, which) -> {
                        double addMoney = parseDouble(editTextAddMoney.getText().toString());
                        double finalTotal = currentMoney - addMoney;

                        dineroRef.setValue(finalTotal).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                                String fecha = dateFormatter.format(new Date()); // Fecha en formato dd-MM-yyyy

                                Map<String, Object> transaccion = new HashMap<>();
                                transaccion.put("anterior", currentMoney);
                                transaccion.put("finalTotal", finalTotal);
                                transaccion.put("cantidad", addMoney);
                                transaccion.put("tipo", "Se Quito Dinero");
                                transaccion.put("fecha", fecha); // Fecha formateada

                                transaccionesRef.push().setValue(transaccion).addOnCompleteListener(transaccionTask -> {
                                    if (transaccionTask.isSuccessful()) {
                                        new AlertDialog.Builder(MainActivity.this)
                                                .setTitle("Efectivo Final")
                                                .setMessage("El efectivo final es: " + finalTotal)
                                                .setPositiveButton("Aceptar", (dialog1, which1) -> dialog1.dismiss())
                                                .show();
                                    } else {
                                        // Manejar el error
                                        Toast.makeText(MainActivity.this, "Error al registrar la transacción", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                // Manejar el error
                                Toast.makeText(MainActivity.this, "Error al actualizar el efectivo", Toast.LENGTH_SHORT).show();
                            }
                        });
                    })
                    .show();
        }).addOnFailureListener(e -> {
            // Manejar el error de obtener el valor
            Toast.makeText(MainActivity.this, "Error al obtener el efectivo actual", Toast.LENGTH_SHORT).show();
        });
    }
    private double parseDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = null;
        FirebaseAuth auth = null;
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
         FirebaseDatabase Tipo = FirebaseDatabase.getInstance();

        DatabaseReference databaseReference = Tipo.getReference("/MI data base Usuarios/" + user.getUid() + "/selectedOptionGroup2");
        DatabaseReference Empleado =  FirebaseDatabase.getInstance().getReference("/MI data base Usuarios/" + user.getUid() + "/Empleados");
        //DatabaseReference Jefe = Tipo.getReference("/MI data base Usuarios/" + user.getUid()+"/isEmployee"  );

        String userId = user.getUid();
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String selectedOption = dataSnapshot.getValue(String.class);
                if ("individual".equals(selectedOption)) {
                } else if ("negocio".equals(selectedOption)) {
                    handleNegocio(userId);

                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Maneja el error
            }
        });

        // logueoUsuarios();

    }
    private void handleNegocio(String userId) {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w("FCM", "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        String token = task.getResult();
                        Log.d("FCM", "FCM Registration Token: " + token);

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("MI data base Usuarios/" + userId + "/not");

                        checkAndHandleToken(reference, token);
                    }
                });
    }
    private void checkAndHandleToken(DatabaseReference reference, String token) {
        reference.orderByChild("key").equalTo(token).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        String tipo = child.child("tipo").getValue(String.class);
                        if ("jefe".equals(tipo)) {
                            //mr1.setVisibility(View.GONE);
                             Log.d("Firebase", "El token pertenece al jefe.");

                         } else if ("empleado".equals(tipo)) {
                            Log.d("Firebase", "El token pertenece a un empleado.");
                            // Oculta botones y muestra el AlertDialog para elegir empleado
                            orden.setVisibility(View.GONE);
                            button.setVisibility(View.GONE);
                            button8.setVisibility(View.GONE);
                             String nombreEmpleado = child.child("nombreEmpleado").getValue(String.class);
                            String horaEntrada = child.child("horaEntrada").getValue(String.class);

                            if (nombreEmpleado != null  ) {
                                Log.d("Firebase", "Empleado ya tiene nombre y horas.");
                                // Puedes realizar cualquier otra acción si es necesario
                            } else {
                                obtenerListaEmpleados(token);
                            }

                        }
                    }
                } else {
                    Log.d("Firebase", "Token no registrado.");
                    // Lógica para registrar un nuevo token si es necesario
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("Firebase", "Error al acceder a la base de datos.", error.toException());
            }
        });

    }
    private void obtenerListaEmpleados(String token) {
        DatabaseReference empleadosRef = FirebaseDatabase.getInstance().getReference("/MI data base Usuarios/"+ user.getUid() +"/Empleados");
        empleadosRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<String> empleadojava = new ArrayList<>();
                List<String> entrda = new ArrayList<>();
                List<String> salida = new ArrayList<>();

                for (DataSnapshot empleadoSnapshot : dataSnapshot.getChildren()) {
                    String nombreEmpleado = empleadoSnapshot.child("nombre").getValue(String.class);
                    String horaEntrada = empleadoSnapshot.child("horaEntrada").getValue(String.class);
                    String horaSalida = empleadoSnapshot.child("horaSalida").getValue(String.class);

                    empleadojava.add(nombreEmpleado);
                    entrda.add(horaEntrada);
                    salida.add(horaSalida);

                }
                mostrarDialogoEmpleados(empleadojava,entrda,salida);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Maneja el error
            }
        });
    }
    private void mostrarDialogoEmpleados(List<String> empleadojava,List<String> entrda,List<String> salida) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Selecciona un empleado");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, empleadojava);
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String empleadoSeleccionado = empleadojava.get(which);
                String entrdaSeleccionado = entrda.get(which);
                String salidaSeleccionado = salida.get(which);

                actualizarNodoConNombreEmpleado(empleadoSeleccionado,entrdaSeleccionado,salidaSeleccionado );

            }
        });

        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }
    private void actualizarNodoConNombreEmpleado(String nombreEmpleado,String entrdaSeleccionado,String salidaSeleccionado) {
        if (user != null) {
            String userId = user.getUid();

            // Obtener el token actual
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                @Override
                public void onComplete(@NonNull Task<String> task) {
                    if (!task.isSuccessful()) {
                        Log.w("FCM", "Fetching FCM registration token failed", task.getException());
                        return;
                    }

                    String token = task.getResult();
                    Log.d("FCM", "FCM Registration Token: " + token);

                    DatabaseReference notRef = FirebaseDatabase.getInstance().getReference("MI data base Usuarios/" + userId + "/not");

                    notRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            boolean tokenFound = false;

                            // Recorrer todas las entradas bajo /not
                            for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                                String storedToken = childSnapshot.child("key").getValue(String.class);

                                // Verificar si el token coincide
                                if (token.equals(storedToken)) {
                                    tokenFound = true;

                                    // Crear mapa de actualizaciones
                                    Map<String, Object> updates = new HashMap<>();

                                    // Agregar datos existentes al mapa de actualizaciones
                                    for (DataSnapshot dataSnapshot : childSnapshot.getChildren()) {
                                        updates.put(dataSnapshot.getKey(), dataSnapshot.getValue());
                                    }

                                    // Añadir el nuevo campo
                                    updates.put("nombreEmpleado", nombreEmpleado);
                                    updates.put("entrda", entrdaSeleccionado);
                                    updates.put("salida", salidaSeleccionado);
                                     // Actualizar el nodo específico
                                    childSnapshot.getRef().updateChildren(updates).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.d("Firebase", "Nombre del empleado actualizado exitosamente.");
                                            } else {
                                                Log.e("Firebase", "Error al actualizar el nombre del empleado.", task.getException());
                                            }
                                        }
                                    });
                                    break;
                                }
                            }

                            if (!tokenFound) {
                                Log.d("Firebase", "El token no coincide con el almacenado en /not.");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.w("Firebase", "Error al acceder a la base de datos.", error.toException());
                        }
                    });
                }
            });
        } else {
            Log.e("Firebase", "El usuario no está autenticado.");
        }
    }

    }