package com.novelforge.domain.backup
import com.novelforge.core.model.*
import kotlinx.coroutines.flow.*
interface BackupRepository {
 suspend fun export(destination:String,bookId:String,markdown:Boolean)
 suspend fun restore(source:String):String
}
