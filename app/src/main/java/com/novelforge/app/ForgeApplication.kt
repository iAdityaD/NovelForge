package com.novelforge.app

import android.app.Application
import androidx.room.Room
import com.novelforge.app.data.ForgeDb
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class ForgeApplication : Application() {
    val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    val db by lazy { Room.databaseBuilder(this, ForgeDb::class.java, "novelforge.db").build() }
}
