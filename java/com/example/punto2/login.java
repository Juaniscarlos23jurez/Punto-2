package com.example.punto2;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class login extends AppCompatActivity {
    FirebaseDatabase firebaseDatabase;
    DatabaseReference perfil2;

    EditText correote, pas;
    Button buttonlo2,button;
    FirebaseAuth auth;
    FirebaseUser user;
    private static final int REQUEST_CAMERA_PERMISSION = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//conteniro
        getWindow().setNavigationBarColor(getResources().getColor(R.color.purple_200));
        getWindow().setStatusBarColor(ContextCompat.getColor(login.this,R.color.purple_200));
        setContentView(R.layout.activity_login);
        // valor
        correote= findViewById(R.id.correote);
        pas= findViewById(R.id.correote2);
        button= findViewById(R.id.button);
        auth=FirebaseAuth.getInstance();
        user= auth.getCurrentUser();
        buttonlo2 = findViewById(R.id.button2);
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        boolean isDarkMode = currentNightMode == Configuration.UI_MODE_NIGHT_YES;

// Cambiar el color de fondo según el modo de la aplicación
        if (isDarkMode) {
            pas.setBackgroundColor(ContextCompat.getColor(this, R.color.relleno));
        } else {
            pas.setBackgroundColor(ContextCompat.getColor(this, R.color.white));
        }

        //funcion
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //obtencion
                String correo =  correote.getText().toString();
                String pasw = pas.getText().toString();

                /////copia de el registro if ( validacion )
                if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
                    correote.setError("correo no valido");
                    correote.setFocusable(true);
                } else if (pasw.length() < 6) {
                    pas.setError("La contraseña debe de ser mayora 6 caracteres");
                    pas.setFocusable(false);

                }
                else {
                    LogeoUsuario(correo, pasw);
                }
            }
        });
        buttonlo2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(login.this,registro.class));

            }
        });
     }



    ///metodo para el logueo de los suarios
    private int intentosFallidos = 0;
    private static final int MAX_INTENTOS_FALLIDOS = 3;

    private void LogeoUsuario(String correo, String pasw) {
        //if (intentosFallidos >= MAX_INTENTOS_FALLIDOS) {
            // Realiza una acción como bloquear el inicio de sesión
        //    Toast.makeText(login.this, "Demasiados intentos fallidos. Por favor, inténtalo más tarde.", Toast.LENGTH_SHORT).show();
        //    return;
        //}

        auth.signInWithEmailAndPassword(correo, pasw)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = auth.getCurrentUser();
                            //if (user != null && user.isEmailVerified()) {
                            if (user != null ) {
                                Toast.makeText(login.this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show();
                             startActivity(new Intent(login.this, divi.class));
                            } else {
                                Toast.makeText(login.this, "Correo no verificado", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            //    intentosFallidos++; // Incrementar el contador de intentos fallidos
                            Toast.makeText(login.this, "Correo o contraseña incorrecta", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //  intentosFallidos++; // Incrementar el contador de intentos fallidos
                        Toast.makeText(login.this, "Error al iniciar sesión: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            // Si el permiso ya ha sido concedido, inicializa el escáner de código de barras
         }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido, inicializa el escáner de código de barras
             } else {
                // Permiso denegado, muestra un mensaje al usuario
                Toast.makeText(this, "Permiso de cámara es necesario para usar el escáner", Toast.LENGTH_SHORT).show();
            }
        }
    }

}