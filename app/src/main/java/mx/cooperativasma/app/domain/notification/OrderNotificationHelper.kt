package mx.cooperativasma.app.domain.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderNotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val CHANNEL_ID = "live_order_updates"
    private val notificationManager = NotificationManagerCompat.from(context)

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Actualizaciones de Pedido"
            val descriptionText = "Notificaciones sobre el estado de tu pedido"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    fun showNewOrderForBusinessNotification(orderId: String) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("¡Nuevo Pedido Recibido!")
            .setContentText("Tienes un nuevo pedido #$orderId esperando confirmación.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        try {
            notificationManager.notify(orderId.hashCode(), builder.build())
        } catch (e: SecurityException) {
            // Missing POST_NOTIFICATIONS permission
        }
    }

    fun showCourierInTransitNotification(orderId: String, etaMinutes: Int) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentTitle("🔔 ¡Tu repartidor ya inició el viaje hacia tu domicilio!")
            .setContentText("⏱️ Tiempo aproximado de llegada: $etaMinutes min")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        try {
            notificationManager.notify(orderId.hashCode(), builder.build())
        } catch (e: SecurityException) {
            // Missing POST_NOTIFICATIONS permission
        }
    }
}
