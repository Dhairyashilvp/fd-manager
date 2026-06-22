package com.fdtracker.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.fdtracker.core.data.worker.WorkManagerInitializer
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var workManagerInitializer: WorkManagerInitializer

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            workManagerInitializer.initialize()
        }
    }
}
