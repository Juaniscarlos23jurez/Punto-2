package com.example.punto2

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.SurfaceHolder
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.punto2.databinding.ActivityNewProductoinsuBinding
import com.example.punto2.databinding.ActivityNewproductosBinding
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

class newProductoinsu : AppCompatActivity() {
    /// registro de codigo
    private lateinit var binding: ActivityNewProductoinsuBinding
    private lateinit var barcodeDetector: BarcodeDetector
    private lateinit var cameraSource: CameraSource
    private lateinit var mediaPlayer: MediaPlayer
    var intentData = ""
    /// Registro de los demas datos
    private lateinit var Nombre: EditText
    private lateinit var precioc: EditText
    private lateinit var preciov: EditText
    private lateinit var cantidad: EditText
    private lateinit var button7: Button
    private lateinit var button8: Button
    private lateinit var adapterItems: ArrayAdapter<String>
    private lateinit var auto: AutoCompleteTextView
    private lateinit var adapterItems2: ArrayAdapter<String>
    private lateinit var auto2: AutoCompleteTextView
    private val items2 = arrayOf("Credito", "Contado")
    private lateinit var agregar: FloatingActionButton
    private lateinit var imageView: ImageView
    private lateinit var capturedImage: Bitmap
    private var provee: String? = null
    private var tipo: String? = null
    private var   CAMERA_PERMISSION_REQUEST_CODE = 100
    private var   CAMERA_REQUEST_CODE = 101

    private lateinit var storage: FirebaseStorage
    private lateinit var storageReference: StorageReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_productoinsu)
        binding = ActivityNewProductoinsuBinding.inflate(layoutInflater)
        setContentView(binding.root)
         /// resistro de demas datos
        storage = FirebaseStorage.getInstance()
        storageReference = storage.reference
        ///
        Nombre = findViewById(R.id.preciov)
        cantidad = findViewById(R.id.cantidad)
        button7 = findViewById(R.id.button7)
        imageView = findViewById(R.id.imageView)
        agregar = findViewById(R.id.floatingActionButton)
        val database1 = FirebaseDatabase.getInstance()
        val user1: FirebaseUser? = FirebaseAuth.getInstance().currentUser
        val userId1 = user1?.uid

        agregar.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this@newProductoinsu,android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                // El permiso ya ha sido concedido
                // Puedes iniciar la funcionalidad de la cámara aquí
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
            } else {
                // El permiso aún no ha sido concedido, así que solicítalo
                ActivityCompat.requestPermissions(
                    this@newProductoinsu,
                    arrayOf(android.Manifest.permission.CAMERA),
                    CAMERA_PERMISSION_REQUEST_CODE
                )
            }
        }
        var user: FirebaseUser? = null
        var auth: FirebaseAuth? = null
        auth = FirebaseAuth.getInstance()
        user = auth!!.currentUser
        val userId = user?.uid
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("/MI data base Usuarios/$userId/stok")
        button7.setOnClickListener {
            // Obtiene los valores de los campos
            val nombre = Nombre.text.toString()
            val cantidadProducto = cantidad.text.toString().toInt()

            // Obtiene el texto de pruebatexto
            val texto = binding.pruebatexto.text.toString()

            // Verifica si se ha capturado una imagen
            if (::capturedImage.isInitialized) {
                // Sube la imagen a Firebase Storage
                val imageRef = storageReference.child("images/${UUID.randomUUID()}.jpg")
                val baos = ByteArrayOutputStream()
                capturedImage.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val data = baos.toByteArray()

                val uploadTask = imageRef.putBytes(data)
                uploadTask.addOnSuccessListener { taskSnapshot ->
                    // Obtener la URL de descarga
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        val imageUrl = uri.toString()
                        // Guardar los datos del producto en Firebase Realtime Database
                        saveProductData(nombre, cantidadProducto, texto, imageUrl)
                    }
                }.addOnFailureListener {
                    // Error al subir la imagen
                    Toast.makeText(this@newProductoinsu, "Error al subir la imagen", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Si no hay imagen, solo guarda los datos del producto
                saveProductData(nombre, cantidadProducto, texto, null)
            }
        }

        // ...
    }

    private fun saveProductData(nombre: String, cantidadProducto: Int, codigo: String, imageUrl: String?) {
        var user: FirebaseUser? = null
        var auth: FirebaseAuth? = null
        auth = FirebaseAuth.getInstance()
        user = auth!!.currentUser
        val userId = user?.uid
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("/MI data base Usuarios/$userId/stok")
        val productId = myRef.push().key

        // Crea un nuevo objeto para los datos del producto
        val producto = HashMap<String, Any>()
        producto["nombre"] = nombre
        producto["precio_venta"] = cantidadProducto
        producto["codigo"] = productId.toString()

        if (imageUrl != null) {
            producto["image_url"] = imageUrl
        }

        // Obtiene la fecha actual en el formato mes-día-año
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val currentDate = sdf.format(Calendar.getInstance().time)
        producto["fecha"] = currentDate

        // Genera una nueva clave única para el producto

        // Guarda los datos del producto en la base de datos en tiempo real
        if (productId != null) {
            myRef.child(productId).setValue(producto)
                .addOnSuccessListener {
                    // Se ha guardado exitosamente
                    Toast.makeText(this@newProductoinsu, "Producto agregado correctamente", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    // Error al guardar
                    Toast.makeText(this@newProductoinsu, "Error al agregar el producto", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun iniBc() {
        barcodeDetector = BarcodeDetector.Builder(this)
            .setBarcodeFormats(Barcode.ALL_FORMATS)
            .build()
        cameraSource = CameraSource.Builder(this, barcodeDetector)
            .setRequestedPreviewSize(1920, 1080)
            .setAutoFocusEnabled(true)
            .build()


        barcodeDetector.setProcessor(object : Detector.Processor<Barcode> {
            override fun release() {
             }

            @SuppressLint("SuspiciousIndentation")
            override fun receiveDetections(detector: Detector.Detections<Barcode>) {
                val barcode = detector.detectedItems
                if (barcode.size() != 0) {
                    binding.pruebatexto!!.post {
                        intentData = barcode.valueAt(0).displayValue
                        binding.pruebatexto.setText(intentData)
                        // Reproducir el sonido cuando se detecta un código de barras
                        mediaPlayer.start()
                    }
                    //  finish()

                }
            }
        })
    }

    override fun onPause() {
        super.onPause()
        cameraSource.release()
    }

    override fun onResume() {
        super.onResume()
        iniBc()

    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release() // Liberar recursos del reproductor de medios
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // El permiso fue concedido por el usuario
                // No se necesita ninguna acción adicional aquí
            } else {
                // El permiso fue denegado por el usuario
                // Aquí puedes mostrar un mensaje o realizar alguna otra acción
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val image = data?.extras?.get("data") as Bitmap?
            if (image != null) {
                capturedImage = image
                imageView.setImageBitmap(capturedImage)
            } else {
                Toast.makeText(this@newProductoinsu, "error", Toast.LENGTH_SHORT).show()
            }
        }
    }


}