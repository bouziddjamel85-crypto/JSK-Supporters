package com.jsk.supporters

import android.Manifest
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.view.Gravity
import android.widget.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : Activity() {
 private val green = Color.rgb(0,105,55)
 private val yellow = Color.rgb(245,190,20)
 private val matchesUrl = "https://www.winwin.com/كرة-قدم/الفرق/شبيبة-القبائل/المباريات-والنتائج"
 private lateinit var root: LinearLayout
 private lateinit var content: LinearLayout
 private lateinit var opponent: EditText
 private lateinit var dateLabel: TextView
 private lateinit var timeLabel: TextView
 private val matchDate = Calendar.getInstance()
 private val matchTime = Calendar.getInstance()

 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
   requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 44)
  home()
 }
 private fun base(title: String) {
  root = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setBackgroundColor(Color.rgb(248,248,244)) }
  root.addView(TextView(this).apply {
   text="🟢 JSK SUPPORTERS 🟡\n$title"; textSize=22f; gravity=Gravity.CENTER
   setTextColor(Color.WHITE); setPadding(10,22,10,22); setBackgroundColor(green)
  })
  val nav=LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL; setBackgroundColor(yellow) }
  nav.addView(navButton("الرئيسية"){home()}, LinearLayout.LayoutParams(0,52,1f))
  nav.addView(navButton("المباريات"){matches()}, LinearLayout.LayoutParams(0,52,1f))
  nav.addView(navButton("الأخبار"){news()}, LinearLayout.LayoutParams(0,52,1f))
  root.addView(nav)
  content=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(16,12,16,24) }
  root.addView(ScrollView(this).apply { addView(content) }, LinearLayout.LayoutParams(-1,0,1f))
  setContentView(root)
 }
 private fun navButton(s:String, f:()->Unit)=Button(this).apply { text=s; textSize=12f; setTextColor(green); setOnClickListener{f()} }
 private fun heading(s:String){ content.addView(TextView(this).apply { text=s; textSize=19f; setTextColor(green); setPadding(0,14,0,8) }) }
 private fun btn(s:String, f:()->Unit)=Button(this).apply { text=s; setTextColor(Color.WHITE); setBackgroundColor(green); setOnClickListener{f()} }
 private fun home() {
  base("مع الشبيبة في كل مكان")
  heading("أضف مباراة واضبط التذكيرات")
  opponent=EditText(this).apply { hint="اسم المنافس" }; content.addView(opponent)
  dateLabel=TextView(this); timeLabel=TextView(this); updateLabels()
  content.addView(btn("اختيار تاريخ المباراة"){pickDate()}); content.addView(dateLabel)
  content.addView(btn("اختيار توقيت المباراة"){pickTime()}); content.addView(timeLabel)
  content.addView(btn("حفظ التذكيرات"){saveMatch()})
  heading("آخر مباراة محفوظة")
  content.addView(TextView(this).apply {
   text=getSharedPreferences("jsk",0).getString("last","لا توجد مباراة محفوظة بعد.") ?: "لا توجد مباراة محفوظة بعد."
   textSize=15f; setTextColor(Color.DKGRAY)
  })
  heading("تنبيه")
  content.addView(TextView(this).apply {
   text="المباريات والأخبار تُفتح من Winwin عبر الإنترنت. تنبيه نهاية المباراة تقديري، ولا توجد حالياً مزامنة تلقائية للنتائج المباشرة."
   textSize=14f; setTextColor(Color.DKGRAY)
  })
 }
 private fun updateLabels() {
  dateLabel.text="التاريخ: "+SimpleDateFormat("yyyy-MM-dd",Locale.getDefault()).format(matchDate.time)
  timeLabel.text="الساعة: "+String.format(Locale.getDefault(),"%02d:%02d",matchTime.get(Calendar.HOUR_OF_DAY),matchTime.get(Calendar.MINUTE))
 }
 private fun pickDate() {
  DatePickerDialog(this,{_,y,m,d->matchDate.set(y,m,d);updateLabels()},
   matchDate.get(Calendar.YEAR),matchDate.get(Calendar.MONTH),matchDate.get(Calendar.DAY_OF_MONTH)).show()
 }
 private fun pickTime() {
  TimePickerDialog(this,{_,h,m->matchTime.set(Calendar.HOUR_OF_DAY,h);matchTime.set(Calendar.MINUTE,m);updateLabels()},
   matchTime.get(Calendar.HOUR_OF_DAY),matchTime.get(Calendar.MINUTE),true).show()
 }
 private fun saveMatch() {
  val name=opponent.text.toString().trim()
  if(name.isEmpty()){opponent.error="اكتب اسم المنافس";return}
  val kickoff=Calendar.getInstance().apply {
   set(matchDate.get(Calendar.YEAR),matchDate.get(Calendar.MONTH),matchDate.get(Calendar.DAY_OF_MONTH),matchTime.get(Calendar.HOUR_OF_DAY),matchTime.get(Calendar.MINUTE),0)
  }
  if(kickoff.timeInMillis<=System.currentTimeMillis()){Toast.makeText(this,"اختر موعداً في المستقبل",Toast.LENGTH_LONG).show();return}
  val label="$name — "+SimpleDateFormat("yyyy-MM-dd HH:mm",Locale.getDefault()).format(kickoff.time)
  getSharedPreferences("jsk",0).edit().putString("last",label).apply()
  alarm(name,kickoff.timeInMillis-3600000L,"باقي ساعة على مباراة الشبيبة ضد $name",1)
  alarm(name,kickoff.timeInMillis,"انطلقت مباراة الشبيبة ضد $name",2)
  alarm(name,kickoff.timeInMillis+150*60000L,"تذكير تقديري بنهاية مباراة الشبيبة ضد $name",3)
  Toast.makeText(this,"تم حفظ التذكيرات",Toast.LENGTH_LONG).show(); home()
 }
 private fun alarm(name:String,at:Long,msg:String,id:Int) {
  if(at<=System.currentTimeMillis())return
  val code=(name.hashCode() and 0x7fffffff)%100000+id
  val i=Intent(this,MatchAlarmReceiver::class.java).apply {
   putExtra("title","JSK Supporters — $name");putExtra("message",msg);putExtra("notificationId",code)
  }
  val pi=PendingIntent.getBroadcast(this,code,i,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
  val am=getSystemService(ALARM_SERVICE) as AlarmManager
  try { if(Build.VERSION.SDK_INT>=23)am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,at,pi) else am.setExact(AlarmManager.RTC_WAKEUP,at,pi) }
  catch(_:SecurityException){am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,at,pi)}
 }
 private fun matches() {
  base("المباريات والنتائج"); heading("متابعة شبيبة القبائل")
  content.addView(btn("فتح المباريات والنتائج في المتصفح"){startActivity(Intent(Intent.ACTION_VIEW,Uri.parse(matchesUrl)))})
  content.addView(btn("عرض الصفحة داخل التطبيق"){source(matchesUrl)})
 }
 private fun news()=source("https://www.winwin.com/كرة-قدم/الفرق/شبيبة-القبائل")
 private fun source(url:String) {
  base("أخبار ومباريات الشبيبة")
  val w=WebView(this);w.settings.javaScriptEnabled=true;w.settings.domStorageEnabled=true;w.webViewClient=WebViewClient()
  content.addView(w,LinearLayout.LayoutParams(-1,1000));w.loadUrl(url)
 }
}
