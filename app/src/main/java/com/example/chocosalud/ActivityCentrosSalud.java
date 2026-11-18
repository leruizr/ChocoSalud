package com.example.chocosalud;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.button.MaterialButton;

/*
 * CLASE: ActivityCentrosSalud -> PANTALLA 5 (Centros de salud)
 * SERVICIO GPS: obtiene la ubicación actual del teléfono y abre Google Maps
 * en esa ubicación o en un centro de salud.
 * BASE DE DATOS (Fase 4): la lista de centros se lee de la tabla centros_salud (SELECT).
 * El botón "Agregar clínica" abre ActivityAgregarCentro (INSERT) y al mantener presionado un
 * centro se puede eliminar (DELETE) si no tiene citas.
 * Diseño visual: res/layout/activity_centros_salud.xml
 */
public class ActivityCentrosSalud extends AppCompatActivity {

    // CONSTANTE: código para identificar nuestra solicitud de permiso
    private static final int PERMISSION_REQUEST_CODE = 100;

    // VARIABLES: acceso a la base de datos y contenedor donde se ponen los botones de los centros.
    // (En la Fase 3 las coordenadas de los centros estaban escritas aquí; ahora están en SQLite.)
    private BaseDatos baseDatos;
    private LinearLayout contenedorCentros;

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
        contenedorCentros = findViewById(R.id.contenedorCentros);
        baseDatos = new BaseDatos(this);
    }

    /*
     * MÉTODO: onResume
     * - Android lo llama cada vez que la pantalla vuelve a verse. Así, al regresar de
     *   "Agregar clínica", la lista ya muestra el centro nuevo.
     */
    @Override
    protected void onResume() {
        super.onResume();
        cargarCentros();
    }

    // MÉTODO: onDestroy -> se cierra la base de datos al salir de la pantalla.
    @Override
    protected void onDestroy() {
        baseDatos.close();
        super.onDestroy();
    }

    /*
     * MÉTODO: cargarCentros  (SELECT en centros_salud)
     * - Borra los botones anteriores y crea un botón por cada centro guardado.
     * - Tocar un botón: abre Google Maps en ese centro.
     * - Mantenerlo presionado: pregunta si se quiere eliminar.
     */
    private void cargarCentros() {
        contenedorCentros.removeAllViews();
        try (Cursor centros = baseDatos.consultarCentros()) {
            // moveToNext() avanza a la siguiente fila; devuelve false cuando ya no hay más.
            while (centros.moveToNext()) {
                long id = centros.getLong(centros.getColumnIndexOrThrow(BaseDatos.CENTRO_ID));
                String nombre = centros.getString(centros.getColumnIndexOrThrow(BaseDatos.CENTRO_NOMBRE));
                String municipio = centros.getString(centros.getColumnIndexOrThrow(BaseDatos.CENTRO_MUNICIPIO));
                double latitud = centros.getDouble(centros.getColumnIndexOrThrow(BaseDatos.CENTRO_LATITUD));
                double longitud = centros.getDouble(centros.getColumnIndexOrThrow(BaseDatos.CENTRO_LONGITUD));
                contenedorCentros.addView(crearBotonCentro(id, nombre, municipio, latitud, longitud));
            }
        }
    }

    /*
     * MÉTODO: crearBotonCentro
     * - Crea, desde el código, un botón con borde (igual a los demás de la app) para un centro.
     * - DEVUELVE: el botón listo para agregarlo a la pantalla.
     */
    private MaterialButton crearBotonCentro(long id, String nombre, String municipio,
                                            double latitud, double longitud) {
        // materialButtonOutlinedStyle: el mismo estilo "OutlinedButton" que se usa en los XML.
        MaterialButton boton = new MaterialButton(this, null,
                com.google.android.material.R.attr.materialButtonOutlinedStyle);
        boton.setText(getString(R.string.centro_boton_texto, nombre, municipio));
        boton.setMinHeight(getResources().getDimensionPixelSize(R.dimen.alto_boton_centro));

        // Ancho completo y un pequeño espacio arriba de cada botón.
        LinearLayout.LayoutParams medidas = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        medidas.topMargin = getResources().getDimensionPixelSize(R.dimen.espacio_boton_centro);
        boton.setLayoutParams(medidas);

        boton.setOnClickListener(v -> abrirMapa(latitud, longitud, nombre));
        boton.setOnLongClickListener(v -> {
            confirmarEliminarCentro(id, nombre);
            return true; // true = el toque largo ya se atendió
        });
        return boton;
    }

    /*
     * MÉTODO: confirmarEliminarCentro
     * - Pregunta antes de borrar. Si el centro tiene citas, no se deja borrar.
     */
    private void confirmarEliminarCentro(long id, String nombre) {
        if (baseDatos.centroTieneCitas(id)) {
            Toast.makeText(this, R.string.error_centro_con_citas, Toast.LENGTH_LONG).show();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirmar_eliminar_centro_titulo)
                .setMessage(getString(R.string.confirmar_eliminar_centro_mensaje, nombre))
                .setPositiveButton(R.string.boton_si_eliminar, (dialogo, boton) -> {
                    if (baseDatos.eliminarCentro(id) > 0) {
                        Toast.makeText(this, R.string.centro_eliminado, Toast.LENGTH_SHORT).show();
                        cargarCentros();
                    }
                })
                .setNegativeButton(R.string.boton_no, null)
                .show();
    }

    /*
     * MÉTODO: irAgregarCentro
     * - Se ejecuta al tocar "Agregar clínica". Abre el formulario.
     */
    public void irAgregarCentro(View v) {
        startActivity(new Intent(this, ActivityAgregarCentro.class));
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
