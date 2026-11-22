# Backend — Captura Digital (`api-gateway`, `ms-login`, `ms-formulario`)

## Arquitectura y Puertos
- **`api-gateway`** (`8080`, único puerto expuesto): Enruta `/api/auth/**` y `/api/usuarios/**` → `ms-login`, y `/api/v1/prospectos/**` y `/api/v1/eventos/**` → `ms-formulario`.
- **`ms-login`** (`8081` interno): Autenticación JWT y gestión de usuarios (`captura_login`).
- **`ms-formulario`** (`8082` interno): Prospectos, eventos, Batch Sync Offline-First y métricas (`captura_formulario`).

---

## Levantar todo el Backend con Docker Compose (Local o AWS EC2)

1. Copia el archivo de variables de entorno en `backend/`:
   ```bash
   cp .env.example .env
   # Edita .env con tus claves reales (ambos MS comparten JWT_SECRET)
   ```
2. Construye y levanta los 5 contenedores (`db-login`, `db-formulario`, `ms-login`, `ms-formulario`, `api-gateway`):
   ```bash
   docker compose up -d --build
   ```
3. Verifica que todos estén saludables (`healthy`):
   ```bash
   docker compose ps
   curl http://localhost:8080/actuator/health
   ```
4. Swagger UI de `ms-formulario` vía Gateway (si abres túnel o local):
   - Directo en `ms-formulario` en modo dev H2: `http://localhost:8082/swagger-ui.html`
   - Esquema OpenAPI vía Gateway: `http://localhost:8080/openapi/formulario`

---

## Notas para AWS EC2
- Abre en el **Security Group** de tu EC2 únicamente **SSH (22)** desde tu IP y **TCP 8080** para el Gateway (mantén cerrados `8081`, `8082` y `3306`).
- Si usas túnel SSH en lugar de abrir `8080`:
  ```bash
  ssh -i clave.pem -N -L 8080:127.0.0.1:8080 ubuntu@IP_PUBLICA
  # Y si pruebas en celular físico conectado por USB:
  adb reverse tcp:8080 tcp:8080
  ```
- Para la app Android (`frontend/`):
  - Por defecto apunta a `http://100.61.210.112:8080/` (EC2 Gateway).
  - Para desarrollo en emulador local, define `GATEWAY_BASE_URL=http://10.0.2.2:8080/` en `frontend/local.properties` (ignorado por Git).
