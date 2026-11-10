package com.example.chocosalud;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class BaseDatos extends SQLiteOpenHelper {

    // CONSTANTES: nombre del archivo y versión de la base de datos.
    // Si se cambia la estructura de una tabla, hay que SUBIR la versión (2, 3...) para que
    // Android ejecute onUpgrade en los teléfonos que ya tienen la app instalada.
    private static final String NOMBRE = "chocosalud.db";
    private static final int VERSION = 1;

    // CONSTANTES: nombres de las tablas y columnas.
    // Se escriben UNA sola vez aquí y las demás clases las usan (por ejemplo
    // BaseDatos.TABLA_USUARIOS), así se evitan errores de escritura en el SQL.
    // "_id" es el identificador de cada fila; el número lo pone SQLite solo (AUTOINCREMENT).

    // Tabla usuarios
    public static final String TABLA_USUARIOS = "usuarios", USUARIO_ID = "_id", USUARIO_NOMBRE = "nombre", USUARIO_DOCUMENTO = "documento",
            USUARIO_CORREO = "correo", USUARIO_TELEFONO = "telefono", USUARIO_CONTRASENA = "contrasena";

    // Tabla centros_salud
    public static final String TABLA_CENTROS = "centros_salud", CENTRO_ID = "_id", CENTRO_NOMBRE = "nombre",CENTRO_MUNICIPIO = "municipio",
            CENTRO_DIRECCION = "direccion",CENTRO_TELEFONO = "telefono", CENTRO_LATITUD = "latitud", CENTRO_LONGITUD = "longitud";

    // Tabla citas
    public static final String TABLA_CITAS = "citas", CITA_ID = "_id",CITA_USUARIO_ID = "usuario_id", CITA_CENTRO_ID = "centro_id",
            CITA_ESPECIALIDAD = "especialidad", CITA_TELEFONO = "telefono",CITA_FECHA = "fecha", CITA_CREADA_EN = "creada_en";


    private static final String CREAR_TABLA_USUARIOS =
            "CREATE TABLE " + TABLA_USUARIOS + " ("
                    + USUARIO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + USUARIO_NOMBRE + " TEXT NOT NULL, "
                    + USUARIO_DOCUMENTO + " TEXT NOT NULL UNIQUE, "
                    + USUARIO_CORREO + " TEXT NOT NULL UNIQUE, "
                    + USUARIO_TELEFONO + " TEXT NOT NULL, "
                    + USUARIO_CONTRASENA + " TEXT NOT NULL)";

    private static final String CREAR_TABLA_CENTROS =
            "CREATE TABLE " + TABLA_CENTROS + " ("
                    + CENTRO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + CENTRO_NOMBRE + " TEXT NOT NULL, "
                    + CENTRO_MUNICIPIO + " TEXT NOT NULL, "
                    + CENTRO_DIRECCION + " TEXT, "
                    + CENTRO_TELEFONO + " TEXT, "
                    + CENTRO_LATITUD + " REAL NOT NULL, "
                    + CENTRO_LONGITUD + " REAL NOT NULL)";

    private static final String CREAR_TABLA_CITAS =
            "CREATE TABLE " + TABLA_CITAS + " ("
                    + CITA_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + CITA_USUARIO_ID + " INTEGER NOT NULL REFERENCES "
                    + TABLA_USUARIOS + "(" + USUARIO_ID + ") ON DELETE CASCADE, "
                    + CITA_CENTRO_ID + " INTEGER NOT NULL REFERENCES "
                    + TABLA_CENTROS + "(" + CENTRO_ID + "), "
                    + CITA_ESPECIALIDAD + " TEXT NOT NULL, "
                    + CITA_TELEFONO + " TEXT NOT NULL, "
                    + CITA_FECHA + " TEXT NOT NULL, "
                    + CITA_CREADA_EN + " TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP)";

    // VARIABLE: contexto -> se guarda para poder leer textos de strings.xml en onCreate.
    private final Context contexto;

    public BaseDatos(Context contexto) {
        super(contexto, NOMBRE, null, VERSION);
        this.contexto = contexto;
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    /*
     * MÉTODO: onCreate
     * - Android lo llama UNA sola vez: cuando el archivo chocosalud.db todavía no existe.
     * - Crea las tablas (el orden importa: citas va al final porque depende de las otras dos)
     * - y agrega los centros de salud iniciales.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREAR_TABLA_USUARIOS);
        db.execSQL(CREAR_TABLA_CENTROS);
        db.execSQL(CREAR_TABLA_CITAS);

        // Datos iniciales:
        insertarCentroInicial(db, contexto.getString(R.string.centro_quibdo), "Quibdó", 5.681290, -76.649315);
        insertarCentroInicial(db, contexto.getString(R.string.centro_tado), "Tadó", 5.264165, -76.556871);
    }

    /*
     * MÉTODO: onUpgrade
     * - Android lo llama cuando VERSION es mayor que la versión instalada en el teléfono.
     * - Aquí se borran las tablas y se vuelven a crear. OJO: así se PIERDEN los datos guardados;
     *   sirve mientras el proyecto está en desarrollo.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int versionAnterior, int versionNueva) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLA_CITAS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLA_CENTROS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLA_USUARIOS);
        onCreate(db);
    }


    private void insertarCentroInicial(SQLiteDatabase db, String nombre, String municipio,
                                       double latitud, double longitud) {
        ContentValues valores = new ContentValues();
        valores.put(CENTRO_NOMBRE, nombre);
        valores.put(CENTRO_MUNICIPIO, municipio);
        valores.put(CENTRO_LATITUD, latitud);
        valores.put(CENTRO_LONGITUD, longitud);
        db.insert(TABLA_CENTROS, null, valores);
    }
}
