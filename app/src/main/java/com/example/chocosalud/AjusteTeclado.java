package com.example.chocosalud;

import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;

/**
 * Ayuda compartida: evita que el teclado del teléfono tape los campos de un formulario.
 *
 * EL PROBLEMA: como la app se dibuja de borde a borde (EdgeToEdge), Android ya no achica la
 * pantalla cuando aparece el teclado. El teclado se dibuja ENCIMA y tapa los campos de abajo.
 *
 * LA SOLUCIÓN (tres pasos, uno en cada bloque del método aplicar):
 *  1. Reservar el espacio del teclado para que el formulario se achique.
 *  2. Volver a mostrar el campo en uso cuando cambia el alto disponible.
 *  3. Volver a mostrarlo también cuando el usuario pasa de un campo a otro.
 *
 * La usan ActivityInicioSesion y ActivityRegistroUsuario. Al tenerla aparte no se repite
 * el mismo código en cada pantalla con formulario.
 */
final class AjusteTeclado {

    // Espacio (en dp) que se deja alrededor del campo enfocado para que no quede pegado
    // al borde del teclado.
    private static final int MARGEN_CAMPO_ENFOCADO_DP = 24;

    // Al cambiar de campo, se espera un instante (milisegundos) antes de desplazar para dar
    // tiempo a que Android termine de acomodar el teclado.
    private static final long RETRASO_AJUSTE_MS = 150;

    // Constructor privado: es una clase de utilidad (solo métodos static) y nadie debe
    // crear objetos de ella.
    private AjusteTeclado() {
    }

    /**
     * Activa el ajuste del teclado en una pantalla con formulario.
     *
     * @param actividad la pantalla donde se usa
     * @param raiz      la vista raíz del diseño (la que tiene id "main")
     * @param scroll    el ScrollView que contiene el formulario
     * @param contenido el layout que está dentro del ScrollView y tiene los campos
     */
    static void aplicar(AppCompatActivity actividad, View raiz, ScrollView scroll, ViewGroup contenido) {

        // PASO 1: reservar el espacio del teclado.
        // Igual que en las demás pantallas se reserva el espacio de las barras del sistema,
        // pero aquí se suma también el teclado (Type.ime). El operador | junta ambos tipos.
        // Con ese relleno abajo, el ScrollView queda achicado y visible por encima del teclado.
        ViewCompat.setOnApplyWindowInsetsListener(raiz, (v, insets) -> {
            Insets systemBars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // PASO 2: cuando el teclado aparece o desaparece, el alto del ScrollView cambia.
        // Este "escuchador" se dispara en cada cambio de tamaño; si la altura es distinta a la
        // anterior (aba - arr es el alto nuevo, oAba - oArr el alto viejo), se vuelve a mostrar
        // el campo que el usuario está llenando.
        // post(...) espera a que Android termine de acomodar la pantalla antes de desplazar.
        scroll.addOnLayoutChangeListener((vista, izq, arr, der, aba, oIzq, oArr, oDer, oAba) -> {
            if ((aba - arr) != (oAba - oArr)) {
                scroll.post(() -> mostrarCampoEnfocado(actividad, scroll, contenido));
            }
        });

        // PASO 3: al pasar de un campo a otro (por ejemplo con el botón "Sig." del teclado)
        // el teclado puede quedar del mismo alto, y entonces el paso 2 no se dispara.
        // Este escuchador avisa cada vez que cambia el campo con el cursor, y si el nuevo
        // es un campo de texto, se desplaza hasta él.
        scroll.getViewTreeObserver().addOnGlobalFocusChangeListener((anterior, nuevo) -> {
            if (nuevo instanceof TextInputEditText) {
                scroll.postDelayed(() -> mostrarCampoEnfocado(actividad, scroll, contenido),
                        RETRASO_AJUSTE_MS);
            }
        });
    }

    /**
     * Desplaza el ScrollView lo justo para que el campo con el cursor quede completo
     * dentro de la zona visible, con un pequeño margen arriba y abajo.
     */
    private static void mostrarCampoEnfocado(AppCompatActivity actividad, ScrollView scroll,
                                             ViewGroup contenido) {
        // getCurrentFocus() devuelve la vista que tiene el cursor en este momento.
        View campoEnfocado = actividad.getCurrentFocus();
        if (!(campoEnfocado instanceof TextInputEditText)) {
            return; // no hay un campo de texto enfocado: no hay nada que mostrar
        }

        // Rect es un rectángulo. Primero se toma el rectángulo del campo en sus propias
        // coordenadas, y luego se convierte a las coordenadas del layout contenedor,
        // que es lo que necesita el ScrollView.
        Rect zona = new Rect();
        campoEnfocado.getDrawingRect(zona);
        contenido.offsetDescendantRectToMyCoords(campoEnfocado, zona);

        // Se convierte el margen de dp (unidad independiente del tamaño de pantalla) a píxeles,
        // multiplicando por la densidad de la pantalla.
        int margen = (int) (MARGEN_CAMPO_ENFOCADO_DP * actividad.getResources().getDisplayMetrics().density);
        zona.top -= margen;
        zona.bottom += margen;

        // Le pide al ScrollView que se desplace hasta que ese rectángulo sea visible.
        // "false" = con animación suave (no salta de golpe).
        scroll.requestChildRectangleOnScreen(contenido, zona, false);
    }
}
