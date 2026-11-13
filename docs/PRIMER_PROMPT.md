# Primer prompt para Claude Code

Pega esto en la primera sesión, con Claude Code abierto en la carpeta del repositorio:

---

Vamos a construir BestID. Lee completos `CLAUDE.md` y `docs/SYSTEM_DESIGN.md`.

Después, antes de escribir código:
1. Resúmeme en 10 líneas qué entendiste del producto y de la arquitectura.
2. Dime qué dudas o contradicciones encuentras en el documento.
3. Propón el plan del hito M0: estructura del proyecto Kotlin Multiplatform con Compose Multiplatform (Android e iOS), librerías con sus versiones estables actuales, tema con los tokens de la sección 7, los componentes de la sección 7 con previews, navegación vacía con las 4 pestañas (Inicio, Cartilla, Salud, Mascota) y CI que compile Android e iOS.

No empieces a programar hasta que apruebe el plan.

---

## Prompts para los siguientes hitos

- "M0 aprobado. Sigue con el plan de M1 según la sección 17. Muéstrame el plan antes de programar."
- Al cerrar cada hito: "Revisa M[n] contra su criterio de terminado de la sección 17 y lista lo que falta."
