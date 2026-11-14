package com.bestid.core.designsystem.icon

import com.bestid.core.designsystem.resources.Res
import com.bestid.core.designsystem.resources.*
import org.jetbrains.compose.resources.DrawableResource

/**
 * Material Symbols Rounded (peso 400, 24 dp) importados como vectores en Compose Resources.
 * Elegidos para parecerse a los del prototipo (docs/design): casa, portapapeles con check, corazón, huella.
 */
object BestIdIcons {
    val Home: DrawableResource get() = Res.drawable.ic_home
    val Vaccines: DrawableResource get() = Res.drawable.ic_vaccines
    val Health: DrawableResource get() = Res.drawable.ic_health
    val Pets: DrawableResource get() = Res.drawable.ic_pets
    val PetsFilled: DrawableResource get() = Res.drawable.ic_pets_filled
    val Add: DrawableResource get() = Res.drawable.ic_add
    val Close: DrawableResource get() = Res.drawable.ic_close
    val ArrowBack: DrawableResource get() = Res.drawable.ic_arrow_back
    val ChevronRight: DrawableResource get() = Res.drawable.ic_chevron_right
    val ExpandMore: DrawableResource get() = Res.drawable.ic_expand_more
    val ExpandLess: DrawableResource get() = Res.drawable.ic_expand_less
    val Calendar: DrawableResource get() = Res.drawable.ic_calendar
    val Check: DrawableResource get() = Res.drawable.ic_check
    val CheckCircle: DrawableResource get() = Res.drawable.ic_check_circle
    val Error: DrawableResource get() = Res.drawable.ic_error
    val Info: DrawableResource get() = Res.drawable.ic_info
    val Warning: DrawableResource get() = Res.drawable.ic_warning
    val Notifications: DrawableResource get() = Res.drawable.ic_notifications
    val PhotoCamera: DrawableResource get() = Res.drawable.ic_photo_camera
    val Image: DrawableResource get() = Res.drawable.ic_image
    val Share: DrawableResource get() = Res.drawable.ic_share
    val Settings: DrawableResource get() = Res.drawable.ic_settings
    val Delete: DrawableResource get() = Res.drawable.ic_delete
    val Edit: DrawableResource get() = Res.drawable.ic_edit
    val Undo: DrawableResource get() = Res.drawable.ic_undo
    val Celebration: DrawableResource get() = Res.drawable.ic_celebration

    /** Ilustración provisional de gato para el avatar sin foto. */
    val CatIllustration: DrawableResource get() = Res.drawable.illustration_cat

    /** Ilustración provisional de perro (Bienvenida). */
    val DogIllustration: DrawableResource get() = Res.drawable.illustration_dog
}
