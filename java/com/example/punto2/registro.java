package com.example.punto2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

public class registro extends AppCompatActivity {
    EditText correote, correote2, nombre, apellido;
    Button button, button2;
    FirebaseAuth auth;
    private CardView abarroterasCard, tortilleriaCard, individualCard, negocioCard;
    private String selectedOptionGroup1 = ""; // Variable para la opción seleccionada del primer grupo
    private String selectedOptionGroup2 = ""; // Variable para la opción seleccionada del segundo grupo

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /////baner y color de el navegador
        getWindow().requestFeature(Window.FEATURE_NO_TITLE); // Eliminar banner
        getWindow().setNavigationBarColor(getResources().getColor(R.color.purple_200));
        getWindow().setNavigationBarColor(getResources().getColor(R.color.purple_200));
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setNavigationBarColor(getResources().getColor(R.color.purple_200));
        }
        //////////////////////////////////
        setContentView(R.layout.activity_registro);

        correote = findViewById(R.id.correote);
        correote2 = findViewById(R.id.correote2);
        apellido = findViewById(R.id.paterno);
        button = findViewById(R.id.button);
        button2 = findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(registro.this, login.class));
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (apellido.getText().toString().trim().equals("")) {
                    Toast.makeText(registro.this, "Rellene todos los datos", Toast.LENGTH_SHORT).show();
                } else {
                    String Email = correote.getText().toString();
                    String password = correote2.getText().toString();
                    String apellido1 = apellido.getText().toString();
                    if (!Patterns.EMAIL_ADDRESS.matcher(Email).matches()) {
                        correote.setError("Correo no válido");
                        correote.setFocusable(true);
                    } else if (password.length() < 6) {
                        correote2.setError("La contraseña debe ser mayor a 6 caracteres");
                        correote2.setFocusable(false);
                    } else {
                        RegistrarUsuario(Email, password, apellido1);
                    }
                }
            }
        });

        abarroterasCard = findViewById(R.id.Abarroteras);
        tortilleriaCard = findViewById(R.id.tortilleria);
        individualCard = findViewById(R.id.individula);
        negocioCard = findViewById(R.id.Negocio);

        setupCardClickListener(abarroterasCard, tortilleriaCard, "abarroteras_clicked", "tortilleria_clicked", "abarroteras");
        setupCardClickListener(tortilleriaCard, abarroterasCard, "tortilleria_clicked", "abarroteras_clicked", "tortilleria");
        setupCardClickListener(individualCard, negocioCard, "individual_clicked", "negocio_clicked", "individual");
        setupCardClickListener(negocioCard, individualCard, "negocio_clicked", "individual_clicked", "negocio");
    }

    // Método para registrar un usuario
    private void RegistrarUsuario(String Email, String password, String apellido1) {
        auth = FirebaseAuth.getInstance();

        String hashedPassword = hashPassword(password);

        auth.createUserWithEmailAndPassword(Email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = auth.getCurrentUser();
                            assert user != null;
                            user.sendEmailVerification();
                            String uidString = user.getUid();
                            // string
                            String correoString = correote.getText().toString();
                            String direc = "MI data base Usuarios/" + uidString + "/carrito";
                            String carrito = "vacio";

                            String Apellido = apellido.getText().toString();
                            HashMap<Object, Object> DatosUsuario = new HashMap<>();
                            DatosUsuario.put("Uid", uidString);
                            DatosUsuario.put("Email", correoString);
                            DatosUsuario.put("Password", hashedPassword);
                            DatosUsuario.put("Apellido", Apellido);
                            DatosUsuario.put("direcciCarrito", direc);
                            DatosUsuario.put("carrito", carrito);
                            DatosUsuario.put("datoubcacion", 1);
                            DatosUsuario.put("imagen", "");
                            DatosUsuario.put("enlace", "");
                            DatosUsuario.put("puntos", 0);
                            DatosUsuario.put("telefono", "");
                            DatosUsuario.put("ubicacion", "");
                            DatosUsuario.put("Dinero", 0);
                            DatosUsuario.put("selectedOptionGroup1", selectedOptionGroup1);
                            DatosUsuario.put("selectedOptionGroup2", selectedOptionGroup2);

                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference reference = database.getReference("MI data base Usuarios"); // nombre de data de base
                            reference.child(uidString).setValue(DatosUsuario);
                            startActivity(new Intent(registro.this, login.class));
                            Toast.makeText(registro.this, "Usuario registrado exitosamente", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(registro.this, "Ocurrió un error", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(registro.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Función para cifrar la contraseña usando SHA-256
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private void setupCardClickListener(CardView cardView, CardView otherCardView, String key, String otherKey, String option) {
        SharedPreferences preferences = getSharedPreferences("app_prefs", MODE_PRIVATE);
        boolean isClicked = preferences.getBoolean(key, false);
        boolean isOtherClicked = preferences.getBoolean(otherKey, false);

        if (isClicked) {
            cardView.setCardBackgroundColor(Color.LTGRAY); // Cambia a cualquier color que desees
        }

        if (isOtherClicked) {
            otherCardView.setCardBackgroundColor(Color.LTGRAY); // Cambia a cualquier color que desees
        }

        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean(key, true);
                editor.putBoolean(otherKey, false);
                editor.apply();

                cardView.setCardBackgroundColor(Color.LTGRAY); // Cambia a cualquier color que desees
                otherCardView.setCardBackgroundColor(Color.WHITE); // Cambia a cualquier color que desees

                if (key.equals("abarroteras_clicked") || key.equals("tortilleria_clicked")) {
                    selectedOptionGroup1 = option;
                } else {
                    selectedOptionGroup2 = option;
                }
            }
        });
    }

}