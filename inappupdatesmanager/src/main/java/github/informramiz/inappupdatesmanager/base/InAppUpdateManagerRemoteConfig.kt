package github.informramiz.inappupdatesmanager.base

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.gson.Gson
import github.informramiz.inappupdatesmanager.common.fromJson
import github.informramiz.inappupdatesmanager.models.UpdateRemoteConfigInfo
import timber.log.Timber


/**
 * Created by Ramiz Raja on 2019-05-30.
 */
internal object InAppUpdateManagerRemoteConfig {
    var updateRemoteConfigKey = "androidAppUpdateConfig"
    var fetchInterval: Long = 6 * 60 * 60 //12 hours

    fun doImmediateUpdate(versionCode: Int): Boolean {
        val remoteConfigStr = FirebaseRemoteConfig.getInstance().getString(updateRemoteConfigKey)
        val remoteConfigInfo = Gson().fromJson<UpdateRemoteConfigInfo>(remoteConfigStr)

        return (versionCode >= remoteConfigInfo.versionCode
                && remoteConfigInfo.isForceUpdate)
    }

    fun fetchAndActivate() {
        val fetchAndActivateTask = getFirebaseRemoteConfig().fetchAndActivate()
        fetchAndActivateTask.addOnCompleteListener { fetchResult ->
            Timber.d("Remote Config: ${FirebaseRemoteConfig.getInstance().getString(updateRemoteConfigKey)}")
        }
    }

    private fun getFirebaseRemoteConfig(): FirebaseRemoteConfig {
        return FirebaseRemoteConfig.getInstance().apply {
            val configSettings = FirebaseRemoteConfigSettings.Builder()
                //12 hours
                .setMinimumFetchIntervalInSeconds(fetchInterval)
                .build()
            this.setConfigSettingsAsync(configSettings)
        }
    }
}