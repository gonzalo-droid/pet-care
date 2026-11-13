package com.bestid.core.platform.notification

import com.bestid.core.domain.reminder.PlannedReminder
import com.bestid.core.domain.reminder.ReminderKind
import com.bestid.core.domain.reminder.ReminderSubject
import com.bestid.core.model.CareKind
import com.bestid.core.platform.resources.Res
import com.bestid.core.platform.resources.reminder_day_before
import com.bestid.core.platform.resources.reminder_follow_up
import com.bestid.core.platform.resources.reminder_same_day
import com.bestid.core.platform.resources.reminder_subject_care
import com.bestid.core.platform.resources.reminder_subject_deworm_external
import com.bestid.core.platform.resources.reminder_subject_deworm_internal
import com.bestid.core.platform.resources.reminder_subject_vaccine
import com.bestid.core.platform.resources.reminder_subject_vaccine_of
import com.bestid.core.platform.resources.reminder_title_care
import com.bestid.core.platform.resources.reminder_title_vaccine
import com.bestid.core.platform.resources.reminder_week_before
import org.jetbrains.compose.resources.getString

/** Título y texto de un aviso, en es-PE, desde Compose Resources (regla 3). */
data class ReminderText(val title: String, val body: String)

suspend fun PlannedReminder.text(): ReminderText {
    val what = when (val s = subject) {
        is ReminderSubject.Vaccine ->
            s.typeLabel?.let { getString(Res.string.reminder_subject_vaccine_of, it.lowercase()) }
                ?: getString(Res.string.reminder_subject_vaccine)
        is ReminderSubject.Care -> when (s.kind) {
            CareKind.DEWORM_INTERNAL -> getString(Res.string.reminder_subject_deworm_internal)
            CareKind.DEWORM_EXTERNAL -> getString(Res.string.reminder_subject_deworm_external)
            CareKind.OTHER -> s.title?.lowercase() ?: getString(Res.string.reminder_subject_care)
        }
    }
    val title = if (subject is ReminderSubject.Vaccine) {
        getString(Res.string.reminder_title_vaccine, petName)
    } else {
        getString(Res.string.reminder_title_care, petName)
    }
    val body = when (kind) {
        ReminderKind.WEEK_BEFORE -> getString(Res.string.reminder_week_before, petName, what, (-kind.daysFromDue).toString())
        ReminderKind.DAY_BEFORE -> getString(Res.string.reminder_day_before, petName, what)
        ReminderKind.SAME_DAY -> getString(Res.string.reminder_same_day, petName, what)
        ReminderKind.FOLLOW_UP -> getString(Res.string.reminder_follow_up, petName, what)
    }
    return ReminderText(title, body)
}
