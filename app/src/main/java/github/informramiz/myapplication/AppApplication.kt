package github.informramiz.myapplication

import android.app.Application


/**
 * Created by Ramiz Raja on 2019-08-04.
 */
class AppApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        //call this if you have enabled firebase remote config to control
        //app update type
        //InAppUpdatesManager.fetchRemoteValues()
    }
}