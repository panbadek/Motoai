package pl.smieciowe.aibtn

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast

object Actions {
    val list = listOf(
        "torch" to "Latarka (wł./wył.)",
        "camera" to "Aparat",
        "silent" to "Tryb cichy / dźwięk",
        "screenshot" to "Zrzut ekranu",
        "assistant" to "Asystent głosowy",
        "search" to "Wyszukiwarka Google",
        "app" to "Otwórz wybraną aplikację",
        "url" to "Otwórz stronę / Home Assistant"
    )
    private var torchOn = false

    fun prefs(c: Context) = c.getSharedPreferences("cfg", Context.MODE_PRIVATE)
    fun selected(c: Context) = prefs(c).getString("action", "torch")!!

    fun run(c: Context) {
        val p = prefs(c)
        try {
            when (selected(c)) {
                "torch" -> {
                    val cm = c.getSystemService(CameraManager::class.java)
                    torchOn = !torchOn
                    cm.setTorchMode(cm.cameraIdList[0], torchOn)
                }
                "camera" -> start(c, Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA))
                "silent" -> {
                    val nm = c.getSystemService(NotificationManager::class.java)
                    if (!nm.isNotificationPolicyAccessGranted) {
                        toast(c, "Nadaj dostęp do 'Nie przeszkadzać' w aplikacji"); return
                    }
                    val am = c.getSystemService(AudioManager::class.java)
                    am.ringerMode = if (am.ringerMode == AudioManager.RINGER_MODE_NORMAL)
                        AudioManager.RINGER_MODE_SILENT else AudioManager.RINGER_MODE_NORMAL
                    toast(c, if (am.ringerMode == AudioManager.RINGER_MODE_NORMAL) "Dźwięk włączony" else "Wyciszono")
                }
                "screenshot" -> {
                    if (!KeyService.screenshot()) toast(c, "Włącz usługę dostępności Przycisk AI")
                }
                "assistant" -> start(c, Intent(Intent.ACTION_VOICE_COMMAND))
                "search" -> start(c, Intent(Intent.ACTION_WEB_SEARCH))
                "app" -> {
                    val pkg = p.getString("app_pkg", "") ?: ""
                    val i = c.packageManager.getLaunchIntentForPackage(pkg)
                    if (i == null) toast(c, "Wybierz aplikację w ustawieniach") else start(c, i)
                }
                "url" -> start(c, Intent(Intent.ACTION_VIEW, Uri.parse(p.getString("url", "https://google.com"))))
            }
        } catch (e: Exception) { toast(c, "Błąd: ${e.message}") }
    }

    private fun start(c: Context, i: Intent) = c.startActivity(i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    private fun toast(c: Context, s: String) = Toast.makeText(c, s, Toast.LENGTH_SHORT).show()
}
