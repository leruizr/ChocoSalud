package com.example.chocosalud;

import android.content.Context;
import android.content.SharedPreferences;

/*
 * CLASE: Sesion -> clase de AYUDA (no es una pantalla)
 *
 * - Recuerda QUÉ usuario inició sesión, guardando su _id de la tabla usuarios.
 * - Las demás pantallas (perfil, citas, historial) llaman a Sesion.obtenerUsuarioId(this)
 *   para saber de quién son los datos que deben mostrar o guardar.
 */
final class Sesion {

    // CONSTANTES: nombre del archivo de preferencias y la llave donde se guarda el _id.
    private static final String ARCHIVO = "sesion";
    private static final String LLAVE_USUARIO_ID = "usuario_id";

    // CONSTRUCTOR privado: nadie crea objetos de esta clase; solo se usan sus métodos static.
    private Sesion() {}

    // MÉTODO: iniciar -> guarda el _id del usuario que acaba de iniciar sesión.
    static void iniciar(Context contexto, long usuarioId) {
        // edit() abre el archivo para escribir; apply() guarda los cambios.
        preferencias(contexto).edit().putLong(LLAVE_USUARIO_ID, usuarioId).apply();
    }

    // MÉTODO: obtenerUsuarioId -> el _id del usuario conectado, o BaseDatos.SIN_USUARIO si no hay nadie.
    static long obtenerUsuarioId(Context contexto) {
        return preferencias(contexto).getLong(LLAVE_USUARIO_ID, BaseDatos.SIN_USUARIO);
    }

    // MÉTODO: cerrar -> borra el usuario guardado (para el botón "Cerrar sesión" del perfil).
    static void cerrar(Context contexto) {
        preferencias(contexto).edit().remove(LLAVE_USUARIO_ID).apply();
    }

    // MÉTODO: preferencias -> abre el archivo "sesion". MODE_PRIVATE: solo esta app puede leerlo.
    private static SharedPreferences preferencias(Context contexto) {
        return contexto.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE);
    }
}