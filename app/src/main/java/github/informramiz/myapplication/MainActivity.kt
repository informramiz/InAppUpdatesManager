package github.informramiz.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import github.informramiz.inappupdatesmanager.base.InAppUpdatesManager
import github.informramiz.myapplication.ui.main.MainFragment

class MainActivity : AppCompatActivity() {

    private var inAppUpdatesManager: InAppUpdatesManager? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        inAppUpdatesManager = InAppUpdatesManager(this)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, MainFragment.newInstance())
                .commitNow()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (inAppUpdatesManager?.isInAppUpdateActivityResult(requestCode) == true) {
            inAppUpdatesManager?.onActivityResult(requestCode, resultCode)
        }
    }
}
