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

/** Agregar (petId = null) o editar mascota (M2). */
@Serializable data class PetEditRoute(val petId: String? = null) : Route

/** Detalle de vacuna (C2). */
@Serializable data class VaccineDetailRoute(val recordId: String) : Route

/** Agregar (recordId = null) o editar vacuna a mano (C3). */
@Serializable data class VaccineEditRoute(val recordId: String? = null) : Route

/** Agregar (careId = null) o editar cuidado (S2). */
@Serializable data class CareEditRoute(val careId: String? = null) : Route

/** Agregar peso (S3). */
@Serializable data object WeightNewRoute : Route

/** Permiso de avisos (O5), después del primer logro. */
@Serializable data object NotificationPermissionRoute : Route
