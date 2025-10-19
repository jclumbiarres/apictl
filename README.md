# 🧰 apictl.clj — Pequeño cliente de línea de comandos para APIs REST

`apictl.clj` es un script escrito en [Clojure](https://clojure.org/) usando [Babashka](https://babashka.org/) que permite realizar peticiones `GET` y `POST` a APIs REST directamente desde la terminal, procesando la salida JSON con [jq](https://stedolan.github.io/jq/).

Ideal para automatizar consultas a APIs, probar endpoints y transformar las respuestas en tiempo real con filtros `jq`.

---

## 🚀 Requisitos

Antes de usar el script, asegúrate de tener instalados:

- [Babashka](https://github.com/babashka/babashka)
- [jq](https://stedolan.github.io/jq/)
- (Opcional) [curl](https://curl.se/) — aunque Babashka ya incluye soporte interno mediante `babashka.curl`

---

## ⚙️ Instalación

1. Copia el archivo `apictl.clj` en tu sistema, por ejemplo:

   ```bash
   mkdir -p ~/.local/bin
   cp apictl.clj ~/.local/bin/
   chmod +x ~/.local/bin/apictl.clj
   ```

2. Asegúrate de que `~/.local/bin` esté en tu `PATH`:

   ```bash
   export PATH="$HOME/.local/bin:$PATH"
   ```

3. Verifica que el script se ejecute:

   ```bash
   apictl.clj
   ```

   Esto mostrará el mensaje de uso si todo está funcionando correctamente.

---

## 🔐 Autenticación

Si necesitas usar autenticación con token (por ejemplo, JWT o Bearer Token), define la variable de entorno `APICTL_TOKEN` antes de ejecutar el script:

```bash
export APICTL_TOKEN="tu_token_aqui"
```

El script agregará automáticamente el header:

```
Authorization: Bearer <tu_token>
```

---

## 🧾 Formato de archivo de entrada

Cada petición se define en un archivo de texto `.url` con uno de los siguientes formatos:

### Ejemplo de GET

Archivo: `get.url`

```text
https://jsonplaceholder.typicode.com/todos/1
```

### Ejemplo de POST

Archivo: `post.url`

```text
POST https://jsonplaceholder.typicode.com/posts
{
    "title": "foo",
    "body": "bar",
    "userId": 1
}
```

---

## ▶️ Uso

### Petición GET

```bash
apictl.clj get.url
```

**Salida esperada:**

```json
{
  "userId": 1,
  "id": 1,
  "title": "delectus aut autem",
  "completed": false
}
```

Puedes aplicar un filtro `jq` directamente desde la línea de comandos:

```bash
apictl.clj get.url '.title'
```

**Salida:**

```
"delectus aut autem"
```

---

### Petición POST

```bash
apictl.clj post.url
```

**Salida esperada (ejemplo de jsonplaceholder):**

```json
{
  "title": "foo",
  "body": "bar",
  "userId": 1,
  "id": 101
}
```

También puedes filtrar con `jq`:

```bash
apictl.clj post.url '.id'
```

**Salida:**

```
101
```

---

## 🧩 Descripción técnica

El script realiza las siguientes tareas:

1. **Lee** un archivo `.url` con la definición de la petición.
2. **Detecta** automáticamente si se trata de un `GET` o `POST`.
3. **Ejecuta** la llamada HTTP usando `babashka.curl`.
4. **Valida** que la respuesta (y el cuerpo de POST) sean JSON válidos.
5. **Pasa** el resultado a `jq` para su filtrado o formato.
6. **Muestra** el resultado procesado en stdout.

---

## 🧠 Ejemplos avanzados

### Guardar salida en archivo

```bash
apictl.clj get.url '.' > salida.json
```

### Filtrar y mostrar múltiples campos

```bash
apictl.clj get.url '{id, title}'
```

### Usar en un pipeline

```bash
apictl.clj get.url '.title' | tr '[:lower:]' '[:upper:]'
```

---

## 🧩 Dependencias internas

El script usa las siguientes librerías de Babashka:

- `babashka.curl` — para hacer las peticiones HTTP
- `cheshire.core` — para parsear JSON
- `clojure.java.shell` — para ejecutar `jq`
- `clojure.string` — para manejo de texto

---

## 🧑‍💻 Autor

**Juan Carlos Lumbiarres**  
💻 [https://github.com/jclumbiarres](https://github.com/jclumbiarres)  

---

## 🪪 Licencia

Este proyecto se distribuye bajo la licencia MIT.  
Puedes usarlo, modificarlo y redistribuirlo libremente, siempre que mantengas la atribución al autor original.

---

## 🧩 Archivos incluidos

```
├── apictl.clj      # Script principal
├── get.url         # Ejemplo de petición GET
└── post.url        # Ejemplo de petición POST
```

