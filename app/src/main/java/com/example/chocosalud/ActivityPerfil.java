package com.example.chocosalud;

// IMPORTS: traen las clases de Android que usa esta pantalla.
import android.Manifest;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/*
 * CLASE: ActivityPerfil -> PANTALLA 8 (Perfil y notificaciones)
 *
 * - Se abre desde la tarjeta "Perfil y notificaciones" del menú principal.
 * - BASE DE DATOS (Fase 4), tabla usuarios:
 *     SELECT -> muestra los datos del usuario que inició sesión (cargarPerfil)
 *     UPDATE -> guarda los cambios de nombre, documento, correo y teléfono (guardarPerfil),
 *               cambia la contraseña (cambiarContrasena) y la preferencia de notificaciones
 *     DELETE -> elimina la cuenta y, en cascada, sus citas (eliminarCuenta)
 * - También permite cerrar sesión (cerrarSesion).
 * - Su diseño visual está en: res/layout/activity_perfil.xml
 */
public class ActivityPerfil extends AppCompatActivity {

    // CONSTANTE: la contraseña debe tener 4 caracteres (igual que en el registro).
    private static final int LONGITUD_CONTRASENA = 4;

    // VARIABLES: acceso a la base de datos y _id del usuario conectado.
    private BaseDatos baseDatos;
    private long usuarioId;

    // VARIABLES: cajas y campos de la sección "Mis datos".
    private TextInputLayout layoutNombre, layoutDocumento, layoutCorreo, layoutTelefono;
    private TextInputEditText campoNombre, campoDocumento, campoCorreo, campoTelefono;

    // VARIABLES: cajas y campos de la sección "Cambiar contraseña".
    private TextInputLayout layoutActual, layoutNueva, layoutConfirmar;
    private TextInputEditText campoActual, campoNueva, campoConfirmar;

    // VARIABLE: interruptor de notificaciones.
    private MaterialSwitch interruptorNotificaciones;

    // VARIABLE: lanzadorPermiso -> pide el permiso de notificaciones (Android 13 o superior)
    // y recibe la respuesta del usuario (true = lo aceptó).
    private ActivityResultLauncher<String> lanzadorPermiso;

    /*
     * MÉTODO: onCreate
     * - Android lo llama automáticamente cuando se crea la pantalla.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_perfil);

        // Evita que el teclado tape los campos del formulario.
        AjusteTeclado.aplicar(this,
                findViewById(R.id.main),
                (ScrollView) findViewById(R.id.scrollFormulario),
                (ViewGroup) findViewById(R.id.contenidoFormulario));

        baseDatos = new BaseDatos(this);
        usuarioId = Sesion.obtenerUsuarioId(this);

        // Si nadie inició sesión, no hay perfil que mostrar: se vuelve al login.
        if (usuarioId == BaseDatos.SIN_USUARIO) {
            irInicioSesion();
            return;
        }

        layoutNombre = findViewById(R.id.layoutNombreCompleto);
        layoutDocumento = findViewById(R.id.layoutDocumento);
        layoutCorreo = findViewById(R.id.layoutCorreo);
        layoutTelefono = findViewById(R.id.layoutTelefono);
        campoNombre = findViewById(R.id.campoNombreCompleto);
        campoDocumento = findViewById(R.id.campoDocumento);
        campoCorreo = findViewById(R.id.campoCorreo);
        campoTelefono = findViewById(R.id.campoTelefono);

        layoutActual = findViewById(R.id.layoutContrasenaActual);
        layoutNueva = findViewById(R.id.layoutContrasenaNueva);
        layoutConfirmar = findViewById(R.id.layoutConfirmarContrasena);
        campoActual = findViewById(R.id.campoContrasenaActual);
        campoNueva = findViewById(R.id.campoContrasenaNueva);
        campoConfirmar = findViewById(R.id.campoConfirmarContrasena);

        interruptorNotificaciones = findViewById(R.id.interruptorNotificaciones);

        // Registra el lanzador del permiso. Si el usuario NO acepta, se apaga el interruptor.
        lanzadorPermiso = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                concedido -> {
                    if (!concedido) {
                        interruptorNotificaciones.setChecked(false);
                        Toast.makeText(this, R.string.permiso_notificaciones_negado,
                                Toast.LENGTH_LONG).show();
                    }
                });

        cargarPerfil();

        // El "escuchador" del interruptor se pone DESPUÉS de cargar el perfil; si no, al marcar
        // el valor guardado se ejecutaría como si el usuario lo hubiera tocado.
        interruptorNotificaciones.setOnCheckedChangeListener(
                (boton, activo) -> cambiarNotificaciones(activo));
    }

    /*
     * MÉTODO: onDestroy
     * - Se cierra la base de datos al salir de la pantalla.
     */
    @Override
    protected void onDestroy() {
        baseDatos.close();
        super.onDestroy();
    }

    /*
     * MÉTODO: cargarPerfil  (SELECT)
     * - Lee los datos del usuario y los escribe en los campos del formulario.
     */
    private void cargarPerfil() {
        try (Cursor usuario = baseDatos.consultarUsuario(usuarioId)) {
            if (!usuario.moveToFirst()) {
                // El usuario ya no existe (por ejemplo, se borró la base de datos).
                Sesion.cerrar(this);
                irInicioSesion();
                return;
            }
            campoNombre.setText(usuario.getString(usuario.getColumnIndexOrThrow(BaseDatos.USUARIO_NOMBRE)));
            campoDocumento.setText(usuario.getString(usuario.getColumnIndexOrThrow(BaseDatos.USUARIO_DOCUMENTO)));
            campoCorreo.setText(usuario.getString(usuario.getColumnIndexOrThrow(BaseDatos.USUARIO_CORREO)));
            campoTelefono.setText(usuario.getString(usuario.getColumnIndexOrThrow(BaseDatos.USUARIO_TELEFONO)));
            boolean notificaciones = usuario.getInt(
                    usuario.getColumnIndexOrThrow(BaseDatos.USUARIO_NOTIFICACIONES)) == 1;
            // Solo se muestra encendido si además el teléfono da el permiso.
            interruptorNotificaciones.setChecked(notificaciones && Notificaciones.tienePermiso(this));
        }
    }

    /*
     * MÉTODO: guardarPerfil  (UPDATE)
     * - Se ejecuta al tocar "Guardar cambios".
     * - Valida los datos, revisa que el correo y el documento no sean de OTRO usuario y guarda.
     */
    public void guardarPerfil(View v) {
        if (!datosValidos() || !datosDisponibles()) {
            return;
        }
        int filas = baseDatos.actualizarUsuario(usuarioId,
                obtenerTexto(campoNombre), obtenerTexto(campoDocumento),
                obtenerTexto(campoCorreo), obtenerTexto(campoTelefono));
        Toast.makeText(this, filas > 0 ? R.string.perfil_actualizado : R.string.error_actualizar_perfil,
                Toast.LENGTH_SHORT).show();
    }

    /*
     * MÉTODO: datosValidos
     * - Revisa que ningún campo de "Mis datos" esté vacío y que el correo tenga formato válido.
     */
    private boolean datosValidos() {
        boolean valido = true;
        valido &= marcarSiVacio(layoutNombre, campoNombre, R.string.error_nombre_requerido);
        valido &= marcarSiVacio(layoutDocumento, campoDocumento, R.string.error_documento_requerido);
        valido &= marcarSiVacio(layoutTelefono, campoTelefono, R.string.error_telefono_requerido);
        if (!Patterns.EMAIL_ADDRESS.matcher(obtenerTexto(campoCorreo)).matches()) {
            layoutCorreo.setError(getString(R.string.error_correo_invalido));
            valido = false;
        } else {
            layoutCorreo.setError(null);
        }
        return valido;
    }

    /*
     * MÉTODO: datosDisponibles  (SELECT)
     * - El correo y el documento son UNIQUE: no pueden ser iguales a los de otro usuario.
     */
    private boolean datosDisponibles() {
        boolean disponibles = true;
        if (baseDatos.existeCorreoDeOtro(obtenerTexto(campoCorreo), usuarioId)) {
            layoutCorreo.setError(getString(R.string.error_correo_registrado));
            disponibles = false;
        }
        if (baseDatos.existeDocumentoDeOtro(obtenerTexto(campoDocumento), usuarioId)) {
            layoutDocumento.setError(getString(R.string.error_documento_registrado));
            disponibles = false;
        }
        return disponibles;
    }

    /*
     * MÉTODO: cambiarContrasena  (UPDATE)
     * - Se ejecuta al tocar "Cambiar contraseña".
     * - Pide la contraseña actual: así nadie que tome el teléfono desbloqueado puede cambiarla.
     */
    public void cambiarContrasena(View v) {
        String actual = obtenerTexto(campoActual);
        String nueva = obtenerTexto(campoNueva);
        boolean valido = true;

        if (actual.length() != LONGITUD_CONTRASENA) {
            layoutActual.setError(getString(R.string.error_contrasena_longitud));
            valido = false;
        } else {
            layoutActual.setError(null);
        }
        if (nueva.length() != LONGITUD_CONTRASENA) {
            layoutNueva.setError(getString(R.string.error_contrasena_longitud));
            valido = false;
        } else {
            layoutNueva.setError(null);
        }
        if (!nueva.equals(obtenerTexto(campoConfirmar))) {
            layoutConfirmar.setError(getString(R.string.error_contrasenas_distintas));
            valido = false;
        } else {
            layoutConfirmar.setError(null);
        }
        if (!valido) {
            return;
        }

        if (baseDatos.cambiarContrasena(usuarioId, actual, nueva)) {
            Toast.makeText(this, R.string.contrasena_actualizada, Toast.LENGTH_SHORT).show();
            // Se limpian los campos para no dejar la contraseña escrita en pantalla.
            campoActual.setText("");
            campoNueva.setText("");
            campoConfirmar.setText("");
        } else {
            layoutActual.setError(getString(R.string.error_contrasena_actual));
        }
    }

    /*
     * MÉTODO: cambiarNotificaciones  (UPDATE)
     * - Se ejecuta cuando el usuario mueve el interruptor.
     * - Guarda la preferencia en la base de datos y, si la activa en Android 13 o superior
     *   sin tener el permiso, lo pide.
     */
    private void cambiarNotificaciones(boolean activas) {
        baseDatos.actualizarNotificaciones(usuarioId, activas);
        if (activas && !Notificaciones.tienePermiso(this)) {
            lanzadorPermiso.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
        Toast.makeText(this, activas ? R.string.notificaciones_activadas
                : R.string.notificaciones_desactivadas, Toast.LENGTH_SHORT).show();
    }

    /*
     * MÉTODO: cerrarSesion
     * - Se ejecuta al tocar "Cerrar sesión". Olvida al usuario y vuelve al login.
     */
    public void cerrarSesion(View v) {
        Sesion.cerrar(this);
        irInicioSesion();
    }

    /*
     * MÉTODO: confirmarEliminarCuenta
     * - Se ejecuta al tocar "Eliminar mi cuenta". Pregunta antes, porque no se puede deshacer.
     */
    public void confirmarEliminarCuenta(View v) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirmar_eliminar_cuenta_titulo)
                .setMessage(R.string.confirmar_eliminar_cuenta_mensaje)
                .setPositiveButton(R.string.boton_si_eliminar, (dialogo, boton) -> eliminarCuenta())
                .setNegativeButton(R.string.boton_no, null)
                .show();
    }

    /*
     * MÉTODO: eliminarCuenta  (DELETE)
     * - Borra el usuario (y sus citas, por ON DELETE CASCADE), cierra la sesión y vuelve al login.
     */
    private void eliminarCuenta() {
        if (baseDatos.eliminarUsuario(usuarioId) > 0) {
            Toast.makeText(this, R.string.cuenta_eliminada, Toast.LENGTH_LONG).show();
            Sesion.cerrar(this);
            irInicioSesion();
        }
    }

    /*
     * MÉTODO: irInicioSesion
     * - Abre el login y borra las pantallas anteriores: con "Atrás" ya no se vuelve al menú.
     */
    private void irInicioSesion() {
        Intent intent = new Intent(this, ActivityInicioSesion.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /*
     * MÉTODO: marcarSiVacio
     * - Si el campo está vacío, lo marca en rojo con el mensaje indicado.
     * - DEVUELVE: true si el campo tiene texto.
     */
    private boolean marcarSiVacio(TextInputLayout layout, TextInputEditText campo, int mensaje) {
        if (obtenerTexto(campo).isEmpty()) {
            layout.setError(getString(mensaje));
            return false;
        }
        layout.setError(null);
        return true;
    }

    // MÉTODO: obtenerTexto -> el texto del campo sin espacios al inicio ni al final.
    private String obtenerTexto(TextInputEditText campo) {
        return campo.getText() == null ? "" : campo.getText().toString().trim();
    }
}
