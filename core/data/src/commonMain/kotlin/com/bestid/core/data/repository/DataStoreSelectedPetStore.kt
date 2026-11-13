package com.bestid.core.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.bestid.core.domain.port.SelectedPetStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DataStoreSelectedPetStore(private val prefs: DataStore<Preferences>) : SelectedPetStore {
    private val key = stringPreferencesKey("selected_pet_id")

    override val selectedPetId: Flow<String?> = prefs.data.map { it[key] }

    override suspend fun select(petId: String?) {
        prefs.edit { if (petId == null) it.remove(key) else it[key] = petId }
    }
}
