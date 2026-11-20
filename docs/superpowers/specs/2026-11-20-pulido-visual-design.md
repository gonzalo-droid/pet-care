# Pulido visual: sistema de diseño, Inicio, Cartilla y carnet

Fecha: 20/11/2026. Maqueta aprobada por Gonzalo: https://claude.ai/artifact/QT9X4ubkR4rVJrqNtU48B5 (privada).
Decisión: D44 en `docs/DECISIONS.md`. Cambia la sección 7 de `SYSTEM_DESIGN.md` y las decisiones 1 y 3 de la sección 6.

## Por qué

Gonzalo quiere mejorar la app sin rediseñarla: se mantiene la base del prototipo (paleta crema y verde,
Nunito, formas redondeadas, navegación y flujos), pero la información debe leerse más rápido y la app
verse menos plana.

## Metas

1. Las tres pantallas que más se ven (Inicio, Cartilla, Mascota) muestran primero lo importante.
2. El color de aviso (durazno) marca solo el dato dudoso, nunca una tarjeta entera.
3. El cambio de tokens llega a todas las pantallas sin tocarlas una por una.

## No metas

- Nada de paleta, fuente, navegación, modelo de datos ni flujos nuevos.
- No se rehacen formularios, Salud, Ajustes ni el onboarding: solo reciben los tokens nuevos.
- Sin tema oscuro (sigue fuera del MVP).

## Anti metas (sería un fracaso aunque funcione)

- Perder accesibilidad: contraste menor a 4,5:1, toques menores a 48 dp, estados solo por color o cortes con texto al 200 %.
- Que el anillo de días pase a ser la única forma de leer cuánto falta.

## Enfoques considerados

- **A. Solo tokens:** bajo riesgo, pero las pantallas siguen ordenadas igual.
- **B. Tokens y tres pantallas (elegido por Gonzalo):** concentra el cambio donde el tutor mira todos los días.
- **C. Rediseño completo:** descartado; contradice "tomar la actual como base" y rehace trabajo probado.

## Diseño

### Sistema de diseño (`core/designsystem`)

- **Elevación:** token nuevo `BestIdTheme.elevation.card`. Las tarjetas de superficie (`PlainCard`, `StatCard`,
  `VaccineRecordCard`, `PetSelector`, botones de ícono de la barra) cambian el borde `line` de 2 dp por
  una sombra suave tibia (color `ink` con alfa bajo). `InfoCard` y `AlertCard` siguen sin borde ni sombra.
  Los controles (campos, botón secundario, chips) conservan su borde `outlineStrong`: delimitan un control.
- **Durazno solo para revisar:** `AlertCard` se mantiene para avisos de pantalla completa (sin permiso, error);
  dentro de una tarjeta, lo dudoso va en un `ReviewStrip` (franja durazno con ícono, texto y acción) o en un
  chip/badge con "!" delante.
- **`DateTile`:** día (22/800, cifras tabulares) sobre mes corto en mayúsculas (13/800). Tono normal
  (`primarySoft`/`primaryInk`) o de revisión (`alert`/`alertInk`). Se lee como "21 de diciembre de 2025".
- **`CountdownRing`:** anillo de 112 dp con el número de días (estilo `hero`) y "días" debajo. Progreso =
  días transcurridos desde la dosis anterior / días entre esa dosis y la próxima. Si no hay fecha anterior o
  está atrasado, se muestra sin progreso (anillo vacío) y el texto lo dice ("Atrasada 3 días"). Es decorativo:
  la semántica la da el texto.
- **`TimelineItem`:** `DateTile` a la izquierda con un riel vertical (`track`) y la tarjeta a la derecha.
- **`ProgressRow`:** texto, barra de progreso y acción "Completar" en una fila de 48 dp o más.
- Con letra grande (D43), `CountdownRing` y el texto se apilan y `TimelineItem` pone la fecha encima de la tarjeta.

### Inicio

Orden: selector y Ajustes → saludo (título) con subtítulo "N vacunas registradas · N dato(s) por confirmar" →
tarjeta "Próxima" con `CountdownRing`, nombre, fecha larga y, si la fecha es dudosa, badge "! Año por confirmar"
y `ReviewStrip` con "Confirmar" → par de `StatCard` con ícono (Última vacuna, Peso) → "Escanear cartilla".
La tarjeta grande de la mascota se elimina: su dato útil pasa al subtítulo. Si lo próximo es un cuidado, la misma
tarjeta muestra el cuidado. Los estados vacíos, de error y de avisos bloqueados se mantienen.

### Cartilla

Título con el mismo subtítulo de conteo → tarjeta "Próxima" con `DateTile` (tono de revisión si es dudosa),
nombre, badge y chevron → historial agrupado por año (encabezado con el año), cada registro como `TimelineItem`
con productos, chips de tipos (los dudosos con "!") y "N foto(s) de la cartilla". "Agregar vacuna" sigue al pie.

### Carnet (Mascota)

`PetIdCard` con encabezado verde más alto y una trama de puntos sutil, avatar de 96 dp montado con aro blanco,
nombre y especie·sexo, grilla de dos columnas solo con los datos llenos (Tutor, Raza, Nacimiento, Señas), un
`ProgressRow` "Faltan raza, nacimiento y señas" con "Completar" (abre el formulario) si falta alguno, separador
perforado y pie con "N vacunas" y "Próxima: fecha". Acciones: "Compartir carnet" (principal) y
"Agregar otra mascota" (`TextAction`). El botón "Completar datos" desaparece porque su acción vive en la fila.
El PDF de la ficha no cambia.

## Decidido por Claude (Gonzalo puede corregirlo)

- Quitar la tarjeta de mascota de Inicio.
- Progreso del anillo desde la dosis anterior.
- Mostrar solo los datos llenos en el carnet.
- Sombra en lugar de borde solo en superficies, no en controles.

## Para quien implemente

- Textos nuevos en Compose Resources (es-PE), con plurales donde haya conteos.
- Los colores de la sombra y la trama salen de tokens; nada suelto en features (`check-rules.sh`).
- Actualizar previews (normal, deshabilitado, texto largo, `fontScale = 2f`), el catálogo y las pruebas de UI
  que busquen textos que cambian.
- Verificar en el emulador con letra 100 % y 200 %, y correr `ContrastTest`, pruebas JVM y `assembleDebug`.
