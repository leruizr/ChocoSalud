package com.example.chocosalud;

// IMPORTS: traen las clases de Android que usa esta clase.
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;

/*
 * CLASE: AjusteTeclado  ->  clase de AYUDA (no es una pantalla)
 *
 * - PROBLEMA: el teclado del teléfono tapaba los campos de los formularios.
 * - SOLUCIÓN: cuando aparece el teclado, el formulario se achica y se desplaza
 *   hasta el campo que el usuario está llenando.
 * - La usan el inicio de sesión y el registro, así no se repite el mismo código.
 */
final class AjusteTeclado {

    // CONSTANTE: espacio (en dp) que se deja alrededor del campo para que no quede pegado al teclado.
    private static final int MARGEN_CAMPO_ENFOCADO_DP = 24;

    // CONSTANTE: tiempo de espera (milisegundos) antes de desplazar, para que el teclado termine de abrir.
    private static final long RETRASO_AJUSTE_MS = 150;

    // CONSTRUCTOR privado: nadie crea objetos de esta clase; solo se usan sus métodos static.
    private AjusteTeclado() {
    }

    /*
     * MÉTODO: aplicar  (static: se llama así -> AjusteTeclado.aplicar(...))
     * - PARÁMETROS:
     *     actividad -> la pantalla donde se usa
     *     raiz      -> el contenedor principal del diseño (id "main")
     *     scroll    -> el ScrollView que permite desplazar el formulario
     *     contenido -> el contenedor que tiene los campos
     * - Tiene 3 pasos, cada uno es una FUNCIÓN ANÓNIMA (lambda) que Android ejecuta cuando pasa algo.
     */
    static void aplicar(AppCompatActivity actividad, View raiz, ScrollView scroll, ViewGroup contenido) {

        // PASO 1: cuando aparece el teclado, se reserva su espacio (Type.ime = el teclado)
        // junto con el de las barras del sistema. Así el formulario se achica y queda visible.
        ViewCompat.setOnApplyWindowInsetsListener(raiz, (v, insets) -> {
            Insets systemBars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // PASO 2: si cambia el alto del formulario (porque se abrió o cerró el teclado),
        // se desplaza hasta el campo que se está llenando.
        scroll.addOnLayoutChangeListener((vista, izq, arr, der, aba, oIzq, oArr, oDer, oAba) -> {
            if ((aba - arr) != (oAba - oArr)) {
                scroll.post(() -> mostrarCampoEnfocado(actividad, scroll, contenido));
            }
        });

        // PASO 3: cuando el usuario pasa de un campo a otro, también se desplaza hasta el nuevo campo.
        scroll.getViewTreeObserver().addOnGlobalFocusChangeListener((anterior, nuevo) -> {
            if (nuevo instanceof TextInputEditText) {
                scroll.postDelayed(() -> mostrarCampoEnfocado(actividad, scroll, contenido),
                        RETRASO_AJUSTE_MS);
            }
        });
    }

    /*
     * MÉTODO: mostrarCampoEnfocado
     * - Mueve el formulario hasta que el campo que tiene el cursor se vea completo.
     */
    private static void mostrarCampoEnfocado(AppCompatActivity actividad, ScrollView scroll,
                                             ViewGroup contenido) {
        // VARIABLE local: campoEnfocado -> el campo que tiene el cursor en este momento.
        View campoEnfocado = actividad.getCurrentFocus();
        if (!(campoEnfocado instanceof TextInputEditText)) {
            return; // si no hay un campo de texto con el cursor, no hace nada
        }

        // VARIABLE local: zona -> un rectángulo con la posición del campo dentro del formulario.
        Rect zona = new Rect();
        campoEnfocado.getDrawingRect(zona);
        contenido.offsetDescendantRectToMyCoords(campoEnfocado, zona);

        // VARIABLE local: margen -> la constante de 24 dp convertida a píxeles de la pantalla.
        int margen = (int) (MARGEN_CAMPO_ENFOCADO_DP * actividad.getResources().getDisplayMetrics().density);
        zona.top -= margen;
        zona.bottom += margen;

        // Le pide al ScrollView que se desplace hasta que esa zona sea visible.
        scroll.requestChildRectangleOnScreen(contenido, zona, false);
    }
}
