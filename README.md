# Captura Digital

Aplicación móvil Android para la **captación digital de prospectos de admisión de Duoc UC**, conectada a una arquitectura distribuida de **microservicios propios en Spring Boot** detrás de un **API Gateway** y con soporte **Offline-First** mediante Room SQLite.

## Descripción del proyecto

En ferias, colegios y eventos, los captadores de Duoc UC registran a los interesados en un **cupón de admisión de papel**. Ese proceso es lento, propenso a errores (RUT mal escrito, datos ilegibles) y obliga a digitar todo nuevamente después.

**Captura Digital** reemplaza el cupón físico por un formulario móvil por pasos que:

- Valida los datos en vivo (RUT con dígito verificador Módulo 11, contacto y campos obligatorios).
- Registra el consentimiento informado del prospecto para el uso de sus datos (Ley N° 19.628 / 21.719).
- Funciona sin conexión (*Offline-First*), persistiendo los cupones localmente en Room y sincronizando automáticamente por lote al recuperar la conectividad.
- Protege las operaciones entre microservicios con autenticación **Bearer JWT** por rol.
- Entrega un dashboard con el perfil de los prospectos captados, gestión CRUD en tiempo real e indicadores económicos del día.

### Perfiles de usuario

| Perfil | Descripción |
|---|---|
| **Captador de terreno** | Perfil principal. Registra prospectos en terreno, sincroniza lotes y consulta sus propios resultados. |
| **Prospecto interesado** | Usuario final: la persona cuyos datos se registran en el cupón de admisión. |
| **Administrativo** | Perfil administrador. Gestiona cuentas de captadores, administra eventos y visualiza el total global de capturas. |

## Integrantes

| Integrante | Responsabilidades |
|---|---|
| **Gonzalo Salas Guevara** | Login, recuperar contraseña, gestión de usuarios, Cómo funciona, dashboard base, microservicio `ms-login`, emisión JWT, consumo de API externa (`mindicador.cl`) y pruebas asociadas. |
| **José Amador** | Página de inicio, formulario modal Stepper de 6 pasos (`StepForm`), validación de RUT Módulo 11, persistencia local con Room, monitoreo global de red y auto-sincronización Offline-First, microservicio `ms-formulario`, `api-gateway`, validación JWT en `ms-formulario`, CRUD remoto en Dashboard, despliegue Docker Compose, cobertura JaCoCo (`>= 80%`) y firma de APK release. |

## Funcionalidades

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
| Página de inicio (`InicioScreen`) con evento activo, métricas diarias, estado de red y botón de sincronización | José | Implementado |
| Formulario modal Stepper de 6 pasos (`StepFormScreen`) con navegación libre, alertas por etapa y validación RUT Módulo 11 | José | Implementado |
| Persistencia local con Room SQLite (`AppDatabase`, `ProspectoDao`) y estados `PENDING_SYNC` / `SYNCED` / `SYNC_ERROR` | José | Implementado |
| Monitor global de conectividad (`NetworkMonitor`, `ConnectivityBadge`, `OfflineStatusBanner`) y auto-sincronización al recuperar red | José | Implementado |
| API Gateway (`backend/api-gateway`) con enrutamiento WebMVC, CORS, logging HTTP global y Swagger agregado | José | Implementado |
| Microservicio `ms-formulario` (`backend/ms-formulario`): CRUD Prospectos/Eventos, Batch Sync aislado (`REQUIRES_NEW` + *upsert* por RUT), métricas y seguridad Bearer JWT | José | Implementado |
| CRUD remoto de prospectos en tiempo real desde Dashboard (`rotarNivelInteres` y `eliminarProspecto`) | José | Implementado |
| Despliegue contenedorizado con Docker (`Dockerfile` multi-stage + `compose.yaml`) y URL configurable vía `BuildConfig` | José | Implementado |
| Firma de APK en modo release (`app-release.apk`) y suite de cobertura JaCoCo (`>= 80%` en `ms-formulario`) | José | Implementado |

## Endpoints utilizados

En ejecución integrada, la aplicación móvil consume todos los servicios propios a través de una única puerta de entrada (**`api-gateway`** en el puerto `8080`) enviando el header `Authorization: Bearer <JWT>`.

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
| `PUT` | `/api/v1/prospectos/{clientUuid}` | `CAPTADOR` / `ADMINISTRATIVO` | Actualiza un prospecto completo | Botón rotar nivel de interés en `Dashboard` |
| `DELETE` | `/api/v1/prospectos/{clientUuid}` | `CAPTADOR` / `ADMINISTRATIVO` | Elimina un prospecto | Botón eliminar en `Dashboard` |
| `POST` | `/api/v1/prospectos/sync` | `CAPTADOR` / `ADMINISTRATIVO` | Sincronización por lote Offline-First: transacción aislada por ítem (`REQUIRES_NEW`), *upsert* idempotente por RUT y respuesta parcial (`exitososUuids`, `fallidosUuids`, `erroresPorUuid`) | Guardado en `StepForm` y sincronización en `Inicio` |
| `GET` | `/api/v1/prospectos/resumen` | `CAPTADOR` / `ADMINISTRATIVO` | Métricas agregadas JPQL (`totalProspectos`, `conteoPorSede`, `conteoPorNivelInteres`, `conteoPorJornada`) | Analítica / Swagger |
| `GET` | `/api/v1/eventos` | `CAPTADOR` / `ADMINISTRATIVO` | Lista eventos (filtro opcional `?activo=true`) | Evento activo en `Inicio` |
| `POST` / `PUT` / `DELETE` | `/api/v1/eventos` | `ADMINISTRATIVO` | Crea, edita o elimina eventos/ferias | Administración vía Swagger/API |

Ejemplo de cuerpo para sincronización por lote (`POST /api/v1/prospectos/sync`):

```json
{
  "captadorId": 2,
  "eventoId": "EVT-DUOC-2026",
  "prospectos": [
    {
      "clientUuid": "demo-uuid-1",
      "captadorId": 2,
      "eventoId": "EVT-DUOC-2026",
      "timestampEpoch": 1763850000000,
      "rut": "20.123.456-5",
      "nombres": "Camila Andrea",
      "apellidos": "Valenzuela Soto",
      "genero": "FEMENINO",
      "fechaNacimiento": "15/04/2008",
      "comunaResidencia": "Viña del Mar",
      "celular": "+56987654321",
      "email": "camila.valenzuela@gmail.com",
      "consentimientoDatos": true,
      "situacionAcademica": "CUARTO_MEDIO",
      "situacionAcademicaOtro": null,
      "establecimientoEducacional": "Colegio Rubén Castro",
      "comunaEstablecimiento": "Viña del Mar",
      "jornadaPreferencia": "DIURNA",
      "sedeInteres": "Viña del Mar",
      "nivelInteres": "ALTO",
      "perfilMotivacional": "VOCACIONAL",
      "carreras": [
        { "prioridad": 1, "nombreCarrera": "Ingeniería en Informática" },
        { "prioridad": 2, "nombreCarrera": "Analista Programador" }
      ]
    }
  ]
}
```

### API externa: `mindicador.cl`

| Método | Endpoint | Descripción | Uso en la app |
|---|---|---|---|
| `GET` | `https://mindicador.cl/api` | Indicadores económicos de Chile del día | Tarjeta "Indicadores económicos de hoy" en el dashboard (UF, dólar y UTM) |

Se consume directamente desde `RetrofitClient.indicadoresApi` (sin pasar por el API Gateway ni enviar JWT). Si la API externa no responde, el dashboard sigue funcionando sin mostrar esa tarjeta.

## Arquitectura

### Aplicación móvil (MVVM + Offline-First)

- **View** (`ui/*/…Screen.kt`): pantallas en Jetpack Compose que observan `StateFlow` y emiten eventos sin lógica de negocio.
- **ViewModel** (`ui/*/…ViewModel.kt`): procesa eventos, aplica validaciones y expone el estado inmutable (`*UiState`).
- **Model / Data** (`data/`): repositorios (`ProspectoRepository`, `AuthRepository`, `UsuarioRepository`) que coordinan persistencia local (**Room SQLite**) y remota (**Retrofit** + interceptor JWT en `RetrofitClient` + `NetworkMonitor`).

```text
Pantalla (Compose) → ViewModel → Repository ─┬─► Room SQLite (PENDING_SYNC / SYNCED / SYNC_ERROR)
                                             ├─► RetrofitClient (JWT) → api-gateway (:8080) ─┬─► ms-login (:8081) → MySQL (captura_login)
                                             │                                               └─► ms-formulario (:8082) → H2/MySQL (captura_formulario)
                                             └─► RetrofitClient (directo) → https://mindicador.cl/api
```

### Backend (Microservicios + API Gateway)

- Patrón **Database-per-Service**: `ms-login` gestiona la base `captura_login` y emite tokens HMAC-SHA (`jjwt 0.12.6`); `ms-formulario` gestiona la base `captura_formulario` y valida el mismo JWT de forma stateless (`JwtAuthFilter` + `JwtValidationService`) sin acoplar bases de datos ni llamadas síncronas entre servicios.
- `api-gateway` actúa como puerta de entrada única (`:8080`), sin base de datos propia, centralizando enrutamiento WebMVC, CORS y logging HTTP.

### Estructura del repositorio

```text
Captura_Digital_v2/
├── frontend/                                   # App Android (Kotlin + Jetpack Compose)
│   └── app/
│       ├── build.gradle.kts                    # BuildConfig GATEWAY_BASE_URL + signingConfigs release
│       └── src/
│           ├── main/java/com/therionlabs/captura_digital_v2/
│           │   ├── CapturaDigitalApp.kt        # Inicialización Room y NetworkMonitor
│           │   ├── MainActivity.kt             # Permiso ACCESS_LOCAL_NETWORK (SDK 37) y NavHost
│           │   ├── data/
│           │   │   ├── local/                  # AppDatabase (v3), Converters, dao/ProspectoDao
│           │   │   ├── model/                  # Prospecto, Enums, CatalogoDuoc, User, dto/ProspectoDto
│           │   │   ├── remote/                 # RetrofitClient, AuthApi, ProspectoApi, IndicadoresApi, NetworkMonitor
│           │   │   └── repository/             # AuthRepository, UsuarioRepository, ProspectoRepository
│           │   ├── ui/
│           │   │   ├── auth/                   # Login, Recover y AuthNavigation
│           │   │   ├── components/             # ConnectivityBadge, OfflineStatusBanner, Duoc*
│           │   │   ├── dashboard/              # Dashboard, Indicadores externos y CRUD prospectos
│           │   │   ├── formularioModal/        # StepForm (6 pasos), components/ y steps/
│           │   │   ├── help/                   # Cómo funciona
│           │   │   ├── inicio/                 # Inicio, métricas diarias y sync
│           │   │   ├── theme/                  # Color, Theme, Type (Duoc UC)
│           │   │   └── usuarios/               # Gestión de usuarios (administrador)
│           │   └── util/                       # AuthValidator, FormularioValidator, RutUtils
│           └── test/java/com/therionlabs/captura_digital_v2/   # 16 archivos de prueba unitaria + Fakes
└── backend/
    ├── Dockerfile                              # Build multi-stage parametrizado (ARG SERVICE_DIR)
    ├── compose.yaml                            # Stack completo: 2 MySQL + ms-login + ms-formulario + api-gateway
    ├── .env.example                            # Plantilla de variables de entorno (MYSQL_*, JWT_SECRET, etc.)
    ├── README.md                               # Guía operativa de Docker Compose y AWS EC2
    ├── api-gateway/                            # Puerta de enlace (Spring Cloud Gateway WebMVC, puerto 8080)
    │   └── src/main/java/com/therionlabs/apigateway/
    │       ├── ApiGatewayApplication.java
    │       └── config/                         # GatewayRoutesConfig (rutas + CORS), RequestLoggingFilter
    ├── ms-login/                               # Autenticación JWT y usuarios (puerto 8081)
    │   └── src/main/java/com/therionlabs/mslogin/
    │       ├── config/                         # AdminInicial
    │       ├── controller/                     # AuthController, UsuarioController
    │       ├── dto/                            # LoginRequest, LoginResponse, UsuarioRequest, UsuarioResponse
    │       ├── model/                          # Usuario, Rol
    │       ├── repository/                     # UsuarioRepository
    │       ├── security/                       # JwtService, JwtFilter, SecurityConfig
    │       └── service/                        # UsuarioService
    └── ms-formulario/                          # Prospectos, eventos y Batch Sync (puerto 8082)
        └── src/main/java/com/therionlabs/msformulario/
            ├── config/                         # SecurityConfig, JwtAuthFilter, JwtValidationService, OpenApiConfig
            ├── controller/                     # ProspectoController, EventoController
            ├── dto/                            # Prospecto*, BatchSync*, Evento*, CarreraPreferenciaDto, DashboardResumenDto
            ├── exception/                      # ApiException, ApiErrorResponse, GlobalExceptionHandler
            ├── mapper/                         # ProspectoMapper
            ├── model/                          # Prospecto, Evento, CarreraPreferencia, enums/
            ├── repository/                     # ProspectoRepository, EventoRepository
            ├── service/                        # ProspectoService, ProspectoSyncItemProcessor (REQUIRES_NEW), EventoService
            └── util/                           # RutValidator (Módulo 11)
```

## Tecnologías

| Parte | Tecnologías |
|---|---|
| **App móvil** | Kotlin `2.2.10`, Jetpack Compose (BOM `2026.02.01`), Material 3, Navigation Compose `2.10.1`, ViewModel/StateFlow `2.11.0`, Room `2.7.0` + KSP, Retrofit `3.0.0` + Converter-Gson `3.0.0` |
| **Backend** | Java `21`, Spring Boot `4.2.0`, Spring Cloud Gateway WebMVC (`2026.0.0-SNAPSHOT`), Spring Data JPA, Bean Validation, Spring Security + `jjwt 0.12.6`, BCrypt, Lombok, SpringDoc OpenAPI `3.1.1`, MySQL `8.4`, H2 |
| **Pruebas** | JUnit 4 + `kotlinx-coroutines-test 1.10.2` (app), JUnit 5 + Mockito + MockMvc (backend), JaCoCo `0.8.13` (cobertura) |
| **Infraestructura y Colaboración** | Docker, Docker Compose, AWS EC2, Git, GitHub y Trello |

## Requisitos

- Android Studio (versión reciente) con SDK Android (API 24+ en dispositivo/emulador)
- Java 21 o superior (JDK 21+)
- Docker Desktop (para levantar todo el backend en 1 comando) **o** MySQL 8+ si ejecutas cada microservicio manualmente

## Pasos para ejecutar

### 1. Clonar el repositorio

```bash
git clone https://github.com/joseamadorcruz96/Captura_Digital.git
cd Captura_Digital
```

### 2. Levantar el Backend

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

### 3. Crear el usuario administrador inicial (si la BD parte vacía)

`ms-login` crea las cuentas desde el rol administrador (`AdminInicial` o vía API):

```bash
curl -X POST http://localhost:8080/api/usuarios \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Administrador","email":"admin@captura.cl","password":"123456","rol":"ADMINISTRATIVO"}'
```

Los demás usuarios (`CAPTADOR` / `ADMINISTRATIVO`) se crean desde la app: **Dashboard → Gestionar usuarios**.

### 4. Aplicación móvil (`frontend/`)

1. Abre la carpeta **`frontend`** en Android Studio y espera la sincronización de Gradle.
2. Configura la URL del Gateway según dónde ejecutes el backend:
   - **AWS EC2 (por defecto):** `BuildConfig.GATEWAY_BASE_URL` apunta a `http://100.61.210.112:8080/` definido en [build.gradle.kts](frontend/app/build.gradle.kts).
   - **Emulador local:** agrega `GATEWAY_BASE_URL=http://10.0.2.2:8080/` en `frontend/local.properties` (ignorado por Git).
   - **Teléfono físico por USB contra backend local/túnel SSH:** ejecuta `adb reverse tcp:8080 tcp:8080` y configura `http://127.0.0.1:8080/`.
3. Selecciona el emulador o dispositivo y presiona **Run**.

## Usuarios de prueba

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

#### App móvil (`frontend/app/src/test/`)

Las pruebas de la app utilizan implementaciones simuladas (**Fakes**: `FakeAuthApi`, `FakeProspectoApi`, `FakeIndicadoresApi`, `FakeProspectoRepository`) y `MainDispatcherRule` (`kotlinx-coroutines-test`), por lo que no requieren backend ni red encendidos:

| Archivo de prueba | Qué valida |
|---|---|
| `AuthValidatorTest` | Reglas de correo, contraseña y nombre |
| `LoginViewModelTest` | Errores por campo, login correcto, credenciales incorrectas y sin conexión (`IOException`) |
| `RecoverViewModelTest` | Correo vacío, inválido, registrado, no registrado y sin conexión |
| `UsuariosViewModelTest` | Carga, crear, editar, cancelar, eliminar y errores del servidor |
| `DashboardViewModelTest` | Filtro por rol, cálculo de género/edades/carreras, API externa, `rotarNivelInteres`, `eliminarProspecto` y texto para compartir |
| `AuthRepositoryTest` | Login, almacenamiento de token JWT, sesión, cierre de sesión y recuperación |
| `UsuarioRepositoryTest` | CRUD de usuarios y manejo de errores (correo repetido, usuario inexistente) |
| `FormularioValidatorTest` | Consentimiento, nombres/apellidos, RUT, contacto (celular o email), establecimiento, carrera y perfilamiento |
| `RutUtilsTest` | Limpieza, validación Módulo 11, formateo y cadenas parciales |
| `StepFormViewModelTest` | Navegación libre entre los 6 pasos, alertas por etapa, bloqueo de guardado inválido y guardado + sync `SYNCED` |
| `InicioViewModelTest` | `alReanudar()`, limpieza de mensajes obsoletos, detección de corte/recuperación de red y auto-sincronización |
| `ProspectoPersistenceTest` | Mapeo bidireccional `toDto()` / `toDomain()`, propiedades calculadas, `Converters` Room y fallback en memoria |
| `ProspectoRemoteSyncTest` | `sincronizarPendientes`, aislamiento de `SYNC_ERROR` en `sincronizarNuevosPendientes`, fallos parciales, `IOException` y `refrescarDesdeServidor` |

#### Backend (`backend/*/src/test/`)

| Servicio / Archivo | Qué valida |
|---|---|
| `ms-formulario` (`13` archivos, `51` pruebas) | `RutValidatorTest`, `ProspectoRequestDtoTest`, `ProspectoMapperTest`, `ProspectoServiceTest`, `ProspectoSyncItemProcessorTest` (`REQUIRES_NEW` + *upsert* por RUT), `EventoServiceTest`, `JwtValidationServiceTest`, `JwtAuthFilterTest`, `ApiExceptionTest`, `GlobalExceptionHandlerTest`, `EventoControllerTest`, `ProspectoControllerTest` (`MockMvc` E2E sobre H2 probando `401`, `403`, `201`, `409` y Batch Sync parcial) y `MsFormularioApplicationTests` |
| `api-gateway` (`ApiGatewayApplicationTests`) | Carga de contexto, beans `gatewayRoutes`, `corsFilter`, `requestLoggingFilter` y matching de todas las rutas hacia `ms-login`, `ms-formulario` y OpenAPI/Swagger |
| `ms-login` (`15` archivos) | `UsuarioServiceTest`, `UsuarioServiceCrudTest`, `UsuarioRepositoryTest`, `AuthControllerTest`, `UsuarioControllerTest`, `JwtServiceTest`, `JwtFilterTest`, `SecurityConfigTest`, `AdminInicialTest`, DTOs (`LoginRequestTest`, `UsuarioRequestTest`, `UsuarioResponseTest`), modelos (`UsuarioTest`, `RolTest`) y `MsLoginApplicationTests` (con H2 en pruebas) |

### Cobertura de la lógica (JaCoCo)

- **Backend `ms-formulario`** (medido con `mvnw clean verify` sobre `jacoco.csv`): **99.33%** instrucciones (`2082/2096`), **98.98%** líneas (`388/392`) y **94.12%** ramas (`144/153`), superando el umbral mínimo exigido del 80% en `pom.xml`.
- **App móvil (módulos de lógica):**

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

| Rama | Uso |
|---|---|
| `main` | Versión estable de entrega, recibe cambios integrados y probados desde `develop`. |
| `develop` | Rama principal de integración del trabajo de ambos integrantes. |
| `gonzalo` | Desarrollo de `ms-login`, autenticación JWT, gestión de usuarios, dashboard base e indicadores externos. |
| `feature/stepform` | Refactorización de captura al formulario modal Stepper de 6 pasos y validadores. |
| `feature/msformulario-gateway` | Implementación de `api-gateway`, `ms-formulario`, seguridad JWT stateless, Offline-First en Android, Docker Compose y cobertura JaCoCo. |
