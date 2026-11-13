package com.bestid.core.ui

import androidx.compose.runtime.Composable
import com.bestid.core.designsystem.component.AvatarSpecies
import com.bestid.core.designsystem.component.PetSelectorItem
import com.bestid.core.model.Pet
import com.bestid.core.model.Sex
import com.bestid.core.model.Species
import com.bestid.core.ui.resources.Res
import com.bestid.core.ui.resources.separator_dot
import com.bestid.core.ui.resources.sex_female
import com.bestid.core.ui.resources.sex_male
import com.bestid.core.ui.resources.species_bird
import com.bestid.core.ui.resources.species_cat
import com.bestid.core.ui.resources.species_cat_female
import com.bestid.core.ui.resources.species_dog
import com.bestid.core.ui.resources.species_dog_female
import com.bestid.core.ui.resources.species_other
import com.bestid.core.ui.resources.species_reptile
import com.bestid.core.ui.resources.species_small_mammal
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** Nombre de la especie; para perros y gatos concuerda con el sexo ("Gata"). */
fun Species.label(sex: Sex? = null): StringResource = when (this) {
    Species.DOG -> if (sex == Sex.FEMALE) Res.string.species_dog_female else Res.string.species_dog
    Species.CAT -> if (sex == Sex.FEMALE) Res.string.species_cat_female else Res.string.species_cat
    Species.REPTILE -> Res.string.species_reptile
    Species.BIRD -> Res.string.species_bird
    Species.SMALL_MAMMAL -> Res.string.species_small_mammal
    Species.OTHER -> Res.string.species_other
}

fun Sex.label(): StringResource = when (this) {
    Sex.FEMALE -> Res.string.sex_female
    Sex.MALE -> Res.string.sex_male
}

/** "Gata · Hembra" o solo la especie si no hay sexo. */
@Composable
fun Pet.speciesAndSex(): String {
    val species = stringResource(species.label(sex))
    return sex?.let { stringResource(Res.string.separator_dot, species, stringResource(it.label())) } ?: species
}

fun Species.avatar(): AvatarSpecies = when (this) {
    Species.CAT -> AvatarSpecies.CAT
    Species.DOG -> AvatarSpecies.DOG
    else -> AvatarSpecies.OTHER
}

fun Pet.toSelectorItem() = PetSelectorItem(id = id, name = name, photo = photoPath, species = species.avatar())
