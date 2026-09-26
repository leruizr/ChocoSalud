package com.example.chocosalud;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;

/**
 * PANTALLA 4: Menú principal.
 *
 * Es el punto de partida una vez que el usuario inició sesión. Muestra cuatro tarjetas:
 * Centros de salud, Agendar cita, Historial médico y Perfil y notificaciones.
 * Cada tarjeta llama a un método de esta clase cuando se toca (android:onClick en el XML).
 *
 * Es la pantalla "raíz" de la app: el login la abre borrando las pantallas anteriores,
 * por eso el botón Atrás del teléfono sale de la app.
 *
 * Diseño: res/layout/activity_menu_principal.xml
 */
public class ActivityMenuPrincipal extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu_principal);

        // Reserva el espacio de las barras del sistema (estado y navegación) para que el
        // contenido no quede debajo de ellas. Igual que en ActivityBienvenida.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // Tarjeta "Centros de salud" (pantalla 5). Esa pantalla aún no existe: por ahora solo se
    // avisa al usuario. Cuando se cree, aquí se abrirá con un Intent, como hace irCitas.
    public void irCentrosSalud(View v) {
        mostrarProximamente();
    }

    // Tarjeta "Agendar cita": abre la pantalla de agendar cita (ActivityAgendarCita).
    public void irCitas(View v) {
        Intent intent = new Intent(this, ActivityAgendarCita.class);
        startActivity(intent);
    }

    // Tarjeta "Historial médico" (pantalla 7): pendiente, por ahora solo avisa.
    public void irHistorial(View v) {
        mostrarProximamente();
    }

    // Tarjeta "Perfil y notificaciones" (pantalla 8): pendiente, por ahora solo avisa.
    public void irPerfil(View v) {
        mostrarProximamente();
    }

    // Método auxiliar para no repetir el mismo código en cada tarjeta pendiente.
    // Toast = mensaje corto que aparece un momento abajo y desaparece solo.
    private void mostrarProximamente() {
        Toast.makeText(this, R.string.proximamente, Toast.LENGTH_SHORT).show();
    }
}
