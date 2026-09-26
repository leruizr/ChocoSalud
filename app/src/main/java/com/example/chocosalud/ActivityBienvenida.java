package com.example.chocosalud;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * PANTALLA 1: Bienvenida.
 *
 * Es la primera pantalla que ve el usuario: en AndroidManifest.xml está marcada como
 * LAUNCHER, que significa "la pantalla con la que arranca la app".
 * Muestra el logo, el nombre de la app, una descripción corta y el botón "Comenzar",
 * que lleva a la pantalla de inicio de sesión.
 *
 * En Android cada pantalla es una clase que hereda de AppCompatActivity (una "Activity").
 * Esta clase es la LÓGICA (Java); el DISEÑO está en res/layout/activity_bienvenida.xml.
 */
public class ActivityBienvenida extends AppCompatActivity {

    /**
     * onCreate se ejecuta una sola vez, cuando Android crea la pantalla.
     * Aquí se prepara todo lo que la pantalla necesita.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Siempre se llama primero al onCreate de la clase padre.
        super.onCreate(savedInstanceState);

        // Hace que la app se dibuje "de borde a borde", también por debajo de la barra de
        // estado (arriba: hora, batería) y de la barra de navegación (abajo).
        EdgeToEdge.enable(this);

        // Enlaza esta clase con su diseño XML. R.layout.activity_bienvenida es una referencia
        // al archivo res/layout/activity_bienvenida.xml (la clase R la genera Android solo).
        setContentView(R.layout.activity_bienvenida);

        // Como la app se dibuja de borde a borde, el contenido quedaría tapado por las barras
        // del sistema. Este bloque le pregunta a Android cuánto miden esas barras ("insets")
        // y le pone al diseño un relleno (padding) de ese tamaño para que nada quede debajo.
        //  - findViewById(R.id.main): busca en el diseño la vista con id "main" (la raíz).
        //  - (v, insets) -> { ... }: función que Android llama cuando conoce esas medidas.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /**
     * Se ejecuta al pulsar el botón "Comenzar".
     * Está conectado en el XML con el atributo android:onClick="irInicioSesion":
     * Android busca en esta clase un método público con ese nombre y que reciba un View.
     */
    public void irInicioSesion(View v) {
        // Un Intent es una "intención": aquí decimos "quiero ir desde esta pantalla (this)
        // hasta la pantalla ActivityInicioSesion".
        Intent intent = new Intent(this, ActivityInicioSesion.class);
        // startActivity abre la pantalla. La de bienvenida queda debajo, y el botón Atrás
        // del teléfono regresa a ella.
        startActivity(intent);
    }
}
