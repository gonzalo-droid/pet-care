package com.bestid.feature.share

import com.bestid.core.domain.share.PetSheet
import com.bestid.core.model.DatePrecision
import com.bestid.core.platform.pdf.PdfBlock
import com.bestid.core.platform.pdf.PdfColors
import com.bestid.core.platform.pdf.PdfDocumentSpec
import com.bestid.core.ui.formatKgNumber
import com.bestid.core.ui.formatLongText
import com.bestid.core.ui.label
import com.bestid.core.ui.resources.Res as UiRes
import com.bestid.core.ui.resources.weight_kg
import com.bestid.feature.share.resources.Res
import com.bestid.feature.share.resources.pdf_birth
import com.bestid.feature.share.resources.pdf_birth_approx
import com.bestid.feature.share.resources.pdf_brand
import com.bestid.feature.share.resources.pdf_breed
import com.bestid.feature.share.resources.pdf_col_date
import com.bestid.feature.share.resources.pdf_col_expiry
import com.bestid.feature.share.resources.pdf_col_product
import com.bestid.feature.share.resources.pdf_col_types
import com.bestid.feature.share.resources.pdf_col_vet
import com.bestid.feature.share.resources.pdf_col_weight
import com.bestid.feature.share.resources.pdf_footer
import com.bestid.feature.share.resources.pdf_lot
import com.bestid.feature.share.resources.pdf_marks
import com.bestid.feature.share.resources.pdf_next_line
import com.bestid.feature.share.resources.pdf_next_none
import com.bestid.feature.share.resources.pdf_next_unconfirmed
import com.bestid.feature.share.resources.pdf_no_vaccines
import com.bestid.feature.share.resources.pdf_owner
import com.bestid.feature.share.resources.pdf_page
import com.bestid.feature.share.resources.pdf_section_data
import com.bestid.feature.share.resources.pdf_section_next
import com.bestid.feature.share.resources.pdf_section_vaccines
import com.bestid.feature.share.resources.pdf_section_weight
import com.bestid.feature.share.resources.pdf_sex
import com.bestid.feature.share.resources.pdf_species
import com.bestid.feature.share.resources.pdf_vaccine_fallback
import org.jetbrains.compose.resources.getString

/**
 * Ficha en PDF (sección 12): encabezado con foto y datos, próxima dosis, tabla de vacunas, peso
 * (si se marcó) y fotos de la cartilla en páginas siguientes (si se marcó). Nunca la dirección.
 */
internal suspend fun PetSheet.toPdf(colors: PdfColors, photoPath: (String) -> String): PdfDocumentSpec {
    val blocks = mutableListOf<PdfBlock>()
    val species = getString(pet.species.label(pet.sex))
    val sex = pet.sex?.let { getString(it.label()) }
    blocks += PdfBlock.Header(
        brand = getString(Res.string.pdf_brand),
        title = pet.name,
        subtitle = listOfNotNull(species, sex, pet.breed).joinToString(" · "),
        photoPath = pet.photoPath?.let(photoPath),
    )

    val birth = pet.birthDate?.formatLongText()?.let { date ->
        if (pet.birthDatePrecision != null && pet.birthDatePrecision != DatePrecision.EXACT) getString(Res.string.pdf_birth_approx, date) else date
    }
    blocks += PdfBlock.Section(getString(Res.string.pdf_section_data))
    blocks += PdfBlock.Fields(
        listOfNotNull(
            getString(Res.string.pdf_species) to species,
            sex?.let { getString(Res.string.pdf_sex) to it },
            pet.breed?.let { getString(Res.string.pdf_breed) to it },
            birth?.let { getString(Res.string.pdf_birth) to it },
            pet.distinguishingMarks?.let { getString(Res.string.pdf_marks) to it },
            pet.ownerName?.let { getString(Res.string.pdf_owner) to it },
        ),
    )

    blocks += PdfBlock.Section(getString(Res.string.pdf_section_next))
    val next = nextDue
    blocks += PdfBlock.Paragraph(
        if (next == null) {
            getString(Res.string.pdf_next_none)
        } else {
            val name = next.record.nextDateLabel
                ?: vaccines.firstOrNull { it.record.id == next.record.id }?.typeLabels?.firstOrNull()
                ?: getString(Res.string.pdf_vaccine_fallback)
            val line = if (next.confirmed) Res.string.pdf_next_line else Res.string.pdf_next_unconfirmed
            getString(line, name, next.date.formatLongText())
        },
    )

    if (vaccines.isNotEmpty()) {
        blocks += PdfBlock.Section(getString(Res.string.pdf_section_vaccines))
        val lotLabel: suspend (String) -> String = { getString(Res.string.pdf_lot, it) }
        blocks += PdfBlock.Table(
            headers = listOf(
                getString(Res.string.pdf_col_date),
                getString(Res.string.pdf_col_types),
                getString(Res.string.pdf_col_product),
                getString(Res.string.pdf_col_expiry),
                getString(Res.string.pdf_col_vet),
            ),
            rows = vaccines.map { v ->
                listOf(
                    v.record.applicationDate.formatLongText(),
                    v.typeLabels.joinToString(", "),
                    v.record.items.mapNotNull { item ->
                        listOfNotNull(item.productName, item.lot?.let { lotLabel(it) }).joinToString(" · ").ifEmpty { null }
                    }.joinToString("\n"),
                    v.record.items.mapNotNull { it.expiryDate?.formatLongText() }.joinToString("\n"),
                    v.vet?.name.orEmpty(),
                )
            },
            weights = listOf(1.1f, 1.6f, 1.8f, 1.1f, 1.4f),
        )
    } else if (cardPhotos.isEmpty()) {
        blocks += PdfBlock.Paragraph(getString(Res.string.pdf_no_vaccines))
    }

    if (weights.isNotEmpty()) {
        blocks += PdfBlock.Section(getString(Res.string.pdf_section_weight))
        blocks += PdfBlock.Table(
            headers = listOf(getString(Res.string.pdf_col_date), getString(Res.string.pdf_col_weight)),
            rows = weights.sortedByDescending { it.date }.map { listOf(it.date.formatLongText(), getString(UiRes.string.weight_kg, formatKgNumber(it.grams))) },
            weights = listOf(1f, 1f),
        )
    }

    if (cardPhotos.isNotEmpty()) {
        blocks += PdfBlock.Images(cardPhotos.mapIndexed { i, photo -> getString(Res.string.pdf_page, i + 1) to photoPath(photo.path) })
    }

    return PdfDocumentSpec(blocks, footer = getString(Res.string.pdf_footer, generatedOn.formatLongText()), colors = colors)
}

/** Nombre de archivo sin tildes ni espacios, para que llegue igual por WhatsApp o correo. */
internal fun safeFileName(name: String, fallback: String): String {
    val plain = name.map { c ->
        when (c) {
            'á', 'à', 'ä', 'â' -> 'a'
            'é', 'è', 'ë', 'ê' -> 'e'
            'í', 'ì', 'ï', 'î' -> 'i'
            'ó', 'ò', 'ö', 'ô' -> 'o'
            'ú', 'ù', 'ü', 'û' -> 'u'
            'ñ' -> 'n'
            'Á', 'À', 'Ä', 'Â' -> 'A'
            'É', 'È', 'Ë', 'Ê' -> 'E'
            'Í', 'Ì', 'Ï', 'Î' -> 'I'
            'Ó', 'Ò', 'Ö', 'Ô' -> 'O'
            'Ú', 'Ù', 'Ü', 'Û' -> 'U'
            'Ñ' -> 'N'
            else -> c
        }
    }.joinToString("")
    return plain.replace(Regex("[^A-Za-z0-9]+"), "-").trim('-').ifEmpty { fallback }.take(40)
}
