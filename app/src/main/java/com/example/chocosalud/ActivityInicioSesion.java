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

public class ActivityInicioSesion extends AppCompatActivity {

    private static final int LONGITUD_CONTRASENA = 4;

    private TextInputLayout layoutCorreo, layoutContrasena;
    private TextInputEditText campoCorreo, campoContrasena;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_inicio_sesion);
        AjusteTeclado.aplicar(this,
                findViewById(R.id.main),
                (ScrollView) findViewById(R.id.scrollFormulario),
                (ViewGroup) findViewById(R.id.contenidoFormulario));

        layoutCorreo = findViewById(R.id.layoutCorreo);
        layoutContrasena = findViewById(R.id.layoutContrasena);
        campoCorreo = findViewById(R.id.campoCorreo);
        campoContrasena = findViewById(R.id.campoContrasena);
    }

    public void iniciarSesion(View v) {
        if (formularioValido()) {
            // Aún no hay base de datos: se entra con cualquier dato válido. El menú pasa a ser la raíz
            // de la app, así que se limpia la pila para que "atrás" no regrese al login.
            Intent intent = new Intent(this, ActivityMenuPrincipal.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }

    public void irRegistro(View v) {
        Intent intent = new Intent(this, ActivityRegistroUsuario.class);
        startActivity(intent);
    }

    private boolean formularioValido() {
        boolean valido = true;

        if (!Patterns.EMAIL_ADDRESS.matcher(obtenerTexto(campoCorreo)).matches()) {
            layoutCorreo.setError(getString(R.string.error_correo_invalido));
            valido = false;
        } else {
            layoutCorreo.setError(null);
        }

        if (obtenerTexto(campoContrasena).length() != LONGITUD_CONTRASENA) {
            layoutContrasena.setError(getString(R.string.error_contrasena_longitud));
            valido = false;
        } else {
            layoutContrasena.setError(null);
        }

        return valido;
    }

    private String obtenerTexto(TextInputEditText campo) {
        return campo.getText() == null ? "" : campo.getText().toString().trim();
    }
}
