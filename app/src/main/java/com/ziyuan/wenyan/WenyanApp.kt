package com.ziyuan.wenyan

import android.app.Application
import com.ziyuan.wenyan.data.parser.DataImporter
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class WenyanApp : Application() {

    @Inject
    lateinit var dataImporter: DataImporter

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        // 首次启动时异步将 assets JSON 数据导入 Room
        appScope.launch { dataImporter.importIfEmpty() }
    }
}
