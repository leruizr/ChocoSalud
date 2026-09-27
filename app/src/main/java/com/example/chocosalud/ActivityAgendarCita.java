package com.example.chocosalud;

// IMPORTS: traen las clases de Android que usa esta pantalla.
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/*
 * CLASE: ActivityAgendarCita  ->  PANTALLA 6 (Agendar cita)
 *
 * - Se abre desde la tarjeta "Agendar cita" del menú principal.
 * - Muestra: selector de especialidad, teléfono de contacto, calendario y botón "Enviar".
 * - Por ahora solo es la parte visual: todavía no guarda la cita.
 * - Su diseño visual está en: res/layout/activity_agendar_cita.xml
 */
public class ActivityAgendarCita extends AppCompatActivity {

    /*
     * MÉTODO: onCreate
     * - Android lo llama automáticamente cuando se crea la pantalla.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_agendar_cita);

        // Deja espacio para que el contenido no quede tapado por las barras del teléfono.
        // (v, insets) -> { ... } es una FUNCIÓN ANÓNIMA (lambda).
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
