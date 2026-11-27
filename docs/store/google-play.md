# Ficha de Google Play

Borrador para Google Play Console. Mercado: solo Perú (sección 20). Idioma: español de Latinoamérica (es-419).

## Datos de la app

| Campo | Valor |
|---|---|
| Nombre (máx. 30) | BestID: cartilla de tu mascota |
| ID de la app | `com.nekodojo.bestid` |
| Categoría | Estilo de vida (alternativa: Medicina) |
| Precio | Gratis, sin anuncios ni compras dentro de la app |
| Público objetivo | Mayores de 18 años (dueños de mascotas) |
| Correo de contacto | `[Pendiente]` correo de soporte de Gonzalo |
| Política de privacidad | `[Pendiente]` URL pública con el texto de `docs/legal/politica-de-privacidad.md` |

## Descripción breve (máx. 80)

La cartilla de vacunas de tu perro o gato en el teléfono, con avisos.

## Descripción completa (máx. 4000)

BestID guarda la cartilla de vacunación de tu mascota en tu teléfono y te avisa cuándo le toca la próxima vacuna o desparasitación.

**Todo en un solo lugar**
• Registra las vacunas de tu perro, gato u otra mascota: fecha, tipo, producto, lote, vencimiento y veterinaria.
• Toma una foto de cada página de la cartilla y del sticker del frasco: la foto queda guardada junto a cada vacuna.
• Lleva las desparasitaciones y otros cuidados, y el peso con su gráfico.
• Varias mascotas, cada una con su carnet BestID.

**Avisos que llegan a tiempo**
• Te avisamos 7 días antes, 1 día antes y el mismo día, a la hora que elijas.
• Funcionan sin internet.

**Comparte con tu veterinaria**
• Envía la ficha en PDF por WhatsApp con sus vacunas, lotes y fotos de la cartilla.
• La dirección nunca va en la ficha; el nombre del tutor, solo si lo eliges.

**Tus datos son tuyos**
• Sin cuenta, sin servidor y sin publicidad: todo se guarda en tu teléfono.
• Exporta un respaldo y pásalo a otro teléfono, Android o iPhone.

BestID no reemplaza la opinión de tu veterinario.

## Seguridad de los datos (formulario de Play Console)

| Pregunta | Respuesta | Por qué |
|---|---|---|
| ¿La app recopila o comparte datos del usuario? | **Sí, recopila** (no comparte) | Solo los informes de Crashlytics (D48). Los datos de la mascota y las fotos se guardan en el teléfono; compartir un PDF o un respaldo lo inicia la persona con la hoja del sistema, lo que Google no cuenta como recopilación. |
| Tipos de datos | Información y rendimiento de la app → **Registros de fallos** y **Diagnóstico**; ID de dispositivo u otros → **ID de instalación de Firebase** | Recopilado, no compartido, no efímero, obligatorio, finalidad "Estadísticas de la app". |
| ¿Los datos se cifran en tránsito? | Sí | Crashlytics envía por HTTPS. |
| ¿El usuario puede pedir que se borren? | Sí | Ajustes → "Borrar todos los datos". |

Revisar de nuevo cuando se agregue la lectura de la cartilla con un servicio externo (sección 14): las fotos se enviarían para procesarlas.

## Permisos y su justificación

| Permiso | Para qué |
|---|---|
| Notificaciones (`POST_NOTIFICATIONS`) | Avisos de vacunas y cuidados. Se pide después de guardar la primera vacuna o cuidado (O5). |
| Iniciar al encender (`RECEIVE_BOOT_COMPLETED`) | Volver a programar los avisos después de reiniciar. |
| Internet (`INTERNET`, lo agrega Firebase) | Enviar los informes de errores de Crashlytics (D48). La app funciona sin internet. |
| Cámara | No se declara: se usa la cámara del sistema. |
| Fotos | No se pide: se usa el selector de fotos del sistema. |

## Recursos gráficos

| Recurso | Tamaño | Estado |
|---|---|---|
| Ícono | 512 × 512 PNG | `[Pendiente]` exportar desde el ícono adaptativo de `androidApp/src/main/res` |
| Gráfico de funciones | 1024 × 500 | `[Pendiente]` |
| Capturas de teléfono | 2 a 8, 1080 × 1920 o más | Base: `docs/design/screens/` (Inicio, Cartilla, Detalle, Fotos de la cartilla, Compartir ficha, Carnet) |

## Prueba interna

Ver `docs/BETA.md`.
