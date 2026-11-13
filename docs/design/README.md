# Diseño de BestID

Copia este contenido en `docs/design/` del repositorio. Complementa a `docs/SYSTEM_DESIGN.md` (secciones 4 a 7).

## Qué hay

- `screens/`: capturas de las 15 pantallas a 2x (ancho de teléfono de 390 px).
- `source/`: el HTML de cada pantalla. Sirve para sacar valores exactos: colores, tamaños, radios, espacios y textos. No es código para copiar: la app se construye en Compose Multiplatform con los componentes de `docs/SYSTEM_DESIGN.md` sección 7.

| Captura | Pantalla | Ruta |
|---|---|---|
| 01-bienvenida | O1 Bienvenida | `onboarding/welcome` |
| 02-fotos-cartilla | O2 Captura | `scan/capture` |
| 03-leyendo | O3 Leyendo | `scan/reading` |
| 04-revisar-lectura | O4 Revisar lectura | `scan/review` |
| 05-permiso-avisos | O5 Permiso de avisos | `onboarding/notifications` |
| 06-inicio | H1 Inicio | `home` |
| 07-cartilla | C1 Cartilla | `cartilla` |
| 08-detalle-vacuna | C2 Detalle de vacuna | `vaccine/{id}` |
| 09-salud-cuidados | S1 Salud, segmento Cuidados | `health?tab=care` |
| 10-salud-peso | S1 Salud, segmento Peso | `health?tab=weight` |
| 11-mascota-carnet | M1 Mascota (carnet) | `pet` |
| 12-nueva-vacuna | C3 Agregar o editar vacuna | `vaccine/edit` |
| 13-nueva-mascota | M2 Agregar o editar mascota | `pet/edit` |
| 14-compartir-ficha | SH1 Compartir ficha | `share` |
| 15-componentes | Tokens y componentes | — |

## Notas para implementar

- Los datos son de ejemplo (Lilith, con el tutor anonimizado). Los días restantes y las fechas se calculan en la app.
- El peso del gráfico es de ejemplo; en la app no existe el badge "Datos de ejemplo".
- La cámara, la galería y el selector de fecha son los del sistema, no los dibujados.
- Las ilustraciones (gato, perro, huellitas) son provisionales hasta tener las propias.
- Pantallas no dibujadas (Ajustes, Respaldo, Agregar peso, Agregar cuidado, estados vacío, error y sin permiso): se arman con los mismos componentes.
