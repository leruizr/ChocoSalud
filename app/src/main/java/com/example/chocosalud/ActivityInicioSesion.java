package com.example.chocosalud;

// IMPORTS: traen las clases de Android que usa esta pantalla.
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

/*
 * CLASE: ActivityInicioSesion  ->  PANTALLA 3 (Inicio de sesión)
 *
 * - El usuario escribe correo y contraseña y pulsa "Ingresar".
 * - Primero revisa el formato; luego busca en la base de datos (tabla usuarios) un usuario con
 * - ese correo y esa contraseña. Solo si existe, guarda la sesión y abre el menú principal.
 * - Su diseño visual está en: res/layout/activity_inicio_sesion.xml
 */
public class ActivityInicioSesion extends AppCompatActivity {

    // VARIABLE: baseDatos -> acceso a la base de datos SQLite de la app.
    private BaseDatos baseDatos;

    // CONSTANTE: LONGITUD_CONTRASENA -> la contraseña debe tener 4 caracteres.
    // "static final" = su valor nunca cambia.
    private static final int LONGITUD_CONTRASENA = 4;

    // VARIABLES (atributos de la clase): las cajas de los campos.
    // Un TextInputLayout es la caja con borde que muestra la etiqueta y el mensaje de error.
    private TextInputLayout layoutCorreo, layoutContrasena;

    // VARIABLES (atributos de la clase): donde el usuario escribe el correo y la contraseña.
    private TextInputEditText campoCorreo, campoContrasena;

    /*
     * MÉTODO: onCreate
     * - Android lo llama automáticamente cuando se crea la pantalla.
     * - Aquí se prepara la pantalla y se buscan los campos del diseño.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_inicio_sesion);

        // Llama al MÉTODO aplicar de la clase AjusteTeclado: evita que el teclado tape los campos.
        AjusteTeclado.aplicar(this,
                findViewById(R.id.main),
                (ScrollView) findViewById(R.id.scrollFormulario),
                (ViewGroup) findViewById(R.id.contenidoFormulario));

        // findViewById busca cada elemento del diseño por su id y lo guarda en su variable.
        layoutCorreo = findViewById(R.id.layoutCorreo);
        layoutContrasena = findViewById(R.id.layoutContrasena);
        campoCorreo = findViewById(R.id.campoCorreo);
        campoContrasena = findViewById(R.id.campoContrasena);

        baseDatos = new BaseDatos(this);
    }

    /*
     * MÉTODO: onDestroy
     * - Android lo llama cuando se cierra la pantalla. Se cierra la base de datos para liberarla.
     */
    @Override
    protected void onDestroy() {
        baseDatos.close();
        super.onDestroy();
    }

    /*
     * MÉTODO: iniciarSesion
     * - Se ejecuta al tocar "Ingresar" (android:onClick="iniciarSesion" en el XML).
     * - Si el formulario está bien y el usuario existe en la base de datos, abre el menú principal.
     */
    public void iniciarSesion(View v) {
        if (formularioValido()) {
            // Busca el usuario en la tabla usuarios (SELECT). Devuelve su _id o SIN_USUARIO.
            long usuarioId = baseDatos.iniciarSesion(
                    obtenerTexto(campoCorreo), obtenerTexto(campoContrasena));

            if (usuarioId == BaseDatos.SIN_USUARIO) {
                // No se dice cuál de los dos está mal: así nadie puede averiguar qué correos existen.
                layoutContrasena.setError(getString(R.string.error_credenciales));
                return; // return: termina el método aquí, no abre el menú.
            }

            // Recuerda quién inició sesión, para las pantallas de perfil, citas e historial.
            Sesion.iniciar(this, usuarioId);

            // VARIABLE local: intent -> orden de abrir el menú principal.
            Intent intent = new Intent(this, ActivityMenuPrincipal.class);

            // Estas banderas borran las pantallas anteriores: así el botón Atrás
            // del teléfono ya no vuelve al login, sino que sale de la app.
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }

    /*
     * MÉTODO: irRegistro
     * - Se ejecuta al tocar "¿No tienes cuenta? Regístrate".
     * - Abre la pantalla de registro.
     */
    public void irRegistro(View v) {
        Intent intent = new Intent(this, ActivityRegistroUsuario.class);
        startActivity(intent);
    }

    /*
     * MÉTODO: formularioValido
     * - Revisa los campos y marca en rojo los que estén mal.
     * - DEVUELVE (return): true si todo está bien, false si hay algún error.
     * - "private" = solo se usa dentro de esta clase.
     */
    private boolean formularioValido() {
        // VARIABLE local: valido -> empieza en true y pasa a false si se encuentra un error.
        boolean valido = true;

        // Revisa que el correo tenga forma de correo (algo@dominio.com).
        if (!Patterns.EMAIL_ADDRESS.matcher(obtenerTexto(campoCorreo)).matches()) {
            layoutCorreo.setError(getString(R.string.error_correo_invalido)); // muestra el error en rojo
            valido = false;
        } else {
            layoutCorreo.setError(null); // quita el error
        }

        // Revisa que la contraseña tenga exactamente 4 caracteres.
        if (obtenerTexto(campoContrasena).length() != LONGITUD_CONTRASENA) {
            layoutContrasena.setError(getString(R.string.error_contrasena_longitud));
            valido = false;
        } else {
            layoutContrasena.setError(null);
        }

        return valido;
    }

    /*
     * MÉTODO: obtenerTexto
     * - PARÁMETRO: campo -> el campo del que se quiere leer el texto.
     * - DEVUELVE: el texto escrito, sin espacios al inicio ni al final.
     *   Si el campo está vacío (null), devuelve "" para evitar errores.
     */
    private String obtenerTexto(TextInputEditText campo) {
        return campo.getText() == null ? "" : campo.getText().toString().trim();
    }
}
