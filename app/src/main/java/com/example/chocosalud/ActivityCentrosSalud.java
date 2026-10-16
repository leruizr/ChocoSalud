package com.example.chocosalud;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

/*
 * CLASE: ActivityCentrosSalud -> PANTALLA 5 (Centros de salud)
 * SERVICIO GPS: obtiene la ubicación actual del teléfono y abre Google Maps
 * en esa ubicación o en un centro de salud.
 * Diseño visual: res/layout/activity_centros_salud.xml
 */
public class ActivityCentrosSalud extends AppCompatActivity {

    // CONSTANTE: código para identificar nuestra solicitud de permiso
    private static final int PERMISSION_REQUEST_CODE = 100;

    // CONSTANTES: coordenadas (latitud, longitud) de los centros de salud
    private static final double LATITUD_QUIBDO = 5.681290;
    private static final double LONGITUD_QUIBDO = -76.649315;
    private static final double LATITUD_TADO = 5.264165;
    private static final double LONGITUD_TADO = -76.556871;

    // VARIABLE: cliente de Google que nos da la ubicación del GPS
    private FusedLocationProviderClient fusedLocationClient;

    // VARIABLES: elementos del diseño que vamos a cambiar
    private TextView textoUbicacion;
    private Button botonVerMiUbicacion;

    // VARIABLES: última ubicación obtenida
    private double miLatitud;
    private double miLongitud;

    // MÉTODO: onCreate -> se ejecuta al abrir la pantalla
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_centros_salud);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Se crea el cliente de ubicación y se buscan los elementos del diseño
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        textoUbicacion = findViewById(R.id.textoUbicacion);
        botonVerMiUbicacion = findViewById(R.id.botonVerMiUbicacion);
    }

    // MÉTODO: obtenerUbicacionActual -> botón "Obtener mi ubicación"
    public void obtenerUbicacionActual(View v) {
        // 1. Verificar permisos
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // 2. Pedir permisos si no los tiene (la respuesta llega a onRequestPermissionsResult)
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSION_REQUEST_CODE);
            return;
        }

        // 3. Obtener la ubicación actual con el GPS
        textoUbicacion.setText(R.string.ubicacion_buscando);
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        // Se guardan y se muestran la latitud y la longitud
                        miLatitud = location.getLatitude();
                        miLongitud = location.getLongitude();
                        textoUbicacion.setText(getString(R.string.ubicacion_actual, miLatitud, miLongitud));
                        botonVerMiUbicacion.setEnabled(true);
                    } else {
                        // El GPS del teléfono está apagado o no respondió
                        textoUbicacion.setText(R.string.ubicacion_no_disponible);
                    }
                });
    }

    // MÉTODO: onRequestPermissionsResult -> Android lo llama cuando el usuario responde al permiso
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                obtenerUbicacionActual(null); // aceptó: se busca la ubicación
            } else {
                Toast.makeText(this, R.string.permiso_ubicacion_negado, Toast.LENGTH_LONG).show();
            }
        }
    }

    // MÉTODO: verMiUbicacion -> botón "Ver mi ubicación en el mapa"
    public void verMiUbicacion(View v) {
        abrirMapa(miLatitud, miLongitud, getString(R.string.mi_ubicacion));
    }

    // MÉTODO: abrirCentroQuibdo -> botón de la clínica de Quibdó
    public void abrirCentroQuibdo(View v) {
        abrirMapa(LATITUD_QUIBDO, LONGITUD_QUIBDO, getString(R.string.centro_quibdo));
    }

    // MÉTODO: abrirCentroTado -> botón del hospital de Tadó
    public void abrirCentroTado(View v) {
        abrirMapa(LATITUD_TADO, LONGITUD_TADO, getString(R.string.centro_tado));
    }

    // MÉTODO: abrirMapa -> abre Google Maps en unas coordenadas con un Intent (no necesita API key)
    private void abrirMapa(double latitud, double longitud, String nombre) {
        Uri ubicacion = Uri.parse("geo:" + latitud + "," + longitud
                + "?q=" + latitud + "," + longitud + "(" + Uri.encode(nombre) + ")");
        Intent intent = new Intent(Intent.ACTION_VIEW, ubicacion);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.mapa_no_disponible, Toast.LENGTH_SHORT).show();
        }
    }
}
