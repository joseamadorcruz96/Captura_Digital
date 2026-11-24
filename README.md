# Captura Digital

Aplicación móvil Android para la **captación digital de prospectos de admisión de Duoc UC**, conectada a una arquitectura distribuida de **microservicios propios en Spring Boot** detrás de un **API Gateway** y con soporte **Offline-First** mediante Room SQLite y Preferences DataStore.

> **Estado actual (rama `v2` — Evolución Post-Etapa Universitaria):**
> El proyecto cerró su entrega académica en `main` / `develop` (tag `v1.0-universidad`) y continúa evolucionando en la rama **`v2`** bajo estrategia **App-First**: todas las nuevas capacidades se construyen primero 100% operativas en local (`LOCAL_DEMO` con Room + DataStore) manteniendo compatibilidad retroactiva con el backend (`REMOTE_GATEWAY`), cuya ampliación quedará para la etapa de integración backend.

## Descripción del proyecto

En ferias, colegios y eventos, los captadores de Duoc UC registran a los interesados en un **cupón de admisión de papel**. Ese proceso es lento, propenso a errores (RUT mal escrito, datos ilegibles) y obliga a digitar todo nuevamente después.

**Captura Digital** reemplaza el cupón físico por un flujo móvil inteligente que:

- Opera **100% sin conexión** (*Offline-First* y modo **Demo Local** desacoplado del backend) con persistencia local en **Room SQLite (`v5`)** y sesión persistente en **Preferences DataStore**.
- Gestiona **eventos, ferias, visitas a colegios y turnos de jornada** con selector de evento activo y metas de captación.
- Acelera el registro mediante **`StepForm` 2.0**: escaneo de **cédula chilena / QR**, **firma digital táctil**, **autocompletado de 346 comunas y establecimientos**, **detección inteligente de RUT duplicado**, **borradores locales** y **Modo Captura Express**.
- Valida los datos en vivo (RUT con dígito verificador Módulo 11, contacto y campos obligatorios) y registra el consentimiento informado (Ley N° 19.628 / 21.719).
- Protege las operaciones entre microservicios con autenticación **Bearer JWT** por rol (`ADMINISTRATIVO`, `CAPTADOR`, `PROSPECTO`).
- Entrega un dashboard con métricas por rol, indicadores económicos del día y vista consolidada de capturas y borradores.

### Perfiles de usuario

| Perfil | Descripción |
|---|---|
| **Captador de terreno** | Perfil principal. Selecciona evento activo, inicia/cierra turno, registra prospectos (Completo o Express), gestiona borradores y sincroniza lotes. |
| **Prospecto interesado** | Usuario final: persona cuyos datos se registran en el cupón (actualmente cuenta con acceso controlado de prueba; tendrá flujo completo de **Modo Kiosco** en la siguiente épica). |
| **Administrativo** | Perfil administrador. Gestiona cuentas de captadores, administra eventos/ferias, consulta métricas globales y opera CRUD remoto/local. |

## Integrantes (Línea base universitaria `v1.0`)

| Integrante | Responsabilidades en entrega base |
|---|---|
| **Gonzalo Salas Guevara** | Login, recuperar contraseña, gestión de usuarios, Cómo funciona, dashboard base, microservicio `ms-login`, emisión JWT, consumo de API externa (`mindicador.cl`) y pruebas asociadas. |
| **José Amador** | Página de inicio, formulario modal Stepper de 6 pasos (`StepForm`), validación de RUT Módulo 11, persistencia local con Room, monitoreo global de red y auto-sincronización Offline-First, microservicio `ms-formulario`, `api-gateway`, validación JWT en `ms-formulario`, CRUD remoto en Dashboard, despliegue Docker Compose, cobertura JaCoCo (`>= 80%`) y firma de APK release. *(Continúa el desarrollo evolutivo en rama `v2`).* |

---

## Lo Nuevo en `v2` (Implementado — Fases 0 y 1 parcial)

| Épica | Funcionalidades implementadas en `v2` | Estado |
|---|---|---|
| **Fase 0 · Épica 0: Base App-First** | Selector en tiempo de ejecución `AppMode` (`LOCAL_DEMO` vs `REMOTE_GATEWAY`), cuentas demo para los 3 roles (`LocalUserDataSource`), persistencia real de sesión en `DataStoreSessionStorage`, `AppContainer` (DI manual), `DemoSeedGenerator` con RUTs Módulo 11, migraciones explícitas Room (`DatabaseMigrations`) y `KioscoPlaceholderScreen` para rol `PROSPECTO`. | Implementado |
| **Fase 1 · Épica 1: Eventos y Turnos** | Entidades Room `Evento` (`FERIA`, `COLEGIO`, `CASA_ABIERTA`, `OTRO`) y `TurnoEvento` (`AppDatabase v4` + `MIGRATION_3_4`), selector de evento activo en `InicioScreen` persistido en DataStore, pantalla `EventosScreen` (CRUD + filtros `Activos` / `Todos` / `Archivados` para `ADMINISTRATIVO`), check-in de turno con barra de progreso en vivo y diálogo **Resumen de Cierre de Turno**. | Implementado |
| **Fase 1 · Épica 2: `StepForm` 2.0** | Borradores locales no sincronizables (`formulario_borradores`, `AppDatabase v5` + `MIGRATION_4_5`) con autoguardado debounced y botón **Guardar borrador**; vista consolidada **`Mis Capturas y Borradores` (`CapturasScreen`)**; **`SignaturePad`** táctil en `Canvas` (PNG Base64 + timestamp); **escáner QR Registro Civil / PDF417** (`CameraX` + `ML Kit` + `CedulaChileParser`); detección de **RUT duplicado** (*Ver ficha* / *Cargar para actualizar*); autocompletado offline con las **16 regiones y 346 comunas de Chile** (`comunas_chile.json`) y establecimientos (`establecimientos_chile.json`); **Modo Captura Express** (`EXPRESS` vs `COMPLETO`) y **geolocalización opcional** (`LocationManager`). | Implementado |

## Hoja de Ruta — Lo que se viene en `v2`

| Fase | Épica | Funcionalidades planificadas |
|---|---|---|
| **Fase 1** *(en curso)* | **Épica 3: Modo Kiosco (`UserRole.PROSPECTO`)** | Flujo autónomo para tablets/tótems en ferias: pantalla de bienvenida, `StepForm` adaptado en 2ª persona con timeout de inactividad y protección PII ante RUT duplicado, pantalla final con resumen + código QR, reinicio automático configurable y bloqueo de salida mediante PIN del captador. |
| **Fase 1** *(en curso)* | **Épica 4: Explorador Vocacional de Carreras** | Migración de `CatalogoDuoc` a catálogo JSON buscable/filtrable por escuela, sede, nivel (`PROFESIONAL`/`TECNICO`) y palabras clave; ficha informativa por carrera y acción directa **"Agregar a nuevo cupón"**. |
| **Fase 2** | **Épica 5: Directorio y CRM Móvil** | Pantalla dedicada de Directorio de Prospectos con búsqueda y filtros combinados, ficha de detalle y edición completa, mini-pipeline de seguimiento (`NUEVO`, `CONTACTADO`, `POSTULANDO`, `MATRICULADO`, `DESCARTADO`), notas del captador, acciones rápidas (WhatsApp con plantillas, llamada, email) y recordatorios locales. |
| **Fase 2** | **Épica 6: Centro de Sincronización y `WorkManager`** | Bandeja de registros `PENDING_SYNC` y `SYNC_ERROR` con detalle de error y reintento unitario/lote, sincronización en segundo plano con `WorkManager` (backoff exponencial) y notificación local de fin de jornada. |
| **Fase 3** | **Épica 7: Dashboard Analítico 2.0 y Metas** | Filtros globales (evento, fechas, sede, captador), embudo por nivel de interés, top carreras y colegios, capturas por hora y seguimiento de metas. |
| **Fase 3** | **Épica 8: Exportación y Reportes Locales** | Exportación offline desde Room a **CSV/Excel** y **PDF** (resumen de jornada y ficha individual) compartibles vía `FileProvider` y ShareSheet nativo. |
| **Fase 4** | **Épica 9: Seguridad Local y Privacidad** | Bloqueo rápido por Biometría/PIN al reanudar la app, enmascaramiento opcional de PII en listados en terreno y bitácora local de auditoría (Ley N° 19.628 / 21.719). |
| **Fase 4** | **Épica 10: UX, Tablet y Calidad** | Tema Oscuro/Claro, diseño adaptativo para tablets (Master-Detail), onboarding interactivo, herramientas de debug (generador masivo de prospectos) y tests UI en Jetpack Compose. |
| **Fase 5** | **Integración Backend** | Ampliar `ms-formulario`, `ms-login` y `api-gateway` con los nuevos contratos (`Evento` extendido, firma/ubicación/completitud en `ProspectoDto`, notas/pipeline, catálogo versionado y refresh tokens). |

---

## Funcionalidades base (`v1.0` integradas)

| Funcionalidad | Responsable | Estado |
|---|---|---|
| Inicio de sesión contra `ms-login` con emisión de token JWT (sin autorregistro: las cuentas las crea el administrador) | Gonzalo | Implementado |
| Validación por campo con íconos y mensajes de error en el login | Gonzalo | Implementado |
| Recuperar contraseña (valida que el correo exista en `ms-login`) | Gonzalo | Implementado (envío de correo simulado) |
| Gestión de usuarios para el administrador: crear, listar, editar y eliminar (CRUD completo) | Gonzalo | Implementado |
| Cómo funciona (guía paso a paso del cupón de admisión) | Gonzalo | Implementado |
| Dashboard por rol: género, rango de edad y carreras de interés (el captador ve lo suyo, el administrador el total) | Gonzalo | Implementado |
| Indicadores económicos del día (UF, dólar y UTM) desde API externa (`mindicador.cl`) | Gonzalo | Implementado |
| Vibración al fallar el inicio de sesión y compartir resumen del dashboard (recursos nativos) | Gonzalo | Implementado |
| Animaciones en el dashboard y en Cómo funciona | Gonzalo | Implementado |
| Página de inicio (`InicioScreen`) con evento activo, métricas diarias, estado de red y botón de sincronización | José | Implementado (extendido en `v2`) |
| Formulario modal Stepper de 6 pasos (`StepFormScreen`) con navegación libre, alertas por etapa y validación RUT Módulo 11 | José | Implementado (extendido en `v2`) |
| Persistencia local con Room SQLite (`AppDatabase`, `ProspectoDao`) y estados `PENDING_SYNC` / `SYNCED` / `SYNC_ERROR` | José | Implementado (extendido a `v5` en `v2`) |
| Monitor global de conectividad (`NetworkMonitor`, `ConnectivityBadge`, `OfflineStatusBanner`) y auto-sincronización al recuperar red | José | Implementado |
| API Gateway (`backend/api-gateway`) con enrutamiento WebMVC, CORS, logging HTTP global y Swagger agregado | José | Implementado |
| Microservicio `ms-formulario` (`backend/ms-formulario`): CRUD Prospectos/Eventos, Batch Sync aislado (`REQUIRES_NEW` + *upsert* por RUT), métricas y seguridad Bearer JWT | José | Implementado |
| CRUD remoto de prospectos en tiempo real desde Dashboard (`rotarNivelInteres` y `eliminarProspecto`) | José | Implementado |
| Despliegue contenedorizado con Docker (`Dockerfile` multi-stage + `compose.yaml`) y URL configurable vía `BuildConfig` | José | Implementado |
| Firma de APK en modo release (`app-release.apk`) y suite de cobertura JaCoCo (`>= 80%` en `ms-formulario`) | José | Implementado |

## Endpoints utilizados

En ejecución integrada (`REMOTE_GATEWAY`), la aplicación móvil consume todos los servicios propios a través de una única puerta de entrada (**`api-gateway`** en el puerto `8080`) enviando el header `Authorization: Bearer <JWT>`. En modo **`LOCAL_DEMO`** (activo por defecto en `v2` para desarrollo App-First), la app opera completamente en local sin emitir peticiones al Gateway.

### API Gateway (`backend/api-gateway`, puerto `8080`, sin BD propia)

| ID de Ruta | Predicado Path | Servicio Destino | Descripción |
|---|---|---|---|
| `ms-login-auth` | `/api/auth/**` | `ms-login` (`:8081`) | Login y recuperación de contraseña |
| `ms-login-usuarios` | `/api/usuarios/**` | `ms-login` (`:8081`) | CRUD administrativo de usuarios |
| `ms-formulario-prospectos` | `/api/v1/prospectos/**` | `ms-formulario` (`:8082`) | CRUD, Batch Sync y métricas de prospectos |
| `ms-formulario-eventos` | `/api/v1/eventos/**` | `ms-formulario` (`:8082`) | Gestión y consulta de ferias/eventos |
| `ms-formulario-docs` | `/openapi/formulario` → `/v3/api-docs` | `ms-formulario` (`:8082`) | Contrato OpenAPI JSON y Swagger UI (`/swagger-ui.html`) |

### Microservicio propio: `ms-login` (Spring Boot + MySQL, puerto interno `8081`)

| Método | Endpoint | Acceso | Descripción | Uso en la app |
|---|---|---|---|---|
| `POST` | `/api/auth/login` | Público | Valida correo/contraseña y devuelve `token` JWT + `usuario` | Pantalla de login |
| `POST` | `/api/auth/recuperar` | Público | Verifica que el correo esté registrado | Recuperar contraseña |
| `GET` | `/api/usuarios` | `ADMINISTRATIVO` | Lista todos los usuarios | Gestión de usuarios |
| `GET` | `/api/usuarios/{id}` | `ADMINISTRATIVO` | Obtiene un usuario por ID | - |
| `POST` | `/api/usuarios` | `ADMINISTRATIVO` | Crea un usuario (contraseña cifrada con BCrypt) | Gestión de usuarios |
| `PUT` | `/api/usuarios/{id}` | `ADMINISTRATIVO` | Actualiza un usuario | Gestión de usuarios |
| `DELETE` | `/api/usuarios/{id}` | `ADMINISTRATIVO` | Elimina un usuario | Gestión de usuarios |

Ejemplo de cuerpo para crear un usuario:

```json
{
  "nombre": "Administrador",
  "email": "admin@captura.cl",
  "password": "123456",
  "rol": "ADMINISTRATIVO"
}
```

Roles válidos: `CAPTADOR`, `PROSPECTO`, `ADMINISTRATIVO`.

### Microservicio propio: `ms-formulario` (Spring Boot + H2/MySQL, puerto interno `8082`)

| Método | Endpoint | Acceso (JWT) | Descripción | Uso en la app |
|---|---|---|---|---|
| `GET` | `/api/v1/prospectos` | `CAPTADOR` / `ADMINISTRATIVO` | Lista prospectos con filtros opcionales (`captadorId`, `eventoId`, `rut`, `sede`) | Refresco remoto (`Inicio` / `Dashboard`) |
| `GET` | `/api/v1/prospectos/{clientUuid}` | `CAPTADOR` / `ADMINISTRATIVO` | Obtiene un prospecto por su UUID | Consulta unitaria / Swagger |
| `POST` | `/api/v1/prospectos` | `CAPTADOR` / `ADMINISTRATIVO` | Crea un prospecto unitario (valida RUT Módulo 11, `409` si RUT existe) | API REST / Swagger |
| `PUT` | `/api/v1/prospectos/{clientUuid}` | `CAPTADOR` / `ADMINISTRATIVO` | Actualiza un prospecto completo | Rotar nivel de interés / edición |
| `DELETE` | `/api/v1/prospectos/{clientUuid}` | `CAPTADOR` / `ADMINISTRATIVO` | Elimina un prospecto | Eliminar en `Dashboard` |
| `POST` | `/api/v1/prospectos/sync` | `CAPTADOR` / `ADMINISTRATIVO` | Sincronización por lote Offline-First: transacción aislada por ítem (`REQUIRES_NEW`), *upsert* idempotente por RUT y respuesta parcial (`exitososUuids`, `fallidosUuids`, `erroresPorUuid`) | Guardado en `StepForm` y sincronización en `Inicio` / `Capturas` |
| `GET` | `/api/v1/prospectos/resumen` | `CAPTADOR` / `ADMINISTRATIVO` | Métricas agregadas JPQL (`totalProspectos`, `conteoPorSede`, `conteoPorNivelInteres`, `conteoPorJornada`) | Analítica / Swagger |
| `GET` | `/api/v1/eventos` | `CAPTADOR` / `ADMINISTRATIVO` | Lista eventos (filtro opcional `?activo=true`) | Refresco de eventos en `REMOTE_GATEWAY` |
| `POST` / `PUT` / `DELETE` | `/api/v1/eventos` | `ADMINISTRATIVO` | Crea, edita o elimina eventos/ferias | Administración vía Swagger/API *(ampliación pendiente en Fase 5)* |

*(Nota: los campos añadidos en `v2` dentro de `ProspectoDto` y `EventoResponseDto` —como `estadoCompletitud`, `firmaDigitalBase64`, `firmaTimestampEpoch`, `latitud`, `longitud`, `tipo`, `metaProspectos` y `archivado`— son opcionales/nullable para mantener compatibilidad retroactiva hasta implementar la Fase 5 en backend).*

### API externa: `mindicador.cl`

| Método | Endpoint | Descripción | Uso en la app |
|---|---|---|---|
| `GET` | `https://mindicador.cl/api` | Indicadores económicos de Chile del día | Tarjeta "Indicadores económicos de hoy" en el dashboard (UF, dólar y UTM) |

Se consume directamente desde `RetrofitClient.indicadoresApi` (sin pasar por el API Gateway ni enviar JWT). Si la API externa no responde, el dashboard sigue funcionando sin mostrar esa tarjeta.

## Arquitectura

### Aplicación móvil (MVVM + Offline-First + App-First)

- **View** (`ui/*/…Screen.kt`): pantallas en Jetpack Compose que observan `StateFlow` y emiten eventos sin lógica de negocio.
- **ViewModel** (`ui/*/…ViewModel.kt`): procesa eventos, aplica validaciones y expone el estado inmutable (`*UiState`).
- **DI Manual** (`di/AppContainer.kt` + `ViewModelFactoryHelper.kt`): `DefaultAppContainer` provee repositorios, `SessionStorage` (DataStore), `LocalUserDataSource`, `CatalogoLocalidadesDataSource`, observador de conectividad y APIs.
- **Model / Data** (`data/`): repositorios (`AuthRepository`, `UsuarioRepository`, `EventoRepository`, `BorradorRepository`, `ProspectoRepository`) que operan en modo **`LOCAL_DEMO`** (Room + DataStore + fuentes locales) o **`REMOTE_GATEWAY`** (Room + Retrofit JWT).

```text
Pantalla (Compose) → ViewModel → AppContainer → Repositories
                                                   ├─► LOCAL_DEMO: Room (v5: prospectos, eventos, turnos_evento, formulario_borradores) + DataStore + assets JSON
                                                   ├─► REMOTE_GATEWAY: Room + RetrofitClient (JWT) → api-gateway (:8080) ─┬─► ms-login (:8081) → MySQL
                                                   │                                                                      └─► ms-formulario (:8082) → H2/MySQL
                                                   └─► IndicadoresApi (directo) → https://mindicador.cl/api
```

### Backend (Microservicios + API Gateway)

- Patrón **Database-per-Service**: `ms-login` gestiona la base `captura_login` y emite tokens HMAC-SHA (`jjwt 0.12.6`); `ms-formulario` gestiona la base `captura_formulario` y valida el mismo JWT de forma stateless (`JwtAuthFilter` + `JwtValidationService`) sin acoplar bases de datos ni llamadas síncronas entre servicios.
- `api-gateway` actúa como puerta de entrada única (`:8080`), sin base de datos propia, centralizando enrutamiento WebMVC, CORS y logging HTTP.

### Estructura del repositorio

```text
Captura_Digital_v2/
├── frontend/                                   # App Android (Kotlin + Jetpack Compose)
│   └── app/
│       ├── build.gradle.kts                    # BuildConfig GATEWAY_BASE_URL + signingConfigs + room.schemaLocation
│       ├── schemas/                            # Esquemas Room exportados (3.json, 4.json, 5.json)
│       └── src/
│           ├── main/assets/                    # Catálogos offline (comunas_chile.json, establecimientos_chile.json)
│           ├── main/java/com/therionlabs/captura_digital_v2/
│           │   ├── CapturaDigitalApp.kt        # Inicialización Room, AppContainer y NetworkMonitor
│           │   ├── MainActivity.kt             # Permiso ACCESS_LOCAL_NETWORK (SDK 37) y NavHost
│           │   ├── di/                         # AppContainer, ViewModelFactoryHelper
│           │   ├── data/
│           │   │   ├── local/                  # AppDatabase (v5), DatabaseMigrations, Converters, SessionStorage, LocalUserDataSource, CatalogoLocalidadesDataSource, DemoSeedGenerator, dao/
│           │   │   ├── model/                  # Prospecto, Evento, TurnoEvento, FormularioBorrador, AppMode, CatalogoDuoc, User, dto/
│           │   │   ├── remote/                 # RetrofitClient, AuthApi, ProspectoApi, IndicadoresApi, NetworkMonitor
│           │   │   └── repository/             # AuthRepository, UsuarioRepository, EventoRepository, BorradorRepository, ProspectoRepository
│           │   ├── ui/
│           │   │   ├── auth/                   # Login (selector LOCAL_DEMO / REMOTE_GATEWAY), Recover y AuthNavigation
│           │   │   ├── capturas/               # CapturasScreen ("Mis Capturas y Borradores")
│           │   │   ├── components/             # ConnectivityBadge, OfflineStatusBanner, Duoc*
│           │   │   ├── dashboard/              # Dashboard, Indicadores externos y CRUD prospectos
│           │   │   ├── eventos/                # Gestión administrativa de eventos y ferias
│           │   │   ├── formularioModal/        # StepForm 2.0 (Completo + Express), SignaturePad, CedulaScannerSheet, AutocompleteTextField, steps/
│           │   │   ├── help/                   # Cómo funciona
│           │   │   ├── inicio/                 # Inicio, selector de evento activo, check-in de turno, métricas y sync
│           │   │   ├── kiosco/                 # KioscoPlaceholderScreen (base para Épica 3)
│           │   │   ├── theme/                  # Color, Theme, Type (Duoc UC)
│           │   │   └── usuarios/               # Gestión de usuarios (administrador)
│           │   └── util/                       # AuthValidator, FormularioValidator, EventoValidator, EventoDateUtils, CedulaChileParser, LocationHelper, RutUtils
│           └── test/java/com/therionlabs/captura_digital_v2/   # 21 suites de pruebas unitarias + Fakes
└── backend/
    ├── Dockerfile                              # Build multi-stage parametrizado (ARG SERVICE_DIR)
    ├── compose.yaml                            # Stack completo: 2 MySQL + ms-login + ms-formulario + api-gateway
    ├── .env.example                            # Plantilla de variables de entorno (MYSQL_*, JWT_SECRET, etc.)
    ├── README.md                               # Guía operativa de Docker Compose y AWS EC2
    ├── api-gateway/                            # Puerta de enlace (Spring Cloud Gateway WebMVC, puerto 8080)
    ├── ms-login/                               # Autenticación JWT y usuarios (puerto 8081)
    └── ms-formulario/                          # Prospectos, eventos y Batch Sync (puerto 8082)
```

## Tecnologías

| Parte | Tecnologías |
|---|---|
| **App móvil** | Kotlin `2.2.10`, Jetpack Compose (BOM `2026.02.01`), Material 3, Navigation Compose `2.10.1`, ViewModel/StateFlow `2.11.0`, Room `2.7.0` + KSP, DataStore Preferences `1.1.7`, CameraX `1.4.2`, ML Kit Barcode Scanning `17.3.0`, Retrofit `3.0.0` + Converter-Gson `3.0.0` |
| **Backend** | Java `21`, Spring Boot `4.2.0`, Spring Cloud Gateway WebMVC (`2026.0.0-SNAPSHOT`), Spring Data JPA, Bean Validation, Spring Security + `jjwt 0.12.6`, BCrypt, Lombok, SpringDoc OpenAPI `3.1.1`, MySQL `8.4`, H2 |
| **Pruebas** | JUnit 4 + `kotlinx-coroutines-test 1.10.2` (app), JUnit 5 + Mockito + MockMvc (backend), JaCoCo `0.8.13` (cobertura) |
| **Infraestructura y Colaboración** | Docker, Docker Compose, AWS EC2, Git, GitHub y Trello |

## Requisitos

- Android Studio (versión reciente) con SDK Android (API 24+ en dispositivo/emulador, `compileSdk` / `targetSdk 37`)
- Java 21 o superior (JDK 21+)
- Docker Desktop (para levantar el backend cuando uses modo `REMOTE_GATEWAY`) **o** MySQL 8+ si ejecutas cada microservicio manualmente. *(En modo `LOCAL_DEMO` no necesitas levantar el backend).*

## Pasos para ejecutar

### 1. Clonar el repositorio

```bash
git clone https://github.com/joseamadorcruz96/Captura_Digital.git
cd Captura_Digital
git checkout v2   # Para la versión evolutiva App-First
```

### 2. Levantar el Backend *(solo requerido en modo `REMOTE_GATEWAY`)*

#### Opción A: Todo el stack con Docker Compose (Recomendado)

Levanta automáticamente `db-login`, `db-formulario`, `ms-login` (`:8081`), `ms-formulario` (`:8082`) y `api-gateway` (`:8080`):

```bash
cd backend
cp .env.example .env
# Edita .env con tus contraseñas de MySQL y un JWT_SECRET compartido (>= 32 caracteres)
docker compose up -d --build
```

Verificación rápida:
- Healthcheck del Gateway: `curl http://localhost:8080/actuator/health`
- Swagger UI de `ms-formulario` vía Gateway: `http://localhost:8080/swagger-ui.html`

#### Opción B: Ejecución local sin Docker

1. Crea la base MySQL para `ms-login` (`CREATE DATABASE captura_login;`; `ms-formulario` usa perfil `h2` por defecto o `mysql` si creas `captura_formulario`).
2. Levanta los 3 servicios en terminales separadas usando un `JWT_SECRET` compartido:

```bash
# Terminal 1: ms-login (:8081)
cd backend/ms-login
export DB_PASSWORD=tu_password_mysql
export JWT_SECRET=captura-digital-dev-secret-key-2026-min-32-bytes
./mvnw spring-boot:run   # En Windows PowerShell: .\mvnw.cmd spring-boot:run

# Terminal 2: ms-formulario (:8082, perfil h2 por defecto)
cd backend/ms-formulario
export JWT_SECRET=captura-digital-dev-secret-key-2026-min-32-bytes
./mvnw spring-boot:run

# Terminal 3: api-gateway (:8080)
cd backend/api-gateway
./mvnw spring-boot:run
```

### 3. Crear el usuario administrador inicial (si la BD parte vacía en `REMOTE_GATEWAY`)

`ms-login` crea las cuentas desde el rol administrador (`AdminInicial` o vía API):

```bash
curl -X POST http://localhost:8080/api/usuarios \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Administrador","email":"admin@captura.cl","password":"123456","rol":"ADMINISTRATIVO"}'
```

Los demás usuarios (`CAPTADOR` / `ADMINISTRATIVO` / `PROSPECTO`) se crean desde la app: **Dashboard → Gestionar usuarios**.

### 4. Aplicación móvil (`frontend/`)

1. Abre la carpeta **`frontend`** en Android Studio y espera la sincronización de Gradle.
2. En modo **`LOCAL_DEMO`** (seleccionado por defecto en la pantalla de Login en `v2`), puedes usar toda la app de inmediato sin configurar servidor.
3. Si cambias a **`REMOTE_GATEWAY`**, configura la URL del Gateway según dónde ejecutes el backend:
   - **AWS EC2 (por defecto):** `BuildConfig.GATEWAY_BASE_URL` apunta a `http://100.61.210.112:8080/` definido en [build.gradle.kts](frontend/app/build.gradle.kts).
   - **Emulador local:** agrega `GATEWAY_BASE_URL=http://10.0.2.2:8080/` en `frontend/local.properties` (ignorado por Git).
   - **Teléfono físico por USB contra backend local/túnel SSH:** ejecuta `adb reverse tcp:8080 tcp:8080` y configura `http://127.0.0.1:8080/`.
4. Selecciona el emulador o dispositivo y presiona **Run**.

## Usuarios de prueba

### Modo `LOCAL_DEMO` (sin backend, botones de acceso rápido en `LoginScreen`)

| Correo | Contraseña | Rol |
|---|---|---|
| `admin@duoc.cl` | `123456` | `ADMINISTRATIVO` |
| `captador@duoc.cl` | `123456` | `CAPTADOR` |
| `prospecto@duoc.cl` | `123456` | `PROSPECTO` |

### Modo `REMOTE_GATEWAY` (`ms-login`)

| Correo | Contraseña | Rol |
|---|---|---|
| `admin@captura.cl` | `123456` | `ADMINISTRATIVO` |

Con este usuario se pueden crear captadores desde **Gestionar usuarios**.

## Pruebas unitarias

### Ejecutar

**App móvil** (desde `frontend/`):

```bash
./gradlew testDebugUnitTest                 # Windows: .\gradlew.bat testDebugUnitTest
./gradlew createDebugUnitTestCoverageReport # Windows: .\gradlew.bat createDebugUnitTestCoverageReport
```

El reporte de cobertura Android queda en `frontend/app/build/reports/coverage/test/debug/index.html`.

**Backend — `ms-formulario`** (tests + verificación obligatoria JaCoCo `>= 80%`, desde `backend/ms-formulario/`):

```bash
./mvnw clean verify                         # Windows: .\mvnw.cmd clean verify
```

El reporte JaCoCo queda en `backend/ms-formulario/target/site/jacoco/index.html`.

**Backend — `api-gateway` y `ms-login`:**

```bash
cd backend/api-gateway && ./mvnw test
cd backend/ms-login && ./mvnw test
```

### Qué se prueba

#### App móvil (`frontend/app/src/test/`, `21` suites — `116` pruebas en `v2`)

Las pruebas de la app utilizan implementaciones simuladas (**Fakes**: `FakeAuthApi`, `FakeProspectoApi`, `FakeIndicadoresApi`, `FakeProspectoRepository`, `InMemorySessionStorage`) y `MainDispatcherRule` (`kotlinx-coroutines-test`), por lo que no requieren backend ni red encendidos:

| Archivo de prueba | Qué valida |
|---|---|
| `AuthValidatorTest` | Reglas de correo, contraseña y nombre |
| `LoginViewModelTest` | Errores por campo, login correcto, credenciales incorrectas, sin conexión (`IOException`) y cambio de modo `LOCAL_DEMO` |
| `RecoverViewModelTest` | Correo vacío, inválido, registrado, no registrado y sin conexión |
| `UsuariosViewModelTest` | Carga, crear, editar, cancelar, eliminar y errores del servidor |
| `DashboardViewModelTest` | Filtro por rol, cálculo de género/edades/carreras, API externa, `rotarNivelInteres`, `eliminarProspecto` y texto para compartir |
| `AuthRepositoryTest` | Login (`LOCAL_DEMO` / `REMOTE_GATEWAY`), persistencia DataStore entre reinicios, token JWT, cierre de sesión y recuperación |
| `UsuarioRepositoryTest` | CRUD de usuarios en modo remoto y `LOCAL_DEMO` (los 3 roles) y manejo de errores |
| `EventoRepositoryTest` | Seed demo de eventos, persistencia de evento activo en DataStore, refresco `REMOTE_GATEWAY`, CRUD y unicidad de turno activo |
| `EventosViewModelTest` | Filtros (`Activos`, `Todos`, `Archivados`), validaciones y CRUD administrativo de eventos |
| `CapturasViewModelTest` | Consolidado de borradores locales no sincronizables vs capturas pendientes/sincronizadas, filtros, sync y descarte |
| `CatalogoLocalidadesTest` | Carga de `comunas_chile.json` (`346` comunas) y `establecimientos_chile.json`, búsqueda sin tildes y texto libre |
| `CedulaChileParserTest` | Parseo QR Registro Civil (`RUN=...`), RUT con `K`, PDF417, líneas MRZ e inputs inválidos |
| `DatabaseMigrationTest` | Migraciones explícitas Room (`v3 → v4 → v5`), verificación de esquemas `4.json` / `5.json`, `Converters` y `BorradorRepository` |
| `FormularioValidatorTest` | Consentimiento + firma digital, borrador mínimo, Modo Express, RUT, contacto, establecimiento, carrera y perfilamiento |
| `RutUtilsTest` | Limpieza, validación Módulo 11, formateo y cadenas parciales |
| `StepFormViewModelTest` | Navegación libre entre pasos, borradores (autoguardado/recuperación/descarte), RUT duplicado, Modo Express, autocompletado, firma, ubicación y evento activo |
| `InicioViewModelTest` | `alReanudar()`, selector de evento, Check-in/cierre de turno, detección de red y auto-sincronización |
| `ProspectoLocalDemoTest` | Generación de RUTs válidos en `DemoSeedGenerator` y flujo completo en `LOCAL_DEMO` con cero llamadas remotas |
| `ProspectoPersistenceTest` | Mapeo bidireccional `toDto()` / `toDomain()` (incluidos campos opcionales `v2`), propiedades calculadas, `Converters` Room y fallback en memoria |
| `ProspectoRemoteSyncTest` | `sincronizarPendientes`, aislamiento de `SYNC_ERROR` en `sincronizarNuevosPendientes`, fallos parciales, `IOException` y `refrescarDesdeServidor` |

#### Backend (`backend/*/src/test/`)

| Servicio / Archivo | Qué valida |
|---|---|
| `ms-formulario` (`13` archivos, `51` pruebas) | `RutValidatorTest`, `ProspectoRequestDtoTest`, `ProspectoMapperTest`, `ProspectoServiceTest`, `ProspectoSyncItemProcessorTest` (`REQUIRES_NEW` + *upsert* por RUT), `EventoServiceTest`, `JwtValidationServiceTest`, `JwtAuthFilterTest`, `ApiExceptionTest`, `GlobalExceptionHandlerTest`, `EventoControllerTest`, `ProspectoControllerTest` (`MockMvc` E2E sobre H2 probando `401`, `403`, `201`, `409` y Batch Sync parcial) y `MsFormularioApplicationTests` |
| `api-gateway` (`ApiGatewayApplicationTests`) | Carga de contexto, beans `gatewayRoutes`, `corsFilter`, `requestLoggingFilter` y matching de todas las rutas hacia `ms-login`, `ms-formulario` y OpenAPI/Swagger |
| `ms-login` (`15` archivos) | `UsuarioServiceTest`, `UsuarioServiceCrudTest`, `UsuarioRepositoryTest`, `AuthControllerTest`, `UsuarioControllerTest`, `JwtServiceTest`, `JwtFilterTest`, `SecurityConfigTest`, `AdminInicialTest`, DTOs (`LoginRequestTest`, `UsuarioRequestTest`, `UsuarioResponseTest`), modelos (`UsuarioTest`, `RolTest`) y `MsLoginApplicationTests` (con H2 en pruebas) |

### Cobertura de la lógica (JaCoCo)

- **Backend `ms-formulario`** (medido con `mvnw clean verify` sobre `jacoco.csv`): **99.33%** instrucciones (`2082/2096`), **98.98%** líneas (`388/392`) y **94.12%** ramas (`144/153`), superando el umbral mínimo exigido del 80% en `pom.xml`.
- **App móvil (módulos de lógica en línea base `v1.0`):**

| Clase | Cobertura de instrucciones |
|---|---|
| `UsuariosViewModel` | 95% |
| `DashboardViewModel` | 91% |
| `LoginViewModel` | 89% |
| `RecoverViewModel` | 85% |
| `AuthValidator` y validadores (`util`) | 95% |

Las pantallas de Jetpack Compose (`…Screen.kt`) no se cubren con pruebas unitarias porque corresponden a la capa visual declarativa y no a la lógica de negocio; por eso el porcentaje global del reporte incluye esas vistas mientras los ViewModels/validadores/repositorios mantienen cobertura alta.

## APK firmado

El APK de producción se compila en variante **release** y se firma digitalmente mediante un keystore `.jks`.

1. En Android Studio: **Build → Generate Signed App Bundle / APK → APK**.
2. Seleccionar o crear el archivo `.jks` e ingresar sus credenciales (`storeFile`, `storePassword`, `keyAlias`, `keyPassword`) en `frontend/keystore.properties` (o variables de entorno `RELEASE_STORE_*`).
3. [build.gradle.kts](frontend/app/build.gradle.kts) carga `keystore.properties` en `signingConfigs.create("release")` y compila el APK apuntando a `GATEWAY_BASE_URL` de EC2.

Ubicación de artefactos y seguridad en el repositorio:

| Archivo | Ruta / Estado |
|---|---|
| APK firmado (release) | [frontend/app/build/outputs/apk/release/app-release.apk](frontend/app/build/outputs/apk/release/app-release.apk) |
| Configuración de firma y llave `.jks` | `frontend/keystore.properties` y `*.jks` (excluidos de Git en `.gitignore` por seguridad; se mantienen locales en la máquina de compilación) |

## Flujo de trabajo con Git

| Rama / Tag | Uso |
|---|---|
| `v2` | Rama principal de evolución post-universidad bajo estrategia **App-First** (Fases 0–5). |
| `v1.0-universidad` *(tag)* | Hito congelado de la entrega académica final (`88ecae7`). |
| `main` | Versión estable de entrega universitaria (`88ecae7`), mantenida intacta como referencia académica. |
| `develop` | Rama histórica de integración universitaria (`88ecae7`). |
| `gonzalo` | Desarrollo histórico de `ms-login`, autenticación JWT, gestión de usuarios, dashboard base e indicadores externos. |
| `feature/stepform` | Refactorización histórica al formulario modal Stepper de 6 pasos y validadores. |
| `feature/msformulario-gateway` | Implementación histórica de `api-gateway`, `ms-formulario`, seguridad JWT stateless, Offline-First en Android, Docker Compose y cobertura JaCoCo. |
