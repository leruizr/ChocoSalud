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


/*
 * CLASE: BaseDatos -> CONEXIÓN a la base de datos SQLite de la app (Fase 4)
 *
 * - "extends SQLiteOpenHelper": hereda de la clase de Android que crea, abre y actualiza
 *   la base de datos. La base es un archivo (chocosalud.db) guardado dentro del teléfono.
 * - Aquí están las 3 tablas (usuarios, centros_salud y citas) y TODAS las consultas (CRUD):
 *   las pantallas no escriben SQL, solo llaman a los métodos de esta clase.
 */
public class BaseDatos extends SQLiteOpenHelper {

    // CONSTANTES: nombre del archivo y versión de la base de datos.
    // Si se cambia la estructura de una tabla, hay que SUBIR la versión (2, 3...) para que
    // Android ejecute onUpgrade en los teléfonos que ya tienen la app instalada.
    private static final String NOMBRE = "chocosalud.db";
    // Historial de versiones:
    //   1 -> tablas usuarios, centros_salud y citas.
    //   2 -> columna notificaciones en usuarios y columna foto (fórmula médica) en citas.
    private static final int VERSION = 2;

    // CONSTANTES: nombres de las tablas y columnas.
    // Se escriben UNA sola vez aquí y las demás clases las usan (por ejemplo
    // BaseDatos.TABLA_USUARIOS), así se evitan errores de escritura en el SQL.
    // "_id" es el identificador de cada fila; el número lo pone SQLite solo (AUTOINCREMENT).

    // Tabla usuarios
    public static final String TABLA_USUARIOS = "usuarios", USUARIO_ID = "_id", USUARIO_NOMBRE = "nombre", USUARIO_DOCUMENTO = "documento",
            USUARIO_CORREO = "correo", USUARIO_TELEFONO = "telefono", USUARIO_CONTRASENA = "contrasena",
            USUARIO_NOTIFICACIONES = "notificaciones";

    // Tabla centros_salud
    public static final String TABLA_CENTROS = "centros_salud", CENTRO_ID = "_id", CENTRO_NOMBRE = "nombre",CENTRO_MUNICIPIO = "municipio",
            CENTRO_DIRECCION = "direccion",CENTRO_TELEFONO = "telefono", CENTRO_LATITUD = "latitud", CENTRO_LONGITUD = "longitud";

    // Tabla citas
    public static final String TABLA_CITAS = "citas", CITA_ID = "_id",CITA_USUARIO_ID = "usuario_id", CITA_CENTRO_ID = "centro_id",
            CITA_ESPECIALIDAD = "especialidad", CITA_TELEFONO = "telefono",CITA_FECHA = "fecha", CITA_CREADA_EN = "creada_en",
            CITA_FOTO = "foto";


    // Columnas agregadas en la VERSIÓN 2. Se escriben una sola vez porque se usan en dos lugares:
    // al crear las tablas (onCreate) y al actualizar una base de datos vieja (onUpgrade).
    // notificaciones: 1 = el usuario quiere recibir avisos de sus citas, 0 = no.
    // foto: la imagen de la fórmula médica en bytes (BLOB). Es opcional, por eso puede ser NULL.
    private static final String COLUMNA_NOTIFICACIONES_V2 =
            USUARIO_NOTIFICACIONES + " INTEGER NOT NULL DEFAULT 1";
    private static final String COLUMNA_FOTO_V2 = CITA_FOTO + " BLOB";

    private static final String CREAR_TABLA_USUARIOS =
            "CREATE TABLE " + TABLA_USUARIOS + " ("
                    + USUARIO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + USUARIO_NOMBRE + " TEXT NOT NULL, "
                    + USUARIO_DOCUMENTO + " TEXT NOT NULL UNIQUE, "
                    + USUARIO_CORREO + " TEXT NOT NULL UNIQUE, "
                    + USUARIO_TELEFONO + " TEXT NOT NULL, "
                    + USUARIO_CONTRASENA + " TEXT NOT NULL, "
                    + COLUMNA_NOTIFICACIONES_V2 + ")";

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
                    + CITA_CREADA_EN + " TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                    + COLUMNA_FOTO_V2 + ")";

    // VARIABLE: contexto -> se guarda para poder leer textos de strings.xml en onCreate.
    // CONSTANTE: SIN_USUARIO -> lo que devuelven registrarUsuario e iniciarSesion cuando no hay
    private final Context contexto;
    public static final long SIN_USUARIO = -1;

    /*
     * CONSTRUCTOR: BaseDatos
     * - Cada pantalla crea la conexión con: baseDatos = new BaseDatos(this);
     * - super(...) le pasa a SQLiteOpenHelper el nombre del archivo y la versión.
     *   El archivo todavía no se abre aquí: se abre con getWritableDatabase() o getReadableDatabase().
     */
    public BaseDatos(Context contexto) {
        super(contexto, NOMBRE, null, VERSION);
        this.contexto = contexto;
    }

    /*
     * MÉTODO: onConfigure
     * - Android lo llama cada vez que abre la base de datos.
     * - Activa las llaves foráneas (FOREIGN KEY): así SQLite respeta las relaciones entre tablas
     *   (por ejemplo, borra las citas de un usuario cuando se elimina su cuenta).
     */
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
     * - En vez de borrar las tablas (y perder los datos), se AGREGAN las columnas nuevas con
     *   ALTER TABLE. Así los usuarios y las citas que ya estaban guardados se conservan.
     * - Cada "if" aplica los cambios de una versión: un teléfono con la versión 1 recibe los
     *   cambios de la 2; cuando exista la 3, se agregará otro "if (versionAnterior < 3)".
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int versionAnterior, int versionNueva) {
        if (versionAnterior < 2) {
            db.execSQL("ALTER TABLE " + TABLA_USUARIOS + " ADD COLUMN " + COLUMNA_NOTIFICACIONES_V2);
            db.execSQL("ALTER TABLE " + TABLA_CITAS + " ADD COLUMN " + COLUMNA_FOTO_V2);
        }
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
            return existeValor(USUARIO_CORREO, normalizarCorreo(correo), SIN_USUARIO);
        }

        // MÉTODO: existeDocumento -> true si ya hay un usuario registrado con ese documento.
        public boolean existeDocumento(String documento) {
            return existeValor(USUARIO_DOCUMENTO, documento, SIN_USUARIO);
        }

        // MÉTODO: existeCorreoDeOtro -> true si OTRO usuario (distinto de usuarioId) ya usa ese correo.
        // Lo usa el perfil: el usuario puede guardar su propio correo sin que se marque como repetido.
        public boolean existeCorreoDeOtro(String correo, long usuarioId) {
            return existeValor(USUARIO_CORREO, normalizarCorreo(correo), usuarioId);
        }

        // MÉTODO: existeDocumentoDeOtro -> igual que el anterior, pero con el documento.
        public boolean existeDocumentoDeOtro(String documento, long usuarioId) {
            return existeValor(USUARIO_DOCUMENTO, documento, usuarioId);
        }

        /*
         * MÉTODO: existeValor  (SELECT)
         * - Revisa si en la tabla usuarios hay alguna fila con ese valor en esa columna.
         * - "columna" siempre es una constante de esta clase (nunca algo que escribe el usuario),
         *   por eso se puede pegar en el SQL; el valor sí va con "?".
         * - idExcluido: el _id de un usuario que NO se debe tener en cuenta (el propio usuario al
         *   editar su perfil). Con SIN_USUARIO (-1) se revisan todos, porque ningún _id vale -1.
         */
        private boolean existeValor(String columna, String valor, long idExcluido) {
            String sql = "SELECT 1 FROM " + TABLA_USUARIOS + " WHERE " + columna + " = ? AND "
                    + USUARIO_ID + " <> ? LIMIT 1";
            String[] argumentos = {valor, String.valueOf(idExcluido)};
            try (Cursor cursor = getReadableDatabase().rawQuery(sql, argumentos)) {
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
     * - Trae los centros de salud, ordenados por nombre. Lo usan el selector "Centro de salud"
     *   de Agendar cita y la lista de la pantalla Centros de salud.
     * - DEVUELVE: un Cursor con todas las columnas de la tabla centros_salud.
     */
    public Cursor consultarCentros() {
        String sql = "SELECT " + CENTRO_ID + ", " + CENTRO_NOMBRE + ", " + CENTRO_MUNICIPIO + ", "
                + CENTRO_DIRECCION + ", " + CENTRO_TELEFONO + ", " + CENTRO_LATITUD + ", "
                + CENTRO_LONGITUD + " FROM " + TABLA_CENTROS
                + " ORDER BY " + CENTRO_NOMBRE;
        return getReadableDatabase().rawQuery(sql, null);
    }

    /*
     * MÉTODO: agendarCita  (INSERT -> C del CRUD)
     * - Guarda una cita nueva del usuario que inició sesión. Al quedar en la tabla citas, la cita
     *   aparece también en su HISTORIAL MÉDICO.
     * - foto: la fórmula médica en bytes, o null si el usuario no tomó foto.
     * - DEVUELVE: el _id de la cita nueva, o SIN_CITA si no se pudo guardar.
     */
    public long agendarCita(long usuarioId, long centroId, String especialidad,
                            String telefono, String fecha, byte[] foto) {
        ContentValues valores = new ContentValues();
        valores.put(CITA_USUARIO_ID, usuarioId);
        valores.put(CITA_CENTRO_ID, centroId);
        valores.put(CITA_ESPECIALIDAD, especialidad);
        valores.put(CITA_TELEFONO, telefono);
        valores.put(CITA_FECHA, fecha);
        if (foto != null) {
            valores.put(CITA_FOTO, foto); // BLOB: se guarda la imagen como bytes
        }
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
     * - DEVUELVE: un Cursor con centro_id, especialidad, telefono, fecha y foto.
     */
    public Cursor consultarCita(long citaId, long usuarioId) {
        String sql = "SELECT " + CITA_CENTRO_ID + ", " + CITA_ESPECIALIDAD + ", "
                + CITA_TELEFONO + ", " + CITA_FECHA + ", " + CITA_FOTO + " FROM " + TABLA_CITAS
                + " WHERE " + CITA_ID + " = ? AND " + CITA_USUARIO_ID + " = ?";
        return getReadableDatabase().rawQuery(sql,
                new String[]{String.valueOf(citaId), String.valueOf(usuarioId)});
    }

    /*
     * MÉTODO: actualizarCita  (UPDATE -> U del CRUD)
     * - Cambia los datos de una cita que ya existe.
     * - foto: la foto NUEVA, o null para dejar la foto que ya tenía la cita.
     * - DEVUELVE: cuántas filas se cambiaron (1 si salió bien, 0 si no encontró la cita).
     */
    public int actualizarCita(long citaId, long usuarioId, long centroId, String especialidad,
                              String telefono, String fecha, byte[] foto) {
        ContentValues valores = new ContentValues();
        valores.put(CITA_CENTRO_ID, centroId);
        valores.put(CITA_ESPECIALIDAD, especialidad);
        valores.put(CITA_TELEFONO, telefono);
        valores.put(CITA_FECHA, fecha);
        if (foto != null) {
            valores.put(CITA_FOTO, foto);
        }
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

    // =====================================================================================
    // PERFIL DEL USUARIO (Fase 4) -> pantalla Perfil y notificaciones
    // R = consultarUsuario (SELECT)
    // U = actualizarUsuario, cambiarContrasena, actualizarNotificaciones (UPDATE)
    // D = eliminarUsuario (DELETE)
    // =====================================================================================

    /*
     * MÉTODO: consultarUsuario  (SELECT)
     * - Trae los datos del usuario que inició sesión para mostrarlos en el perfil.
     * - La contraseña NO se pide: no hace falta mostrarla (y solo está guardado su hash).
     * - DEVUELVE: un Cursor con nombre, documento, correo, telefono y notificaciones.
     */
    public Cursor consultarUsuario(long usuarioId) {
        String sql = "SELECT " + USUARIO_NOMBRE + ", " + USUARIO_DOCUMENTO + ", " + USUARIO_CORREO
                + ", " + USUARIO_TELEFONO + ", " + USUARIO_NOTIFICACIONES + " FROM " + TABLA_USUARIOS
                + " WHERE " + USUARIO_ID + " = ?";
        return getReadableDatabase().rawQuery(sql, new String[]{String.valueOf(usuarioId)});
    }

    /*
     * MÉTODO: actualizarUsuario  (UPDATE)
     * - Guarda los cambios del perfil: nombre, documento, correo y teléfono.
     * - DEVUELVE: cuántas filas se cambiaron (1 si salió bien).
     */
    public int actualizarUsuario(long usuarioId, String nombre, String documento,
                                 String correo, String telefono) {
        ContentValues valores = new ContentValues();
        valores.put(USUARIO_NOMBRE, nombre);
        valores.put(USUARIO_DOCUMENTO, documento);
        valores.put(USUARIO_CORREO, normalizarCorreo(correo));
        valores.put(USUARIO_TELEFONO, telefono);
        return getWritableDatabase().update(TABLA_USUARIOS, valores,
                USUARIO_ID + " = ?", new String[]{String.valueOf(usuarioId)});
    }

    /*
     * MÉTODO: cambiarContrasena  (UPDATE)
     * - Cambia la contraseña SOLO si la contraseña actual es correcta: el WHERE pide el _id
     *   y también el hash de la contraseña actual. Si no coincide, no se cambia ninguna fila.
     * - DEVUELVE: true si se cambió, false si la contraseña actual era incorrecta.
     */
    public boolean cambiarContrasena(long usuarioId, String actual, String nueva) {
        ContentValues valores = new ContentValues();
        valores.put(USUARIO_CONTRASENA, calcularHash(nueva));
        int filas = getWritableDatabase().update(TABLA_USUARIOS, valores,
                USUARIO_ID + " = ? AND " + USUARIO_CONTRASENA + " = ?",
                new String[]{String.valueOf(usuarioId), calcularHash(actual)});
        return filas > 0;
    }

    /*
     * MÉTODO: actualizarNotificaciones  (UPDATE)
     * - Guarda si el usuario quiere (true) o no (false) recibir avisos de sus citas.
     *   SQLite no tiene tipo booleano: se guarda 1 o 0.
     */
    public void actualizarNotificaciones(long usuarioId, boolean activas) {
        ContentValues valores = new ContentValues();
        valores.put(USUARIO_NOTIFICACIONES, activas ? 1 : 0);
        getWritableDatabase().update(TABLA_USUARIOS, valores,
                USUARIO_ID + " = ?", new String[]{String.valueOf(usuarioId)});
    }

    /*
     * MÉTODO: notificacionesActivas  (SELECT)
     * - DEVUELVE: true si el usuario tiene activadas las notificaciones de citas.
     */
    public boolean notificacionesActivas(long usuarioId) {
        String sql = "SELECT " + USUARIO_NOTIFICACIONES + " FROM " + TABLA_USUARIOS
                + " WHERE " + USUARIO_ID + " = ?";
        try (Cursor cursor = getReadableDatabase().rawQuery(sql,
                new String[]{String.valueOf(usuarioId)})) {
            return cursor.moveToFirst() && cursor.getInt(0) == 1;
        }
    }

    /*
     * MÉTODO: eliminarUsuario  (DELETE)
     * - Borra la cuenta del usuario. Por el "ON DELETE CASCADE" de la tabla citas, SQLite borra
     *   también todas sus citas (su historial).
     * - DEVUELVE: cuántas filas se borraron (1 si salió bien).
     */
    public int eliminarUsuario(long usuarioId) {
        return getWritableDatabase().delete(TABLA_USUARIOS,
                USUARIO_ID + " = ?", new String[]{String.valueOf(usuarioId)});
    }

    // =====================================================================================
    // CENTROS DE SALUD (Fase 4) -> pantalla Centros de salud y Agregar clínica
    // C = agregarCentro (INSERT)   R = consultarCentros (SELECT, más arriba)
    // D = eliminarCentro (DELETE)
    // =====================================================================================

    // CONSTANTE: SIN_CENTRO -> lo que devuelve agregarCentro cuando no se pudo guardar.
    public static final long SIN_CENTRO = -1;

    /*
     * MÉTODO: agregarCentro  (INSERT)
     * - Guarda una clínica u hospital nuevo. Desde ese momento aparece en la lista de
     *   Centros de salud y en el selector de Agendar cita.
     * - direccion y telefono son opcionales: si vienen vacíos se guardan como NULL.
     * - DEVUELVE: el _id del centro nuevo, o SIN_CENTRO si no se pudo guardar.
     */
    public long agregarCentro(String nombre, String municipio, String direccion,
                              String telefono, double latitud, double longitud) {
        ContentValues valores = new ContentValues();
        valores.put(CENTRO_NOMBRE, nombre);
        valores.put(CENTRO_MUNICIPIO, municipio);
        valores.put(CENTRO_DIRECCION, direccion.isEmpty() ? null : direccion);
        valores.put(CENTRO_TELEFONO, telefono.isEmpty() ? null : telefono);
        valores.put(CENTRO_LATITUD, latitud);
        valores.put(CENTRO_LONGITUD, longitud);
        return getWritableDatabase().insert(TABLA_CENTROS, null, valores);
    }

    /*
     * MÉTODO: centroTieneCitas  (SELECT)
     * - DEVUELVE: true si alguna cita usa ese centro. En ese caso no se puede borrar, porque
     *   la cita quedaría apuntando a un centro que no existe (llave foránea).
     */
    public boolean centroTieneCitas(long centroId) {
        String sql = "SELECT 1 FROM " + TABLA_CITAS + " WHERE " + CITA_CENTRO_ID + " = ? LIMIT 1";
        try (Cursor cursor = getReadableDatabase().rawQuery(sql,
                new String[]{String.valueOf(centroId)})) {
            return cursor.moveToFirst();
        }
    }

    /*
     * MÉTODO: eliminarCentro  (DELETE)
     * - Borra un centro de salud que no tenga citas.
     * - DEVUELVE: cuántas filas se borraron (1 si salió bien).
     */
    public int eliminarCentro(long centroId) {
        return getWritableDatabase().delete(TABLA_CENTROS,
                CENTRO_ID + " = ?", new String[]{String.valueOf(centroId)});
    }

    // =====================================================================================
    // HISTORIAL MÉDICO (Fase 4) -> pantalla Historial médico
    // Cada cita que se agenda queda guardada en la tabla citas; el historial la muestra con
    // su centro de salud, su estado (próxima o realizada) y la foto de la fórmula si la tiene.
    // =====================================================================================

    /*
     * MÉTODO: consultarHistorial  (SELECT con JOIN y CASE)
     * - Trae todas las citas del usuario, de la más reciente a la más antigua.
     * - CASE: decide el estado comparando la fecha de la cita con la fecha de hoy
     *   (date('now','localtime') = hoy en la hora del teléfono). Las fechas "aaaa-mm-dd"
     *   se pueden comparar como texto porque el año va primero.
     * - Los textos "Próxima", "Realizada"... vienen de strings.xml y se pasan con "?".
     * - DEVUELVE: un Cursor con _id, titulo (especialidad - fecha) y detalle (centro · estado).
     */
    public Cursor consultarHistorial(long usuarioId, String textoProxima, String textoRealizada,
                                     String textoConFormula) {
        String sql = "SELECT c." + CITA_ID + ", "
                + "c." + CITA_ESPECIALIDAD + " || ' - ' || c." + CITA_FECHA + " AS titulo, "
                + "ce." + CENTRO_NOMBRE + " || ' · ' || "
                + "CASE WHEN c." + CITA_FECHA + " >= date('now','localtime') THEN ? ELSE ? END || "
                + "CASE WHEN c." + CITA_FOTO + " IS NOT NULL THEN ? ELSE '' END AS detalle "
                + "FROM " + TABLA_CITAS + " c "
                + "JOIN " + TABLA_CENTROS + " ce ON ce." + CENTRO_ID + " = c." + CITA_CENTRO_ID + " "
                + "WHERE c." + CITA_USUARIO_ID + " = ? "
                + "ORDER BY c." + CITA_FECHA + " DESC";
        return getReadableDatabase().rawQuery(sql, new String[]{textoProxima, textoRealizada,
                textoConFormula, String.valueOf(usuarioId)});
    }

    /*
     * MÉTODO: contarCitas  (SELECT con COUNT y SUM)
     * - Cuenta las citas del usuario: el total y cuántas son próximas (de hoy en adelante).
     * - DEVUELVE: un arreglo {total, proximas}. Las realizadas son total - proximas.
     */
    public int[] contarCitas(long usuarioId) {
        String sql = "SELECT COUNT(*), "
                + "SUM(CASE WHEN " + CITA_FECHA + " >= date('now','localtime') THEN 1 ELSE 0 END) "
                + "FROM " + TABLA_CITAS + " WHERE " + CITA_USUARIO_ID + " = ?";
        try (Cursor cursor = getReadableDatabase().rawQuery(sql,
                new String[]{String.valueOf(usuarioId)})) {
            cursor.moveToFirst(); // COUNT siempre devuelve una fila
            // Si no hay citas, SUM devuelve NULL; getInt de NULL da 0.
            return new int[]{cursor.getInt(0), cursor.getInt(1)};
        }
    }

    /*
     * MÉTODO: consultarDetalleCita  (SELECT con JOIN)
     * - Trae todos los datos de una cita del historial, con el nombre y municipio del centro
     *   y la foto de la fórmula.
     * - DEVUELVE: un Cursor con especialidad, fecha, telefono, creada_en, foto, centro y municipio.
     */
    public Cursor consultarDetalleCita(long citaId, long usuarioId) {
        String sql = "SELECT c." + CITA_ESPECIALIDAD + ", c." + CITA_FECHA + ", c." + CITA_TELEFONO
                + ", c." + CITA_CREADA_EN + ", c." + CITA_FOTO + ", "
                + "ce." + CENTRO_NOMBRE + " AS centro, ce." + CENTRO_MUNICIPIO + " AS municipio "
                + "FROM " + TABLA_CITAS + " c "
                + "JOIN " + TABLA_CENTROS + " ce ON ce." + CENTRO_ID + " = c." + CITA_CENTRO_ID + " "
                + "WHERE c." + CITA_ID + " = ? AND c." + CITA_USUARIO_ID + " = ?";
        return getReadableDatabase().rawQuery(sql,
                new String[]{String.valueOf(citaId), String.valueOf(usuarioId)});
    }
}
