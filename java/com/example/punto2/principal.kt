package com.example.punto2

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.punto2.adaptadores.AdapDiario
import com.example.punto2.adaptadores.productosAdaptador
import com.example.punto2.databinding.ActivityPrincipalBinding
import com.example.punto2.datos.Diario
import com.example.punto2.datos.producto
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

//.punto.databinding.ActivityPrincipalBinding

private const val REQUEST_CAMERA_PERMISSION = 100

class principal : AppCompatActivity() {
    private lateinit var binding: ActivityPrincipalBinding
    private lateinit var barcodeDetector: BarcodeDetector
    private lateinit var cameraSource: CameraSource
    private lateinit var mediaPlayer: MediaPlayer
     var add: productosAdaptador? = null
     var list: ArrayList<producto>? = null
    var databas: DatabaseReference? = null
    var add1: AdapDiario? = null
    var list1: ArrayList<Diario>? = null

    private var lastScanTime: Long = 0 // Almacena el tiempo del último escaneo
    private val scanInterval = 1000 // Intervalo de tiempo en milisegundos (1 segundo)

    //productos inicio
    //producto total

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrincipalBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mediaPlayer = MediaPlayer.create(this, R.raw.lectura) // Cargar el archivo de sonido

        // Iniciar el lector QR al cargar la actividad
        binding.ordenes.setOnClickListener(View.OnClickListener {
            startActivity(
                Intent(
                    this@principal,
                    MainActivityreportes::class.java
                )
            )
        })


        // Verifica y solicita permisos de cámara si es necesario

        binding.floatingActionButton2.setOnClickListener(View.OnClickListener {
            startActivity(
                Intent(
                    this@principal,
                    MainActivity_add::class.java
                )
            )
        })
        var user: FirebaseUser? = null
        var auth: FirebaseAuth? = null
        auth = FirebaseAuth.getInstance()
        user = auth!!.currentUser
        val userId = user?.uid
        //total precio
        val totalpr: TextView = findViewById(R.id.totalcanti)
        val databasesuma = FirebaseDatabase.getInstance()


        if (userId != null) {
            val sumas = databasesuma.getReference("/MI data base Usuarios/$userId/preventa")
            sumas.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        var total = 0.0 // Inicializa el total como un número decimal

                        // Itera sobre los hijos del dataSnapshot (suponiendo que cada hijo representa un producto)
                        for (productSnapshot in dataSnapshot.children) {
                            val cantidad = productSnapshot.child("cantidad").getValue(Double::class.java) ?: 0.0 // Obtiene la cantidad como un número decimal, si es nulo, se asigna 0.0
                            val precioVenta = productSnapshot.child("precio_venta").getValue(Double::class.java) ?: 0.0 // Obtiene el precio de venta como un número decimal, si es nulo, se asigna 0.0

                            total += cantidad * precioVenta // Calcula el total sumando el producto de cantidad y precio de venta
                        }

                        totalpr.text = total.toString() // Actualiza el texto del TextView con el total obtenido
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Manejar el error en caso de que la lectura de datos falle
                }
            })
        }
        ///
        binding.button4.setOnClickListener {
            val ventaRef = FirebaseDatabase.getInstance().getReference("/MI data base Usuarios/$userId/preventa")
            val ventasRef = FirebaseDatabase.getInstance().getReference("/MI data base Usuarios/$userId/ventas")

            ventaRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (productSnapshot in dataSnapshot.children) {
                            val key = productSnapshot.key ?: continue
                            val cantidadVendida = (productSnapshot.child("cantidad").value as Long).toInt()
                            val codigo = productSnapshot.child("codigo").value.toString()
                            val fecha = productSnapshot.child("fecha").value.toString()
                            val imageUrl = productSnapshot.child("image_url").value.toString()
                            val nombre = productSnapshot.child("nombre").value.toString()
                            val precioCompra = (productSnapshot.child("precio_compra").value as Long).toInt()
                            val precioVenta = (productSnapshot.child("precio_venta").value as Long).toInt()

                            val ventaData = mapOf(
                                "cantidad" to cantidadVendida,
                                "codigo" to codigo,
                                "fecha" to fecha,
                                "image_url" to imageUrl,
                                "nombre" to nombre,
                                "precio_compra" to precioCompra,
                                "precio_venta" to precioVenta
                            )

                            ventasRef.child(key).setValue(ventaData).addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    // Leer el stok del producto
                                    ventaRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(ventaSnapshot: DataSnapshot) {
                                            for (snapshot in ventaSnapshot.children) {
                                                val key = snapshot.key
                                                val cantidadVendida = snapshot.child("cantidad").value as? Long ?: 0L

                                                 val stokRef = FirebaseDatabase.getInstance().getReference("/MI data base Usuarios/$userId/stok")
                                                Log.d("FirebaseCheck", "Accediendo a: ${stokRef.toString()}")
                                                stokRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                                    override fun onDataChange(stokSnapshot: DataSnapshot) {
                                                        if (stokSnapshot.exists()) {
                                                            val cantidadActual = (stokSnapshot.child("cantidad").value as Long).toInt()
                                                            val nuevaCantidad = cantidadActual - cantidadVendida

                                                            // Actualizar el stok
                                                            stokRef.child("cantidad").setValue(nuevaCantidad).addOnCompleteListener { updateTask ->
                                                                if (updateTask.isSuccessful) {
                                                                    // Mover los datos a "ventas" y borrar de "preventa"
                                                                    ventasRef.child(key!!).setValue(snapshot.value).addOnCompleteListener { moveTask ->
                                                                        if (moveTask.isSuccessful) {
                                                                            ventaRef.child(key).removeValue().addOnCompleteListener { removeTask ->
                                                                                if (removeTask.isSuccessful) {
                                                                                    Toast.makeText(this@principal, "Datos movidos a ventas y stok actualizado", Toast.LENGTH_SHORT).show()
                                                                                } else {
                                                                                    Toast.makeText(this@principal, "Error al borrar datos de preventa", Toast.LENGTH_SHORT).show()
                                                                                }
                                                                            }
                                                                        } else {
                                                                            Toast.makeText(this@principal, "Error al mover datos a ventas", Toast.LENGTH_SHORT).show()
                                                                        }
                                                                    }
                                                                } else {
                                                                    Toast.makeText(this@principal, "Error al actualizar stok", Toast.LENGTH_SHORT).show()
                                                                }
                                                            }
                                                        } else {
                                                            Toast.makeText(this@principal, "Producto no encontrado en stok", Toast.LENGTH_SHORT).show()
                                                        }
                                                    }

                                                    override fun onCancelled(stokError: DatabaseError) {
                                                        Toast.makeText(this@principal, "Error al leer datos de stok", Toast.LENGTH_SHORT).show()
                                                    }
                                                })
                                            }
                                        }

                                        override fun onCancelled(ventaError: DatabaseError) {
                                            Toast.makeText(this@principal, "Error al leer datos de preventa", Toast.LENGTH_SHORT).show()
                                        }
                                    })
                                }
        }
    }
}
}

                override fun onCancelled(error: DatabaseError) {
                    // Manejar el error en caso de que la lectura de datos falle
                    Toast.makeText(this@principal, "Error al leer datos de preventa", Toast.LENGTH_SHORT).show()
                }
            })
        }

        ///
        binding.button3.setOnClickListener {
            val sumas = databasesuma.getReference("/MI data base Usuarios/$userId/preventa")
            sumas.removeValue().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Notificar al usuario que la operación se completó
                    Toast.makeText(this@principal, "Datos eliminados correctamente", Toast.LENGTH_SHORT).show()
                } else {
                    // Manejar el error de borrado
                    Toast.makeText(this@principal, "Error al eliminar los datos", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.button6.setOnClickListener {
            // Mostrar una alerta para confirmar el cierre de caja
            AlertDialog.Builder(this@principal)
                .setTitle("Confirmación")
                .setMessage("¿Estás seguro de que quieres cerrar la caja?")
                .setPositiveButton("Sí") { dialog, which ->
                    // Si el usuario confirma, calcula el total y muestra el total en otra alerta
                   // calcularTotalVentasYMostrarAlerta()
                     cierreCaja()
                }
                .setNegativeButton("No") { dialog, which ->
                    // Si el usuario cancela, simplemente cierra el diálogo
                    Efectivo()
                    dialog.dismiss()
                }
                .show()
        }
        checkCameraPermission()

        val dinerotiempo = databasesuma.getReference("/MI data base Usuarios/$userId/dinero")

// Añadir el ValueEventListener para escuchar cambios en tiempo real
        dinerotiempo.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val currentMoney = dataSnapshot.getValue(Double::class.java) ?: 0.0
                binding.textView8.text = "Efectivo en caja: $"+ currentMoney.toString()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Manejar el error al obtener el valor inicial
                Toast.makeText(this@principal, "Error al obtener el efectivo actual", Toast.LENGTH_SHORT).show()
            }
        })
        ///----------------------------------------------------------------------------------------------------------------------


        val database = FirebaseDatabase.getInstance()
         val mr : RecyclerView = findViewById(R.id.productos)
        val myRef = database.getReference("/MI data base Usuarios/$userId/preventa")
   //     val myRef = database.getReference("/MI data base Usuarios/D0mWaxh5CMVLO31M5uKITRCbwsW2/preventa")
        val registro : RecyclerView = findViewById(R.id.Registrodiario)
        val myregistro = database.getReference("/MI data base Usuarios/$userId/ventas")


        myRef.addListenerForSingleValueEvent(object  :ValueEventListener
        {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    // La ruta no existe, mostrar alerta
                    val mensaje = "La ruta no existe"
                     Toast.makeText(this@principal, mensaje, Toast.LENGTH_SHORT).show()
                }else{
                    mr.setHasFixedSize(true)
                    mr.layoutManager = LinearLayoutManager(this@principal, LinearLayoutManager.VERTICAL, false)
                    list = ArrayList()
                    add = productosAdaptador(list!!,this@principal ) // Pasa el contexto de la actividad principal
                    mr.adapter = add
                    val query = myRef
                    query.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            list!!.clear()
                            for (pedido in dataSnapshot.children) {
                                val pedido = pedido.getValue(producto::class.java)
                                list!!.add(pedido!!)
                            }
                            add!!.notifyDataSetChanged()
                        }
                        override fun onCancelled(error: DatabaseError) {
                        }
                    })
                }
            }
            override fun onCancelled(error: DatabaseError) {
                // Ocurrió un error al acceder a la base de datos
                val mensaje = "Error al acceder a la base de datos"
                //Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
            }
        })
        myregistro.addListenerForSingleValueEvent(object  :ValueEventListener
        {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    // La ruta no existe, mostrar alerta
                    val mensaje = "La ruta no existe"
                    Toast.makeText(this@principal, mensaje, Toast.LENGTH_SHORT).show()
                }else{
                    registro.setHasFixedSize(true)
                    registro.layoutManager = LinearLayoutManager(this@principal, LinearLayoutManager.VERTICAL, false)
                    list1 = ArrayList()
                    add1 = AdapDiario(list1!!,this@principal ) // Pasa el contexto de la actividad principal
                    registro.adapter = add1
                    val query = myregistro
                    query.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            list1!!.clear()
                            for (pedido in dataSnapshot.children) {
                                val pedido = pedido.getValue(Diario::class.java)
                                list1!!.add(pedido!!)
                            }
                            add1!!.notifyDataSetChanged()
                        }
                        override fun onCancelled(error: DatabaseError) {
                        }
                    })
                }
            }
            override fun onCancelled(error: DatabaseError) {
                // Ocurrió un error al acceder a la base de datos
                val mensaje = "Error al acceder a la base de datos"
                //Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
            }
        })

    }

    private fun cierreCaja() {

        var user: FirebaseUser? = null
        var auth: FirebaseAuth? = null
        auth = FirebaseAuth.getInstance()
        user = auth!!.currentUser
        val userId = user?.uid
        val ventasRef = FirebaseDatabase.getInstance().getReference("/MI data base Usuarios/$userId/ventas")
        val ventaVerificada = FirebaseDatabase.getInstance().getReference("/MI data base Usuarios/$userId/ventaVerificada")

        ventasRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val ventasMap = mutableMapOf<String, Any>()
                    for (productSnapshot in dataSnapshot.children) {
                        ventasMap[productSnapshot.key!!] = productSnapshot.value!!
                    }
                    ventaVerificada.updateChildren(ventasMap).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Borra los datos de "preventa" después de haberlos copiado a "ventas"
                            ventasRef.removeValue().addOnCompleteListener { removeTask ->
                                if (removeTask.isSuccessful) {
                                    // Opcional: notificar al usuario que la operación se completó
                                    Toast.makeText(this@principal, "Datos movidos a ventas", Toast.LENGTH_SHORT).show()
                                    Efectivo()

                                } else {
                                    // Manejar el error de borrado
                                    Toast.makeText(this@principal, "Error al borrar datos de preventa", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejar el error en caso de que la lectura de datos falle
            }
        })
    }


    private fun Efectivo() {
        val databasesuma = FirebaseDatabase.getInstance()
        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid
        val dialogView = layoutInflater.inflate(R.layout.dialog_layout, null)
        val editTextAddMoney = dialogView.findViewById<EditText>(R.id.editTextAddMoney)
        val dineroRef = databasesuma.getReference("/MI data base Usuarios/$userId/dinero")
        val transaccionesRef = databasesuma.getReference("/MI data base Usuarios/$userId/transacciones")

        dineroRef.get().addOnSuccessListener { dataSnapshot ->
            val currentMoney = dataSnapshot.getValue(Double::class.java) ?: 0.0

            AlertDialog.Builder(this@principal)
                .setTitle("Total de Efectivo")
                .setMessage("El total de efectivo es: $currentMoney")
                .setView(dialogView)
                .setPositiveButton("Agregar dinero") { dialog, which ->
                    val addMoney = editTextAddMoney.text.toString().toDoubleOrNull() ?: 0.0
                    val finalTotal = currentMoney + addMoney

                    dineroRef.setValue(finalTotal).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val dateFormatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                            val fecha = dateFormatter.format(Date()) // Fecha en formato dd-MM-yyyy

                            val transaccion = mapOf(
                                "anterior" to currentMoney,
                                "finalTotal" to finalTotal,
                                "cantidad" to addMoney,
                                "tipo" to "Se Agreego Dinero",
                                "fecha" to fecha // Fecha formateada
                            )
                            transaccionesRef.push().setValue(transaccion).addOnCompleteListener { transaccionTask ->
                                if (transaccionTask.isSuccessful) {
                                    AlertDialog.Builder(this@principal)
                                        .setTitle("Efectivo Final")
                                        .setMessage("El efectivo final es: $finalTotal")
                                        .setPositiveButton("Aceptar") { dialog, which ->
                                            dialog.dismiss()
                                        }
                                        .show()
                                } else {
                                    // Manejar el error
                                    Toast.makeText(this@principal, "Error al registrar la transacción", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            // Manejar el error
                            Toast.makeText(this@principal, "Error al actualizar el efectivo", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .setNegativeButton("Sacar dinero") { dialog, which ->
                    val addMoney = editTextAddMoney.text.toString().toDoubleOrNull() ?: 0.0
                    val finalTotal = currentMoney - addMoney

                    dineroRef.setValue(finalTotal).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val dateFormatter = SimpleDateFormat("MM-dd-yyyy", Locale.getDefault())
                            val fecha = dateFormatter.format(Date()) // Fecha en formato dd-MM-yyyy

                            val transaccion = mapOf(
                                "anterior" to currentMoney,
                                "finalTotal" to finalTotal,
                                "cantidad" to addMoney,
                                "tipo" to "Se Quito Dinero",
                                "fecha" to fecha // Fecha formateada
                            )
                            transaccionesRef.push().setValue(transaccion).addOnCompleteListener { transaccionTask ->
                                if (transaccionTask.isSuccessful) {
                                    AlertDialog.Builder(this@principal)
                                        .setTitle("Efectivo Final")
                                        .setMessage("El efectivo final es: $finalTotal")
                                        .setPositiveButton("Aceptar") { dialog, which ->
                                            dialog.dismiss()
                                        }
                                        .show()
                                } else {
                                    // Manejar el error
                                    Toast.makeText(this@principal, "Error al registrar la transacción", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            // Manejar el error
                            Toast.makeText(this@principal, "Error al actualizar el efectivo", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .show()
        }.addOnFailureListener {
            // Manejar el error de obtener el valor
            Toast.makeText(this@principal, "Error al obtener el efectivo actual", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
        } else {
            // Si el permiso ya ha sido concedido, inicializa el escáner de código de barras
         }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Permiso concedido, inicializa el escáner de código de barras
             } else {
                // Permiso denegado, muestra un mensaje al usuario
                Toast.makeText(this, "Permiso de cámara es necesario para usar el escáner", Toast.LENGTH_SHORT).show()
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

        binding.prueba!!.holder.addCallback(object : SurfaceHolder.Callback {
            @SuppressLint("MissingPermission")
            override fun surfaceCreated(p0: SurfaceHolder) {
                try {
                    cameraSource.start(binding.prueba!!.holder)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {}

            override fun surfaceDestroyed(p0: SurfaceHolder) {
                cameraSource.stop()
            }
        })

        barcodeDetector.setProcessor(object : Detector.Processor<Barcode> {
            override fun release() {
                Toast.makeText(applicationContext, "escanner stop", Toast.LENGTH_LONG)
                    .show()
            }

            override fun receiveDetections(detector: Detector.Detections<Barcode>) {
                val barcode = detector.detectedItems
                if (barcode.size() != 0) {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastScanTime >= scanInterval) {
                        lastScanTime = currentTime
                    binding.pruebatexto!!.post {
                        // Reproducir el sonido cuando se detecta un código de barras
                        mediaPlayer.start()

                        val codigoBarras = barcode.valueAt(0).displayValue
                        var user: FirebaseUser? = null
                        var auth: FirebaseAuth? = null
                        auth = FirebaseAuth.getInstance()
                        user = auth!!.currentUser
                        val userId = user?.uid
                        val database = FirebaseDatabase.getInstance()
                        val myRef = database.getReference("/MI data base Usuarios/$userId/Stok")
                        // Consulta la base de datos para obtener información del producto
                        myRef.orderByChild("codigo").equalTo(codigoBarras).addListenerForSingleValueEvent(object :
                            ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    for (productSnapshot in dataSnapshot.children) {
                                        // Obtener los datos del producto directamente del DataSnapshot
                                        val nombre = productSnapshot.child("nombre").getValue(String::class.java)
                                        val precioCompra = productSnapshot.child("precio_compra").getValue(Double::class.java)
                                        val precioVenta = productSnapshot.child("precio_venta").getValue(Double::class.java)
                                        val image_urlP = productSnapshot.child("image_url").getValue(String::class.java)

                                        // Verificar si el producto ya está en la lista de preventa
                                        val ventaRef = database.getReference("/MI data base Usuarios/$userId/preventa")
                                        ventaRef.orderByChild("codigo").equalTo(codigoBarras).addListenerForSingleValueEvent(object :
                                            ValueEventListener {
                                            override fun onDataChange(preventaSnapshot: DataSnapshot) {
                                                if (preventaSnapshot.exists()) {
                                                    // El producto ya está en la lista de preventa, incrementar la cantidad
                                                    for (itemSnapshot in preventaSnapshot.children) {
                                                        val cantidadActual = itemSnapshot.child("cantidad").getValue(Int::class.java) ?: 0
                                                        itemSnapshot.ref.child("cantidad").setValue(cantidadActual + 1)
                                                    }
                                                } else {
                                                    // El producto no está en la lista de preventa, agregarlo
                                                    val productoPreventa = HashMap<String, Any>()
                                                    productoPreventa["nombre"] = nombre ?: ""
                                                    productoPreventa["precio_compra"] = precioCompra ?: 0.0
                                                    productoPreventa["precio_venta"] = precioVenta ?: 0.0
                                                    productoPreventa["cantidad"] = 1
                                                    productoPreventa["codigo"] = codigoBarras // Agregar el código de barras para futuras búsquedas más eficientes
                                                    productoPreventa["image_url"] =  image_urlP ?: "" // Agregar el código de barras para futuras búsquedas más eficientes
                                                    val sdf = SimpleDateFormat("MM-dd-yyyy", Locale.getDefault())
                                                    val currentDate = sdf.format(Calendar.getInstance().time)
                                                    productoPreventa["fecha"] = currentDate
                                                    ventaRef.push().setValue(productoPreventa)
                                                }
                                            }

                                            override fun onCancelled(databaseError: DatabaseError) {
                                                // Error en la consulta
                                                Toast.makeText(this@principal, "Error al buscar el producto", Toast.LENGTH_SHORT).show()
                                            }
                                        })
                                    }
                                } else {
                                    // El código de barras no está en la base de datos
                                    Toast.makeText(this@principal, "Producto no encontrado", Toast.LENGTH_SHORT).show()
                                }
                            }

                            override fun onCancelled(databaseError: DatabaseError) {
                                // Error en la consulta
                                Toast.makeText(this@principal, "Error al buscar el producto", Toast.LENGTH_SHORT).show()
                            }
                        })


                    }
                }
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

    ///  1 _ primero
}