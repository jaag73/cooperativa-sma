package com.example.cooperativadeliveryapp.data.alert

import android.content.Context
import android.media.Ringtone
import android.media.RingtoneManager
import com.example.cooperativadeliveryapp.domain.alert.OrderAlertManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.*

@Singleton
class OrderAlertManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : OrderAlertManager {

    private var ringtone: Ringtone? = null
    private var alertJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun startAlertLoop() {
        if (alertJob?.isActive == true) return

        alertJob = scope.launch {
            try {
                val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

                if (ringtone == null) {
                    ringtone = RingtoneManager.getRingtone(context, uri)
                }

                while (isActive) {
                    if (ringtone?.isPlaying == false) {
                        ringtone?.play()
                    }
                    delay(2000)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun stopAlert() {
        alertJob?.cancel()
        alertJob = null
        ringtone?.stop()
    }
}
