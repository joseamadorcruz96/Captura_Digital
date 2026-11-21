# Captura Digital

Aplicación móvil Android para la **captación digital de prospectos de admisión de Duoc UC**, conectada a microservicios propios en Spring Boot.

## Descripción del proyecto

En ferias, colegios y eventos, los captadores de Duoc UC registran a los interesados en un **cupón de admisión de papel**. Ese proceso es lento, propenso a errores (RUT mal escrito, datos ilegibles) y obliga a digitar todo nuevamente después.

**Captura Digital** reemplaza el cupón físico por un formulario en el teléfono que:

- Valida los datos en el momento (por ejemplo, el RUT con dígito verificador).
- Registra el consentimiento del prospecto para el uso de sus datos (Ley N° 19.628).
- Funciona sin conexión, guardando los cupones en el teléfono.
- Entrega un dashboard con el perfil de los prospectos captados.

### Perfiles de usuario

| Perfil | Descripción |
|---|---|
| **Captador de terreno** | Perfil principal. Registra prospectos y ve sus propios resultados. Pueden existir muchos captadores simultáneos. |
| **Prospecto interesado** | Usuario final: la persona cuyos datos se registran en el cupón. |
| **Administrativo** | Perfil secundario. Gestiona las cuentas de los captadores y ve el total de las capturas. |

## Integrantes

| Integrante | Responsabilidades |
|---|---|
| **Gonzalo Salas Guevara** | Login, recuperar contraseña, gestión de usuarios, Cómo funciona, dashboard, microservicio `ms-login` y consumo de API externa. |
| **José Amador** | Página de inicio, formulario de captura (online y offline), persistencia local con Room y microservicio `ms-formulario`. |

## Funcionalidades

| Funcionalidad | Responsable | Estado |
|---|---|---|
| Inicio de sesión contra el microservicio `ms-login` (sin registro: las cuentas las crea el administrador) | Gonzalo | Implementado |
| Validación por campo con íconos y mensajes de error en el login | Gonzalo | Implementado |
| Recuperar contraseña (valida que el correo exista en `ms-login`) | Gonzalo | Implementado (envío de correo simulado) |
| Gestión de usuarios para el administrador: crear, listar, editar y eliminar (CRUD completo) | Gonzalo | Implementado |
| Cómo funciona (guía paso a paso del cupón de admisión) | Gonzalo | Implementado |
| Dashboard por rol: género, rango de edad y carreras de interés (el captador ve lo suyo, el administrador el total) | Gonzalo | Implementado |
| Indicadores económicos del día (UF, dólar y UTM) desde una API externa | Gonzalo | Implementado |
| Vibración al fallar el inicio de sesión (recurso nativo) | Gonzalo | Implementado |
| Compartir el resumen del dashboard con otras apps (recurso nativo) | Gonzalo | Implementado |
| Animaciones en el dashboard y en Cómo funciona | Gonzalo | Implementado |
| Página de inicio | José | Implementado |
| Formulario de captura por pasos con validación de RUT | José | Implementado |
| Persistencia local con Room y funcionamiento sin conexión | José | Implementado |
| Microservicio `ms-formulario` | José | En desarrollo |

## Endpoints utilizados

### Microservicio propio: `ms-login` (Spring Boot + MySQL, puerto 8081)

| Método | Endpoint | Descripción | Uso en la app |
|---|---|---|---|
| POST | `/api/auth/login` | Valida correo y contraseña y devuelve el usuario | Pantalla de login |
| POST | `/api/auth/recuperar` | Verifica que el correo esté registrado | Recuperar contraseña |
| GET | `/api/usuarios` | Lista todos los usuarios | Gestión de usuarios |
| GET | `/api/usuarios/{id}` | Obtiene un usuario | - |
| POST | `/api/usuarios` | Crea un usuario (la contraseña se guarda cifrada con BCrypt) | Gestión de usuarios |
| PUT | `/api/usuarios/{id}` | Actualiza un usuario | Gestión de usuarios |
| DELETE | `/api/usuarios/{id}` | Elimina un usuario | Gestión de usuarios |

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

### API externa: mindicador.cl

| Método | Endpoint | Descripción | Uso en la app |
|---|---|---|---|
| GET | `https://mindicador.cl/api` | Indicadores económicos de Chile del día | Tarjeta "Indicadores económicos de hoy" en el dashboard (UF, dólar y UTM) |

Ambas se consumen desde la app con **Retrofit**. Si la API externa no responde, el dashboard sigue funcionando sin los indicadores.

## Arquitectura

### Aplicación móvil (MVVM)

- **Model** (`data/`): modelos, repositorios y clientes Retrofit (`data/remote`).
- **ViewModel** (`ui/*/…ViewModel.kt`): procesa los eventos, aplica validaciones y expone el estado con `StateFlow`.
- **View** (`ui/*/…Screen.kt`): pantallas en Jetpack Compose que solo muestran el estado.

```
Pantalla (Compose) → ViewModel → Repository → Retrofit → ms-login / mindicador.cl
```

### Backend (`ms-login`)

```
Controller → Service → Repository (JPA) → MySQL
```

### Estructura del repositorio

```
Captura_Digital/
├── frontend/                         # App Android (Kotlin + Jetpack Compose)
│   └── app/src/main/java/com/therionlabs/captura_digital_v2/
│       ├── data/
│       │   ├── local/                # Base de datos Room
│       │   ├── model/                # User, Prospecto, enums del cupón
│       │   ├── remote/               # Retrofit: AuthApi, IndicadoresApi, RetrofitClient
│       │   └── repository/           # AuthRepository, UsuarioRepository, ProspectoRepository
│       ├── ui/
│       │   ├── auth/                 # Login, recuperar contraseña y navegación
│       │   ├── components/           # Componentes reutilizables
│       │   ├── dashboard/            # Dashboard e indicadores
│       │   ├── formularioModal/      # Formulario de captura por pasos
│       │   ├── help/                 # Cómo funciona
│       │   ├── inicio/               # Página de inicio
│       │   ├── usuarios/             # Gestión de usuarios (administrador)
│       │   └── theme/                # Colores Duoc UC, tipografía y tema
│       └── util/                     # Validadores (AuthValidator, RUT, formulario)
└── backend/
    ├── ms-login/                     # Usuarios y autenticación (Spring Boot + MySQL)
    │   └── src/main/java/com/therionlabs/mslogin/
    │       ├── controller/           # UsuarioController, AuthController
    │       ├── dto/                  # UsuarioRequest, UsuarioResponse, LoginRequest
    │       ├── model/                # Usuario, Rol
    │       ├── repository/           # UsuarioRepository
    │       └── service/              # UsuarioService
    ├── ms-formulario/                # Prospectos (en desarrollo)
    └── api-gateway/                  # Puerta de enlace (en desarrollo)
```

## Tecnologías

| Parte | Tecnologías |
|---|---|
| App móvil | Kotlin, Jetpack Compose, Material 3, Navigation Compose, ViewModel, StateFlow, Retrofit, Room |
| Backend | Java 21, Spring Boot, Spring Data JPA, Bean Validation, BCrypt, MySQL |
| Pruebas | JUnit 4, kotlinx-coroutines-test (app), JUnit 5 y Mockito (backend), JaCoCo (cobertura) |
| Colaboración | Git y GitHub |

## Requisitos

- Android Studio (versión reciente)
- Java 21 o superior
- MySQL 8 o superior
- Emulador o dispositivo Android con Android 7.0 (API 24) o superior

## Pasos para ejecutar

### 1. Clonar el repositorio

```
git clone https://github.com/joseamadorcruz96/Captura_Digital.git
cd Captura_Digital
```

### 2. Base de datos

```
mysql -u root -p -e "CREATE DATABASE captura_login;"
```

Las tablas se crean solas al iniciar el microservicio.

### 3. Microservicio `ms-login`

```
cd backend/ms-login
export DB_PASSWORD=tu_contraseña_de_mysql
./mvnw spring-boot:run
```

Queda disponible en `http://localhost:8081`. La contraseña de la base de datos se entrega por variable de entorno y no se guarda en el repositorio. También se pueden definir `DB_HOST` (por defecto `localhost`) y `DB_USER` (por defecto `root`).

### 4. Crear el usuario administrador

Las cuentas las crea el administrador, por lo que el primero se crea directamente en la API:

```
curl -X POST http://localhost:8081/api/usuarios \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Administrador","email":"admin@captura.cl","password":"123456","rol":"ADMINISTRATIVO"}'
```

Los demás usuarios se crean desde la app: **Dashboard → Gestionar usuarios**.

### 5. Aplicación móvil

1. Abrir la carpeta **`frontend`** en Android Studio y esperar la sincronización de Gradle.
2. Si se usa el emulador con el backend local, conectar el puerto del emulador con el computador:
   ```
   adb reverse tcp:8081 tcp:8081
   ```
3. Seleccionar el emulador o dispositivo y presionar **Run**.

La dirección del backend se configura en `data/remote/RetrofitClient.kt` (`BASE_URL`).

## Usuarios de prueba

| Correo | Contraseña | Rol |
|---|---|---|
| admin@captura.cl | 123456 | Administrativo |

Con este usuario se pueden crear captadores desde **Gestionar usuarios**.

## Pruebas unitarias

### Ejecutar

App móvil (desde `frontend`):

```
./gradlew testDebugUnitTest
./gradlew createDebugUnitTestCoverageReport
```

El reporte de cobertura queda en `frontend/app/build/reports/coverage/test/debug/index.html`.

Backend (desde `backend/ms-login`):

```
./mvnw test -Dtest=UsuarioServiceTest
```

### Qué se prueba

| Archivo de prueba | Qué valida |
|---|---|
| `AuthValidatorTest` | Reglas de correo, contraseña y nombre |
| `LoginViewModelTest` | Errores por campo, login correcto, credenciales incorrectas y sin conexión |
| `RecoverViewModelTest` | Correo vacío, inválido, registrado, no registrado y sin conexión |
| `UsuariosViewModelTest` | Carga, crear, editar, cancelar, eliminar y errores del servidor |
| `DashboardViewModelTest` | Filtro por rol, cálculo de género, edades y carreras, API externa y texto para compartir |
| `AuthRepositoryTest` | Login, sesión, cierre de sesión y recuperación |
| `UsuarioRepositoryTest` | CRUD de usuarios y manejo de errores (correo repetido, usuario inexistente) |
| `UsuarioServiceTest` (backend) | Cifrado de contraseña, correo repetido, login y recuperación |

Las pruebas de la app usan una API simulada (`FakeAuthApi`), por lo que no necesitan el backend encendido.

### Cobertura de la lógica (JaCoCo)

| Clase | Cobertura de instrucciones |
|---|---|
| `UsuariosViewModel` | 95% |
| `DashboardViewModel` | 91% |
| `LoginViewModel` | 89% |
| `RecoverViewModel` | 85% |
| `AuthValidator` y validadores (`util`) | 95% |

Las pantallas de Jetpack Compose (`…Screen.kt`) no se cubren con pruebas unitarias, porque corresponden a la interfaz y no a la lógica; por eso el porcentaje total del reporte es menor que el de las clases de lógica.

## APK firmado

El APK se genera en modo release y se firma con un archivo `.jks`.

1. **Build → Generate Signed App Bundle / APK → APK**.
2. Seleccionar el archivo `.jks` del proyecto e ingresar sus contraseñas.
3. Elegir la variante **release**.

Ubicación del APK y de la llave en el repositorio:

| Archivo | Ruta |
|---|---|
| APK firmado | _pendiente_ |
| Llave `.jks` | _pendiente_ |

Captura del APK firmado y del `.jks`:

_pendiente_

## Flujo de trabajo con Git

| Rama | Uso |
|---|---|
| `main` | Versión estable, solo recibe cambios probados desde `develop`. |
| `develop` | Integración del trabajo de ambos integrantes. |
| `gonzalo`, `feature/*` | Desarrollo individual de cada funcionalidad. |