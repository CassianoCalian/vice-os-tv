package com.viceos.tv

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.widget.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {
 private val bg = Color.rgb(12, 12, 28)
 private val handler = Handler(Looper.getMainLooper())
 private lateinit var clock: TextView
 private fun shape(color: Int, radius: Float = 18f): GradientDrawable = GradientDrawable().apply { setColor(color); cornerRadius = radius }
 private fun label(text: String, size: Float, bold: Boolean = false): TextView = TextView(this).apply {
  this.text = text; textSize = size; setTextColor(Color.WHITE); if (bold) typeface = Typeface.DEFAULT_BOLD
 }
 override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState)
  window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
  val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(42,25,42,24); background = BitmapDrawable(resources, BitmapFactory.decodeResource(resources, R.drawable.vice_background)).apply { gravity = Gravity.FILL } }
  val top = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
  top.addView(label("VICE  OS",27,true), LinearLayout.LayoutParams(0,-2,1f))
  clock = label("",19,true); top.addView(clock)
  root.addView(top)
  val hero = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(30,24,24,24); background = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, intArrayOf(Color.argb(225,18,13,39),Color.argb(155,86,24,89),Color.argb(90,27,23,54))).apply { cornerRadius=26f } }
  val title = label("BEM-VINDO A VICE CITY",29,true); hero.addView(title)
  val subtitle = label("Sua TV. Seu universo. Seu estilo.",17); subtitle.setPadding(0,8,0,12); hero.addView(subtitle)
  hero.addView(label("VICE OS 1.0  •  MIAMI EDITION",12,true))
  root.addView(hero,LinearLayout.LayoutParams(-1,0,1.05f).apply { topMargin=22 })
  val heading = label("Seus aplicativos",21,true); heading.setPadding(0,19,0,12); root.addView(heading)
  val scroll = HorizontalScrollView(this).apply { isHorizontalScrollBarEnabled = false }
  val apps = LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL }
  val pm = packageManager
  val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
  val entries = (pm.queryIntentActivities(intent,0) + pm.queryIntentActivities(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),0)).distinctBy { it.activityInfo.packageName }.filter { it.activityInfo.packageName != packageName }.sortedBy { it.loadLabel(pm).toString().lowercase() }
  for (entry in entries) {
   val card = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; gravity=Gravity.CENTER; background=shape(Color.rgb(35,35,59)); isFocusable=true; isFocusableInTouchMode=false; setPadding(14,12,14,10) }
   val icon = ImageView(this).apply { setImageDrawable(entry.loadIcon(pm)); scaleType=ImageView.ScaleType.FIT_CENTER }
   card.addView(icon, LinearLayout.LayoutParams(64,64))
   val name=label(entry.loadLabel(pm).toString(),13,true).apply { gravity=Gravity.CENTER; maxLines=1; ellipsize=android.text.TextUtils.TruncateAt.END; setPadding(0,9,0,0) }
   card.addView(name,LinearLayout.LayoutParams(-1,-2))
   card.setOnFocusChangeListener { v, focused -> v.alpha=if(focused) 1f else .82f; v.scaleX=if(focused) 1.06f else 1f; v.scaleY=if(focused) 1.06f else 1f; v.background=shape(if(focused) Color.rgb(133,48,136) else Color.rgb(35,35,59)) }
   card.setOnClickListener { try { val launch = pm.getLeanbackLaunchIntentForPackage(entry.activityInfo.packageName) ?: pm.getLaunchIntentForPackage(entry.activityInfo.packageName) ?: Intent().setClassName(entry.activityInfo.packageName,entry.activityInfo.name); startActivity(launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) } catch (_:Exception) { Toast.makeText(this,"Não foi possível abrir o aplicativo",Toast.LENGTH_SHORT).show() } }
   apps.addView(card,LinearLayout.LayoutParams(156,124).apply { rightMargin=14 })
  }
  if(entries.isEmpty()) apps.addView(label("Nenhum aplicativo de TV encontrado",17))
  scroll.addView(apps); root.addView(scroll,LinearLayout.LayoutParams(-1,140))
  val footer=Button(this).apply { text="⚙  Configurações do Android"; isAllCaps=false; setOnClickListener { startActivity(Intent(Settings.ACTION_SETTINGS)) } }
  root.addView(footer,LinearLayout.LayoutParams(260,55).apply { topMargin=8 })
  setContentView(root); if(apps.childCount>0) apps.getChildAt(0).requestFocus()
  val update = object:Runnable { override fun run() { clock.text=SimpleDateFormat("HH:mm  •  EEE, dd MMM",Locale("pt","BR")).format(Date()); handler.postDelayed(this,30000) } }
  handler.post(update)
 }
 override fun onDestroy() { handler.removeCallbacksAndMessages(null); super.onDestroy() }
}
