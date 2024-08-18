package com.example.punto2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.punto2.datos.stok;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

public class punto extends AppCompatActivity {
 TextView Ventas,valorAlmacen,infogastos,informcion4;
    FirebaseUser user;
    FirebaseAuth auth;
    DatabaseReference infoVentas, infoAlmacen,infodatagasos,infodatasueldos;
    Button pdf, excel;
    private LineChart lineChart;
    private List<String> xValues;
    private LineChart lineChart1;
    private List<String> xValues1;
    private LineChart lineChart3;
    private List<String> xValues3;
    private double totalGastos = 0.0, sumaTotalSueldo = 0.0, sumaTotal = 0.0 ,suel = 0.0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_punto);
       Ventas= findViewById(R.id.informcion2);
        valorAlmacen = findViewById(R.id.precio21);
        infogastos=findViewById(R.id.informcion3);
        informcion4=findViewById(R.id.informcion4);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        if (user != null) {
            infodatagasos = FirebaseDatabase.getInstance().getReference("/MI data base Usuarios/" + user.getUid() + "/gastos_costos");
            sumar_costos();
            infoVentas = FirebaseDatabase.getInstance().getReference("/MI data base Usuarios/" + user.getUid() + "/ventaVerificada");
            sumarPreciosDeVenta();
            infoAlmacen= FirebaseDatabase.getInstance().getReference("/MI data base Usuarios/" + user.getUid() + "/insumos_verificados");
            sumarStok();
            infodatasueldos= FirebaseDatabase.getInstance().getReference("/MI data base Usuarios/" + user.getUid() + "/Empleados");
            sumar_sueldo();
        }

        sumar_sueldos1();
          sumar_costos1();
        setupChart( );
        setupChart2( );
        setupChart3( );

        /////////
        pdf = findViewById(R.id.button5);
        pdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                generatePdf();
            }
        });
        excel=findViewById(R.id.excel);
        excel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                datosexel( );
            }
        });
    }
    private void sumar_sueldo() {
        infodatasueldos.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                sumaTotalSueldo = 0.0;
                Calendar calendar = Calendar.getInstance();
                int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
                int currentMonth = calendar.get(Calendar.MONTH); // Enero = 0, Febrero = 1, etc.
                int currentYear = calendar.get(Calendar.YEAR);

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Double sueldo = snapshot.child("sueldo").getValue(Double.class);
                    String tiempo = snapshot.child("tiempo").getValue(String.class);
                    String fechaPrimerPago = snapshot.child("prime_pago").getValue(String.class);
                    suel=sueldo;
                    if (sueldo != null && tiempo != null && fechaPrimerPago != null) {
                        int factor = 0;

                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                            Date fechaPago = sdf.parse(fechaPrimerPago);
                            Calendar fechaCal = Calendar.getInstance();
                            fechaCal.setTime(fechaPago);

                            int mesPago = fechaCal.get(Calendar.MONTH);
                            int diaPago = fechaCal.get(Calendar.DAY_OF_MONTH);
                            int yearPago = fechaCal.get(Calendar.YEAR);

                            if (mesPago == currentMonth && yearPago == currentYear) {
                                switch (tiempo) {
                                    case "Diario":
                                        factor = dayOfMonth - diaPago + 1; // Días desde el primer pago hasta hoy
                                        break;
                                    case "Semanal":
                                        // Calcula el número de semanas completas desde la fecha de primer pago hasta hoy
                                        int semanasTranscurridas = (dayOfMonth - diaPago) / 7;
                                        factor = semanasTranscurridas + 1; // Incluye la semana actual
                                        break;
                                    case "Mensual":
                                        factor = 1; // Una vez al mes
                                        break;
                                    case "Trimestral":
                                    case "Semestral":
                                    case "Anual":
                                        factor = 1; // Considera una vez en el mes actual
                                        break;
                                    case "Unico":
                                        factor = 1; // Solo se suma una vez
                                        break;
                                }
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        // Asegúrate de que el factor sea al menos 1 si se trata de un sueldo único
                        factor = Math.max(factor, 1);
                        sumaTotalSueldo += sueldo * factor;
                    }
                }
                // Muestra la suma total de los sueld
                Double TOTALFI=sumaTotalSueldo+suel;
                informcion4.setText("$" + TOTALFI);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                informcion4.setText("Error al obtener datos");
            }
        });
    }
    private void sumar_costos() {
        infodatagasos.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                double sumaTotal = 0.0;
                Calendar calendar = Calendar.getInstance();
                int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
                int currentMonth = calendar.get(Calendar.MONTH); // Enero = 0, Febrero = 1, etc.
                int currentYear = calendar.get(Calendar.YEAR);
                 int mesActual = calendar.get(Calendar.MONTH) + 1; // Los meses en Calendar empiezan desde 0, por eso sumamos 1

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Double monto = snapshot.child("monto").getValue(Double.class);
                    String tiempo = snapshot.child("tiempo").getValue(String.class);
                    String fecha = snapshot.child("fecha").getValue(String.class);
                    Integer mes = snapshot.child("mes").getValue(Integer.class); // Obtener el mes como int

                    if (monto != null && tiempo != null && fecha != null) {
                        int factor = 0;
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

                        try {
                            Date fechaPago = sdf.parse(fecha);
                            Calendar fechaCal = Calendar.getInstance();
                            fechaCal.setTime(fechaPago);

                            int monthPago = fechaCal.get(Calendar.MONTH);
                            int yearPago = fechaCal.get(Calendar.YEAR);

                            // Verificar si el gasto corresponde al mes y año actuales
                            if (yearPago == currentYear && monthPago == currentMonth) {
                                factor = 1; // Cuenta el gasto inicial

                                // Ajustar el factor dependiendo del tipo de tiempo
                                switch (tiempo) {
                                    case "Diario":
                                        factor += dayOfMonth - 1; // Se repite todos los días del mes, suma 1 para incluir el gasto inicial
                                        break;
                                    case "Semanal":
                                        factor += (int) Math.ceil(dayOfMonth / 7.0) - 1; // Se repite cada semana, resta 1 porque el gasto inicial ya se sumó
                                        break;
                                    case "Mensual":
                                        factor = 1; // Se repite una vez al mes, ya incluido
                                        break;
                                    case "Trimestral":
                                    case "Semestral":
                                    case "Anual":
                                        factor = 1; // Solo se suma una vez en el mes actual
                                        break;
                                    case "Unico":
                                        factor = 1; // Solo se suma una vez, ya incluido
                                        break;
                                }
                                if (mes == mesActual) {
                                    sumaTotal += monto * factor;
                                }
                             }

                        } catch (ParseException e) {
                            e.printStackTrace();
                            infogastos.setText("Error al parsear la fecha");
                            return;
                        }
                    }
                }
                infogastos.setText("$" + sumaTotal);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                infogastos.setText("Error al obtener datos");
            }
        });
    }
    private void sumarPreciosDeVenta() {
        infoVentas.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                double sumaTotal = 0.0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Double precioVenta = snapshot.child("precio_venta").getValue(Double.class);
                    Double cantidad = snapshot.child("cantidad").getValue(Double.class);

                    if (precioVenta != null) {
                        sumaTotal += precioVenta * cantidad;
                    }
                }
                Ventas.setText("$" + sumaTotal);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Manejo de error en caso de falla en la lectura de datos
                Ventas.setText("Error al obtener datos");
            }
        });
    }
     private void sumarStok() {
        infoAlmacen.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map<Integer, Double> sumaPorMes = new HashMap<>();
                sumaTotal = 0.0;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Double precioVenta = snapshot.child("precioCompra").getValue(Double.class);
                    Double cantidad = snapshot.child("cantidad").getValue(Double.class);
                    Integer mes = snapshot.child("mes").getValue(Integer.class); // Obtener el mes como int

                    if (precioVenta != null && mes != null && cantidad != null) {
                        double subtotal = precioVenta * cantidad;
                        sumaTotal += subtotal; // Suma al total general

                        // Ajusta el mes restando 1 para coincidir con la indexación de Calendar
                        mes = mes - 1;

                        if (sumaPorMes.containsKey(mes)) {
                            sumaPorMes.put(mes, sumaPorMes.get(mes) + subtotal);

                            sumaTotal +=  precioVenta * cantidad;
                        } else {
                            sumaPorMes.put(mes, subtotal);
                        }
                    }
                }
                valorAlmacen.setText("$" + sumaTotal);
                actualizarGrafica(sumaPorMes);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                valorAlmacen.setText("Error al obtener datos");
            }
        });
    }
    private void sumar_costos1() {
        infodatagasos.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map<Integer, Double> sumaPorMes = new HashMap<>();
                totalGastos = 0.0;
                Calendar calendar = Calendar.getInstance();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Double monto = snapshot.child("monto").getValue(Double.class);
                    String tiempo = snapshot.child("tiempo").getValue(String.class);
                    String fecha = snapshot.child("fecha").getValue(String.class);
                    Integer mes = snapshot.child("mes").getValue(Integer.class);

                    if (monto != null && tiempo != null && fecha != null && mes != null) {
                        double subtotal = monto ;
                        totalGastos += subtotal; // Suma al total general

                        // Ajusta el mes restando 1 para coincidir con la indexación de Calendar
                        mes = mes - 1;

                        if (sumaPorMes.containsKey(mes)) {
                            sumaPorMes.put(mes, sumaPorMes.get(mes) + subtotal);
                        } else {
                            sumaPorMes.put(mes, subtotal);
                        }
                    }
                }
                actualizarGrafica2(sumaPorMes);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Manejo de error
            }
        });
    }


    private void actualizarGrafica2(Map<Integer, Double> sumaPorMes) {
        List<Entry> entries = new ArrayList<>();
        float valorMaximo = 0f;

        // Crear las entradas y encontrar el valor máximo
        for (Map.Entry<Integer, Double> entry : sumaPorMes.entrySet()) {
            int mes = entry.getKey();
            float valorInsumos = entry.getValue().floatValue();

            entries.add(new Entry(mes, valorInsumos));
            if (valorInsumos > valorMaximo) {
                valorMaximo = valorInsumos;
            }
        }

        LineDataSet dataSet = new LineDataSet(entries, "Gastos por Mes");
        dataSet.setColor(Color.parseColor("#CB6CE6")); // Color de la línea en hexadecimal
        dataSet.setValueTextColor(Color.BLACK); // Color del texto de los valores
        dataSet.setValueTextSize(10f); // Tamaño del texto de los valores

        LineData lineData = new LineData(dataSet);
        lineChart1.setData(lineData);

        // Ajustar el valor máximo del eje Y con un margen adicional
        YAxis yAxis = lineChart1.getAxisLeft();
        yAxis.setAxisMaximum(valorMaximo + valorMaximo * 0.1f); // Añadir un 10% al valor máximo

        lineChart1.invalidate(); // Refresca la gráfica
    }

    private void actualizarGrafica(Map<Integer, Double> sumaPorMes) {
        List<Entry> entries = new ArrayList<>();
        float valorMaximo = 0f;

        // Crear las entradas y encontrar el valor máximo
        for (Map.Entry<Integer, Double> entry : sumaPorMes.entrySet()) {
            int mes = entry.getKey();
            float valorInsumos = entry.getValue().floatValue();

            entries.add(new Entry(mes, valorInsumos));
            if (valorInsumos > valorMaximo) {
                valorMaximo = valorInsumos;
            }
        }

        LineDataSet dataSet = new LineDataSet(entries, "Insumos por Mes");
        dataSet.setColor(Color.parseColor("#0071CE")); // Color de la línea en hexadecimal
        dataSet.setValueTextColor(Color.BLACK); // Color del texto de los valores
        dataSet.setValueTextSize(10f); // Tamaño del texto de los valores

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);

        // Ajustar el valor máximo del eje Y con un margen adicional
        YAxis yAxis = lineChart.getAxisLeft();
        yAxis.setAxisMaximum(valorMaximo + valorMaximo * 0.1f); // Añadir un 10% al valor máximo

        lineChart.invalidate(); // Refresca la gráfica
    }
    private void setupChart() {
        lineChart = findViewById(R.id.Grafica2);
        Description description = new Description();
        description.setText("Insumos por Mes");
        lineChart.setDescription(description);

        // Etiquetas para los meses
        xValues = Arrays.asList("Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic");
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xValues));
        xAxis.setLabelCount(12);
        xAxis.setGranularity(1F);

        YAxis yAxis = lineChart.getAxisLeft();
        yAxis.setAxisLineWidth(2f);
        yAxis.setAxisLineColor(android.R.color.black);
        yAxis.setLabelCount(10);

        lineChart.getAxisRight().setEnabled(false); // Desactiva el eje derecho

        lineChart.getDescription().setEnabled(false);
        lineChart.getLegend().setEnabled(true);
    }
    private void setupChart2() {
        lineChart1 = findViewById(R.id.Grafica);
        Description description = new Description();
        description.setText("Gastos por Mes");
        lineChart1.setDescription(description);

        // Etiquetas para los meses
        xValues1 = Arrays.asList("Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic");
        XAxis xAxis = lineChart1.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xValues1));
        xAxis.setLabelCount(12);
        xAxis.setGranularity(1F);

        YAxis yAxis = lineChart1.getAxisLeft();
        yAxis.setAxisLineWidth(2f);
        yAxis.setAxisLineColor(android.R.color.black);
        yAxis.setLabelCount(10);

        lineChart1.getAxisRight().setEnabled(false); // Desactiva el eje derecho

        lineChart1.getDescription().setEnabled(false);
        lineChart1.getLegend().setEnabled(true);
    }
    private void sumar_sueldos1() {
        infodatasueldos.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map<Integer, Double> sumaPorMes = new HashMap<>();
                // Inicializar el mapa con 0 para cada mes
                for (int i = 1; i <= 12; i++) {
                    sumaPorMes.put(i, 0.0);
                }

                Calendar calendar = Calendar.getInstance();
                int currentMonth = calendar.get(Calendar.MONTH) + 1; // Mes actual (1 a 12)

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Double sueldo = snapshot.child("sueldo").getValue(Double.class);
                    String tiempo = snapshot.child("tiempo").getValue(String.class);
                    String fechaPrimerPago = snapshot.child("prime_pago").getValue(String.class);

                    if (sueldo != null && tiempo != null && fechaPrimerPago != null) {
                        int mesPago = obtenerMesDeFecha(fechaPrimerPago);
                        if (mesPago != -1 && mesPago <= currentMonth) {
                            int factor = calcularFactor(fechaPrimerPago, tiempo, mesPago);
                            double sumaActual = sumaPorMes.get(mesPago);
                            Double Totalfinal1=(sueldo * factor)+sueldo;
                            sumaPorMes.put(mesPago, sumaActual + Totalfinal1);
                        }
                    }
                }
                actualizarGrafica3(sumaPorMes);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Manejo de error
            }
        });
    }
    private int obtenerMesDeFecha(String fecha) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date fechaDate = sdf.parse(fecha);
            Calendar cal = Calendar.getInstance();
            cal.setTime(fechaDate);
            return cal.get(Calendar.MONTH) + 1; // Los meses en Calendar comienzan en 0
        } catch (ParseException e) {
            e.printStackTrace();
            return -1; // Valor de error
        }
    }

    private int calcularFactor(String fechaPrimerPago, String tiempo, int mesPago) {
        Calendar calendar = Calendar.getInstance();
        int currentMonth = calendar.get(Calendar.MONTH) + 1; // Mes actual (1 a 12)

        switch (tiempo) {
            case "Diario":
                return 30; // Asumiendo 30 días en un mes
            case "Semanal":
                if (mesPago == currentMonth) {
                    // Calcula las semanas desde el primer pago hasta el final del mes actual
                    Calendar fechaCal = Calendar.getInstance();
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        fechaCal.setTime(sdf.parse(fechaPrimerPago));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    int diaPago = fechaCal.get(Calendar.DAY_OF_MONTH);
                    int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
                    return (int) Math.ceil((dayOfMonth - diaPago + 1) / 7.0); // Número de semanas transcurridas
                }
                return 4; // Asumiendo 4 semanas en un mes
            case "Mensual":
            case "Trimestral":
            case "Semestral":
            case "Anual":
            case "Unico":
                return 1;
            default:
                return 1;
        }
    }
    private void actualizarGrafica3(Map<Integer, Double> sumaPorMes) {
        List<Entry> entries = new ArrayList<>();
        float valorMaximo = 0f;

        for (int mes = 1; mes <= 12; mes++) {
            float valorSueldos = sumaPorMes.get(mes).floatValue();
            entries.add(new Entry(mes - 1, valorSueldos)); // Restar 1 porque los índices de la gráfica comienzan en 0
            if (valorSueldos > valorMaximo) {
                valorMaximo = valorSueldos;
            }
        }

        LineDataSet dataSet = new LineDataSet(entries, "Sueldos por Mes");
        dataSet.setColor(Color.parseColor("#4D4ED9")); // Color de la línea en hexadecimal
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(10f);

        LineData lineData = new LineData(dataSet);
        lineChart3.setData(lineData);

        YAxis yAxis = lineChart3.getAxisLeft();
        yAxis.setAxisMaximum(valorMaximo + valorMaximo * 0.1f);

        lineChart3.invalidate();
    }
    private void setupChart3() {
        lineChart3 = findViewById(R.id.Grafica3);
        Description description = new Description();
        description.setText("Sueldos por Mes");
        lineChart3.setDescription(description);

        List<String> xValues3 = Arrays.asList("Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic");
        XAxis xAxis = lineChart3.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xValues3));
        xAxis.setLabelCount(12);
        xAxis.setGranularity(1F);

        YAxis yAxis = lineChart3.getAxisLeft();
        yAxis.setAxisLineWidth(2f);
        yAxis.setAxisLineColor(Color.BLACK);
        yAxis.setLabelCount(10);

        lineChart3.getAxisRight().setEnabled(false);
        lineChart3.getDescription().setEnabled(true);
        lineChart3.getLegend().setEnabled(true);
    }
    private void datosexel() {
        auth = FirebaseAuth.getInstance();

        user = auth.getCurrentUser();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("/MI data base Usuarios/" + user.getUid() + "/stok");


        // Obtener las ventas en el rango de fechas
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<stok> AlmacenList = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    stok Almacen = snapshot.getValue(stok.class);

                    AlmacenList.add(Almacen);


                }
                createSimpleExcel( AlmacenList);
                // Generar el PDF con las ventas obtenidas
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(punto.this, "Error al obtener datos de ventas", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void createSimpleExcel(ArrayList<stok> almacenList)
    {
        // Obtener la ruta de la carpeta de descargas
        File downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(downloadsFolder, "reporte_Almacen.xls");

        try {
            WritableWorkbook workbook = Workbook.createWorkbook(file);
            WritableSheet sheet = workbook.createSheet("Reporte", 0);

            // Crear encabezados de columnas
            sheet.addCell(new Label(0, 0, "Tipo"));
            sheet.addCell(new Label(1, 0, "Nombre"));
            sheet.addCell(new Label(2, 0, "Cantidad"));
            sheet.addCell(new Label(3, 0, "Código"));
            sheet.addCell(new Label(4, 0, "Fecha"));
            sheet.addCell(new Label(5, 0, "Precio Compra"));
            sheet.addCell(new Label(6, 0, "Precio Venta"));
            sheet.addCell(new Label(7, 0, "Total Compra"));
            sheet.addCell(new Label(8, 0, "Total Venta"));

            int row = 1;

            double totalCompraSum = 0.0;
            double totalVentaSum = 0.0;

            // Añadir datos de ventas
            for (stok venta : almacenList) {
                sheet.addCell(new Label(0, row, "Venta"));
                sheet.addCell(new Label(1, row, venta.getNombre()));
                sheet.addCell(new Number(2, row, venta.getCantidad()));
                sheet.addCell(new Label(3, row, venta.getCodigo()));
                sheet.addCell(new Label(4, row, venta.getHora()));
                sheet.addCell(new Number(5, row, venta.getPrecio_compra()));
                sheet.addCell(new Number(6, row, venta.getPrecio_venta()));

                double cantidad = venta.getCantidad();
                double precioCompra = venta.getPrecio_compra();
                double precioVenta = venta.getPrecio_venta();

                totalCompraSum += cantidad * precioCompra;
                totalVentaSum += cantidad * precioVenta;

                sheet.addCell(new Number(7, row, cantidad * precioCompra));
                sheet.addCell(new Number(8, row, cantidad * precioVenta));

                row++;
            }

            // Añadir totales
            sheet.addCell(new Label(0, row, "Total"));
            sheet.addCell(new Number(7, row, totalCompraSum));
            sheet.addCell(new Number(8, row, totalVentaSum));

            // Añadir datos de stock



            workbook.write();
            workbook.close();
            Toast.makeText(this, "Excel guardado en: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (IOException | WriteException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al guardar Excel: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    private void generatePdf() {
        // Referencia a la base de datos de Firebase
        auth = FirebaseAuth.getInstance();

        user = auth.getCurrentUser();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("/MI data base Usuarios/" + user.getUid() + "/stok");


        // Obtener las ventas en el rango de fechas
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<stok> AlmacenList = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    stok Almacen = snapshot.getValue(stok.class);

                    AlmacenList.add(Almacen);


                }
                createPdfWithSales( AlmacenList);
                // Generar el PDF con las ventas obtenidas
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(punto.this, "Error al obtener datos de ventas", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void createPdfWithSales(ArrayList<stok> almacenList) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View reportView = inflater.inflate(R.layout.reporte_almacen, null);

        // Configurar el encabezado del reporte
        TextView reportHeader = reportView.findViewById(R.id.Titulo);
        String headerText = "Reporte Inventario " ;
        reportHeader.setText(headerText);

        // Obtener el contenedor de ventas
        LinearLayout salesContainer = reportView.findViewById(R.id.report_sales_container);

        // Añadir los datos de ventas al contenedor
        double totalCompraSum = 0.0;
        double totalVentaSum = 0.0;

// Recorre la lista de ventas
        for (stok venta : almacenList) {
            // Inflar el diseño del item de venta
            View salesItemView = inflater.inflate(R.layout.almacen_item_layout, null);

            // Obtener referencias a los TextView dentro del item de venta
            TextView nameTextView = salesItemView.findViewById(R.id.sales_item_name);
            TextView quantityTextView = salesItemView.findViewById(R.id.sales_item_quantity);
            TextView codeTextView = salesItemView.findViewById(R.id.sales_item_code);
            TextView dateTextView = salesItemView.findViewById(R.id.sales_item_date);
            TextView purchasePriceTextView = salesItemView.findViewById(R.id.sales_item_purchase_price);
            TextView salePriceTextView = salesItemView.findViewById(R.id.sales_item_sale_price);

            // Configurar el texto y el color para cada TextView
            nameTextView.setText(venta.getNombre());
            nameTextView.setTextColor(Color.BLACK);

            quantityTextView.setText(String.valueOf(venta.getCantidad()));
            quantityTextView.setTextColor(Color.BLACK);

            codeTextView.setText(venta.getCodigo());
            codeTextView.setTextColor(Color.BLACK);

            dateTextView.setText(venta.getHora());
            dateTextView.setTextColor(Color.BLACK);

            purchasePriceTextView.setText(String.valueOf(venta.getPrecio_compra()));
            purchasePriceTextView.setTextColor(Color.BLACK);

            salePriceTextView.setText(String.valueOf(venta.getPrecio_venta()));
            salePriceTextView.setTextColor(Color.BLACK);

            // Calcular y acumular los totales
            double cantidad = venta.getCantidad();
            double precioCompra = venta.getPrecio_compra();
            double precioVenta = venta.getPrecio_venta();

            totalCompraSum += cantidad * precioCompra;
            totalVentaSum += cantidad * precioVenta;

            // Añadir el View del item de venta al contenedor
            salesContainer.addView(salesItemView);
        }

// Inflar el diseño para mostrar los totales
        View totalesView = inflater.inflate(R.layout.vemtas_total_layout, null);

// Obtener referencias a los TextView de totales
        TextView totalCompraTextView = totalesView.findViewById(R.id.totalCompra);
        TextView totalVentaTextView = totalesView.findViewById(R.id.TotalVenta);
        TextView totalTextView = totalesView.findViewById(R.id.Total);

// Configurar los valores calculados en los TextView de totales
        totalCompraTextView.setText(String.format("Total Compra: %.2f", totalCompraSum));
        totalCompraTextView.setTextColor(Color.BLACK);
        totalVentaTextView.setText(String.format("Total Venta: %.2f", totalVentaSum));
        totalVentaTextView.setTextColor(Color.BLACK);
        totalTextView.setText(String.format("Total: %.2f", totalVentaSum - totalCompraSum));
        totalTextView.setTextColor(Color.BLACK);

// Añadir el View de totales al contenedor
        salesContainer.addView(totalesView);
        // Crear un bitmap del View inflado
        int specWidth = View.MeasureSpec.makeMeasureSpec(1595, View.MeasureSpec.EXACTLY);  // A4 width in points
        int specHeight = View.MeasureSpec.makeMeasureSpec(1100, View.MeasureSpec.UNSPECIFIED);
        reportView.measure(specWidth, specHeight);
        int measuredWidth = reportView.getMeasuredWidth();
        int measuredHeight = reportView.getMeasuredHeight();
        reportView.layout(0, 0, measuredWidth, measuredHeight);

        Bitmap bitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);  // Establecer el color de fondo
        reportView.draw(canvas);

        // Crear un nuevo documento PDF
        PdfDocument pdfDocument = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(measuredWidth, measuredHeight, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);

        // Dibujar el bitmap en el PDF
        Paint bitmapPaint = new Paint();
        bitmapPaint.setAlpha(255);  // Asegurarse de que el bitmap sea completamente opaco
        page.getCanvas().drawBitmap(bitmap, 0, 0, bitmapPaint);

        // Terminar la página actual
        pdfDocument.finishPage(page);

        // Formatear las fechas para el nombre del archivo

        // Guardar el PDF en la carpeta de descargas
        File downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File filePath = new File(downloadsFolder, "reporte_Inventario.pdf");

        try {
            // Asegurarse de que la página esté terminada antes de escribir el archivo
            pdfDocument.writeTo(new FileOutputStream(filePath));
            Toast.makeText(this, "PDF guardado en: " + filePath.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al guardar PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            // Cerrar el documento
            pdfDocument.close();
        }
    }

}