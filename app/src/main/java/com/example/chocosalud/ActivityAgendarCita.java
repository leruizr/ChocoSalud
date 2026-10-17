package com.example.chocosalud;

// IMPORTS: traen las clases de Android que usa esta pantalla.
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.View;
import android.widget.CalendarView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.util.Calendar;

/*
 * CLASE: ActivityAgendarCita  ->  PANTALLA 6 (Agendar cita)
 *
 * - Se abre desde la tarjeta "Agendar cita" del menú principal.
 * - Muestra: selector de especialidad, teléfono de contacto, calendario y botón "Enviar".
 * - Su diseño visual está en: res/layout/activity_agendar_cita.xml
 */
public class ActivityAgendarCita extends AppCompatActivity {

    // VARIABLES (atributos de la clase): los controles del formulario.
    private Spinner selectorEspecialidad;
    private TextInputLayout layoutContacto;
    private TextInputEditText campoContacto;

    // VARIABLE: fechaCita -> guarda el día que el usuario toca en el calendario.
    private final Calendar fechaCita = Calendar.getInstance();

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
        CalendarView calendario = findViewById(R.id.calendarView);

        // setMinDate: no deja escoger días que ya pasaron (no tiene sentido una cita en el pasado).
        calendario.setMinDate(System.currentTimeMillis());
        calendario.setOnDateChangeListener((vista, anio, mes, dia) -> fechaCita.set(anio, mes, dia));

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // Se ejecuta al tocar "Enviar" (android:onClick="agendarCita" en el XML).
    public void agendarCita(View v) {if (formularioValido()) {abrirCalendario();}}


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
