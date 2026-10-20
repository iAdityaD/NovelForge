package com.novelforge.domain.analytics
import com.novelforge.core.model.*
import kotlinx.coroutines.flow.*
interface AnalyticsRepository { val writingDays:Flow<List<WritingDay>> }
