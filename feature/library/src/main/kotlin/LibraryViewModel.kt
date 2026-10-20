package com.novelforge.presentation
import com.novelforge.core.model.*
import com.novelforge.core.presentation.FeatureViewModel
import com.novelforge.domain.workspace.*
import com.novelforge.domain.writing.*
import com.novelforge.domain.story.*
import com.novelforge.domain.preferences.*
import com.novelforge.domain.backup.*
import com.novelforge.domain.analytics.*
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
data class SearchUiState(val query:String="",val results:List<EntrySummary> = emptyList())
@HiltViewModel
class LibraryViewModel @Inject constructor(private val repository:WritingRepository):FeatureViewModel(){
 private val mutable=MutableStateFlow(SearchUiState());val state=mutable.asStateFlow();private var job:Job?=null
 fun search(query:String){job?.cancel();job=launchWork{delay(200);mutable.value=SearchUiState(query,repository.search(query))}}
}
