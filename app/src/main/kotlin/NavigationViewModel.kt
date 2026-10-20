package com.novelforge.app.ui

import androidx.lifecycle.ViewModel
import com.novelforge.domain.preferences.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NavigationViewModel @Inject constructor(private val preferences: PreferencesRepository) :
    ViewModel() {
    fun lastBook() = preferences.lastBook()

    fun lastEntry() = preferences.lastEntry()

    fun rememberBook(id: String) = preferences.rememberBook(id)
}
