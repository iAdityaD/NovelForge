package com.novelforge.data.local

import android.content.Context
import androidx.room.Room
import com.novelforge.app.data.*
import com.novelforge.core.model.*
import com.novelforge.domain.analytics.*
import com.novelforge.domain.backup.*
import com.novelforge.domain.preferences.*
import com.novelforge.domain.story.*
import com.novelforge.domain.workspace.*
import com.novelforge.domain.writing.*
import com.novelforge.domain.manuscript.ManuscriptRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    @Provides
    @Singleton
    fun editorSession(repository: WritingRepository) =
        EditorSession(repository, CoroutineScope(SupervisorJob() + Dispatchers.IO))

    @Provides
    @Singleton
    fun database(@ApplicationContext context: Context): ForgeDb =
        Room.databaseBuilder(context, ForgeDb::class.java, "novelforge.db")
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
            .build()

    @Provides fun manuscript(value: LocalManuscriptRepository): ManuscriptRepository = value

    @Provides fun workspace(value: LocalWorkspaceRepository): WorkspaceRepository = value

    @Provides fun writing(value: LocalWritingRepository): WritingRepository = value

    @Provides fun story(value: LocalStoryRepository): StoryRepository = value

    @Provides fun analytics(value: LocalAnalyticsRepository): AnalyticsRepository = value

    @Provides fun backup(value: LocalBackupRepository): BackupRepository = value

    @Provides fun preferences(value: LocalPreferencesRepository): PreferencesRepository = value
}
