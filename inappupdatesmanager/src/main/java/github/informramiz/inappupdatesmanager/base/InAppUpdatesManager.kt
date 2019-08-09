package github.informramiz.inappupdatesmanager.base

import android.app.Activity
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.model.*
import github.informramiz.inappupdatesmanager.R


/**
 * Created by Ramiz Raja on 2019-05-30.
 */
class InAppUpdatesManager(private val activity: Activity,
                          private val listener: ((event: InAppUpdateEvent) -> Unit)? = null) : LifecycleObserver {
    companion object {
        private const val REQ_CODE_UPDATE_APP_IMMEDIATELY = 10110
        private const val REQ_CODE_UPDATE_APP_FLEXIBLY = 10120

        fun fetchRemoteValues() {
            InAppUpdateManagerRemoteConfig.fetchAndActivate()
        }
    }

    private val appUpdateManager = AppUpdateManagerFactory.create(activity)
    private val lifecycleOwner = activity as LifecycleOwner
    private var requestedUpdateType: Int = AppUpdateType.IMMEDIATE

    private val installStateListener = { installState: InstallState ->
        if (installState.installErrorCode() != InstallErrorCode.NO_ERROR) {
            //some error occurred
            val errorMessage = activity.getString(R.string.error_msg_update_failed, installState.installErrorCode())
            Toast.makeText(activity, errorMessage, Toast.LENGTH_LONG).show()
            handleUpdateFailure(requestedUpdateType)
        } else {
            when (installState.installStatus()) {
                InstallStatus.DOWNLOADED -> {
                    if (requestedUpdateType == AppUpdateType.FLEXIBLE) showSnackbarToCompleteUpdate()
                }
            }
        }
        when (installState.installStatus()) {
            InstallStatus.DOWNLOADED -> showSnackbarToCompleteUpdate()
        }
    }

    init {
        lifecycleOwner.lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    private fun onCreate() {
        checkIfUpdateAvailable()
    }

    private fun checkIfUpdateAvailable() {
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                onUpdateAvailable(appUpdateInfo)
            }
        }
    }

    private fun onUpdateAvailable(appUpdateInfo: AppUpdateInfo) {
        if (InAppUpdateManagerRemoteConfig.doImmediateUpdate(appUpdateInfo.availableVersionCode())) {
            if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                startImmediateUpdate(appUpdateInfo)
            }
        } else {
            startFlexibleUpdate(appUpdateInfo)
        }
    }

    private fun startFlexibleUpdate(appUpdateInfo: AppUpdateInfo) {
        requestedUpdateType = AppUpdateType.FLEXIBLE
        appUpdateManager.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.FLEXIBLE,
            activity, REQ_CODE_UPDATE_APP_FLEXIBLY
        )
    }

    private fun startImmediateUpdate(appUpdateInfo: AppUpdateInfo) {
        requestedUpdateType = AppUpdateType.IMMEDIATE
        appUpdateManager.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.IMMEDIATE,
            activity, REQ_CODE_UPDATE_APP_IMMEDIATELY
        )
    }

    /**
     * This is to check if any update is not pending and stalled.
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private fun onResume() {
        checkForPendingUpdates()
    }

    private fun checkForPendingUpdates() {
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                //this happens only for immediate update, as immediate update is already running,
                // let's resume it
                onUpdateAvailable(appUpdateInfo)
            } else if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                //this happens only for flexible updates, prompt the user to update
                showSnackbarToCompleteUpdate()
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private fun onStart() {
        appUpdateManager.registerListener(installStateListener)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private fun onStop() {
        appUpdateManager.unregisterListener(installStateListener)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private fun onDestroy() {
        lifecycleOwner.lifecycle.removeObserver(this)
    }

    fun isInAppUpdateActivityResult(requestCode: Int): Boolean {
        return requestCode in arrayOf(REQ_CODE_UPDATE_APP_IMMEDIATELY, REQ_CODE_UPDATE_APP_IMMEDIATELY)
    }

    fun onActivityResult(requestCode: Int, resultCode: Int) {
        if (!isInAppUpdateActivityResult(requestCode)
            || resultCode == Activity.RESULT_OK) return

        if (requestCode == REQ_CODE_UPDATE_APP_IMMEDIATELY) {
            handleImmediateUpdateFailure(resultCode)
        } else {
            handleFlexibleUpdateFailure(resultCode)
        }
    }

    private fun handleUpdateFailure(updateType: Int) {
        when (updateType) {
            AppUpdateType.FLEXIBLE -> handleFlexibleUpdateFailure(ActivityResult.RESULT_IN_APP_UPDATE_FAILED)
            else -> handleImmediateUpdateFailure(ActivityResult.RESULT_IN_APP_UPDATE_FAILED)
        }
    }

    private fun handleImmediateUpdateFailure(resultCode: Int) {
        val event = if (resultCode == Activity.RESULT_CANCELED) {
            InAppUpdateEvent.ERROR_IMMEDIATE_UPDATE_CANCELLED_BY_USER
        } else {
            InAppUpdateEvent.ERROR_IMMEDIATE_UPDATE_FAILED
        }
        listener?.invoke(event) ?: run {
            Toast.makeText(activity,
                    activity.getString(R.string.error_must_update_app_before_using),
                    Toast.LENGTH_SHORT
            ).show()
            activity.finish()
        }
    }

    private fun handleFlexibleUpdateFailure(resultCode: Int) {
        val event = if (resultCode == Activity.RESULT_CANCELED) {
            InAppUpdateEvent.ERROR_FLEXIBLE_UPDATE_CANCELLED_BY_USER
        } else {
            InAppUpdateEvent.ERROR_FLEXIBLE_UPDATE_FAILED
        }
        listener?.invoke(event)
    }

    private fun showSnackbarToCompleteUpdate() {
        val view = activity.findViewById<View>(android.R.id.content)
        val snackbar = Snackbar.make(
            view,
            activity.getString(R.string.msg_update_downloaded),
            Snackbar.LENGTH_INDEFINITE)
        snackbar.setAction(R.string.restart) {
            appUpdateManager.completeUpdate()
        }

        snackbar.show()
    }
}