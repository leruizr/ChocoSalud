package com.example.chocosalud;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/*
 * CLASE: ActivityMisCitas -> pantalla "Mis citas" (Fase 4, base de datos SQLite)
 *
 * - Muestra la lista de citas del usuario que inició sesión (SELECT -> R del CRUD).
 * - Al tocar una cita se puede:
 *     Editar       -> abre Agendar cita con los datos cargados (UPDATE -> U del CRUD)
 *     Cancelar cita -> la borra de la base de datos             (DELETE -> D del CRUD)
 * - Se abre desde el botón "Ver mis citas" de Agendar cita.
 * - Su diseño visual está en: res/layout/activity_mis_citas.xml
 */
public class ActivityMisCitas extends AppCompatActivity {

    // VARIABLE: baseDatos -> acceso a la base de datos SQLite de la app.
    private BaseDatos baseDatos;

    // VARIABLE: adaptadorCitas -> une el Cursor con las citas y la lista de la pantalla.
    private SimpleCursorAdapter adaptadorCitas;

    // VARIABLE: usuarioId -> el usuario que inició sesión (solo se muestran SUS citas).
    private long usuarioId;

    /*
     * MÉTODO: onCreate
     * - Android lo llama automáticamente cuando se crea la pantalla.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_mis_citas);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        baseDatos = new BaseDatos(this);
        usuarioId = Sesion.obtenerUsuarioId(this);

        // El adaptador pone la columna "titulo" (especialidad - fecha) en la primera línea de cada
        // fila y la columna "centro" en la segunda. Empieza sin datos (null): los carga cargarCitas.
        adaptadorCitas = new SimpleCursorAdapter(this,
                android.R.layout.simple_list_item_2, null,
                new String[]{"titulo", "centro"},
                new int[]{android.R.id.text1, android.R.id.text2}, 0);

        ListView listaCitas = findViewById(R.id.listaCitas);
        listaCitas.setAdapter(adaptadorCitas);
        // setEmptyView: si la lista está vacía, se muestra este texto en su lugar.
        listaCitas.setEmptyView(findViewById(R.id.textoSinCitas));

        // Al tocar una cita se abre un menú con Editar / Cancelar cita.
        // "id" es el _id de la cita en la base de datos (lo entrega el adaptador).
        listaCitas.setOnItemClickListener((lista, vista, posicion, id) -> mostrarOpciones(id));
    }

    /*
     * MÉTODO: onResume
     * - Android lo llama cada vez que la pantalla vuelve a verse (por ejemplo, al regresar de
     *   editar una cita). Así la lista siempre muestra los datos actualizados.
     */
    @Override
    protected void onResume() {
        super.onResume();
        cargarCitas();
    }

    /*
     * MÉTODO: onDestroy
     * - Android lo llama cuando se cierra la pantalla. Se cierran el Cursor y la base de datos.
     */
    @Override
    protected void onDestroy() {
        adaptadorCitas.changeCursor(null); // changeCursor(null) cierra el Cursor anterior
        baseDatos.close();
        super.onDestroy();
    }

    /*
     * MÉTODO: cargarCitas  (SELECT -> R del CRUD)
     * - Consulta las citas del usuario y las muestra en la lista.
     */
    private void cargarCitas() {
        Cursor citas = baseDatos.consultarCitas(usuarioId);
        adaptadorCitas.changeCursor(citas); // cambia los datos de la lista (y cierra el Cursor viejo)
    }

    /*
     * MÉTODO: mostrarOpciones
     * - Muestra un cuadro de diálogo con las acciones que se pueden hacer con la cita.
     */
    private void mostrarOpciones(long citaId) {
        String[] opciones = {getString(R.string.opcion_editar), getString(R.string.opcion_cancelar_cita)};
        new AlertDialog.Builder(this)
                .setTitle(R.string.opciones_cita_titulo)
                .setItems(opciones, (dialogo, opcion) -> {
                    if (opcion == 0) {
                        editarCita(citaId);
                    } else {
                        confirmarCancelacion(citaId);
                    }
                })
                .show();
    }

    /*
     * MÉTODO: editarCita
     * - Abre Agendar cita enviándole el id de la cita: esa pantalla entra en modo edición.
     */
    private void editarCita(long citaId) {
        Intent intent = new Intent(this, ActivityAgendarCita.class);
        intent.putExtra(ActivityAgendarCita.EXTRA_CITA_ID, citaId);
        startActivity(intent);
    }

    /*
     * MÉTODO: confirmarCancelacion
     * - Pregunta antes de borrar, porque una cita borrada no se puede recuperar.
     */
    private void confirmarCancelacion(long citaId) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirmar_cancelar_titulo)
                .setMessage(R.string.confirmar_cancelar_mensaje)
                .setPositiveButton(R.string.boton_si_cancelar, (dialogo, boton) -> cancelarCita(citaId))
                .setNegativeButton(R.string.boton_no, null)
                .show();
    }

    /*
     * MÉTODO: cancelarCita  (DELETE -> D del CRUD)
     * - Borra la cita de la base de datos y recarga la lista.
     */
    private void cancelarCita(long citaId) {
        int filas = baseDatos.cancelarCita(citaId, usuarioId);
        if (filas > 0) {
            Toast.makeText(this, R.string.cita_cancelada, Toast.LENGTH_SHORT).show();
            cargarCitas();
        }
    }
}
