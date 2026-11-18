package com.example.chocosalud;

// IMPORTS: traen las clases de Android que usa esta clase.
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

/*
 * CLASE: Notificaciones -> clase de AYUDA (no es una pantalla)
 *
 * - SERVICIO DE NOTIFICACIONES: muestra un aviso en la barra del teléfono cuando el usuario
 *   agenda una cita. Al tocarlo se abre el Historial médico.
 * - Solo se muestra si el usuario tiene activadas las notificaciones en su perfil (columna
 *   notificaciones de la tabla usuarios) y si dio el permiso de notificaciones del teléfono.
 */
final class Notificaciones {

    // CONSTANTE: id del "canal" de notificaciones de citas. Desde Android 8 cada notificación
    // debe pertenecer a un canal; el usuario puede silenciar cada canal desde los ajustes.
    private static final String CANAL_CITAS = "citas";

    // CONSTRUCTOR privado: nadie crea objetos de esta clase; solo se usan sus métodos static.
    private Notificaciones() {}

    /*
     * MÉTODO: necesitaPermiso
     * - Desde Android 13 (API 33, TIRAMISU) hay que pedir el permiso POST_NOTIFICATIONS.
     *   En versiones anteriores las notificaciones están permitidas sin pedir nada.
     */
    static boolean necesitaPermiso() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU;
    }

    // MÉTODO: tienePermiso -> true si la app puede mostrar notificaciones.
    static boolean tienePermiso(Context contexto) {
        if (!necesitaPermiso()) {
            return true;
        }
        return ContextCompat.checkSelfPermission(contexto, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED;
    }

    // MÉTODO: crearCanal -> crea el canal "Recordatorios de citas" (Android 8 o superior).
    // Si el canal ya existe, Android no hace nada, así que se puede llamar varias veces.
    private static void crearCanal(Context contexto) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel canal = new NotificationChannel(CANAL_CITAS,
                    contexto.getString(R.string.canal_citas_nombre),
                    NotificationManager.IMPORTANCE_DEFAULT);
            canal.setDescription(contexto.getString(R.string.canal_citas_descripcion));
            contexto.getSystemService(NotificationManager.class).createNotificationChannel(canal);
        }
    }

    /*
     * MÉTODO: avisarCitaAgendada
     * - Muestra la notificación "Cita agendada" con la especialidad y la fecha.
     * - PARÁMETROS: citaId -> se usa como número de la notificación (una por cita).
     * - @SuppressLint: Android Studio avisa que notify() necesita permiso; ya se revisó arriba
     *   con tienePermiso, por eso se oculta ese aviso.
     */
    @SuppressLint("MissingPermission")
    static void avisarCitaAgendada(Context contexto, long citaId, String especialidad, String fecha) {
        if (!tienePermiso(contexto)) {
            return;
        }
        crearCanal(contexto);

        // PendingIntent: un Intent "guardado" que el sistema ejecuta cuando el usuario toca la
        // notificación. Abre el Historial médico. FLAG_IMMUTABLE es obligatorio desde Android 12.
        Intent intent = new Intent(contexto, ActivityHistorial.class);
        PendingIntent alTocar = PendingIntent.getActivity(contexto, 0, intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notificacion = new NotificationCompat.Builder(contexto, CANAL_CITAS)
                .setSmallIcon(R.drawable.ic_citas)
                .setContentTitle(contexto.getString(R.string.notificacion_cita_titulo))
                .setContentText(contexto.getString(R.string.notificacion_cita_texto, especialidad, fecha))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(alTocar)
                .setAutoCancel(true); // se quita sola al tocarla

        NotificationManagerCompat.from(contexto).notify((int) citaId, notificacion.build());
    }
}
