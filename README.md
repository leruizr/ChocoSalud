# ChocóSalud

Aplicación móvil para Android que busca facilitar el acceso a servicios de salud a los habitantes de
Quibdó y el Chocó, donde la dispersión geográfica, el estado de las vías y la poca oferta de centros de
atención dificultan recibir atención médica.

**Objetivo:** permitir consultar centros de salud cercanos, agendar citas médicas, ver el historial
clínico básico y recibir notificaciones sobre la atención en salud.

> Este proyecto se construye **paso a paso, pantalla por pantalla**. Este documento explica cómo
> instalarlo, cómo usarlo y cómo agregar las pantallas que faltan.

---

## 1. Estado actual del proyecto

| # | Pantalla | Estado |
|---|----------|--------|
| 1 | Bienvenida | Hecha |
| 2 | Registro de usuario | Hecha (solo valida los datos, no los guarda) |
| 3 | Inicio de sesión | Hecha (solo valida el formato, no comprueba usuarios) |
| 4 | Menú principal | Hecha ("Centros de salud" y "Agendar cita" abren su pantalla; las demás tarjetas muestran "Esta sección estará disponible pronto") |
| 5 | Directorio de centros de salud (con GPS) | Hecha (obtiene la ubicación actual y abre Google Maps) |
| 6 | Agendamiento de citas | Hecha (el botón "Enviar" permite guardar la cita en el calendario de eleccion del usuario) |
| 7 | Historial médico | Pendiente |
| 8 | Perfil y notificaciones | Pendiente |

**Base de datos SQLite.** Ya existe la clase `BaseDatos` con las
tablas de usuarios, centros de salud y citas.
Los servicios previstos son: **GPS** (centros cercanos), **cámara** (adjuntar fotos de fórmulas
o documentos) y **notificaciones** (recordatorios de citas).

---

## 2. Qué necesitas para instalarla

**En el computador (Windows, Mac o Linux):**

- **Android Studio**, en su versión estable más reciente: <https://developer.android.com/studio>.
  Debe ser **Android Studio**, no IntelliJ IDEA ni otro editor: se parecen mucho, pero solo Android
  Studio trae el soporte para Android.
  Este proyecto usa herramientas muy nuevas (Android Gradle Plugin 9.3.3 y Gradle 9.5.0). Si tu Android
  Studio es antiguo, te pedirá actualizarlo: hazlo.
- **Conexión a internet la primera vez**. Android Studio descargará solo lo que falte (Gradle, el JDK
  necesario y las librerías). No tienes que instalar Java por separado.
- Unos **10 GB libres** en disco.

**Para probar la app (elige una opción):**

- Un **teléfono Android** con Android 7.0 o superior, y un cable USB de datos (no uno que solo cargue).
- O un **emulador** (teléfono virtual) creado desde Android Studio. No necesitas teléfono físico.

---

## 3. Cómo obtener el proyecto

El repositorio es público: <https://github.com/leruizr/ChocoSalud>. Tienes dos formas de obtenerlo:

**Opción A: descargar el ZIP (la más fácil, solo para ver o probar el proyecto)**

1. Entra a <https://github.com/leruizr/ChocoSalud>.
2. Pulsa el botón verde **Code** y luego **Download ZIP**.
3. Descomprime el ZIP en una carpeta de tu computador. La carpeta que se crea se llama
   **`ChocoSalud-main`**: esa es la que debes abrir en Android Studio.

**Opción B: clonar con Git (recomendada si vas a trabajar en el proyecto)**

Necesitas tener [Git](https://git-scm.com/downloads) instalado. Abre una terminal y ejecuta:

```bash
git clone https://github.com/leruizr/ChocoSalud.git
```

Esto crea la carpeta **`ChocoSalud`**.

---

## 4. Cómo abrirla y ejecutarla

### 4.1 Abrir el proyecto

1. Abre **Android Studio** (no IntelliJ IDEA).
2. Elige **Open** (Abrir) y selecciona la carpeta del proyecto: **`ChocoSalud`** si lo clonaste, o
   **`ChocoSalud-main`** si descargaste el ZIP. Es la que contiene el archivo `settings.gradle.kts`.
   Elige esa carpeta raíz, no la carpeta `app`.
3. Espera. Abajo a la derecha verás una barra de progreso ("Gradle sync"). **La primera vez puede tardar
   varios minutos** porque descarga muchas cosas. No la interrumpas.
4. Si aparece un mensaje pidiendo instalar **"Android SDK Platform 37"** (u otro componente), pulsa el
   enlace **Install** y acepta.

Cuando termine sin errores rojos, el proyecto está listo.

### 4.2 Ejecutarla en un teléfono real

1. En el teléfono, activa las **opciones de desarrollador**: entra a *Ajustes → Información del teléfono
   → Información de software* y toca **Número de compilación** 7 veces seguidas. (En algunas marcas el
   camino cambia un poco; busca "Número de compilación".)
2. Entra a *Ajustes → Opciones de desarrollador* y activa **Depuración USB**.
3. Conecta el teléfono al computador con el cable. En el teléfono aparecerá "¿Permitir depuración USB?":
   pulsa **Permitir**.
4. En Android Studio, en la barra de arriba, elige tu teléfono en la lista de dispositivos (junto al
   botón verde ▶).
5. Pulsa el botón verde **▶ Run 'app'** (o `Shift + F10`). La app se instala y se abre sola.

### 4.3 Ejecutarla en un emulador (sin teléfono)

1. En Android Studio abre *Tools → Device Manager* y pulsa **Create Device**.
2. Elige un teléfono (por ejemplo *Pixel*), una imagen del sistema (Android 12 o superior) y termina el
   asistente. Espera a que se descargue.
3. Selecciona ese emulador en la lista de dispositivos y pulsa **▶ Run 'app'**.

### 4.4 Instalarla en un teléfono sin Android Studio (archivo APK)

Si solo quieres que otra persona pruebe la app:

1. En Android Studio: *Build → Build Bundle(s) / APK(s) → Build APK(s)*.
2. Cuando termine, pulsa **locate** en el aviso. El archivo es `app-debug.apk`
   (queda en `app/build/outputs/apk/debug/`).
3. Envíalo al teléfono (WhatsApp, correo, USB, Drive…) y ábrelo. Android pedirá permitir instalar apps
   de "orígenes desconocidos": acepta solo para este archivo.

---

## 5. Cómo se usa la app

1. **Bienvenida:** verás el logo y el botón **Comenzar**.
2. **Inicio de sesión:** escribe un correo con formato válido (por ejemplo `prueba@correo.com`) y una
   contraseña de **4 caracteres** (letras o números, por ejemplo `ab12`). Pulsa **Ingresar**.
   *Por ahora no hay base de datos, así que entra cualquier correo válido con una contraseña de 4
   caracteres.*
3. **Registro:** desde el login pulsa **¿No tienes cuenta? Regístrate**. Llena nombre, documento, correo,
   teléfono y contraseña (4 caracteres, repetida en "Confirmar contraseña"). Si algo está mal, la app
   marca el campo en rojo. *Por ahora no guarda nada.* Para regresar pulsa **¿Ya tienes cuenta? Inicia
   sesión**.
4. **Menú principal:** cuatro tarjetas (Centros de salud, Agendar cita, Historial médico, Perfil y
   notificaciones). **Centros de salud** y **Agendar cita** abren su pantalla; las otras dos todavía
   muestran el aviso "Esta sección estará disponible pronto".
5. **Centros de salud (GPS):** pulsa **Obtener mi ubicación** y acepta el permiso de ubicación. La app
   muestra tu latitud y longitud; con **Ver mi ubicación en el mapa** se abre Google Maps en ese punto.
   Los botones de la clínica de Quibdó y del hospital de Tadó abren Google Maps en cada centro (desde
   ahí puedes pulsar "Cómo llegar"). La ubicación del teléfono debe estar activada. *No usa API key.*
6. **Agendar cita:** permite elegir la especialidad, escribir un teléfono de contacto, tomar fotografia de la formula (o evidencia similar) y escoger la fecha
   en un calendario. 

El botón **Atrás** del menú principal cierra la app (no vuelve al login).

---

## 6. Qué hace cada archivo

Las carpetas y archivos que **no** aparecen aquí son de configuración y normalmente no se tocan.

### 6.1 Código Java (`app/src/main/java/com/example/chocosalud/`)

Cada pantalla es una clase "Activity" (código) que va de la mano con un archivo de diseño (layout).

| Archivo | Qué hace                                                                                                                                                                                                     |
|---------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `ActivityBienvenida.java` | Pantalla 1. Es la primera que se abre. El botón "Comenzar" (`irInicioSesion`) lleva al login.                                                                                                                |
| `ActivityInicioSesion.java` | Pantalla 3. Valida correo y contraseña; si están bien abre el menú (`iniciarSesion`). También enlaza al registro (`irRegistro`).                                                                             |
| `ActivityRegistroUsuario.java` | Pantalla 2. Valida los 6 campos del formulario (`registrarUsuario`) y permite volver al login (`irInicioSesion`).                                                                                            |
| `ActivityMenuPrincipal.java` | Pantalla 4. Las cuatro tarjetas (`irCentrosSalud`, `irCitas`, `irHistorial`, `irPerfil`). `irCitas` abre Agendar cita; las demás muestran un aviso (`mostrarProximamente`).                                  |
| `ActivityCentrosSalud.java` | Pantalla 5 (servicio GPS). Pide el permiso de ubicación, obtiene la ubicación actual (`obtenerUbicacionActual`) y abre Google Maps con un Intent (`verMiUbicacion`, `abrirCentroQuibdo`, `abrirCentroTado`). |
| `ActivityAgendarCita.java` | Pantalla 6. Agendar cita; Permite guardar en la agenda(calendario) del dispositivo movil, y guardar la cita en `citas`.                                                                                             |
| `AjusteTeclado.java` | Ayuda compartida: evita que el teclado del teléfono tape los campos de un formulario. La usan el login y el registro.                                                                                        |
| `MainActivity.java` | Pantalla de ejemplo que trae Android Studio ("Hello World"). **No se usa**; se puede borrar más adelante.                                                                                                    |
| `BaseDatos.java` | Base de datos SQLite (`chocosalud.db`): crea las tablas `usuarios`, `centros_salud` y `citas`, y agrega los dos centros iniciales.                                                                           |

### 6.2 Diseños de pantalla (`app/src/main/res/layout/`)

| Archivo | Qué es |
|---------|--------|
| `activity_bienvenida.xml` | Diseño de la pantalla 1 (logo, nombre, descripción y botón). |
| `activity_inicio_sesion.xml` | Diseño de la pantalla 3 (correo, contraseña, botones). |
| `activity_registro_usuario.xml` | Diseño de la pantalla 2 (formulario de registro). |
| `activity_menu_principal.xml` | Diseño de la pantalla 4 (las cuatro tarjetas). |
| `activity_centros_salud.xml` | Diseño de la pantalla 5 (botón de ubicación, texto con la ubicación y botones de los centros de salud). |
| `activity_agendar_cita.xml` | Diseño de la pantalla 6 (especialidad, teléfono, calendario y botón "Enviar"). |
| `activity_main.xml` | Diseño de `MainActivity` (no se usa). |

### 6.3 Recursos (`app/src/main/res/`)

| Archivo | Qué es |
|---------|--------|
| `values/strings.xml` | **Todos los textos** que ve el usuario. Si quieres cambiar una palabra, se hace aquí. |
| `values/colors.xml` | **Paleta de colores** tomada del logo: `azul_chocosalud`, `azul_oscuro`, `dorado`, `crema`. |
| `values/themes.xml` | Tema visual (modo claro): colores principales de la app. |
| `values-night/themes.xml` | Tema visual para el modo oscuro del teléfono. |
| `drawable/logo_chocosalud.png` | El logo de la app (recortado en círculo). |
| `drawable/ic_centros_salud.xml`, `ic_citas.xml`, `ic_historial.xml`, `ic_perfil.xml` | Íconos de las tarjetas del menú. |
| `drawable/ic_launcher_*.xml` y `mipmap-*/` | Ícono de la app en el teléfono. **Sigue siendo el de Android por defecto** (falta cambiarlo por el logo). |
| `xml/backup_rules.xml`, `xml/data_extraction_rules.xml` | Reglas de copia de seguridad que trae la plantilla. No se tocan. |

### 6.4 Archivos de configuración

| Archivo | Qué hace |
|---------|----------|
| `app/src/main/AndroidManifest.xml` | **Lista de pantallas y permisos de la app.** Toda pantalla nueva debe estar registrada aquí (Android Studio lo hace solo si la creas con su asistente). Define también cuál pantalla se abre primero (`ActivityBienvenida`). |
| `app/build.gradle.kts` | Configuración del módulo `app`: nombre del paquete, versión mínima de Android (7.0), y las librerías que se usan. |
| `gradle/libs.versions.toml` | Lista central de las versiones de las librerías. |
| `build.gradle.kts`, `settings.gradle.kts`, `gradle.properties` | Configuración general de Gradle (la herramienta que compila el proyecto). |
| `gradle/`, `gradlew`, `gradlew.bat` | Descarga y ejecuta la versión correcta de Gradle. No se tocan. |
| `.gitignore` | Indica qué archivos **no** se suben a Git (carpetas generadas, `local.properties`, etc.). |
| `local.properties` | Ruta de tu Android SDK. **Es propio de cada computador**: Android Studio lo crea solo y no se sube a Git. |
| `app/src/test/` y `app/src/androidTest/` | Pruebas automáticas de ejemplo. Aún no hay pruebas propias. |

---

## 7. Cómo agregar una pantalla nueva

Ejemplo: crear la pantalla 5, *Directorio de centros de salud*.

**Paso 1. Crear la pantalla con el asistente**

1. En Android Studio, en el panel izquierdo, haz clic derecho sobre el paquete
   `com.example.chocosalud`.
2. Elige *New → Activity → Empty Views Activity*.
3. Nombre: **`ActivityCentrosSalud`**. Android Studio propone el layout `activity_centros_salud`:
   déjalo así. Verifica que el lenguaje sea **Java** y pulsa **Finish**.

Esto crea la clase, el layout y la registra en `AndroidManifest.xml`.

**Paso 2. Diseñar la pantalla**

Abre `activity_centros_salud.xml` y arma el diseño. Reglas del proyecto:

- **Nada de texto escrito directamente** en el layout: agrega el texto en `values/strings.xml` y úsalo con
  `@string/nombre`.
- **Nada de colores escritos directamente**: usa `?attr/colorPrimary`, `?attr/colorSecondary` o los
  colores de `colors.xml`, así respeta la paleta y el modo oscuro.
- Copia el estilo de las pantallas existentes: logo pequeño arriba, título en `?attr/colorPrimary`,
  campos con `Widget.Material3.TextInputLayout.OutlinedBox`, botones `MaterialButton`.
- Deja el `id` de la raíz del layout como `@+id/main`, porque el código lo usa para respetar las barras del
  sistema.

**Paso 3. Conectar la pantalla desde el menú**

En `ActivityMenuPrincipal.java`, reemplaza el aviso por la navegación:

```java
public void irCentrosSalud(View v) {
    Intent intent = new Intent(this, ActivityCentrosSalud.class);
    startActivity(intent);
}
```

(Agrega `import android.content.Intent;` arriba si no está.)

**Paso 4. Si la pantalla tiene formularios**

Para que el teclado no tape los campos: pon el contenido dentro de un `ScrollView` con id
`scrollFormulario` y un `LinearLayout` interno con id `contenidoFormulario` (mira
`activity_inicio_sesion.xml`), y en el `onCreate` llama:

```java
AjusteTeclado.aplicar(this,
        findViewById(R.id.main),
        (ScrollView) findViewById(R.id.scrollFormulario),
        (ViewGroup) findViewById(R.id.contenidoFormulario));
```

**Paso 5. Probar**

Ejecuta la app (▶), entra hasta el menú y pulsa la tarjeta. Revisa también con el teclado abierto y con el
teléfono en modo oscuro.

### Convenciones de nombres (todo en español)

| Elemento | Formato | Ejemplo |
|----------|---------|---------|
| Clase de pantalla | `Activity` + nombre en español, con mayúsculas al inicio de cada palabra | `ActivityCentrosSalud` |
| Archivo de diseño | `activity_` + nombre en minúsculas con guion bajo | `activity_centros_salud.xml` |
| Método de un botón | Verbo en español | `irCitas`, `registrarUsuario`, `iniciarSesion` |
| Identificadores (`id`) | Tipo + nombre, en camelCase | `layoutCorreo`, `campoCorreo`, `botonIngresar`, `tarjetaCitas` |
| Textos en `strings.xml` | Minúsculas con guion bajo, por pantalla | `error_correo_invalido`, `menu_citas` |

---

## 8. Trabajar en equipo con Git (guía corta)

**Antes de empezar:** para poder subir cambios necesitas que el dueño del repositorio te invite como
colaborador (en GitHub: *Settings → Collaborators → Add people*). Te llegará un correo de GitHub:
acepta la invitación. Sin ese paso puedes descargar el proyecto, pero el `git push` te dará un error de
permisos.

Para no pisarse el trabajo, cada persona trabaja en su **propia rama** y luego se unen los cambios.

```bash
git pull                            # trae lo último del repositorio
git checkout -b pantalla-5-centros  # crea tu rama de trabajo
# ... haces tus cambios en Android Studio ...
git add .                           # prepara los cambios
git commit -m "Agrega pantalla de centros de salud"
git push -u origin pantalla-5-centros
```

Después, en GitHub, abre un **Pull Request** para que el equipo revise y una tu rama a la principal.

Recomendaciones:

- **Una persona por pantalla** para evitar conflictos.
- Los archivos compartidos (`strings.xml`, `colors.xml`, `AndroidManifest.xml`) los toca todo el mundo:
  haz `git pull` antes de empezar y haz commits pequeños.
- Antes de subir, comprueba que el proyecto compila (▶ Run o *Build → Make Project*).
- No hace falta que te preocupes por las carpetas generadas (`build/`, `.gradle/`) ni por
  `local.properties`: el `.gitignore` ya evita que se suban.

---

## 9. Problemas comunes

| Problema | Qué hacer |
|----------|-----------|
| Al pulsar ▶ sale "Unknown run configuration type AndroidRunConfigurationType" o "Plugin Android… is currently not installed" | Abriste el proyecto con **IntelliJ IDEA** (u otro editor) y no con **Android Studio**. Ciérralo y ábrelo con Android Studio (*File → Open*). |
| `git push` da un error de permisos (403 o "Permission denied") | Aún no eres colaborador del repositorio. Pide la invitación al dueño y acéptala desde el correo de GitHub. |
| El *Gradle sync* falla o se queda pegado | Comprueba que tienes internet (la primera vez descarga mucho). Prueba *File → Sync Project with Gradle Files*. |
| "SDK location not found" | Android Studio no encontró el SDK. Abre *File → Project Structure → SDK Location* y elige la carpeta del SDK, o deja que Android Studio lo instale. |
| Pide instalar "Android SDK Platform 37" | Pulsa **Install** en el aviso y acepta. |
| Android Studio dice que el plugin es muy nuevo | Actualiza Android Studio (*Help → Check for Updates*). |
| El teléfono no aparece en la lista | Revisa que la Depuración USB esté activa, acepta el aviso en el teléfono, y prueba con otro cable (debe ser de datos). |
| Aparece un aviso de "guardar contraseña" (por ejemplo Samsung Pass) al iniciar sesión | Es del teléfono, no de la app. Elige "Ahora no". |
| Un cambio no se ve | Vuelve a ejecutar con ▶ Run. Si sigue igual: *Build → Clean Project* y luego ▶ Run. |

---

## 10. Datos técnicos

- **Lenguaje:** Java · **Interfaz:** vistas XML con Material Design 3
- **Versión mínima de Android:** 7.0 (API 24) · **Versión objetivo:** API 37
- **Paquete:** `com.example.chocosalud`
- **Herramientas:** Android Gradle Plugin 9.3.3 · Gradle 9.5.0
- **Base de datos:** SQLite con `SQLiteOpenHelper` (archivo `chocosalud.db`, versión 1)

---

## 11. Base de datos SQLite (Fase 4)

La app guarda sus datos en una base de datos **SQLite** dentro del teléfono (archivo `chocosalud.db`), así
la información se conserva aunque se cierre la app o no haya internet. Toda la base de datos se maneja
desde una sola clase: **`BaseDatos.java`**.

### 11.1 Tablas

**`usuarios`**: las personas registradas.

| Columna | Tipo | Notas |
|---------|------|-------|
| `_id` | INTEGER | Llave primaria, la asigna SQLite |
| `nombre` | TEXT | Obligatorio |
| `documento` | TEXT | Obligatorio, **único** (no se repite) |
| `correo` | TEXT | Obligatorio, **único**; se usa para iniciar sesión |
| `telefono` | TEXT | Obligatorio |
| `contrasena` | TEXT | Obligatorio. Se guarda el **hash SHA-256**, nunca la contraseña tal cual |

**`centros_salud`**: clínicas y hospitales. Al crear la base de datos se agregan los dos centros que
ya mostraba la app (Quibdó y Tadó).

| Columna | Tipo | Notas |
|---------|------|-------|
| `_id` | INTEGER | Llave primaria |
| `nombre` | TEXT | Obligatorio |
| `municipio` | TEXT | Obligatorio |
| `direccion` | TEXT | Opcional |
| `telefono` | TEXT | Opcional |
| `latitud` | REAL | Obligatorio (número con decimales) |
| `longitud` | REAL | Obligatorio |

**`citas`**: las citas agendadas. **Esta tabla es también el historial médico** del usuario.

| Columna | Tipo | Notas |
|---------|------|-------|
| `_id` | INTEGER | Llave primaria |
| `usuario_id` | INTEGER | Llave foránea → `usuarios._id`. Si se borra el usuario, se borran sus citas |
| `centro_id` | INTEGER | Llave foránea → `centros_salud._id` |
| `especialidad` | TEXT | Obligatorio |
| `telefono` | TEXT | Teléfono de contacto de la cita |
| `fecha` | TEXT | Formato `yyyy-MM-dd` (ej. `2026-11-20`), para poder ordenar por fecha |
| `creada_en` | TEXT | Fecha y hora de creación; la pone SQLite sola |

### 11.2 Plan de implementación:

| # | Paso | Pantallas                                                | Operación SQL | Estado |
|---|------|----------------------------------------------------------|---------------|--------|
| 0 | Crear la base de datos y las tablas | `BaseDatos.java`                                         | CREATE TABLE | Hecho |
| 1 | Registro e inicio de sesión reales | Registro, Inicio de sesión                               | INSERT, SELECT | Pendiente |
| 2 | Ver y editar el perfil | Perfil y notificaciones (pantalla 8)                     | SELECT, UPDATE | Pendiente |
| 3 | Agregar clínicas | Centros de salud (pantalla 5)                            | INSERT, SELECT | Pendiente |
| 4 | Elegir clínica al agendar y ver el historial | Agendar cita (pantalla 6), Historial médico (pantalla 7) | INSERT, SELECT | Pendiente |

1. **Registro e inicio de sesión.** El formulario de registro (desde *"¿No tienes cuenta? Regístrate"*)
   guarda el usuario en la tabla `usuarios`. Si el correo o el documento ya existen, avisa. Para entrar a
   la app es **obligatorio** iniciar sesión con un correo y una contraseña que estén guardados en la base
   de datos. Al iniciar sesión se recuerda el `_id` del usuario para que las demás pantallas sepan quién
   está conectado.
2. **Perfil y notificaciones.** Muestra los datos del usuario conectado y permite **editar** nombre,
   correo, teléfono, contraseña y demás datos.
3. **Centros de salud.** Se agrega un botón **"Agregar clínica"** que guarda en `centros_salud` el nombre,
   municipio, latitud, longitud y demás datos. La lista de centros se lee desde la tabla (ya no queda
   escrita en el código).
4. **Agendar cita e historial.** Se agrega un **desplegable para elegir la clínica** (leído de
   `centros_salud`). Al pulsar **Enviar** hace lo mismo que hoy (abre el calendario del teléfono) y
   además **guarda la cita** en `citas`. La pantalla **Historial médico** lista las citas del usuario.

### 11.3 Reglas para trabajar con la base de datos

- **Usar las constantes** de `BaseDatos` (`TABLA_USUARIOS`, `USUARIO_CORREO`…) en vez de
  escribir los nombres a mano.
- **Nunca armar el SQL pegando textos con `+`** con datos que escribe el usuario. Usar `ContentValues`
  para insertar o actualizar, y `?` en las consultas
  (`rawQuery("... WHERE correo = ?", new String[]{correo})`). Así se evita la **inyección SQL**.
- **Si se cambia una tabla**, subir `VERSION` en `BaseDatos.java`. `onCreate` solo
  corre la primera vez; si no se sube la versión, en los teléfonos que ya tenían la app aparecerán errores
  como "no such column". Otra opción mientras se desarrolla: desinstalar la app del teléfono.
- **El archivo se crea la primera vez que una pantalla abre la base de datos** (`getWritableDatabase()` o
  `getReadableDatabase()`), no al instalar la app.
- **Para ver las tablas y los datos en vivo:** ejecuta la app y abre en Android Studio
  *View → Tool Windows → App Inspection → Database Inspector*.
