# Ficha de App Store

Borrador para App Store Connect. Mercado: solo Perú (sección 20). Idioma principal: español (México), que App Store usa para Latinoamérica.

## Datos de la app

| Campo | Valor |
|---|---|
| Nombre (máx. 30) | BestID: cartilla de tu mascota |
| Subtítulo (máx. 30) | Vacunas y avisos de tu mascota |
| Bundle ID | `com.nekodojo.bestid` (en `iosApp/Configuration/Config.xcconfig`) |
| Categoría principal | Estilo de vida; secundaria: Medicina |
| Precio | Gratis |
| Clasificación por edad | 4+ |
| URL de soporte | `[Pendiente]` |
| URL de la política de privacidad | `[Pendiente]` URL pública con `docs/legal/politica-de-privacidad.md` |

## Texto promocional (máx. 170)

Guarda la cartilla de vacunas de tu mascota con fotos y recibe avisos de la próxima vacuna o desparasitación. Sin cuenta: todo queda en tu iPhone.

## Descripción

La misma que en `docs/store/google-play.md`, cambiando "teléfono" por "iPhone" donde corresponda.

## Palabras clave (máx. 100, separadas por coma)

vacunas,mascota,perro,gato,cartilla,veterinaria,desparasitación,recordatorio,carnet,salud

## Privacidad de la app ("etiquetas de privacidad")

- **¿Recopilas datos de esta app?** Sí, solo diagnóstico (D48): **Diagnóstico → Datos de fallos** y **Otros datos de diagnóstico**, más **Identificadores → ID de dispositivo** (ID de instalación de Firebase). Para cada uno: no vinculado a la identidad, sin rastreo, finalidad "Funcionalidad de la app". Los datos de la mascota y las fotos no salen del iPhone salvo cuando la persona comparte un PDF o un respaldo.
- `iosApp/iosApp/PrivacyInfo.xcprivacy` declara lo mismo, sin rastreo, y las API que Apple pide justificar.

## Cumplimiento de exportación

`ITSAppUsesNonExemptEncryption = NO` en `Info.plist`: la app no usa cifrado propio (TestFlight no vuelve a preguntar).

## Permisos

| Permiso | Texto (Info.plist) |
|---|---|
| Cámara | "BestID usa la cámara para tomar fotos de la cartilla de vacunación, del sticker de la vacuna y de tu mascota. Las fotos se guardan solo en tu teléfono." |
| Fotos | No se pide: `PHPickerViewController` no necesita permiso. |
| Notificaciones | Se pide desde la app, después de guardar la primera vacuna o cuidado (O5). |

## Recursos gráficos

| Recurso | Tamaño | Estado |
|---|---|---|
| Ícono | 1024 × 1024, sin transparencia | En `iosApp/iosApp/Assets.xcassets/AppIcon.appiconset` |
| Capturas iPhone 6,9" | 1320 × 2868 | `[Pendiente]` desde el simulador; base en `docs/design/screens/` |

## TestFlight

Ver `docs/BETA.md`.
