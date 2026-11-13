import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun VersionCatalog.version(alias: String): String =
    findVersion(alias).get().requiredVersion

internal fun VersionCatalog.library(alias: String) = findLibrary(alias).get()

/**
 * Paquete y namespace derivados de la ruta del módulo:
 * `:core:designsystem` -> `com.bestid.core.designsystem`.
 */
internal val Project.bestIdNamespace: String
    get() = "com.bestid." + path.removePrefix(":").replace(":", ".").replace("-", "")
