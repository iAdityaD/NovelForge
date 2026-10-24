package com.novelforge.app

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.novelforge.data.local.LocalPreferencesRepository
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PreferencesTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    @Before fun clear() { context.getSharedPreferences("preferences", 0).edit().clear().commit() }

    @Test fun legacyTypographyMigratesAndReaderCanChangeIndependently() {
        context.getSharedPreferences("preferences", 0).edit().putFloat("fontSize", 24f).commit()
        val repository = LocalPreferencesRepository(context)
        assertEquals(24f, repository.state.value.readerFontSize)
        repository.update(repository.state.value.copy(readerFontSize = 17f, synopsisLimit = 1200))
        val reopened = LocalPreferencesRepository(context)
        assertEquals(24f, reopened.state.value.fontSize)
        assertEquals(17f, reopened.state.value.readerFontSize)
        assertEquals(1200, reopened.state.value.synopsisLimit)
        reopened.update(reopened.state.value.copy(fontSize = 28f))
        assertEquals(17f, LocalPreferencesRepository(context).state.value.readerFontSize)
    }

    @Test fun invalidPreferencesDoNotChangeSavedState() {
        val repository = LocalPreferencesRepository(context)
        val original = repository.state.value
        for (invalid in listOf(original.copy(synopsisLimit = 0), original.copy(fontSize = Float.NaN), original.copy(readerFontSize = 100f))) {
            assertTrue(runCatching { repository.update(invalid) }.isFailure)
            assertEquals(original, repository.state.value)
            assertEquals(original, LocalPreferencesRepository(context).state.value)
        }
    }
}
