package com.example.chocosalud;

// IMPORTS: traen las clases de Android que usa esta pantalla.
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Calendar;
import java.util.Locale;

/*
 * CLASE: ActivityHistorial -> PANTALLA 7 (Historial médico)
 *
 * - Se abre desde la tarjeta "Historial médico" del menú principal (y al tocar la
 *   notificación de una cita agendada).
 * - Cada cita que el usuario agenda queda guardada en la tabla citas; esta pantalla la muestra
 *   como su historial médico: especialidad, fecha, centro de salud y estado
 *   (Próxima = de hoy en adelante, Realizada = fecha pasada).
 * - BASE DE DATOS (Fase 4): consultas SELECT con JOIN, CASE, COUNT y SUM.
 * - Al tocar una cita se ve su detalle con la foto de la fórmula médica (columna BLOB).
 * - Su diseño visual está en: res/layout/activity_historial.xml
 */
public class ActivityHistorial extends AppCompatActivity {

    // VARIABLES: acceso a la base de datos, usuario conectado y adaptador de la lista.
    private BaseDatos baseDatos;
    private long usuarioId;
    private SimpleCursorAdapter adaptadorHistorial;
    private TextView textoResumen;

    /*
     * MÉTODO: onCreate
     * - Android lo llama automáticamente cuando se crea la pantalla.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_historial);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        baseDatos = new BaseDatos(this);
        usuarioId = Sesion.obtenerUsuarioId(this);
        textoResumen = findViewById(R.id.textoResumen);

        // Cada fila muestra "titulo" (especialidad - fecha) arriba y "detalle" (centro · estado) abajo.
        adaptadorHistorial = new SimpleCursorAdapter(this,
                android.R.layout.simple_list_item_2, null,
                new String[]{"titulo", "detalle"},
                new int[]{android.R.id.text1, android.R.id.text2}, 0);

        ListView lista = findViewById(R.id.listaHistorial);
        lista.setAdapter(adaptadorHistorial);
        lista.setEmptyView(findViewById(R.id.textoSinHistorial));
        lista.setOnItemClickListener((padre, vista, posicion, id) -> mostrarDetalle(id));
    }

    /*
     * MÉTODO: onResume
     * - Cada vez que se vuelve a esta pantalla se recarga el historial (por ejemplo, después de
     *   agendar una cita nueva desde el botón "Agendar nueva cita").
     */
    @Override
    protected void onResume() {
        super.onResume();
        cargarHistorial();
    }

    // MÉTODO: onDestroy -> cierra el Cursor y la base de datos al salir.
    @Override
    protected void onDestroy() {
        adaptadorHistorial.changeCursor(null);
        baseDatos.close();
        super.onDestroy();
    }

    /*
     * MÉTODO: cargarHistorial  (SELECT)
     * - Llena la lista con las citas del usuario y muestra el resumen de arriba.
     */
    private void cargarHistorial() {
        Cursor historial = baseDatos.consultarHistorial(usuarioId,
                getString(R.string.estado_proxima), getString(R.string.estado_realizada),
                getString(R.string.historial_con_formula));
        adaptadorHistorial.changeCursor(historial);

        int[] conteo = baseDatos.contarCitas(usuarioId); // {total, proximas}
        int total = conteo[0];
        int proximas = conteo[1];
        textoResumen.setText(getString(R.string.historial_resumen, total, proximas, total - proximas));
    }

    /*
     * MÉTODO: mostrarDetalle  (SELECT de una cita)
     * - Abre un cuadro de diálogo con todos los datos de la cita y la foto de la fórmula.
     */
    private void mostrarDetalle(long citaId) {
        try (Cursor cita = baseDatos.consultarDetalleCita(citaId, usuarioId)) {
            if (!cita.moveToFirst()) {
                return;
            }
            String especialidad = cita.getString(cita.getColumnIndexOrThrow(BaseDatos.CITA_ESPECIALIDAD));
            String fecha = cita.getString(cita.getColumnIndexOrThrow(BaseDatos.CITA_FECHA));
            String telefono = cita.getString(cita.getColumnIndexOrThrow(BaseDatos.CITA_TELEFONO));
            String creada = cita.getString(cita.getColumnIndexOrThrow(BaseDatos.CITA_CREADA_EN));
            String centro = cita.getString(cita.getColumnIndexOrThrow("centro"));
            String municipio = cita.getString(cita.getColumnIndexOrThrow("municipio"));
            byte[] foto = cita.getBlob(cita.getColumnIndexOrThrow(BaseDatos.CITA_FOTO));

            // "Inflar" = convertir el diseño XML del diálogo en vistas que se pueden llenar.
            View vista = getLayoutInflater().inflate(R.layout.dialogo_detalle_cita, null);
            TextView texto = vista.findViewById(R.id.textoDetalle);
            ImageView imagen = vista.findViewById(R.id.imagenDetalleFormula);

            texto.setText(getString(R.string.historial_detalle, especialidad, centro, municipio,
                    fecha, estadoDe(fecha), telefono, creada,
                    getString(foto != null ? R.string.historial_formula_si : R.string.historial_formula_no)));
            if (foto != null) {
                imagen.setImageBitmap(BitmapFactory.decodeByteArray(foto, 0, foto.length));
                imagen.setVisibility(View.VISIBLE);
            }

            new AlertDialog.Builder(this)
                    .setTitle(R.string.historial_detalle_titulo)
                    .setView(vista)
                    .setPositiveButton(R.string.boton_cerrar, null)
                    .show();
        }
    }

    /*
     * MÉTODO: estadoDe
     * - Compara la fecha de la cita ("aaaa-mm-dd") con la de hoy.
     * - DEVUELVE: "Próxima" si es hoy o después, "Realizada" si ya pasó.
     */
    private String estadoDe(String fecha) {
        Calendar hoy = Calendar.getInstance();
        String textoHoy = String.format(Locale.ROOT, "%04d-%02d-%02d", hoy.get(Calendar.YEAR),
                hoy.get(Calendar.MONTH) + 1, hoy.get(Calendar.DAY_OF_MONTH));
        // compareTo compara textos en orden alfabético; con el formato aaaa-mm-dd eso
        // coincide con el orden de las fechas.
        return getString(fecha.compareTo(textoHoy) >= 0 ? R.string.estado_proxima : R.string.estado_realizada);
    }

    /*
     * MÉTODO: irAgendarCita
     * - Se ejecuta al tocar "Agendar nueva cita".
     */
    public void irAgendarCita(View v) {
        startActivity(new Intent(this, ActivityAgendarCita.class));
    }
}
