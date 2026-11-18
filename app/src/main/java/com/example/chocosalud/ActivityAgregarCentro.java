package com.example.chocosalud;

// IMPORTS: traen las clases de Android que usa esta pantalla.
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Locale;
import java.util.Map;

/*
 * CLASE: ActivityAgregarCentro -> pantalla "Agregar clínica" (Fase 4, base de datos SQLite)
 *
 * - Se abre desde el botón "Agregar clínica" de Centros de salud.
 * - Guarda un centro de salud nuevo en la tabla centros_salud (INSERT): nombre, municipio,
 *   dirección, teléfono, latitud y longitud.
 * - SERVICIO GPS: el botón "Usar mi ubicación actual" llena la latitud y la longitud con la
 *   posición del teléfono (útil cuando el usuario está en la clínica).
 * - El centro nuevo aparece en la lista de Centros de salud y en el selector de Agendar cita.
 * - Su diseño visual está en: res/layout/activity_agregar_centro.xml
 */
public class ActivityAgregarCentro extends AppCompatActivity {

    // VARIABLES: acceso a la base de datos y cliente de ubicación (GPS).
    private BaseDatos baseDatos;
    private FusedLocationProviderClient clienteUbicacion;

    // VARIABLES: cajas y campos del formulario.
    private TextInputLayout layoutNombre, layoutMunicipio, layoutLatitud, layoutLongitud;
    private TextInputEditText campoNombre, campoMunicipio, campoDireccion, campoTelefono,
            campoLatitud, campoLongitud;

    // VARIABLE: lanzadorPermisos -> pide los permisos de ubicación y recibe la respuesta.
    private ActivityResultLauncher<String[]> lanzadorPermisos;

    /*
     * MÉTODO: onCreate
     * - Android lo llama automáticamente cuando se crea la pantalla.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_agregar_centro);

        AjusteTeclado.aplicar(this,
                findViewById(R.id.main),
                (ScrollView) findViewById(R.id.scrollFormulario),
                (ViewGroup) findViewById(R.id.contenidoFormulario));

        layoutNombre = findViewById(R.id.layoutNombreCentro);
        layoutMunicipio = findViewById(R.id.layoutMunicipio);
        layoutLatitud = findViewById(R.id.layoutLatitud);
        layoutLongitud = findViewById(R.id.layoutLongitud);
        campoNombre = findViewById(R.id.campoNombreCentro);
        campoMunicipio = findViewById(R.id.campoMunicipio);
        campoDireccion = findViewById(R.id.campoDireccion);
        campoTelefono = findViewById(R.id.campoTelefonoCentro);
        campoLatitud = findViewById(R.id.campoLatitud);
        campoLongitud = findViewById(R.id.campoLongitud);

        baseDatos = new BaseDatos(this);
        clienteUbicacion = LocationServices.getFusedLocationProviderClient(this);

        // Registra el lanzador de permisos. "respuesta" dice, por cada permiso, si se aceptó.
        lanzadorPermisos = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                (Map<String, Boolean> respuesta) -> {
                    if (respuesta.containsValue(true)) {
                        obtenerUbicacion(); // aceptó al menos uno de los dos permisos
                    } else {
                        Toast.makeText(this, R.string.permiso_ubicacion_negado, Toast.LENGTH_LONG).show();
                    }
                });
    }

    // MÉTODO: onDestroy -> se cierra la base de datos al salir de la pantalla.
    @Override
    protected void onDestroy() {
        baseDatos.close();
        super.onDestroy();
    }

    /*
     * MÉTODO: usarMiUbicacion
     * - Se ejecuta al tocar "Usar mi ubicación actual". Revisa el permiso y, si falta, lo pide.
     */
    public void usarMiUbicacion(View v) {
        if (tienePermisoUbicacion()) {
            obtenerUbicacion();
        } else {
            lanzadorPermisos.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION});
        }
    }

    // MÉTODO: tienePermisoUbicacion -> true si el usuario ya dio el permiso de ubicación.
    private boolean tienePermisoUbicacion() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    /*
     * MÉTODO: obtenerUbicacion  (servicio GPS)
     * - Pide la ubicación actual y la escribe en los campos de latitud y longitud.
     * - @SuppressLint: Android Studio avisa que falta revisar el permiso; ya se revisa arriba.
     */
    @SuppressLint("MissingPermission")
    private void obtenerUbicacion() {
        if (!tienePermisoUbicacion()) {
            return;
        }
        Toast.makeText(this, R.string.ubicacion_buscando, Toast.LENGTH_SHORT).show();
        clienteUbicacion.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(this, ubicacion -> {
                    if (ubicacion != null) {
                        // Locale.ROOT: usa punto decimal (5.68) aunque el teléfono use coma.
                        campoLatitud.setText(String.format(Locale.ROOT, "%.6f", ubicacion.getLatitude()));
                        campoLongitud.setText(String.format(Locale.ROOT, "%.6f", ubicacion.getLongitude()));
                    } else {
                        Toast.makeText(this, R.string.ubicacion_no_disponible, Toast.LENGTH_LONG).show();
                    }
                });
    }

    /*
     * MÉTODO: guardarCentro  (INSERT en centros_salud)
     * - Se ejecuta al tocar "Guardar clínica".
     * - Valida el formulario, guarda el centro y vuelve a la lista de Centros de salud.
     */
    public void guardarCentro(View v) {
        boolean valido = marcarSiVacio(layoutNombre, campoNombre, R.string.error_nombre_centro_requerido);
        valido &= marcarSiVacio(layoutMunicipio, campoMunicipio, R.string.error_municipio_requerido);
        // La latitud va de -90 a 90 y la longitud de -180 a 180.
        Double latitud = leerCoordenada(layoutLatitud, campoLatitud, 90);
        Double longitud = leerCoordenada(layoutLongitud, campoLongitud, 180);
        if (!valido || latitud == null || longitud == null) {
            return;
        }

        long nuevoCentro = baseDatos.agregarCentro(obtenerTexto(campoNombre),
                obtenerTexto(campoMunicipio), obtenerTexto(campoDireccion),
                obtenerTexto(campoTelefono), latitud, longitud);

        if (nuevoCentro == BaseDatos.SIN_CENTRO) {
            Toast.makeText(this, R.string.error_guardar_centro, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, R.string.centro_guardado, Toast.LENGTH_SHORT).show();
            finish(); // vuelve a Centros de salud, que recarga la lista en onResume
        }
    }

    /*
     * MÉTODO: leerCoordenada
     * - Convierte el texto del campo en número y revisa que esté entre -limite y limite.
     * - Acepta coma o punto decimal ("5,68" o "5.68").
     * - DEVUELVE: el número, o null si el campo está mal (y lo marca en rojo).
     */
    private Double leerCoordenada(TextInputLayout layout, TextInputEditText campo, double limite) {
        try {
            double valor = Double.parseDouble(obtenerTexto(campo).replace(',', '.'));
            if (valor < -limite || valor > limite) {
                throw new NumberFormatException();
            }
            layout.setError(null);
            return valor;
        } catch (NumberFormatException e) {
            layout.setError(getString(R.string.error_coordenada_invalida, (int) limite));
            return null;
        }
    }

    // MÉTODO: marcarSiVacio -> marca el campo en rojo si está vacío. DEVUELVE true si tiene texto.
    private boolean marcarSiVacio(TextInputLayout layout, TextInputEditText campo, int mensaje) {
        if (obtenerTexto(campo).isEmpty()) {
            layout.setError(getString(mensaje));
            return false;
        }
        layout.setError(null);
        return true;
    }

    // MÉTODO: obtenerTexto -> el texto del campo sin espacios al inicio ni al final.
    private String obtenerTexto(TextInputEditText campo) {
        return campo.getText() == null ? "" : campo.getText().toString().trim();
    }
}
