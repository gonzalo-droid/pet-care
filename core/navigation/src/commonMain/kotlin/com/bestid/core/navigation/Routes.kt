package com.bestid.core.navigation

import kotlinx.serialization.Serializable

/**
 * Rutas tipadas de la app. Los features navegan entre sí solo a través de estas rutas,
 * nunca dependiendo de otro feature.
 */
sealed interface Route

/** Pestañas (sección 4). Cada una es la raíz de su propio grafo con su propia pila. */
@Serializable data object HomeGraph : Route
@Serializable data object CartillaGraph : Route
@Serializable data object HealthGraph : Route
@Serializable data object PetGraph : Route

@Serializable data object HomeRoute : Route
@Serializable data object CartillaRoute : Route
@Serializable data object HealthRoute : Route
@Serializable data object PetRoute : Route

/** Ajustes (A1). En M0 solo enlaza al catálogo de componentes. */
@Serializable data object SettingsRoute : Route

/** Catálogo de componentes del design system (herramienta de desarrollo). */
@Serializable data object ComponentCatalogRoute : Route

/** Contenedor de las 4 pestañas (barra superior con selector + barra inferior). */
@Serializable data object MainRoute : Route

/**
 * Agregar (petId = null) o editar mascota (M2). Con [thenCapture], al guardar sigue a las fotos de
 * la cartilla ([CardCaptureRoute]): es el primer uso "Escanear su cartilla" (O1).
 */
@Serializable data class PetEditRoute(val petId: String? = null, val thenCapture: Boolean = false) : Route

/** Detalle de vacuna (C2). */
@Serializable data class VaccineDetailRoute(val recordId: String) : Route

/**
 * Agregar (recordId = null) o editar vacuna a mano (C3). [photoIds]: páginas ya importadas en la
 * captura (O2), separadas por coma.
 */
@Serializable data class VaccineEditRoute(val recordId: String? = null, val photoIds: String? = null) : Route

/** Fotos de la cartilla (O2): hasta 4 páginas y después el formulario de vacuna. */
@Serializable data object CardCaptureRoute : Route

/** Visor de foto a pantalla completa. [path] es la ruta relativa guardada ("photos/…"). */
@Serializable data class PhotoViewerRoute(val path: String) : Route

/** Agregar (careId = null) o editar cuidado (S2). */
@Serializable data class CareEditRoute(val careId: String? = null) : Route

/** Agregar peso (S3). */
@Serializable data object WeightNewRoute : Route

/** Permiso de avisos (O5), después del primer logro. */
@Serializable data object NotificationPermissionRoute : Route
