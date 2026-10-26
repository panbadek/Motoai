package pl.smieciowe.aibtn

import android.app.Activity
import android.os.Bundle

class AssistActivity : Activity() {
    override fun onCreate(b: Bundle?) { super.onCreate(b); Actions.run(applicationContext); finish() }
}
