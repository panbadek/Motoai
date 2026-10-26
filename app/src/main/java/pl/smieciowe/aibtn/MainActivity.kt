package pl.smieciowe.aibtn

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.*

class MainActivity : Activity() {
    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        val p = Actions.prefs(this)
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(48, 96, 48, 48) }
        root.addView(TextView(this).apply { text = "Przycisk AI – wybierz akcję"; textSize = 22f })

        val group = RadioGroup(this)
        Actions.list.forEachIndexed { i, (id, label) ->
            group.addView(RadioButton(this).apply { this.id = i + 1; text = label; textSize = 17f; isChecked = id == Actions.selected(this@MainActivity) })
        }
        group.setOnCheckedChangeListener { _, cid ->
            val id = Actions.list[cid - 1].first
            p.edit().putString("action", id).apply()
            if (id == "app") pickApp()
            if (id == "url") askUrl()
        }
        root.addView(group)

        fun btn(t: String, f: () -> Unit) = root.addView(Button(this).apply { text = t; setOnClickListener { f() } })
        btn("▶ Testuj akcję") { Actions.run(this) }
        btn("1. Ustaw jako domyślnego asystenta") { startActivity(Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)) }
        btn("2. Włącz usługę dostępności") { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
        btn("3. Naucz klawisz (potem naciśnij przycisk AI)") {
            p.edit().putBoolean("learn", true).apply()
            Toast.makeText(this, "Naciśnij teraz przycisk AI", Toast.LENGTH_LONG).show()
        }
        btn("Dostęp do trybu cichego") { startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)) }
        setContentView(ScrollView(this).apply { addView(root) })
    }

    private fun pickApp() {
        val pm = packageManager
        val apps = pm.queryIntentActivities(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), 0)
            .sortedBy { it.loadLabel(pm).toString() }
        AlertDialog.Builder(this).setTitle("Wybierz aplikację")
            .setItems(apps.map { it.loadLabel(pm) }.toTypedArray()) { _, i ->
                Actions.prefs(this).edit().putString("app_pkg", apps[i].activityInfo.packageName).apply()
            }.show()
    }

    private fun askUrl() {
        val e = EditText(this).apply { setText(Actions.prefs(context).getString("url", "https://")) }
        AlertDialog.Builder(this).setTitle("Adres URL").setView(e)
            .setPositiveButton("OK") { _, _ -> Actions.prefs(this).edit().putString("url", e.text.toString()).apply() }.show()
    }
}
