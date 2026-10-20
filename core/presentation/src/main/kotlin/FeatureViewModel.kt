package com.novelforge.core.presentation
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
abstract class FeatureViewModel:ViewModel() {
 private val _error=MutableStateFlow<String?>(null)
 val error:StateFlow<String?> = _error.asStateFlow()
 fun clearError(){ _error.value=null }
 protected fun launchWork(block:suspend ()->Unit)=viewModelScope.launch {
  try { block() } catch(e:CancellationException){throw e} catch(e:Exception){_error.value=e.message ?: "Something went wrong. Please retry."}
 }
}
