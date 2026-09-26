package com.example.chocosalud;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * PANTALLA 2: Registro de usuario.
 *
 * Formulario con seis campos: nombre completo, documento, correo, teléfono, contraseña y
 * confirmación de la contraseña. Al pulsar "Registrarme" se validan los datos y se marca
 * en rojo cada campo que tenga un problema.
 *
 * IMPORTANTE: todavía no hay base de datos, así que si todo está bien solo se muestra un
 * aviso ("Datos validados correctamente"); no se guarda nada. Cuando se agregue SQLite,
 * el guardado del usuario se hará en registrarUsuario(), justo donde aparece el aviso.
 *
 * Diseño: res/layout/activity_registro_usuario.xml
 */
public class ActivityRegistroUsuario extends AppCompatActivity {

    // Constante con la longitud que debe tener la contraseña.
    private static final int LONGITUD_CONTRASENA = 4;

    // Por cada campo hay dos variables: el "layout" (caja con etiqueta y mensaje de error)
    // y el "campo" (el texto que escribe el usuario). Se declaran varias en una sola línea.
    private TextInputLayout layoutNombreCompleto, layoutDocumento, layoutCorreo,
            layoutTelefono, layoutContrasena, layoutConfirmarContrasena;
    private TextInputEditText campoNombreCompleto, campoDocumento, campoCorreo,
            campoTelefono, campoContrasena, campoConfirmarContrasena;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registro_usuario);

        // Evita que el teclado tape los campos (ver AjusteTeclado.java).
        AjusteTeclado.aplicar(this,
                findViewById(R.id.main),
                (ScrollView) findViewById(R.id.scrollFormulario),
                (ViewGroup) findViewById(R.id.contenidoFormulario));

        // Se enlazan las variables con las vistas del diseño usando sus ids.
        layoutNombreCompleto = findViewById(R.id.layoutNombreCompleto);
        layoutDocumento = findViewById(R.id.layoutDocumento);
        layoutCorreo = findViewById(R.id.layoutCorreo);
        layoutTelefono = findViewById(R.id.layoutTelefono);
        layoutContrasena = findViewById(R.id.layoutContrasena);
        layoutConfirmarContrasena = findViewById(R.id.layoutConfirmarContrasena);

        campoNombreCompleto = findViewById(R.id.campoNombreCompleto);
        campoDocumento = findViewById(R.id.campoDocumento);
        campoCorreo = findViewById(R.id.campoCorreo);
        campoTelefono = findViewById(R.id.campoTelefono);
        campoContrasena = findViewById(R.id.campoContrasena);
        campoConfirmarContrasena = findViewById(R.id.campoConfirmarContrasena);
    }

    /**
     * Se ejecuta al pulsar "Registrarme" (android:onClick="registrarUsuario" en el XML).
     */
    public void registrarUsuario(View v) {
        if (formularioValido()) {
            // Toast = mensaje corto que aparece un momento abajo y desaparece solo.
            // Aquí, en el futuro, se guardará el usuario en la base de datos.
            Toast.makeText(this, R.string.datos_validos, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Se ejecuta al pulsar "¿Ya tienes cuenta? Inicia sesión".
     * El registro solo se abre desde el inicio de sesión, así que volver a él es simplemente
     * cerrar esta pantalla: finish() la saca de la pila y aparece la anterior (el login).
     */
    public void irInicioSesion(View v) {
        finish();
    }

    /**
     * Revisa los seis campos y muestra un error en cada uno que esté mal.
     *
     * @return true si todos los campos están bien, false si alguno tiene error
     */
    private boolean formularioValido() {
        // Se supone que todo está bien y se cambia a false al encontrar un error.
        // No se sale al primer error: así el usuario ve todos sus errores de una vez.
        boolean valido = true;

        // Nombre: no puede estar vacío.
        if (obtenerTexto(campoNombreCompleto).isEmpty()) {
            layoutNombreCompleto.setError(getString(R.string.error_nombre_requerido));
            valido = false;
        } else {
            layoutNombreCompleto.setError(null); // quita el error si ya estaba corregido
        }

        // Documento: no puede estar vacío (el teclado ya solo deja escribir números).
        if (obtenerTexto(campoDocumento).isEmpty()) {
            layoutDocumento.setError(getString(R.string.error_documento_requerido));
            valido = false;
        } else {
            layoutDocumento.setError(null);
        }

        // Correo: debe tener forma de correo. Patterns.EMAIL_ADDRESS es una expresión regular
        // que ya trae Android para reconocer direcciones como algo@dominio.com.
        if (!Patterns.EMAIL_ADDRESS.matcher(obtenerTexto(campoCorreo)).matches()) {
            layoutCorreo.setError(getString(R.string.error_correo_invalido));
            valido = false;
        } else {
            layoutCorreo.setError(null);
        }

        // Teléfono: no puede estar vacío.
        if (obtenerTexto(campoTelefono).isEmpty()) {
            layoutTelefono.setError(getString(R.string.error_telefono_requerido));
            valido = false;
        } else {
            layoutTelefono.setError(null);
        }

        // Contraseña: exactamente 4 caracteres. En el XML el campo además tiene
        // android:maxLength="4", así que no deja escribir más; aquí se controla que no falten.
        String contrasena = obtenerTexto(campoContrasena);
        if (contrasena.length() != LONGITUD_CONTRASENA) {
            layoutContrasena.setError(getString(R.string.error_contrasena_longitud));
            valido = false;
        } else {
            layoutContrasena.setError(null);
        }

        // Confirmación: debe ser igual a la contraseña. Para comparar textos en Java se usa
        // equals(); el operador == compararía si son el mismo objeto, no si dicen lo mismo.
        if (!contrasena.equals(obtenerTexto(campoConfirmarContrasena))) {
            layoutConfirmarContrasena.setError(getString(R.string.error_contrasenas_distintas));
            valido = false;
        } else {
            layoutConfirmarContrasena.setError(null);
        }

        return valido;
    }

    /**
     * Devuelve el texto de un campo sin espacios al inicio ni al final (trim).
     * Si el campo no tiene texto (null), devuelve un texto vacío para no tener errores.
     */
    private String obtenerTexto(TextInputEditText campo) {
        return campo.getText() == null ? "" : campo.getText().toString().trim();
    }
}
