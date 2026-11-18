package com.example.chocosalud;

// IMPORTS: traen las clases de Android que usa esta pantalla.
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;

/*
 * CLASE: ActivityMenuPrincipal  ->  PANTALLA 4 (Menú principal)
 *
 * - Muestra 4 tarjetas: Centros de salud, Agendar cita, Historial médico y Perfil y notificaciones.
 * - Cada tarjeta llama a un MÉTODO de esta clase (android:onClick en el XML).
 * - Su diseño visual está en: res/layout/activity_menu_principal.xml
 */
public class ActivityMenuPrincipal extends AppCompatActivity {

    /*
     * MÉTODO: onCreate
     * - Android lo llama automáticamente cuando se crea la pantalla.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu_principal);

        // Deja espacio para que el contenido no quede tapado por las barras del teléfono.
        // (v, insets) -> { ... } es una FUNCIÓN ANÓNIMA (lambda).
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // MÉTODO: irCentrosSalud -> tarjeta "Centros de salud". Abre la pantalla ActivityCentrosSalud (GPS).
    public void irCentrosSalud(View v) {
        Intent intent = new Intent(this, ActivityCentrosSalud.class);
        startActivity(intent);
    }

    // MÉTODO: irCitas -> tarjeta "Agendar cita". Abre la pantalla ActivityAgendarCita.
    public void irCitas(View v) {
        Intent intent = new Intent(this, ActivityAgendarCita.class);
        startActivity(intent);
    }

    // MÉTODO: irHistorial -> tarjeta "Historial médico". Abre la pantalla ActivityHistorial.
    public void irHistorial(View v) {
        Intent intent = new Intent(this, ActivityHistorial.class);
        startActivity(intent);
    }

    // MÉTODO: irPerfil -> tarjeta "Perfil y notificaciones". Abre la pantalla ActivityPerfil.
    public void irPerfil(View v) {
        Intent intent = new Intent(this, ActivityPerfil.class);
        startActivity(intent);
    }
}
