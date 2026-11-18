package com.example.chocosalud;

// IMPORTS: traen las clases de Android que usa esta pantalla.
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.io.ByteArrayOutputStream;
import java.util.Calendar;
import java.util.Locale;

/*
 * CLASE: ActivityAgendarCita  ->  PANTALLA 6 (Agendar cita)
 *
 * - Se abre desde la tarjeta "Agendar cita" del menú principal.
 * - Muestra: selector de especialidad, centro de salud, teléfono de contacto, botón "Tomar foto"
 *   (cámara), calendario, botón "Enviar" y botón "Ver mis citas".
 * - SERVICIOS (Fase 3): cámara (botón "Tomar foto") y calendario (botón "Enviar").
 * - BASE DE DATOS (Fase 4): guarda la cita en la tabla citas (INSERT) o, en modo edición,
 *   la actualiza (UPDATE). La foto de la fórmula se guarda en la columna foto (BLOB) y la
 *   cita queda en el HISTORIAL MÉDICO del usuario.
 * - NOTIFICACIONES (Fase 4): si el usuario las tiene activas en su perfil, se avisa que la
 *   cita quedó agendada.
 * - MODO EDICIÓN: si la pantalla se abre desde "Mis citas" con el id de una cita, carga sus
 *   datos y el botón pasa a decir "Guardar cambios".
 * - Su diseño visual está en: res/layout/activity_agendar_cita.xml
 */
public class ActivityAgendarCita extends AppCompatActivity {

    // CONSTANTE: EXTRA_CITA_ID -> nombre del dato que envía "Mis citas" cuando se va a editar una cita.
    public static final String EXTRA_CITA_ID = "cita_id";

    // VARIABLES (atributos de la clase): los controles del formulario.
    private Spinner selectorEspecialidad;
    private TextInputLayout layoutContacto;
    private TextInputEditText campoContacto;

    // VARIABLE: fechaCita -> guarda el día que el usuario toca en el calendario.
    private final Calendar fechaCita = Calendar.getInstance();

    // SERVICIO DE CÁMARA (Fase 3)
    // imagenFormula -> donde se muestra la foto de la fórmula médica una vez tomada.
    private ImageView imagenFormula;

    // lanzadorCamara -> el "mensajero" que abre la cámara y recibe la foto de vuelta.
    // Usa por dentro el Intent MediaStore.ACTION_IMAGE_CAPTURE: le pide a la app de cámara
    // del teléfono que tome una foto. No necesita el permiso CAMERA, porque la foto la toma
    // la app de cámara del sistema y no ChocóSalud.
    private ActivityResultLauncher<Void> lanzadorCamara;

    // fotoNueva -> la foto que se acaba de tomar (null si no se ha tomado ninguna).
    // Al guardar la cita se convierte en bytes para la columna foto de la tabla citas.
    private Bitmap fotoNueva;

    // BASE DE DATOS (Fase 4)
    // baseDatos -> acceso a la base de datos SQLite de la app.
    private BaseDatos baseDatos;
    // selectorCentro -> lista desplegable con los centros de salud de la tabla centros_salud.
    private Spinner selectorCentro;
    // adaptadorCentros -> une el Cursor de la base de datos con el selector de centros.
    private SimpleCursorAdapter adaptadorCentros;
    // calendario -> el calendario de la pantalla (se usa para mostrar la fecha al editar).
    private CalendarView calendario;
    // citaId -> id de la cita que se está editando, o SIN_CITA si se está creando una nueva.
    private long citaId = BaseDatos.SIN_CITA;

    /*
     * MÉTODO: onCreate
     * - Android lo llama automáticamente cuando se crea la pantalla.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_agendar_cita);

        // findViewById busca cada control del diseño por su id y lo guarda en su variable.
        selectorEspecialidad = findViewById(R.id.selectordetipo);
        layoutContacto = findViewById(R.id.textcontacto);
        campoContacto = findViewById(R.id.inputcontacto);
        calendario = findViewById(R.id.calendarView);
        imagenFormula = findViewById(R.id.imagenFormula);
        selectorCentro = findViewById(R.id.selectorCentro);

        // Registra el lanzador de la cámara. Lo que está dentro de la lambda se ejecuta cuando
        // la cámara termina: "foto" trae la imagen, o null si el usuario canceló.
        lanzadorCamara = registerForActivityResult(
                new ActivityResultContracts.TakePicturePreview(),
                (Bitmap foto) -> {
                    if (foto != null) {
                        fotoNueva = foto;
                        imagenFormula.setImageBitmap(foto);
                        imagenFormula.setVisibility(View.VISIBLE);
                        Toast.makeText(this, R.string.foto_adjuntada, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, R.string.foto_cancelada, Toast.LENGTH_SHORT).show();
                    }
                });

        // setMinDate: no deja escoger días que ya pasaron (no tiene sentido una cita en el pasado).
        calendario.setMinDate(System.currentTimeMillis());
        calendario.setOnDateChangeListener((vista, anio, mes, dia) -> fechaCita.set(anio, mes, dia));

        // Abre la base de datos y llena el selector de centros de salud.
        baseDatos = new BaseDatos(this);
        cargarCentros();

        // ¿La pantalla se abrió para EDITAR una cita? Si "Mis citas" envió un id, sí.
        citaId = getIntent().getLongExtra(EXTRA_CITA_ID, BaseDatos.SIN_CITA);
        if (modoEdicion()) {
            prepararModoEdicion();
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /*
     * MÉTODO: onDestroy
     * - Android lo llama cuando se cierra la pantalla. Se cierran el Cursor y la base de datos.
     */
    @Override
    protected void onDestroy() {
        adaptadorCentros.getCursor().close();
        baseDatos.close();
        super.onDestroy();
    }

    /*
     * MÉTODO: cargarCentros  (lee la tabla centros_salud)
     * - Pide los centros a la base de datos y los muestra en el selector.
     * - SimpleCursorAdapter toma la columna "nombre" de cada fila y la pone en el texto de la lista.
     */
    private void cargarCentros() {
        Cursor centros = baseDatos.consultarCentros();
        adaptadorCentros = new SimpleCursorAdapter(this,
                android.R.layout.simple_spinner_item, centros,
                new String[]{BaseDatos.CENTRO_NOMBRE}, new int[]{android.R.id.text1}, 0);
        adaptadorCentros.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        selectorCentro.setAdapter(adaptadorCentros);
    }

    // MÉTODO: modoEdicion -> true si se está editando una cita, false si se está creando una nueva.
    private boolean modoEdicion() {
        return citaId != BaseDatos.SIN_CITA;
    }

    /*
     * MÉTODO: prepararModoEdicion
     * - Cambia los textos de la pantalla y carga los datos de la cita a editar.
     */
    private void prepararModoEdicion() {
        TextView titulo = findViewById(R.id.tituloCita);
        Button botonEnviar = findViewById(R.id.botonEnviar1);
        titulo.setText(R.string.editar_cita_titulo);
        botonEnviar.setText(R.string.boton_guardar_cambios);
        findViewById(R.id.botonMisCitas).setVisibility(View.GONE);
        cargarCita();
    }

    /*
     * MÉTODO: cargarCita  (SELECT de una cita)
     * - Lee la cita de la base de datos y llena el formulario con sus datos.
     */
    private void cargarCita() {
        try (Cursor cita = baseDatos.consultarCita(citaId, Sesion.obtenerUsuarioId(this))) {
            if (!cita.moveToFirst()) {
                return; // la cita no existe (por ejemplo, la borraron)
            }
            // getColumnIndexOrThrow busca la columna por su nombre y devuelve su posición.
            long centroId = cita.getLong(cita.getColumnIndexOrThrow(BaseDatos.CITA_CENTRO_ID));
            String especialidad = cita.getString(cita.getColumnIndexOrThrow(BaseDatos.CITA_ESPECIALIDAD));
            String telefono = cita.getString(cita.getColumnIndexOrThrow(BaseDatos.CITA_TELEFONO));
            String fecha = cita.getString(cita.getColumnIndexOrThrow(BaseDatos.CITA_FECHA));
            // getBlob: lee la foto en bytes (null si la cita no tiene foto).
            byte[] foto = cita.getBlob(cita.getColumnIndexOrThrow(BaseDatos.CITA_FOTO));

            seleccionarEspecialidad(especialidad);
            seleccionarCentro(centroId);
            campoContacto.setText(telefono);
            mostrarFecha(fecha);
            mostrarFotoGuardada(foto);
        }
    }

    // MÉTODO: mostrarFotoGuardada -> si la cita ya tenía foto, la muestra en la pantalla.
    private void mostrarFotoGuardada(byte[] foto) {
        if (foto != null) {
            // decodeByteArray: convierte los bytes guardados otra vez en una imagen.
            imagenFormula.setImageBitmap(BitmapFactory.decodeByteArray(foto, 0, foto.length));
            imagenFormula.setVisibility(View.VISIBLE);
        }
    }

    /*
     * MÉTODO: fotoEnBytes
     * - Convierte la foto en bytes (formato JPEG) para guardarla en la columna BLOB.
     * - DEVUELVE: los bytes, o null si no se tomó foto.
     */
    private byte[] fotoEnBytes() {
        if (fotoNueva == null) {
            return null;
        }
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        fotoNueva.compress(Bitmap.CompressFormat.JPEG, 90, salida); // 90 = calidad (0 a 100)
        return salida.toByteArray();
    }

    // MÉTODO: seleccionarEspecialidad -> deja marcada en el selector la especialidad guardada.
    private void seleccionarEspecialidad(String especialidad) {
        for (int i = 0; i < selectorEspecialidad.getCount(); i++) {
            if (selectorEspecialidad.getItemAtPosition(i).toString().equals(especialidad)) {
                selectorEspecialidad.setSelection(i);
                return;
            }
        }
    }

    // MÉTODO: seleccionarCentro -> deja marcado en el selector el centro guardado (por su _id).
    private void seleccionarCentro(long centroId) {
        for (int i = 0; i < adaptadorCentros.getCount(); i++) {
            if (adaptadorCentros.getItemId(i) == centroId) {
                selectorCentro.setSelection(i);
                return;
            }
        }
    }

    // MÉTODO: mostrarFecha -> convierte el texto "aaaa-mm-dd" en fecha y la marca en el calendario.
    private void mostrarFecha(String fecha) {
        String[] partes = fecha.split("-");
        // En Calendar los meses van de 0 (enero) a 11 (diciembre), por eso se resta 1.
        fechaCita.set(Integer.parseInt(partes[0]), Integer.parseInt(partes[1]) - 1,
                Integer.parseInt(partes[2]));
        // Solo se marca si no es una fecha pasada (el calendario no permite días anteriores a hoy).
        if (fechaCita.getTimeInMillis() >= calendario.getMinDate()) {
            calendario.setDate(fechaCita.getTimeInMillis());
        }
    }

    /*
     * MÉTODO: agendarCita
     * - Se ejecuta al tocar "Enviar" / "Guardar cambios" (android:onClick="agendarCita" en el XML).
     * - Cita nueva: la guarda en la base de datos (INSERT) y abre el calendario del teléfono.
     * - Modo edición: actualiza la cita (UPDATE) y vuelve a "Mis citas".
     */
    public void agendarCita(View v) {
        if (!formularioValido()) {
            return;
        }

        // VARIABLE local: usuarioId -> el usuario que inició sesión (dueño de la cita).
        long usuarioId = Sesion.obtenerUsuarioId(this);
        if (usuarioId == BaseDatos.SIN_USUARIO) {
            Toast.makeText(this, R.string.error_sin_sesion, Toast.LENGTH_LONG).show();
            return;
        }

        // VARIABLES locales: los datos de la cita, tomados del formulario.
        String especialidad = selectorEspecialidad.getSelectedItem().toString();
        String telefono = obtenerTexto(campoContacto);
        String fecha = formatearFecha(fechaCita);
        long centroId = selectorCentro.getSelectedItemId(); // el _id del centro elegido
        byte[] foto = fotoEnBytes(); // la fórmula médica (o null si no hay foto nueva)

        if (modoEdicion()) {
            // UPDATE: cambia la cita que ya existe.
            int filas = baseDatos.actualizarCita(citaId, usuarioId, centroId, especialidad, telefono, fecha, foto);
            if (filas > 0) {
                Toast.makeText(this, R.string.cita_actualizada, Toast.LENGTH_SHORT).show();
                finish(); // vuelve a "Mis citas", que se recarga sola
            } else {
                Toast.makeText(this, R.string.error_guardar_cita, Toast.LENGTH_LONG).show();
            }
        } else {
            // INSERT: guarda la cita nueva.
            long nuevaCita = baseDatos.agendarCita(usuarioId, centroId, especialidad, telefono, fecha, foto);
            if (nuevaCita == BaseDatos.SIN_CITA) {
                Toast.makeText(this, R.string.error_guardar_cita, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, R.string.cita_guardada, Toast.LENGTH_SHORT).show();
                // Servicio de notificaciones: solo si el usuario las activó en su perfil.
                if (baseDatos.notificacionesActivas(usuarioId)) {
                    Notificaciones.avisarCitaAgendada(this, nuevaCita, especialidad, fecha);
                }
                abrirCalendario(); // servicio de calendario (Fase 3)
            }
        }
    }

    /*
     * MÉTODO: verMisCitas
     * - Se ejecuta al tocar "Ver mis citas". Abre la lista de citas guardadas.
     */
    public void verMisCitas(View v) {
        Intent intent = new Intent(this, ActivityMisCitas.class);
        startActivity(intent);
    }

    // MÉTODO: formatearFecha -> convierte la fecha en texto "aaaa-mm-dd" para guardarla en la base.
    private String formatearFecha(Calendar fecha) {
        return String.format(Locale.ROOT, "%04d-%02d-%02d",
                fecha.get(Calendar.YEAR), fecha.get(Calendar.MONTH) + 1, fecha.get(Calendar.DAY_OF_MONTH));
    }

    /*
     * MÉTODO: adjuntarFoto
     * - Se ejecuta al tocar "Tomar foto" (android:onClick="adjuntarFoto" en el XML).
     * - Lanza la cámara. Si el teléfono no tiene ninguna app de cámara, avisa con un mensaje
     *   en vez de cerrar la aplicación.
     */
    public void adjuntarFoto(View v) {
        try {
            lanzadorCamara.launch(null);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.error_sin_camara, Toast.LENGTH_LONG).show();
        }
    }


    /*
     * MÉTODO: abrirCalendario
     * - Arma un INTENT IMPLÍCITO: no dice QUÉ app abrir, sino QUÉ se quiere hacer
     *   ("insertar un evento en el calendario"). Android busca la app que sabe hacerlo
     * - No necesita permisos: quien guarda el evento es la app de calendario, no ChocóSalud.
     */
    private void abrirCalendario() {
        String especialidad = selectorEspecialidad.getSelectedItem().toString();
        String telefono = obtenerTexto(campoContacto);

        Intent intent = new Intent(Intent.ACTION_INSERT);
        intent.setData(CalendarContract.Events.CONTENT_URI);

        intent.putExtra(CalendarContract.Events.TITLE, getString(R.string.cita_titulo_evento, especialidad));
        intent.putExtra(CalendarContract.Events.DESCRIPTION, getString(R.string.cita_descripcion_evento, telefono));

        intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, fechaCita.getTimeInMillis());
        intent.putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.error_sin_calendario, Toast.LENGTH_LONG).show();
        }
    }

    /*
     * MÉTODO: formularioValido
     * - Revisa la especialidad y el teléfono.
     * - DEVUELVE: true si todo está bien, false si hay algún error.
     */
    private boolean formularioValido() {
        boolean valido = true;
        if (selectorEspecialidad.getSelectedItemPosition() == 0) {
            Toast.makeText(this, R.string.error_especialidad_requerida, Toast.LENGTH_SHORT).show();
            valido = false;
        }
        if (obtenerTexto(campoContacto).isEmpty()) {
            layoutContacto.setError(getString(R.string.error_telefono_requerido));
            valido = false;
        } else {
            layoutContacto.setError(null);
        }
        return valido;
    }

    private String obtenerTexto(TextInputEditText campo) {
        return campo.getText() == null ? "" : campo.getText().toString().trim();
    }
}
