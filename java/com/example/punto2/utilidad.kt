package com.example.punto2

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Arrays
import java.util.Calendar
import java.util.Locale

class utilidad : AppCompatActivity() {
    private var lineChart: LineChart? = null
    private var xValues: List<String>? = null
    private  var totalGastos = 0.0
    private  var sumaTotalSueldo= 0.0
    private  var sumaTotal  = 0.0
    private  var suel = 0.0
    private  var venta = 0.0
    private  var totla  = 0.0
    var user: FirebaseUser? = null
    var auth: FirebaseAuth? = null
    var infoVentas: DatabaseReference? = null
    var infoAlmacen:DatabaseReference? = null
    var infodatagasos:DatabaseReference? = null
    var infodatasueldos:DatabaseReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_utilidad)
          auth = FirebaseAuth.getInstance()
          user = auth!!.currentUser
          if (user != null) {
              infodatagasos = FirebaseDatabase.getInstance()
                  .getReference("/MI data base Usuarios/" + user!!.uid + "/gastos_costos")
              sumar_costos1()
              infoVentas = FirebaseDatabase.getInstance()
                  .getReference("/MI data base Usuarios/" + user!!.uid + "/ventaVerificada")
              sumarPreciosDeVenta()
              infoAlmacen = FirebaseDatabase.getInstance()
                  .getReference("/MI data base Usuarios/" + user!!.uid + "/insumos_verificados")
              sumarStok()
              infodatasueldos = FirebaseDatabase.getInstance()
                  .getReference("/MI data base Usuarios/" + user!!.uid + "/Empleados")
              sumar_sueldos1()
          }
          setupChart()
     }
    private fun sumar_sueldos1() {
        infodatasueldos!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                sumaTotalSueldo = 0.0
                totla = 0.0
                val sumaPorMes: MutableMap<Int, Double> = HashMap()

                // Inicializar el mapa con 0 para cada mes
                for (i in 1..12) {
                    sumaPorMes[i] = 0.0
                }
                val calendar = Calendar.getInstance()
                val currentMonth = calendar[Calendar.MONTH] + 1 // Mes actual (1 a 12)
                for (snapshot in dataSnapshot.children) {
                    val sueldo = snapshot.child("sueldo").getValue(
                        Double::class.java
                    )
                    val tiempo = snapshot.child("tiempo").getValue(
                        String::class.java
                    )
                    val fechaPrimerPago = snapshot.child("prime_pago").getValue(
                        String::class.java
                    )
                    if (sueldo != null && tiempo != null && fechaPrimerPago != null) {
                        val mesPago = obtenerMesDeFecha(fechaPrimerPago)
                        if (mesPago != -1 && mesPago <= currentMonth) {
                            val factor = calcularFactor(fechaPrimerPago, tiempo, mesPago)

                            // Corregir la suma para evitar repetición de sueldos
                            val sumaActual = sumaPorMes[mesPago]!!
                            val sumaFactorizada =
                                sueldo * factor // Sumar sueldo según el factor calculado
                            sumaPorMes[mesPago] = sumaActual + sumaFactorizada
                            totla += sumaFactorizada
                            // Sumar al total general de sueldos
                            sumaTotalSueldo = totla + sueldo
                        }
                    }
                }
                calcularUtilidad(sumaPorMes)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Manejo de error
            }
        })
    }

    private fun sumar_costos1() {
        infodatagasos!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val sumaPorMes: MutableMap<Int, Double> = HashMap()
                totalGastos = 0.0
                val calendar = Calendar.getInstance()
                for (snapshot in dataSnapshot.children) {
                    val monto = snapshot.child("monto").getValue(Double::class.java)
                    val tiempo = snapshot.child("tiempo").getValue(
                        String::class.java
                    )
                    val fecha = snapshot.child("fecha").getValue(String::class.java)
                    var mes = snapshot.child("mes").getValue(Int::class.java)
                    if (monto != null && tiempo != null && fecha != null && mes != null) {
                        val subtotal: Double = monto
                        totalGastos += subtotal // Suma al total general

                        // Ajusta el mes restando 1 para coincidir con la indexación de Calendar
                        mes = mes - 1
                        if (sumaPorMes.containsKey(mes)) {
                            sumaPorMes[mes] = sumaPorMes[mes]!! + subtotal
                        } else {
                            sumaPorMes[mes] = subtotal
                        }
                    }
                }
                calcularUtilidad(sumaPorMes)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Manejo de error
            }
        })
    }

    private fun sumarPreciosDeVenta() {
        infoVentas!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val sumaPorMes: MutableMap<Int, Double> = HashMap()
                venta = 0.0 // Inicializar el total de ventas
                for (snapshot in dataSnapshot.children) {
                    val precioVenta = snapshot.child("precio_venta").getValue(
                        Double::class.java
                    )
                    val cantidad = snapshot.child("cantidad").getValue(
                        Double::class.java
                    )
                    var mes = snapshot.child("mes").getValue(Int::class.java)
                    if (precioVenta != null && cantidad != null && mes != null) {
                        val subtotal = precioVenta * cantidad
                        venta += subtotal // Suma al total general

                        // Ajusta el mes restando 1 para coincidir con la indexación de Calendar
                        mes = mes - 1
                        if (sumaPorMes.containsKey(mes)) {
                            sumaPorMes[mes] = sumaPorMes[mes]!! + venta
                        } else {
                            sumaPorMes[mes] = subtotal
                        }
                    }
                }

                // Llamar a calcularUtilidad() después de que se han procesado todas las ventas
                calcularUtilidad(sumaPorMes)

                // Actualizar la gráfica con los valores calculados
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Manejo de error en caso de falla en la lectura de datos
            }
        })
    }

    private fun sumarStok() {
        infoAlmacen!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val sumaPorMes: MutableMap<Int, Double> = HashMap()
                sumaTotal = 0.0
                for (snapshot in dataSnapshot.children) {
                    val precioVenta = snapshot.child("precioCompra").getValue(
                        Double::class.java
                    )
                    val cantidad = snapshot.child("cantidad").getValue(
                        Double::class.java
                    )
                    var mes =
                        snapshot.child("mes").getValue(Int::class.java) // Obtener el mes como int
                    if (precioVenta != null && mes != null && cantidad != null) {
                        val subtotal = precioVenta * cantidad
                        sumaTotal += subtotal // Suma al total general

                        // Ajusta el mes restando 1 para coincidir con la indexación de Calendar
                        mes = mes - 1
                        if (sumaPorMes.containsKey(mes)) {
                            sumaPorMes[mes] = sumaPorMes[mes]!! + subtotal
                            sumaTotal += precioVenta * cantidad
                        } else {
                            sumaPorMes[mes] = subtotal
                        }
                    }
                }
                calcularUtilidad(sumaPorMes) // Llama a calcular la utilidad después de sumar precios de venta
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun calcularFactor(fechaPrimerPago: String, tiempo: String, mesPago: Int): Int {
        val calendar = Calendar.getInstance()
        val currentMonth = calendar[Calendar.MONTH] + 1 // Mes actual (1 a 12)
        return when (tiempo) {
            "Diario" -> 30 // Asumiendo 30 días en un mes
            "Semanal" -> {
                if (mesPago == currentMonth) {
                    // Calcula las semanas desde el primer pago hasta el final del mes actual
                    val fechaCal = Calendar.getInstance()
                    try {
                        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        fechaCal.time = sdf.parse(fechaPrimerPago)
                    } catch (e: ParseException) {
                        e.printStackTrace()
                    }
                    val diaPago = fechaCal[Calendar.DAY_OF_MONTH]
                    val dayOfMonth = calendar[Calendar.DAY_OF_MONTH]
                    return Math.ceil((dayOfMonth - diaPago + 1) / 7.0)
                        .toInt() // Número de semanas transcurridas
                }
                4 // Asumiendo 4 semanas en un mes
            }

            "Mensual", "Trimestral", "Semestral", "Anual", "Unico" -> 1
            else -> 1
        }
    }

    private fun obtenerMesDeFecha(fecha: String): Int {
        return try {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val fechaDate = sdf.parse(fecha)
            val cal = Calendar.getInstance()
            cal.time = fechaDate
            cal[Calendar.MONTH] + 1 // Los meses en Calendar comienzan en 0
        } catch (e: ParseException) {
            e.printStackTrace()
            -1 // Valor de error
        }
    }

    private fun calcularUtilidad(sumaPorMes: MutableMap<Int, Double>) {
        val utilidad = venta - (sumaTotalSueldo + totalGastos + sumaTotal)
        val mesActual = Calendar.getInstance().get(Calendar.MONTH) + 1 // +1 para obtener el mes de 1 a 12
        sumaPorMes[mesActual] = utilidad

        actualizarGrafica(sumaPorMes)  // Actualiza la gráfica con la utilidad del mes actual
    }

    private fun actualizarGrafica(sumaPorMes: Map<Int, Double>) {
        val entries = mutableListOf<Entry>()
        var valorMaximo = 0f

        // Crear las entradas y encontrar el valor máximo
        for ((mes, valor) in sumaPorMes) {
            val valorInsumos = valor.toFloat()
            entries.add(Entry(mes.toFloat(), valorInsumos))
            if (valorInsumos > valorMaximo) {
                valorMaximo = valorInsumos
            }
        }

        val dataSet = LineDataSet(entries, "Utilidad por Mes").apply {
            color = Color.parseColor("#0071CE") // Color de la línea en hexadecimal
            valueTextColor = Color.BLACK // Color del texto de los valores
            valueTextSize = 10f // Tamaño del texto de los valores
        }

        val lineData = LineData(dataSet)
        lineChart!!.data = lineData

        // Ajustar el valor máximo del eje Y con un margen adicional
        val yAxis = lineChart!!.axisLeft
        yAxis.axisMaximum = valorMaximo + valorMaximo * 0.1f // Añadir un 10% al valor máximo

        lineChart!!.invalidate() // Refresca la gráfica
    }
    private fun setupChart() {
        lineChart?.let { chart ->
            val description = Description().apply {
                text = "Insumos por Mes"
            }
            chart.description = description

            // Etiquetas para los meses
            val xValues = listOf("Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic")
            val xAxis = chart.xAxis
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.valueFormatter = IndexAxisValueFormatter(xValues)
            xAxis.labelCount = 12
            xAxis.granularity = 1f

            val yAxis = chart.axisLeft
            yAxis.axisLineWidth = 2f
            yAxis.axisLineColor = Color.BLACK
            yAxis.setLabelCount(10, true)

            chart.axisRight.isEnabled = false // Desactiva el eje derecho

            chart.description.isEnabled = false
            chart.legend.isEnabled = true
        }
    }
}