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
 * Evita que el teclado tape los campos de un formulario: reserva el espacio del teclado
 * y desplaza el formulario hasta el campo que se está llenando.
 */
final class AjusteTeclado {

    private static final int MARGEN_CAMPO_ENFOCADO_DP = 24;
    private static final long RETRASO_AJUSTE_MS = 150;

    private AjusteTeclado() {
    }

    static void aplicar(AppCompatActivity actividad, View raiz, ScrollView scroll, ViewGroup contenido) {
        // Se incluye el teclado (ime) para que el formulario no quede tapado al escribir
        ViewCompat.setOnApplyWindowInsetsListener(raiz, (v, insets) -> {
            Insets systemBars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Cuando el teclado cambia el alto disponible, se vuelve a mostrar el campo en uso
        scroll.addOnLayoutChangeListener((vista, izq, arr, der, aba, oIzq, oArr, oDer, oAba) -> {
            if ((aba - arr) != (oAba - oArr)) {
                scroll.post(() -> mostrarCampoEnfocado(actividad, scroll, contenido));
            }
        });

        // Al pasar de un campo a otro el teclado puede no cambiar de alto, por eso también se ajusta aquí
        scroll.getViewTreeObserver().addOnGlobalFocusChangeListener((anterior, nuevo) -> {
            if (nuevo instanceof TextInputEditText) {
                scroll.postDelayed(() -> mostrarCampoEnfocado(actividad, scroll, contenido),
                        RETRASO_AJUSTE_MS);
            }
        });
    }

    private static void mostrarCampoEnfocado(AppCompatActivity actividad, ScrollView scroll,
                                             ViewGroup contenido) {
        View campoEnfocado = actividad.getCurrentFocus();
        if (!(campoEnfocado instanceof TextInputEditText)) {
            return;
        }
        Rect zona = new Rect();
        campoEnfocado.getDrawingRect(zona);
        contenido.offsetDescendantRectToMyCoords(campoEnfocado, zona);
        int margen = (int) (MARGEN_CAMPO_ENFOCADO_DP * actividad.getResources().getDisplayMetrics().density);
        zona.top -= margen;
        zona.bottom += margen;
        scroll.requestChildRectangleOnScreen(contenido, zona, false);
    }
}
