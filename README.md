
# Captura Digital

Aplicación móvil Android para la **captación digital de prospectos de admisión de Duoc UC**.

## Descripción del proyecto

En ferias, colegios y eventos, los captadores de Duoc UC registran a los interesados en un **cupón de admisión de papel**. Ese proceso es lento, propenso a errores (RUT mal escrito, datos ilegibles) y obliga a digitar todo nuevamente después.

**Captura Digital** reemplaza el cupón físico por un formulario en el teléfono que:

- Valida los datos en el momento (por ejemplo, el RUT con dígito verificador).
- Registra el consentimiento del prospecto para el uso de sus datos (Ley N° 19.628).
- Entrega un dashboard con el perfil de los prospectos captados.

### Perfiles de usuario

| Perfil | Descripción |
|---|---|
| **Captador de terreno** | Perfil principal. Registra prospectos y ve sus propios resultados. Pueden existir muchos captadores simultáneos. |
| **Prospecto interesado** | Usuario final: la persona cuyos datos se registran en el cupón. |
| **Administrativo** | Perfil secundario. Crea las cuentas de los captadores y ve el total de las capturas. |

## Integrantes

| Integrante                | Responsabilidades |
|---------------------------|---|
| **Gonzalo Salas Guevara** | Login, recuperar contraseña, Cómo funciona y dashboard de atributos (género, edades y carreras de interés). |
| **José Amador**           | Página de inicio con menú y formulario de captura (online y offline). |

## Funcionalidades

| Funcionalidad | Responsable | Estado                        |
|---|---|-------------------------------|
| Inicio de sesión (sin registro: las cuentas las crea el administrador) | Gonzalo | Implementado                  |
| Recuperar contraseña (validación de correo registrado) | Gonzalo | Implementado (envío simulado) |
| Cómo funciona (guía paso a paso del formulario) | Gonzalo | Implementado                  |
| Dashboard de atributos: género, rango de edad y carreras de interés | Gonzalo | Implementado                  |
| Página de inicio | José | Implementado                  |
| Formulario de captura (cupón de admisión) con validación de RUT | José | Implementado                  |
| Formulario sin conexión (offline) | José | Implementado                  |

## Arquitectura

El proyecto sigue el patrón **MVVM** (Model - View - ViewModel):

- **Model** (`data/`): modelos de datos y repositorios que administran la información.
- **ViewModel** (`ui/*/…ViewModel.kt`): procesa los eventos de la pantalla, aplica validaciones y expone el estado con `StateFlow`.
- **View** (`ui/*/…Screen.kt`): pantallas en Jetpack Compose que solo muestran el estado y comunican las acciones del usuario.

```
Evento del usuario → ViewModel → Repository → UiState → Pantalla (Compose)
```

### Estructura de carpetas

```
app/src/main/java/com/therionlabs/captura_digital_v2/
├── data/
│   ├── model/          # User, UserRole, Prospecto, enums del cupón, catálogo Duoc
│   └── repository/     # AuthRepository, ProspectoRepository
├── ui/
│   ├── auth/           # Login, recuperar contraseña y navegación
│   ├── components/     # Componentes reutilizables (botón, tarjeta, encabezado, campo de texto)
│   ├── dashboard/      # Dashboard de atributos
│   ├── formulario/     # Formulario de captura
│   ├── help/           # Cómo funciona
│   ├── inicio/         # Página de inicio
│   └── theme/          # Colores (paleta Duoc UC), tipografía y tema
├── util/               # Utilidades (validador de RUT)
└── MainActivity.kt
```

## Tecnologías

- Kotlin
- Jetpack Compose con Material 3
- Navigation Compose
- ViewModel y StateFlow (gestión de estado)
- Git y GitHub (control de versiones)

## Requisitos

- Android Studio (versión reciente)
- SDK mínimo: Android 7.0 (API 24)
- Emulador o dispositivo Android

## Pasos para ejecutar

1. Clonar el repositorio:
   ```
   git clone https://github.com/joseamadorcruz96/Captura_Digital.git
   ```
2. Abrir la carpeta del proyecto en Android Studio.
3. Esperar que termine la sincronización de Gradle.
4. Seleccionar un emulador o dispositivo y presionar **Run**.

## Usuarios de prueba

| Correo | Contraseña | Rol |
|---|---|---|
| admin@captura.cl | 123456 | Administrativo |
| captador@captura.cl | 123456 | Captador |

> Las credenciales son simuladas y solo sirven para probar el flujo de la aplicación.

## Flujo de trabajo con Git

| Rama | Uso |
|---|---|
| `main` | Versión estable, solo recibe cambios probados desde `develop`. |
| `develop` | Integración del trabajo de ambos integrantes. |
| `gonzalo`, `feature/*` | Desarrollo individual de cada funcionalidad. |