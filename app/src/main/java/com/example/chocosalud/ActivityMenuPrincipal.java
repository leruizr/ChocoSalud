package com.example.chocosalud;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ActivityMenuPrincipal extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu_principal);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // Las pantallas de destino aún no existen; por ahora solo se avisa al usuario
    public void irCentrosSalud(View v) {
        mostrarProximamente();
    }

    public void irCitas(View v) {
        mostrarProximamente();
    }

    public void irHistorial(View v) {
        mostrarProximamente();
    }

    public void irPerfil(View v) {
        mostrarProximamente();
    }

    private void mostrarProximamente() {
        Toast.makeText(this, R.string.proximamente, Toast.LENGTH_SHORT).show();
    }
}
