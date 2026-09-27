package com.example.chocosalud;

// IMPORTS: traen las clases de Android que usa esta pantalla.
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

/*
 * CLASE: ActivityRegistroUsuario  ->  PANTALLA 2 (Registro de usuario)
 *
 * - Formulario de 6 campos: nombre, documento, correo, teléfono, contraseña y confirmación.
 * - Al tocar "Registrarme" revisa los datos y marca en rojo los que estén mal.
 * - Todavía NO guarda nada (la base de datos SQLite se agrega más adelante).
 * - Su diseño visual está en: res/layout/activity_registro_usuario.xml
 */
public class ActivityRegistroUsuario extends AppCompatActivity {

    // CONSTANTE: LONGITUD_CONTRASENA -> la contraseña debe tener 4 caracteres.
    private static final int LONGITUD_CONTRASENA = 4;

    // VARIABLES (atributos de la clase): las cajas de cada campo (etiqueta y mensaje de error).
    private TextInputLayout layoutNombreCompleto, layoutDocumento, layoutCorreo,
            layoutTelefono, layoutContrasena, layoutConfirmarContrasena;

    // VARIABLES (atributos de la clase): donde el usuario escribe cada dato.
    private TextInputEditText campoNombreCompleto, campoDocumento, campoCorreo,
            campoTelefono, campoContrasena, campoConfirmarContrasena;

    /*
     * MÉTODO: onCreate
     * - Android lo llama automáticamente cuando se crea la pantalla.
     * - Prepara la pantalla y busca los 6 campos del diseño.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registro_usuario);

        // Llama al MÉTODO aplicar de AjusteTeclado: evita que el teclado tape los campos.
        AjusteTeclado.aplicar(this,
                findViewById(R.id.main),
                (ScrollView) findViewById(R.id.scrollFormulario),
                (ViewGroup) findViewById(R.id.contenidoFormulario));

        // findViewById busca cada caja del diseño por su id y la guarda en su variable.
        layoutNombreCompleto = findViewById(R.id.layoutNombreCompleto);
        layoutDocumento = findViewById(R.id.layoutDocumento);
        layoutCorreo = findViewById(R.id.layoutCorreo);
        layoutTelefono = findViewById(R.id.layoutTelefono);
        layoutContrasena = findViewById(R.id.layoutContrasena);
        layoutConfirmarContrasena = findViewById(R.id.layoutConfirmarContrasena);

        // Lo mismo para los campos donde se escribe.
        campoNombreCompleto = findViewById(R.id.campoNombreCompleto);
        campoDocumento = findViewById(R.id.campoDocumento);
        campoCorreo = findViewById(R.id.campoCorreo);
        campoTelefono = findViewById(R.id.campoTelefono);
        campoContrasena = findViewById(R.id.campoContrasena);
        campoConfirmarContrasena = findViewById(R.id.campoConfirmarContrasena);
    }

    /*
     * MÉTODO: registrarUsuario
     * - Se ejecuta al tocar "Registrarme" (android:onClick="registrarUsuario" en el XML).
     * - Si todo está bien muestra un mensaje. Aquí se guardará el usuario cuando exista la base de datos.
     */
    public void registrarUsuario(View v) {
        if (formularioValido()) {
            // Toast = mensaje corto que aparece abajo unos segundos.
            Toast.makeText(this, R.string.datos_validos, Toast.LENGTH_SHORT).show();
        }
    }

    /*
     * MÉTODO: irInicioSesion
     * - Se ejecuta al tocar "¿Ya tienes cuenta? Inicia sesión".
     * - finish() cierra esta pantalla y se vuelve a la anterior (el login).
     */
    public void irInicioSesion(View v) {
        finish();
    }

    /*
     * MÉTODO: formularioValido
     * - Revisa los 6 campos y marca en rojo los que estén mal.
     * - DEVUELVE: true si todo está bien, false si hay algún error.
     */
    private boolean formularioValido() {
        // VARIABLE local: valido -> empieza en true y pasa a false si algo está mal.
        boolean valido = true;

        // 1. Nombre: no puede estar vacío.
        if (obtenerTexto(campoNombreCompleto).isEmpty()) {
            layoutNombreCompleto.setError(getString(R.string.error_nombre_requerido));
            valido = false;
        } else {
            layoutNombreCompleto.setError(null);
        }

        // 2. Documento: no puede estar vacío.
        if (obtenerTexto(campoDocumento).isEmpty()) {
            layoutDocumento.setError(getString(R.string.error_documento_requerido));
            valido = false;
        } else {
            layoutDocumento.setError(null);
        }

        // 3. Correo: debe tener forma de correo (algo@dominio.com).
        if (!Patterns.EMAIL_ADDRESS.matcher(obtenerTexto(campoCorreo)).matches()) {
            layoutCorreo.setError(getString(R.string.error_correo_invalido));
            valido = false;
        } else {
            layoutCorreo.setError(null);
        }

        // 4. Teléfono: no puede estar vacío.
        if (obtenerTexto(campoTelefono).isEmpty()) {
            layoutTelefono.setError(getString(R.string.error_telefono_requerido));
            valido = false;
        } else {
            layoutTelefono.setError(null);
        }

        // 5. Contraseña: exactamente 4 caracteres.
        // VARIABLE local: contrasena -> guarda la contraseña para usarla también en el paso 6.
        String contrasena = obtenerTexto(campoContrasena);
        if (contrasena.length() != LONGITUD_CONTRASENA) {
            layoutContrasena.setError(getString(R.string.error_contrasena_longitud));
            valido = false;
        } else {
            layoutContrasena.setError(null);
        }

        // 6. Confirmación: debe ser igual a la contraseña.
        // En Java los textos se comparan con equals(), no con ==.
        if (!contrasena.equals(obtenerTexto(campoConfirmarContrasena))) {
            layoutConfirmarContrasena.setError(getString(R.string.error_contrasenas_distintas));
            valido = false;
        } else {
            layoutConfirmarContrasena.setError(null);
        }

        return valido;
    }

    /*
     * MÉTODO: obtenerTexto
     * - PARÁMETRO: campo -> el campo del que se quiere leer el texto.
     * - DEVUELVE: el texto escrito sin espacios al inicio ni al final ("" si está vacío).
     */
    private String obtenerTexto(TextInputEditText campo) {
        return campo.getText() == null ? "" : campo.getText().toString().trim();
    }
}
