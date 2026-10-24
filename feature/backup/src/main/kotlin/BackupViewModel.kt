package com.novelforge.presentation

import com.novelforge.core.model.*
import com.novelforge.core.presentation.FeatureViewModel
import com.novelforge.domain.backup.*
import com.novelforge.domain.writing.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

@HiltViewModel
class BackupViewModel
@Inject
constructor(private val repository: BackupRepository, private val session: EditorSession) :
    FeatureViewModel() {
    private val active = MutableStateFlow(false)
    val busy = active.asStateFlow()

    fun export(destination: String, id: String, markdown: Boolean, done: () -> Unit) = launchWork {
        if (active.value) return@launchWork
        active.value = true
        try {
            if (!session.flush()) return@launchWork
            repository.export(destination, id, markdown)
            done()
        } finally {
            active.value = false
        }
    }

    fun importBackup(source: String, done: (String) -> Unit) = launchWork {
        if (active.value) return@launchWork
        active.value = true
        try {
            if (!session.flush()) return@launchWork
            done(repository.restore(source))
        } finally {
            active.value = false
        }
    }
}
