package com.example.chocosalud;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * PANTALLA 3: Inicio de sesión.
 *
 * El usuario escribe su correo y su contraseña. Al pulsar "Ingresar" se validan los datos
 * y, si están bien, se abre el menú principal. También hay un enlace para ir al registro.
 *
 * IMPORTANTE: todavía no hay base de datos, así que solo se valida el FORMATO de los datos
 * (correo con forma de correo, contraseña de 4 caracteres). No se comprueba que el usuario
 * exista. Cuando se agregue SQLite, aquí se consultará la base de datos.
 *
 * Diseño: res/layout/activity_inicio_sesion.xml
 */
public class ActivityInicioSesion extends AppCompatActivity {

    // "static final" = constante: un valor que no cambia. Así, si mañana la contraseña
    // pasa a tener 6 caracteres, se cambia en un solo lugar.
    private static final int LONGITUD_CONTRASENA = 4;

    // Cada campo del diseño tiene DOS piezas:
    //  - TextInputLayout: la "caja" con borde, la etiqueta que flota y el mensaje de error.
    //  - TextInputEditText: el texto que escribe el usuario.
    private TextInputLayout layoutCorreo, layoutContrasena;
    private TextInputEditText campoCorreo, campoContrasena;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_inicio_sesion);

        // Activa el ajuste del teclado (ver AjusteTeclado.java): evita que el teclado tape
        // los campos. Reemplaza al bloque de "insets" que usan las pantallas sin formulario.
        AjusteTeclado.aplicar(this,
                findViewById(R.id.main),
                (ScrollView) findViewById(R.id.scrollFormulario),
                (ViewGroup) findViewById(R.id.contenidoFormulario));

        // Se busca cada vista del diseño por su id y se guarda en una variable para poder
        // usarla después. Los ids salen del XML: android:id="@+id/campoCorreo" -> R.id.campoCorreo
        layoutCorreo = findViewById(R.id.layoutCorreo);
        layoutContrasena = findViewById(R.id.layoutContrasena);
        campoCorreo = findViewById(R.id.campoCorreo);
        campoContrasena = findViewById(R.id.campoContrasena);
    }

    /**
     * Se ejecuta al pulsar "Ingresar" (android:onClick="iniciarSesion" en el XML).
     */
    public void iniciarSesion(View v) {
        if (formularioValido()) {
            // Aún no hay base de datos: se entra con cualquier dato válido.
            Intent intent = new Intent(this, ActivityMenuPrincipal.class);

            // Estas "banderas" (flags) cambian cómo se abre la pantalla:
            //  - CLEAR_TASK: borra todas las pantallas anteriores (bienvenida, login...).
            //  - NEW_TASK: empieza una pila de pantallas nueva.
            // Resultado: el menú pasa a ser la pantalla raíz, y el botón Atrás del teléfono
            // sale de la app en vez de volver al login.
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }

    /**
     * Se ejecuta al pulsar "¿No tienes cuenta? Regístrate".
     * Esta vez la pantalla de login NO se cierra, así que Atrás desde el registro regresa aquí.
     */
    public void irRegistro(View v) {
        Intent intent = new Intent(this, ActivityRegistroUsuario.class);
        startActivity(intent);
    }

    /**
     * Revisa los campos y muestra un mensaje de error en cada uno que esté mal.
     *
     * @return true si todos los campos están bien, false si alguno tiene error
     */
    private boolean formularioValido() {
        // Se empieza suponiendo que todo está bien y se cambia a false al encontrar un error.
        // No se sale al primer error a propósito: así el usuario ve TODOS los errores a la vez.
        boolean valido = true;

        // Patterns.EMAIL_ADDRESS es una expresión regular que trae Android para reconocer
        // si un texto tiene forma de correo (algo@dominio.com). matches() devuelve true/false.
        if (!Patterns.EMAIL_ADDRESS.matcher(obtenerTexto(campoCorreo)).matches()) {
            // setError muestra el mensaje en rojo debajo del campo.
            layoutCorreo.setError(getString(R.string.error_correo_invalido));
            valido = false;
        } else {
            // setError(null) quita el error si antes lo había y ya se corrigió.
            layoutCorreo.setError(null);
        }

        // La contraseña debe tener exactamente 4 caracteres.
        if (obtenerTexto(campoContrasena).length() != LONGITUD_CONTRASENA) {
            layoutContrasena.setError(getString(R.string.error_contrasena_longitud));
            valido = false;
        } else {
            layoutContrasena.setError(null);
        }

        return valido;
    }

    /**
     * Devuelve el texto de un campo sin espacios al inicio ni al final (trim).
     * Si por alguna razón el campo no tiene texto (null), devuelve un texto vacío.
     * Así el resto del código nunca falla por un valor nulo.
     */
    private String obtenerTexto(TextInputEditText campo) {
        return campo.getText() == null ? "" : campo.getText().toString().trim();
    }
}
