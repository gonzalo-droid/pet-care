package com.bestid.core.model

enum class Species { DOG, CAT, REPTILE, BIRD, SMALL_MAMMAL, OTHER }

enum class Sex { FEMALE, MALE }

/** Precisión de una fecha aproximada (por ejemplo, el nacimiento). */
enum class DatePrecision { EXACT, MONTH, YEAR }

enum class RecordSource { SCAN, MANUAL }

enum class CareKind { DEWORM_INTERNAL, DEWORM_EXTERNAL, OTHER }

/** Unidad del intervalo de un cuidado. Los meses son de calendario, no 30 días. */
enum class IntervalUnit { DAY, MONTH }

/** Estado de un trabajo de lectura de la cartilla. `[Propuesta]`: se revisa en M3. */
enum class ScanStatus {
    /** Fotos guardadas, esperando lectura (por ejemplo, sin internet). */
    PENDING,

    /** Enviado al servicio de lectura. */
    READING,

    /** Lectura recibida; falta que la persona confirme en "Revisar lectura". */
    DONE,

    /** La lectura falló; ver `ScanJob.error`. */
    FAILED,

    /** La persona canceló la lectura. */
    CANCELLED,
}
