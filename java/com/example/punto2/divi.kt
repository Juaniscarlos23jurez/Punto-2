package com.example.punto2

import android.app.Dialog
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import com.example.punto2.sqlite.DBContract
import com.example.punto2.sqlite.DBHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.suspendCoroutine


class divi : AppCompatActivity() {
    private lateinit var loadingDialog: Dialog
    private lateinit var auth: FirebaseAuth
    private var user: FirebaseUser? = null
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_divi)
        // Inicializar la pantalla de carga
        auth = FirebaseAuth.getInstance()

        initLoadingScreen()
    }

    private fun initLoadingScreen() {
        loadingDialog = Dialog(this).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.loading_screen)
            setCancelable(false)
            window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }
    }

    override fun onStart() {
        super.onStart()
        user = auth.currentUser

        if (!isUserLoggedIn()) {
            // Redirige al usuario a la pantalla de inicio de sesión.
            val intent = Intent(this, registro::class.java)
            startActivity(intent)
            finish()
        } else {
            showLoadingScreen()
            CoroutineScope(Dispatchers.IO).launch {
                handleFirebaseTasks()
                withContext(Dispatchers.Main) {
                    hideLoadingScreen()
                    navigateBasedOnUserSelection()
                }
            }
        }
    }

    private fun showLoadingScreen() {
        loadingDialog.show()
    }

    private fun hideLoadingScreen() {
        loadingDialog.dismiss()
    }

    private suspend fun handleFirebaseTasks() {
        val token = getToken()
        val userId = user?.uid ?: return // Verificar si user es null
        val reference = FirebaseDatabase.getInstance().reference.child("MI data base Usuarios/$userId/not")

        val tokenExists = checkIfTokenExists(reference, token)
        if (!tokenExists) {
            val userCount = getUserCount(reference)
            val userType = if (userCount == 0L) "jefe" else "empleado"
            val userList =   (userCount + 1)
            registerUser(reference, token, userType, userList)
            saveTokenInSQLite(token)
        }
    }

    private suspend fun getToken(): String = suspendCoroutine { continuation ->
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                continuation.resumeWith(Result.failure(task.exception!!))
            } else {
                continuation.resumeWith(Result.success(task.result!!))
            }
        }
    }

    private suspend fun checkIfTokenExists(reference: DatabaseReference, token: String): Boolean = suspendCoroutine { continuation ->
        reference.orderByChild("key").equalTo(token).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                continuation.resumeWith(Result.success(snapshot.exists()))
            }

            override fun onCancelled(error: DatabaseError) {
                continuation.resumeWith(Result.failure(error.toException()))
            }
        })
    }

    private suspend fun getUserCount(reference: DatabaseReference): Long = suspendCoroutine { continuation ->
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                continuation.resumeWith(Result.success(snapshot.childrenCount))
            }

            override fun onCancelled(error: DatabaseError) {
                continuation.resumeWith(Result.failure(error.toException()))
            }
        })
    }

    private suspend fun registerUser(reference: DatabaseReference, token: String, userType: String, userList: Long) = suspendCoroutine<Unit> { continuation ->
        val newUserReference = reference.push()
        val userMap = mapOf(
            "key" to token,
            "tipo" to userType,
            "lista" to userList
        )
        newUserReference.setValue(userMap).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("Firebase", "Usuario registrado correctamente.")
                continuation.resumeWith(Result.success(Unit))
            } else {
                Log.w("Firebase", "Error al registrar usuario.", task.exception)
                continuation.resumeWith(Result.failure(task.exception!!))
            }
        }
    }

    private fun saveTokenInSQLite(token: String) {
        val dbHelper = DBHelper(this)
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DBContract.TokenEntry.COLUMN_NAME_TOKEN, token)
        }
        val newRowId = db.insert(DBContract.TokenEntry.TABLE_NAME, null, values)
        Log.d("SQLite", "Token guardado en SQLite con ID: $newRowId")
    }

    private fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    private fun navigateBasedOnUserSelection() {
        val userId = user?.uid ?: return // Verificar si user es null
        val databaseReference = FirebaseDatabase.getInstance().reference.child("/MI data base Usuarios/$userId/selectedOptionGroup1")
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val selectedOption = dataSnapshot.getValue(String::class.java)
                if (selectedOption == "tortilleria") {
                    val intent = Intent(this@divi, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else if (selectedOption == "abarroteras") {
                    val intent = Intent(this@divi, principal::class.java)
                    startActivity(intent)
                    finish()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Maneja el error
            }
        })
    }
}


