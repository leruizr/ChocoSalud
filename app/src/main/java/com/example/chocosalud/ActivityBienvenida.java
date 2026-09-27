package com.example.chocosalud;

// IMPORTS: traen las clases de Android que usa esta pantalla.
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/*
 * CLASE: ActivityBienvenida  ->  PANTALLA 1 (Bienvenida)
 *
 * - Es la primera pantalla que se abre (así está marcada en AndroidManifest.xml).
 * - "extends AppCompatActivity" = HERENCIA: esta clase hereda todo lo que tiene una pantalla de Android.
 * - Su diseño visual está en: res/layout/activity_bienvenida.xml
 */
public class ActivityBienvenida extends AppCompatActivity {

    /*
     * MÉTODO: onCreate
     * - Android lo llama automáticamente UNA vez, cuando crea la pantalla.
     * - @Override = estamos reescribiendo un método que viene de la clase padre (AppCompatActivity).
     * - PARÁMETRO: savedInstanceState -> datos guardados de la pantalla (aquí no lo usamos).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Llama al onCreate original de la clase padre (siempre va primero).
        super.onCreate(savedInstanceState);

        // Hace que la app ocupe toda la pantalla, también detrás de la barra de arriba y la de abajo.
        EdgeToEdge.enable(this);

        // Conecta esta clase con su diseño XML (activity_bienvenida.xml).
        setContentView(R.layout.activity_bienvenida);

        // Deja un espacio arriba y abajo para que el contenido no quede tapado por las barras del teléfono.
        // (v, insets) -> { ... } es una FUNCIÓN ANÓNIMA (lambda): una función sin nombre que Android ejecuta
        // cuando conoce el tamaño de esas barras.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            // VARIABLE local: systemBars -> guarda el tamaño de las barras del sistema.
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /*
     * MÉTODO: irInicioSesion
     * - Se ejecuta al tocar el botón "Comenzar".
     * - Está conectado en el XML con: android:onClick="irInicioSesion"
     * - PARÁMETRO: v -> el botón que se tocó.
     */
    public void irInicioSesion(View v) {
        // VARIABLE local: intent -> un Intent es la "orden" de ir de esta pantalla a otra.
        Intent intent = new Intent(this, ActivityInicioSesion.class);
        // Abre la pantalla de inicio de sesión.
        startActivity(intent);
    }
}
