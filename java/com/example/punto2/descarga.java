package com.example.punto2;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.punto2.datos.Transacciones;
import com.example.punto2.datos.Venta;
import com.example.punto2.datos.cambioDeProductos;
import com.example.punto2.datos.empleadojava;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

public class descarga extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 100;
    private long startDate = -1;
    private long endDate = -1;
    FirebaseUser user;
    FirebaseAuth auth;
    private RewardedAd mRewardedAd;
    private boolean adLoaded = false;
    File downloadsFolder;
    File filePath;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_descarga);
        CalendarView calendarView1 = findViewById(R.id.calendarView1);
        CalendarView calendarView2 = findViewById(R.id.calendarView2);
        Button buttonPdf = findViewById(R.id.button5);
        Button bottonExcel = findViewById(R.id.button6);
        calendarView1.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            startDate = new Date(year - 1900, month, dayOfMonth).getTime();
        });
        calendarView2.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            endDate = new Date(year - 1900, month, dayOfMonth).getTime();
        });

        MobileAds.initialize(this, initializationStatus -> {});
        loadRewardedAd();

        // Load an interstitial ad
         buttonPdf.setOnClickListener(v -> {

            if (startDate == -1 || endDate == -1) {
                Toast.makeText(this, "Por favor selecciona ambas fechas", Toast.LENGTH_SHORT).show();
                return;
            }
            if (startDate > endDate) {
                Toast.makeText(this, "La fecha de inicio no puede ser después de la fecha de fin", Toast.LENGTH_SHORT).show();
                return;
            }

             if (adLoaded) {
                 showRewardedAd();
             } else {
                 Toast.makeText(this, "El anuncio no está listo aún", Toast.LENGTH_SHORT).show();
             }            // Solicitar permisos
            if (checkPermission()) {
                // Generar el PDF con el rango de fechas
                generatePdf(startDate, endDate);
            } else {
                requestPermission();
            }


         });
        bottonExcel.setOnClickListener(v -> {
            if (startDate == -1 || endDate == -1) {
                Toast.makeText(this, "Por favor selecciona ambas fechas", Toast.LENGTH_SHORT).show();
                return;
            }

            if (startDate > endDate) {
                Toast.makeText(this, "La fecha de inicio no puede ser después de la fecha de fin", Toast.LENGTH_SHORT).show();
                return;
            }

            // Solicitar permisos
            if (checkPermission()) {
                // Generar el Excel con el rango de fechas
//                generateExcel(startDate, endDate);
                datosexel(startDate, endDate);

            } else {
                requestPermission();
            }
        });

    }
    private void loadRewardedAd() {
        AdRequest adRequest = new AdRequest.Builder().build();

        RewardedAd.load(this, "ca-app-pub-3940256099942544/5224354917", adRequest, new RewardedAdLoadCallback() {
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                Log.d("DescargaActivity", "Ad failed to load: " + loadAdError.getMessage());
                mRewardedAd = null;
                adLoaded = false;
            }

            @Override
            public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                Log.d("DescargaActivity", "Ad was loaded.");
                mRewardedAd = rewardedAd;
                adLoaded = true;
            }
        });
    }
    private void showRewardedAd() {
        if (mRewardedAd != null) {
            mRewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    Log.d("DescargaActivity", "Ad was dismissed.");
                    // Recargar el anuncio
                    loadRewardedAd();
                }

                @Override
                public void onAdFailedToShowFullScreenContent(AdError adError) {
                    Log.d("DescargaActivity", "Ad failed to show.");
                }

                @Override
                public void onAdShowedFullScreenContent() {
                    Log.d("DescargaActivity", "Ad showed fullscreen content.");
                    mRewardedAd = null;
                    adLoaded = false;
                }
            });

            mRewardedAd.show(this, rewardItem -> {
                // El usuario ha ganado la recompensa, genera el PDF
                if (checkPermission()) {
                    generatePdf(startDate, endDate);
                } else {
                    requestPermission();
                }
            });
        } else {
            Log.d("DescargaActivity", "The rewarded ad wasn't ready yet.");
        }
    }
     private void generatePdf(long startDate, long endDate) {
        // Referencia a la base de datos de Firebase
        auth = FirebaseAuth.getInstance();

        user = auth.getCurrentUser();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("/MI data base Usuarios/" + user.getUid() + "/ventaVerificada");
        DatabaseReference databaseconsultastock = FirebaseDatabase.getInstance().getReference("/MI data base Usuarios/" + user.getUid() + "/cambioDeProductos");
        DatabaseReference databaseTransacion = FirebaseDatabase.getInstance().getReference("/MI data base Usuarios/" + user.getUid() + "/transacciones");

        // Convertir las fechas a formato String para comparar
        final SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault());
        final String startDateString = sdf.format(new Date(startDate));
        final String endDateString = sdf.format(new Date(endDate));

        // Obtener las ventas en el rango de fechas
         databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
             @Override
             public void onDataChange(DataSnapshot dataSnapshot) {
                 ArrayList<Venta> ventasList = new ArrayList<>();
                 for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                     Venta venta = snapshot.getValue(Venta.class);
                     if (venta != null) {
                         // Comparar fechas
                         String fechaVenta = venta.getFecha();
                         if (fechaVenta != null && fechaVenta.compareTo(startDateString) >= 0 && fechaVenta.compareTo(endDateString) <= 0) {
                             ventasList.add(venta);
                         }
                     }
                 }
                 databaseconsultastock.addValueEventListener(new ValueEventListener() {
                     @Override
                     public void onDataChange(@NonNull DataSnapshot snapshot) {
                         ArrayList<cambioDeProductos> Stoklist = new ArrayList<>();
                         for (DataSnapshot snapshot11 : snapshot.getChildren()) {
                             cambioDeProductos Stok = snapshot11.getValue(cambioDeProductos.class);
                             if (Stok != null) {
                                 // Comparar fechas
                                 String fechaVenta = Stok.getHora();
                                 if (fechaVenta != null && fechaVenta.compareTo(startDateString) >= 0 && fechaVenta.compareTo(endDateString) <= 0) {
                                     Stoklist.add(Stok);
                                 }
                             }
                         }
                         databaseTransacion.addValueEventListener(new ValueEventListener() {
                             @Override
                             public void onDataChange(@NonNull DataSnapshot snapshot) {
                                 ArrayList<Transacciones> Translist = new ArrayList<>();
                                 for (DataSnapshot snapshot11 : snapshot.getChildren()) {
                                     Transacciones Trans = snapshot11.getValue(Transacciones.class);
                                     if (Trans != null) {
                                         // Comparar fechas
                                         String fechaVenta = Trans.getFecha();
                                         if (fechaVenta != null && fechaVenta.compareTo(startDateString) >= 0 && fechaVenta.compareTo(endDateString) <= 0) {
                                             Translist.add(Trans);
                                         }
                                     }
                                 }
                                 createPdfWithSales(Translist, Stoklist, ventasList, startDate, endDate);
                             }

                             @Override
                             public void onCancelled(@NonNull DatabaseError error) {
                                 // Manejo de error
                             }
                         });
                     }

                     @Override
                     public void onCancelled(@NonNull DatabaseError error) {
                         // Manejo de error
                     }
                 });
             }

             @Override
             public void onCancelled(DatabaseError databaseError) {
                 Toast.makeText(descarga.this, "Error al obtener datos de ventas", Toast.LENGTH_SHORT).show();
             }
         });
    }
    @SuppressLint("MissingInflatedId")
    private void createPdfWithSales(ArrayList<Transacciones> Translist,ArrayList<cambioDeProductos> Stoklist,ArrayList<Venta> ventasList, long startDate, long endDate)
    {
        // Inflar el diseño XML
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View reportView = inflater.inflate(R.layout.pdf_report_layout, null);

        // Configurar el encabezado del reporte
        TextView reportHeader = reportView.findViewById(R.id.report_header);
        String headerText = "Reporte desde " + formatDate(startDate) + " hasta " + formatDate(endDate);
        reportHeader.setText(headerText);

        // Obtener el contenedor de ventas
        LinearLayout salesContainer = reportView.findViewById(R.id.report_sales_container);
        LinearLayout stokContainer = reportView.findViewById(R.id.reporte_STOK);
        LinearLayout TransContainer = reportView.findViewById(R.id.Reporte_Efecctivo);
        LinearLayout TotalVentas = reportView.findViewById(R.id.total_Ventas);


        // Añadir los datos de ventas al contenedor
        double totalCompraSum = 0.0;
        double totalVentaSum = 0.0;

// Recorre la lista de ventas
        for (Venta venta : ventasList) {
            // Inflar el diseño del item de venta
            View salesItemView = inflater.inflate(R.layout.sales_item_layout, null);

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

            dateTextView.setText(venta.getFecha());
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

        double TotalInvertido = 0.0;
        double TotalQuitados = 0.0;
        for (cambioDeProductos Stok : Stoklist) {
            // Inflar el diseño del item de venta
            View stokItemView = inflater.inflate(R.layout.sales_item_layout_stok, null);

            // Obtener referencias a los TextView
            TextView nameTextView = stokItemView.findViewById(R.id.sales_item_name);
            TextView quantityTextView = stokItemView.findViewById(R.id.sales_item_quantity);
            TextView codeTextView = stokItemView.findViewById(R.id.sales_item_code);
            TextView dateTextView = stokItemView.findViewById(R.id.sales_item_date);
            TextView purchasePriceTextView = stokItemView.findViewById(R.id.sales_item_purchase_price);
            TextView AnterpurchasePriceTextView = stokItemView.findViewById(R.id.Anterior);
            TextView salePriceTextView = stokItemView.findViewById(R.id.sales_item_sale_price);

            // Configurar el texto y el color para cada TextView
            nameTextView.setText(Stok.getNombre());
            nameTextView.setTextColor(Color.BLACK);

            quantityTextView.setText(String.valueOf(Stok.getResultCantidad()));
            quantityTextView.setTextColor(Color.BLACK);

            codeTextView.setText(Stok.getCodigo());
            codeTextView.setTextColor(Color.BLACK);

            dateTextView.setText(Stok.getHora());
            dateTextView.setTextColor(Color.BLACK);

            purchasePriceTextView.setText(String.valueOf(Stok.getPrecio_compra()));
            purchasePriceTextView.setTextColor(Color.BLACK);

            AnterpurchasePriceTextView.setText(String.valueOf(Stok.getAnterPrecio_compra()));
            AnterpurchasePriceTextView.setTextColor(Color.BLACK);

            salePriceTextView.setText(String.valueOf(Stok.getPrecio_venta()));
            salePriceTextView.setTextColor(Color.BLACK);
            // Calcular y acumular los totales
            double cantidad = Stok.getResultCantidad();
            double cantidadAnter = Stok.getAnterCantidad();

            double precioCompra = Stok.getPrecio_compra();
            double precioVenta = Stok.getPrecio_venta();


            TotalInvertido += cantidad * precioCompra;
            totalVentaSum += cantidad - cantidad;

            // Añadir el View del item de venta al contenedor
            stokContainer.addView(stokItemView);
        }
// Inflar el diseño para mostrar los totales
        View inversiTotal = inflater.inflate(R.layout.ingresos_total_layout, null);

// Obtener referencias a los TextView de totales
        TextView totalComprainversiTotal = inversiTotal.findViewById(R.id.totalAgregados);

// Configurar los valores calculados en los TextView de totales
        totalComprainversiTotal.setText(String.format("Total Compra: %.2f", TotalInvertido));
        totalComprainversiTotal.setTextColor(Color.BLACK);


// Añadir el View de totales al contenedor
        stokContainer.addView(inversiTotal);
        for (Transacciones ransacciones : Translist) {
            // Inflar el diseño del item de venta
            View transItemView = inflater.inflate(R.layout.sales_item_layout_tensacion, null);

            // Obtener referencias a los TextView
            TextView tipo = transItemView.findViewById(R.id.Tipo);
            TextView canidadInicio = transItemView.findViewById(R.id.Cantidad_Inicial);
            TextView Cantidad = transItemView.findViewById(R.id.Cantidad);
            TextView CantidadFinal = transItemView.findViewById(R.id.Cantidad_Final);
            TextView Fecha = transItemView.findViewById(R.id.Fecha);

            tipo.setText(ransacciones.getTipo());
            tipo.setTextColor(Color.BLACK);
            // Configurar el texto y el color para cada TextView
            canidadInicio.setText(String.valueOf(ransacciones.getAnterior()));
            canidadInicio.setTextColor(Color.BLACK);
            Cantidad.setText(String.valueOf(ransacciones.getCantidad()));
            Cantidad.setTextColor(Color.BLACK);
            CantidadFinal.setText(String.valueOf(ransacciones.getFinalTotal()));
            CantidadFinal.setTextColor(Color.BLACK);
            Fecha.setText(ransacciones.getFecha());
            Fecha.setTextColor(Color.BLACK);
            // Añadir el View del item de venta al contenedor
            TransContainer.addView(transItemView);
        }


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
        String formattedStartDate = formatDate(startDate);
        String formattedEndDate = formatDate(endDate);

        // Guardar el PDF en la carpeta de descargas

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            File downloadsFolder = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
            File filePath = new File(downloadsFolder, "reporte_" + formatDate(startDate) + "_a_" + formatDate(endDate)  + ".pdf");

            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                pdfDocument.writeTo(fos);
                Toast.makeText(this, "PDF guardado en: " + filePath.getAbsolutePath(), Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error al guardar PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            } finally {
                pdfDocument.close();
            }
        }
        else {
            File downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File filePath = new File(downloadsFolder, "reporte_" + formatDate(startDate) + "_a_" + formatDate(endDate)  + ".pdf");

            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                pdfDocument.writeTo(fos);
                Toast.makeText(this, "PDF guardado en: " + filePath.getAbsolutePath(), Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error al guardar PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            } finally {
                pdfDocument.close();
            }
        }

    }

    private void datosexel(long startDate, long endDate) {
        auth = FirebaseAuth.getInstance();

        user = auth.getCurrentUser();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("/MI data base Usuarios/" + user.getUid() + "/ventaVerificada");
        DatabaseReference databaseconsultastock = FirebaseDatabase.getInstance().getReference("/MI data base Usuarios/" + user.getUid() + "/cambioDeProductos");
        DatabaseReference databaseTransacion = FirebaseDatabase.getInstance().getReference("/MI data base Usuarios/" + user.getUid() + "/transacciones");
        DatabaseReference Sueldo = FirebaseDatabase.getInstance().getReference("/MI data base Usuarios/" + user.getUid() + "/Empleados");
        DatabaseReference Gastos = FirebaseDatabase.getInstance().getReference("/MI data base Usuarios/" + user.getUid() + "/gastos_costos");

        // Convertir las fechas a formato String para comparar
        final SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault());
        final String startDateString = sdf.format(new Date(startDate));
        final String endDateString = sdf.format(new Date(endDate));

        // Obtener las ventas en el rango de fechas
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<Venta> ventasList = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Venta venta = snapshot.getValue(Venta.class);
                    if (venta != null) {
                        // Comparar fechas
                        String fechaVenta = venta.getFecha();
                        if (fechaVenta.compareTo(startDateString) >= 0 && fechaVenta.compareTo(endDateString) <= 0) {
                            ventasList.add(venta);
                        }
                    }
                }
                databaseconsultastock.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ArrayList<cambioDeProductos> Stoklist = new ArrayList<>();
                        for (DataSnapshot snapshot11 : snapshot.getChildren()) {
                            cambioDeProductos Stok = snapshot11.getValue(cambioDeProductos.class);
                            if (Stok != null) {
                                // Comparar fechas
                                String fechaVenta = Stok.getHora();
                                if (fechaVenta.compareTo(startDateString) >= 0 && fechaVenta.compareTo(endDateString) <= 0) {
                                    Stoklist.add(Stok);
                                }
                            }
                        }
                        databaseTransacion.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                ArrayList<Transacciones> Translist = new ArrayList<>();
                                for (DataSnapshot snapshot11 : snapshot.getChildren()) {
                                    Transacciones Trans = snapshot11.getValue(Transacciones.class);
                                    if (Trans != null) {
                                        // Comparar fechas
                                        String fechaVenta = Trans.getFecha();
                                        if (fechaVenta.compareTo(startDateString) >= 0 && fechaVenta.compareTo(endDateString) <= 0) {
                                            Translist.add(Trans);
                                        }
                                    }
                                }
                                Sueldo.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        ArrayList<empleadojava> Empleadolist = new ArrayList<>();
                                        for (DataSnapshot snapshot11 : snapshot.getChildren()) {
                                            empleadojava Trans = snapshot11.getValue(empleadojava.class);
                                            //if (Trans != null) {
                                                // Comparar fechas
                                                // String fechaVenta = Trans.get();
                                                // if (fechaVenta.compareTo(startDateString) >= 0 && fechaVenta.compareTo(endDateString) <= 0) {
                                                    Empleadolist.add(Trans);
                                                // }
                                            //}
                                        }
                                        createSimpleExcel(Empleadolist,Translist, Stoklist, ventasList, startDate, endDate);

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                // Generar el PDF con las ventas obtenidas
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(descarga.this, "Error al obtener datos de ventas", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void createSimpleExcel(ArrayList<empleadojava> Empleadolist, ArrayList<Transacciones> transList, ArrayList<cambioDeProductos> stokList, ArrayList<Venta> ventasList, long startDate, long endDate)
    {


        // Obtener la ruta de la carpeta de descargas
        File downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(downloadsFolder, "reporte_" + formatDate(startDate) + "_a_" + formatDate(endDate) + ".xls");

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
            for (Venta venta : ventasList) {
                sheet.addCell(new Label(0, row, "Venta"));
                sheet.addCell(new Label(1, row, venta.getNombre()));
                sheet.addCell(new Number(2, row, venta.getCantidad()));
                sheet.addCell(new Label(3, row, venta.getCodigo()));
                sheet.addCell(new Label(4, row, venta.getFecha()));
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
            row++;
            for (cambioDeProductos stok : stokList) {
                sheet.addCell(new Label(0, row, "Stock"));
                sheet.addCell(new Label(1, row, stok.getNombre()));
                sheet.addCell(new Number(2, row, stok.getResultCantidad()));
                sheet.addCell(new Label(3, row, stok.getCodigo()));
                sheet.addCell(new Label(4, row, stok.getHora()));
                sheet.addCell(new Number(5, row, stok.getPrecio_compra()));
                sheet.addCell(new Number(6, row, stok.getPrecio_venta()));

                double cantidad = stok.getResultCantidad();
                double precioCompra = stok.getPrecio_compra();
                double precioVenta = stok.getPrecio_venta();

                sheet.addCell(new Number(7, row, cantidad * precioCompra));
                sheet.addCell(new Number(8, row, cantidad * precioVenta));

                row++;
            }

            // Añadir datos de transacciones
            row++;
            for (Transacciones trans : transList) {
                sheet.addCell(new Label(0, row, "Transacción"));
                sheet.addCell(new Label(1, row, trans.getTipo()));
                sheet.addCell(new Number(2, row, trans.getCantidad()));
                sheet.addCell(new Label(3, row, trans.getFecha()));
                row++;
            }

            workbook.write();
            workbook.close();
            Toast.makeText(this, "Excel guardado en: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (IOException | WriteException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al guardar Excel: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            int result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            return result == PackageManager.PERMISSION_GRANTED;
        }
    }
    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(intent);
            } catch (Exception e) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0) {
                boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (storageAccepted) {
                    generatePdf(startDate, endDate);
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    // Método para formatear la fecha
    private String formatDate(long dateInMillis) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date(dateInMillis));
    }

}