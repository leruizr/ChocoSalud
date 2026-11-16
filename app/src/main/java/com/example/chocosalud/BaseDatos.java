package com.example.chocosalud;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import android.database.Cursor;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;


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
    // CONSTANTE: SIN_USUARIO -> lo que devuelven registrarUsuario e iniciarSesion cuando no hay
    private final Context contexto;
    public static final long SIN_USUARIO = -1;

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

    /*
     * MÉTODO: registrarUsuario (INSERT)
     * - Guarda un usuario nuevo en la tabla usuarios.
     * - La contraseña NO se guarda tal cual: se guarda su hash (ver calcularHash).
     * - DEVUELVE: el _id del usuario nuevo, o SIN_USUARIO si no se pudo guardar.
     */
    public long registrarUsuario(String nombre, String documento, String correo,
                                 String telefono, String contrasena) {
        // ContentValues: "caja" de pares columna -> valor. insert arma el SQL por nosotros,
        // así los datos que escribe el usuario nunca se pegan dentro del SQL (evita inyección SQL).
        ContentValues valores = new ContentValues();
        valores.put(USUARIO_NOMBRE, nombre);
        valores.put(USUARIO_DOCUMENTO, documento);
        valores.put(USUARIO_CORREO, normalizarCorreo(correo));
        valores.put(USUARIO_TELEFONO, telefono);
        valores.put(USUARIO_CONTRASENA, calcularHash(contrasena));

        // getWritableDatabase(): abre la base de datos para ESCRIBIR. La primera vez que se llama,
        // Android crea el archivo chocosalud.db y ejecuta onCreate.
        // insert devuelve el _id de la fila nueva, o -1 si falló (por ejemplo, correo repetido).
        return getWritableDatabase().insert(TABLA_USUARIOS, null, valores);
    }

    /*
     * MÉTODO: iniciarSesion  (SELECT)
     * - Busca un usuario con ese correo y esa contraseña.
     * - DEVUELVE: el _id del usuario si los datos coinciden, o SIN_USUARIO si no.
     */
    public long iniciarSesion(String correo, String contrasena) {
        // Los "?" son espacios que SQLite llena con los valores del arreglo, EN ORDEN.
        // Se compara el hash de lo escrito con el hash guardado: así nunca se necesita
        // la contraseña original.
        String sql = "SELECT " + USUARIO_ID + " FROM " + TABLA_USUARIOS
                + " WHERE " + USUARIO_CORREO + " = ? AND " + USUARIO_CONTRASENA + " = ?";
        String[] argumentos = {normalizarCorreo(correo), calcularHash(contrasena)};

        // Cursor: el resultado de la consulta, se recorre fila por fila.
        // try (...) { } cierra el cursor solo al terminar, aunque ocurra un error.
        try (Cursor cursor = getReadableDatabase().rawQuery(sql, argumentos)) {
            // moveToFirst() devuelve true si hay al menos una fila (el usuario existe).
            if (cursor.moveToFirst()) {
                return cursor.getLong(0); // columna 0 = _id (es la única que se pidió)
            }
        }
        return SIN_USUARIO;
    }

        // MÉTODO: existeCorreo -> true si ya hay un usuario registrado con ese correo.
        public boolean existeCorreo(String correo) {
            return existeValor(USUARIO_CORREO, normalizarCorreo(correo));
        }

        // MÉTODO: existeDocumento -> true si ya hay un usuario registrado con ese documento.
        public boolean existeDocumento(String documento) {
            return existeValor(USUARIO_DOCUMENTO, documento);
        }

        /*
         * MÉTODO: existeValor  (SELECT)
         * - Revisa si en la tabla usuarios hay alguna fila con ese valor en esa columna.
         * - "columna" siempre es una constante de esta clase (nunca algo que escribe el usuario),
         *   por eso se puede pegar en el SQL; el valor sí va con "?".
         */
        private boolean existeValor(String columna, String valor) {
            String sql = "SELECT 1 FROM " + TABLA_USUARIOS + " WHERE " + columna + " = ? LIMIT 1";
            try (Cursor cursor = getReadableDatabase().rawQuery(sql, new String[]{valor})) {
                return cursor.moveToFirst();
            }
        }

        /*
         * MÉTODO: normalizarCorreo
         * - Quita espacios y pasa el correo a minúsculas, para que "Ana@Correo.com" y "ana@correo.com"
         *   se consideren el mismo correo al registrarse y al iniciar sesión.
         */
        private static String normalizarCorreo(String correo) {
            return correo.trim().toLowerCase(Locale.ROOT);
        }

        /*
         * MÉTODO: calcularHash
         * - Convierte la contraseña en su hash SHA-256: un código de 64 caracteres que no se puede
         *   "devolver" a la contraseña original. La misma contraseña siempre da el mismo hash, por eso
         *   sirve para comparar al iniciar sesión.
         * - Así, si alguien copia el archivo chocosalud.db, no puede leer las contraseñas.
         */
        private static String calcularHash(String contrasena) {
            try {
                MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
                byte[] bytes = sha256.digest(contrasena.getBytes(StandardCharsets.UTF_8));

                // El resultado son 32 bytes; cada byte se escribe como 2 caracteres hexadecimales (0-9, a-f).
                StringBuilder hash = new StringBuilder();
                for (byte b : bytes) {
                    hash.append(String.format("%02x", b));
                }
                return hash.toString();
            } catch (NoSuchAlgorithmException e) {
                // Todos los Android traen SHA-256, así que esto no debería pasar nunca.
                throw new IllegalStateException("SHA-256 no está disponible", e);
            }
        }

    // =====================================================================================
    // CRUD DE CITAS (Fase 4)
    // C = Crear (INSERT) -> agendarCita
    // R = Leer  (SELECT) -> consultarCitas y consultarCita
    // U = Actualizar (UPDATE) -> actualizarCita
    // D = Borrar (DELETE) -> cancelarCita
    // =====================================================================================

    // CONSTANTE: SIN_CITA -> lo que devuelve agendarCita cuando no se pudo guardar.
    public static final long SIN_CITA = -1;

    /*
     * MÉTODO: consultarCentros  (SELECT)
     * - Trae los centros de salud para el selector "Centro de salud" de Agendar cita.
     * - DEVUELVE: un Cursor con las columnas _id y nombre.
     */
    public Cursor consultarCentros() {
        String sql = "SELECT " + CENTRO_ID + ", " + CENTRO_NOMBRE + " FROM " + TABLA_CENTROS
                + " ORDER BY " + CENTRO_NOMBRE;
        return getReadableDatabase().rawQuery(sql, null);
    }

    /*
     * MÉTODO: agendarCita  (INSERT -> C del CRUD)
     * - Guarda una cita nueva del usuario que inició sesión.
     * - DEVUELVE: el _id de la cita nueva, o SIN_CITA si no se pudo guardar.
     */
    public long agendarCita(long usuarioId, long centroId, String especialidad,
                            String telefono, String fecha) {
        ContentValues valores = new ContentValues();
        valores.put(CITA_USUARIO_ID, usuarioId);
        valores.put(CITA_CENTRO_ID, centroId);
        valores.put(CITA_ESPECIALIDAD, especialidad);
        valores.put(CITA_TELEFONO, telefono);
        valores.put(CITA_FECHA, fecha);
        return getWritableDatabase().insert(TABLA_CITAS, null, valores);
    }

    /*
     * MÉTODO: consultarCitas  (SELECT -> R del CRUD)
     * - Trae todas las citas de un usuario, ordenadas por fecha, para la lista de "Mis citas".
     * - JOIN: une la tabla citas con centros_salud para mostrar el NOMBRE del centro
     *   en vez de su número (centro_id).
     * - DEVUELVE: un Cursor con las columnas _id, titulo (especialidad - fecha) y centro.
     */
    public Cursor consultarCitas(long usuarioId) {
        String sql = "SELECT c." + CITA_ID + ", "
                + "c." + CITA_ESPECIALIDAD + " || ' - ' || c." + CITA_FECHA + " AS titulo, "
                + "ce." + CENTRO_NOMBRE + " AS centro "
                + "FROM " + TABLA_CITAS + " c "
                + "JOIN " + TABLA_CENTROS + " ce ON ce." + CENTRO_ID + " = c." + CITA_CENTRO_ID + " "
                + "WHERE c." + CITA_USUARIO_ID + " = ? "
                + "ORDER BY c." + CITA_FECHA;
        return getReadableDatabase().rawQuery(sql, new String[]{String.valueOf(usuarioId)});
    }

    /*
     * MÉTODO: consultarCita  (SELECT -> R del CRUD)
     * - Trae UNA cita, para llenar el formulario cuando el usuario la va a editar.
     * - Se filtra también por usuario: así nadie puede abrir la cita de otra persona.
     * - DEVUELVE: un Cursor con centro_id, especialidad, telefono y fecha.
     */
    public Cursor consultarCita(long citaId, long usuarioId) {
        String sql = "SELECT " + CITA_CENTRO_ID + ", " + CITA_ESPECIALIDAD + ", "
                + CITA_TELEFONO + ", " + CITA_FECHA + " FROM " + TABLA_CITAS
                + " WHERE " + CITA_ID + " = ? AND " + CITA_USUARIO_ID + " = ?";
        return getReadableDatabase().rawQuery(sql,
                new String[]{String.valueOf(citaId), String.valueOf(usuarioId)});
    }

    /*
     * MÉTODO: actualizarCita  (UPDATE -> U del CRUD)
     * - Cambia los datos de una cita que ya existe.
     * - DEVUELVE: cuántas filas se cambiaron (1 si salió bien, 0 si no encontró la cita).
     */
    public int actualizarCita(long citaId, long usuarioId, long centroId, String especialidad,
                              String telefono, String fecha) {
        ContentValues valores = new ContentValues();
        valores.put(CITA_CENTRO_ID, centroId);
        valores.put(CITA_ESPECIALIDAD, especialidad);
        valores.put(CITA_TELEFONO, telefono);
        valores.put(CITA_FECHA, fecha);
        // update(tabla, valores nuevos, condición WHERE, valores de los "?")
        return getWritableDatabase().update(TABLA_CITAS, valores,
                CITA_ID + " = ? AND " + CITA_USUARIO_ID + " = ?",
                new String[]{String.valueOf(citaId), String.valueOf(usuarioId)});
    }

    /*
     * MÉTODO: cancelarCita  (DELETE -> D del CRUD)
     * - Borra una cita del usuario.
     * - DEVUELVE: cuántas filas se borraron (1 si salió bien, 0 si no encontró la cita).
     */
    public int cancelarCita(long citaId, long usuarioId) {
        return getWritableDatabase().delete(TABLA_CITAS,
                CITA_ID + " = ? AND " + CITA_USUARIO_ID + " = ?",
                new String[]{String.valueOf(citaId), String.valueOf(usuarioId)});
    }
}
