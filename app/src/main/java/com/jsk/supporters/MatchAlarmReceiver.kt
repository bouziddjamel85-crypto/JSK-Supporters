package com.jsk.supporters
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class MatchAlarmReceiver : BroadcastReceiver() {
 override fun onReceive(context: Context, intent: Intent) {
  val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
  if (Build.VERSION.SDK_INT >= 26) manager.createNotificationChannel(NotificationChannel("jsk_matches", "تذكيرات المباريات", NotificationManager.IMPORTANCE_HIGH))
  val n = NotificationCompat.Builder(context, "jsk_matches")
   .setSmallIcon(android.R.drawable.ic_dialog_info)
   .setContentTitle(intent.getStringExtra("title") ?: "JSK Supporters")
   .setContentText(intent.getStringExtra("message") ?: "موعد مباراة الشبيبة")
   .setPriority(NotificationCompat.PRIORITY_HIGH).setAutoCancel(true).build()
  manager.notify(intent.getIntExtra("notificationId", 1001), n)
 }
}
