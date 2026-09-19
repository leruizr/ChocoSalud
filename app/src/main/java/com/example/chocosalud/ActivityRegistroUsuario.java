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

public class ActivityRegistroUsuario extends AppCompatActivity {

    private static final int LONGITUD_CONTRASENA = 4;

    private TextInputLayout layoutNombreCompleto, layoutDocumento, layoutCorreo,
            layoutTelefono, layoutContrasena, layoutConfirmarContrasena;
    private TextInputEditText campoNombreCompleto, campoDocumento, campoCorreo,
            campoTelefono, campoContrasena, campoConfirmarContrasena;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registro_usuario);
        AjusteTeclado.aplicar(this,
                findViewById(R.id.main),
                (ScrollView) findViewById(R.id.scrollFormulario),
                (ViewGroup) findViewById(R.id.contenidoFormulario));

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

    public void registrarUsuario(View v) {
        if (formularioValido()) {
            Toast.makeText(this, R.string.datos_validos, Toast.LENGTH_SHORT).show();
        }
    }

    // El registro solo se abre desde el inicio de sesión, así que volver a él es cerrar esta pantalla
    public void irInicioSesion(View v) {
        finish();
    }

    private boolean formularioValido() {
        boolean valido = true;

        if (obtenerTexto(campoNombreCompleto).isEmpty()) {
            layoutNombreCompleto.setError(getString(R.string.error_nombre_requerido));
            valido = false;
        } else {
            layoutNombreCompleto.setError(null);
        }

        if (obtenerTexto(campoDocumento).isEmpty()) {
            layoutDocumento.setError(getString(R.string.error_documento_requerido));
            valido = false;
        } else {
            layoutDocumento.setError(null);
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(obtenerTexto(campoCorreo)).matches()) {
            layoutCorreo.setError(getString(R.string.error_correo_invalido));
            valido = false;
        } else {
            layoutCorreo.setError(null);
        }

        if (obtenerTexto(campoTelefono).isEmpty()) {
            layoutTelefono.setError(getString(R.string.error_telefono_requerido));
            valido = false;
        } else {
            layoutTelefono.setError(null);
        }

        String contrasena = obtenerTexto(campoContrasena);
        if (contrasena.length() != LONGITUD_CONTRASENA) {
            layoutContrasena.setError(getString(R.string.error_contrasena_longitud));
            valido = false;
        } else {
            layoutContrasena.setError(null);
        }

        if (!contrasena.equals(obtenerTexto(campoConfirmarContrasena))) {
            layoutConfirmarContrasena.setError(getString(R.string.error_contrasenas_distintas));
            valido = false;
        } else {
            layoutConfirmarContrasena.setError(null);
        }

        return valido;
    }

    private String obtenerTexto(TextInputEditText campo) {
        return campo.getText() == null ? "" : campo.getText().toString().trim();
    }
}
