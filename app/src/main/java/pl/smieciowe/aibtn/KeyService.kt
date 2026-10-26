package pl.smieciowe.aibtn

import android.accessibilityservice.AccessibilityService
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent

class KeyService : AccessibilityService() {
    companion object {
        var inst: KeyService? = null
        fun screenshot() = inst?.performGlobalAction(GLOBAL_ACTION_TAKE_SCREENSHOT) ?: false
    }
    override fun onServiceConnected() { inst = this }
    override fun onDestroy() { inst = null; super.onDestroy() }
    override fun onAccessibilityEvent(e: AccessibilityEvent?) {}
    override fun onInterrupt() {}

    override fun onKeyEvent(e: KeyEvent): Boolean {
        val p = Actions.prefs(this)
        if (p.getBoolean("learn", false) && e.action == KeyEvent.ACTION_UP) {
            p.edit().putInt("key", e.keyCode).putBoolean("learn", false).apply()
            android.widget.Toast.makeText(this, "Zapamiętano klawisz ${KeyEvent.keyCodeToString(e.keyCode)}", 0).show()
            return true
        }
        val key = p.getInt("key", -1)
        if (key != -1 && e.keyCode == key) {
            if (e.action == KeyEvent.ACTION_UP) Actions.run(this)
            return true
        }
        return false
    }
}
