package com.novelforge.presentation

import androidx.lifecycle.viewModelScope
import com.novelforge.core.model.*
import com.novelforge.core.presentation.FeatureViewModel
import com.novelforge.domain.analytics.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

@HiltViewModel
class AnalyticsViewModel @Inject constructor(repository: AnalyticsRepository) : FeatureViewModel() {
    val days =
        repository.writingDays.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList(),
        )
}
