package com.novelforge.presentation

import com.novelforge.core.model.*
import com.novelforge.core.presentation.FeatureViewModel
import com.novelforge.domain.preferences.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

@HiltViewModel
class ReaderViewModel @Inject constructor(private val preferences: PreferencesRepository) :
    FeatureViewModel() {
    val state = preferences.state

    fun bookmark(id: Long) = preferences.bookmark(id)

    fun bookmark(id: Long, value: Boolean) = preferences.bookmark(id, value)

    fun readingPosition(id: Long) = preferences.readingPosition(id)

    fun readingPosition(id: Long, index: Int, offset: Int) =
        preferences.readingPosition(id, index, offset)

    fun setFont(value: Float) = preferences.update(state.value.copy(readerFontSize = value))

    fun setSerif(value: Boolean) = preferences.update(state.value.copy(readerSerif = value))

    fun setWarm(value: Boolean) = preferences.update(state.value.copy(readerWarm = value))
}
