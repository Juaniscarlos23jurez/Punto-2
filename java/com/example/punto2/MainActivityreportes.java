package com.example.punto2;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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

public class MainActivityreportes extends AppCompatActivity {
    Button stock, report, adeu,Utilidad1;
    private LineChart lineChart;

    private List<String> xValues;
    private double totalGastos = 0.0, sumaTotalSueldo = 0.0, sumaTotal = 0.0 ,suel = 0.0,venta=0.0,totla=0.0;
    FirebaseUser user;
    FirebaseAuth auth;
    DatabaseReference infoVentas, infoAlmacen,infodatagasos,infodatasueldos;
    private NativeAd nativeAd;
    NativeAdView adView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_activityreportes2);

        MobileAds.initialize(this, initializationStatus -> {});
          adView = findViewById(R.id.nativeAdView);

        loadNativeAd();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        if (user != null) {
            infodatagasos = FirebaseDatabase.getInstance().getReference("/MI data base Usuarios/" + user.getUid() + "/gastos_costos");
            sumar_costos1();
            infoVentas = FirebaseDatabase.getInstance().getReference("/MI data base Usuarios/" + user.getUid() + "/ventaVerificada");
            sumarPreciosDeVenta();
            infoAlmacen= FirebaseDatabase.getInstance().getReference("/MI data base Usuarios/" + user.getUid() + "/insumos_verificados");
            sumarStok();
            infodatasueldos= FirebaseDatabase.getInstance().getReference("/MI data base Usuarios/" + user.getUid() + "/Empleados");
            sumar_sueldos1();
        }
        setupChart();
        stock = findViewById(R.id.Producto);
        report=findViewById(R.id.Proveedor);
        adeu=findViewById(R.id.Cliente);
        Utilidad1=findViewById(R.id.Utilidad);
        Utilidad1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivityreportes.this,  MainActivity5.class));

            }
        });
        adeu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivityreportes.this,  adeudo.class));

            }
        });
        report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivityreportes.this,  descarga.class));

            }
        });
        stock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivityreportes.this,  punto.class));

            }
        });
    }
    private void sumar_sueldos1() {
        infodatasueldos.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                sumaTotalSueldo = 0.0;
                totla =0.0;
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

                            // Corregir la suma para evitar repetición de sueldos
                            double sumaActual = sumaPorMes.get(mesPago);
                            double sumaFactorizada = sueldo * factor; // Sumar sueldo según el factor calculado

                            sumaPorMes.put(mesPago, sumaActual + sumaFactorizada);
                            totla+= sumaFactorizada;
                            // Sumar al total general de sueldos
                            sumaTotalSueldo =totla+ sueldo;
                        }
                    }
                }

                calcularUtilidad(sumaPorMes);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Manejo de error
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
                calcularUtilidad(sumaPorMes);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Manejo de error
            }
        });
    }
    private void sumarPreciosDeVenta() {
        infoVentas.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map<Integer, Double> sumaPorMes = new HashMap<>();

                venta = 0.0; // Inicializar el total de ventas

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Double precioVenta = snapshot.child("precio_venta").getValue(Double.class);
                    Double cantidad = snapshot.child("cantidad").getValue(Double.class);
                    Integer mes = snapshot.child("mes").getValue(Integer.class);

                    if (precioVenta != null && cantidad != null && mes != null) {
                        double subtotal = precioVenta * cantidad;

                        venta += subtotal; // Suma al total general

                        // Ajusta el mes restando 1 para coincidir con la indexación de Calendar
                        mes = mes - 1;

                        if (sumaPorMes.containsKey(mes)) {
                            sumaPorMes.put(mes, sumaPorMes.get(mes) + venta);
                        } else {
                            sumaPorMes.put(mes, subtotal);
                        }
                    }
                }

                // Llamar a calcularUtilidad() después de que se han procesado todas las ventas
                calcularUtilidad(sumaPorMes);

                // Actualizar la gráfica con los valores calculados
             }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Manejo de error en caso de falla en la lectura de datos
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
                calcularUtilidad(sumaPorMes);  // Llama a calcular la utilidad después de sumar precios de venta

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
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
    private void calcularUtilidad(Map<Integer, Double> sumaPorMes) {
        double utilidad = venta - (sumaTotalSueldo + totalGastos + sumaTotal);
        int mesActual = Calendar.getInstance().get(Calendar.MONTH) + 1; // +1 para obtener el mes de 1 a 12
        sumaPorMes.put(mesActual, utilidad);

        actualizarGrafica(sumaPorMes);  // Actualiza la gráfica con la utilidad del mes actual
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

        LineDataSet dataSet = new LineDataSet(entries, "Utilidad por Mes");
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
        lineChart = findViewById(R.id.Grafica);
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
    private void loadNativeAd() {
        AdLoader adLoader = new AdLoader.Builder(this, "ca-app-pub-3940256099942544/2247696110") // Este es el ID de prueba
                .forNativeAd(nativeAd -> {
                    // Aquí se maneja el anuncio nativo
                    if (isDestroyed()) {
                        nativeAd.destroy();
                        return;
                    }
                    if (this.nativeAd != null) {
                        this.nativeAd.destroy();
                    }
                    this.nativeAd = nativeAd;
                    NativeAdView adView = findViewById(R.id.nativeAdView);
                    populateNativeAdView(nativeAd, adView);
                })
                .build();

        adLoader.loadAd(new AdRequest.Builder().build());
    }

    private void populateNativeAdView(NativeAd nativeAd, NativeAdView adView) {
        // Asignar los elementos del anuncio a los componentes de la vista
        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setMediaView(adView.findViewById(R.id.ad_media));
        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));

        // Mostrar el título
        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());

        // Mostrar la imagen o video principal
        if (nativeAd.getMediaContent() != null) {
            adView.getMediaView().setMediaContent(nativeAd.getMediaContent());
        }

        // Mostrar el botón de llamada a la acción
        if (nativeAd.getCallToAction() != null) {
            adView.getCallToActionView().setVisibility(View.VISIBLE);
            adView.getCallToActionView().setBackgroundColor(0000);

            ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
        } else {
            adView.getCallToActionView().setVisibility(View.INVISIBLE);
        }

        // Establecer el NativeAd en NativeAdView
        adView.setNativeAd(nativeAd);
    }

    @Override
    protected void onDestroy() {
        if (nativeAd != null) {
            nativeAd.destroy();
        }
        super.onDestroy();
    }
}
