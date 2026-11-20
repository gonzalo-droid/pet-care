package com.bestid.core.domain.usecase

import com.bestid.core.model.VaccineRecord

/** Años descendentes; dentro de cada año, registros por fecha de aplicación descendente. */
fun List<VaccineRecord>.groupedByYear(): List<Pair<Int, List<VaccineRecord>>> =
    sortedByDescending { it.applicationDate }
        .groupBy { it.applicationDate.year }
        .map { (year, records) -> year to records }
