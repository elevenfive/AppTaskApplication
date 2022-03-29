package com.example.apptaskapplication

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import junit.framework.TestCase.fail
import org.junit.Test
import org.junit.runner.RunWith

private const val TAG = "AppTaskTest"

// After running this test, uninstalling the app causes a system reboot due to NPE:
//
// E/AndroidRuntime: *** FATAL EXCEPTION IN SYSTEM PROCESS: PackageManager
//    java.lang.NullPointerException: Attempt to invoke virtual method 'com.android.server.wm.ActivityRecord com.android.server.wm.TaskDisplayArea.topRunningActivity()' on a null object reference
//        at com.android.server.wm.ActivityRecord.destroyIfPossible(ActivityRecord.java:3345)
//        at com.android.server.wm.Task.lambda$performClearTask$3$Task(Task.java:1602)
//        at com.android.server.wm.Task$$ExternalSyntheticLambda23.accept(Unknown Source:6)
//        at com.android.server.wm.ActivityRecord.forAllActivities(ActivityRecord.java:4251)
//        at com.android.server.wm.WindowContainer.forAllActivities(WindowContainer.java:1448)
//        at com.android.server.wm.WindowContainer.forAllActivities(WindowContainer.java:1442)
//        at com.android.server.wm.Task.performClearTask(Task.java:1593)
//        at com.android.server.wm.ActivityTaskSupervisor.removeTask(ActivityTaskSupervisor.java:1574)
//        at com.android.server.wm.RecentTasks.removeTasksByPackageName(RecentTasks.java:679)
//        at com.android.server.wm.ActivityTaskManagerService$LocalService.removeRecentTasksByPackageName(ActivityTaskManagerService.java:6432)
//        at com.android.server.am.ActivityManagerService.broadcastIntentLocked(ActivityManagerService.java:13077)
//        at com.android.server.am.ActivityManagerService$LocalService.broadcastIntent(ActivityManagerService.java:15872)
//        at com.android.server.pm.PackageManagerService.doSendBroadcast(PackageManagerService.java:15790)
//        at com.android.server.pm.PackageManagerService.lambda$sendPackageBroadcast$40$PackageManagerService(PackageManagerService.java:15685)
//        at com.android.server.pm.PackageManagerService$$ExternalSyntheticLambda61.run(Unknown Source:22)
//        at android.os.Handler.handleCallback(Handler.java:938)
//        at android.os.Handler.dispatchMessage(Handler.java:99)
//        at android.os.Looper.loopOnce(Looper.java:201)
//        at android.os.Looper.loop(Looper.java:288)
//        at android.os.HandlerThread.run(HandlerThread.java:67)
//        at com.android.server.ServiceThread.run(ServiceThread.java:44)
//
@RunWith(AndroidJUnit4::class)
class AppTaskTest {

    @Test
    fun testAppTask() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val targetContext = instrumentation.targetContext
        val activityManager = targetContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val newActivityIntent = Intent(targetContext, MainActivity::class.java)
        newActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        val activity = instrumentation.startActivitySync(newActivityIntent)
        val addAppTaskIntent = Intent(targetContext, MainActivity::class.java)
        addAppTaskIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
        addAppTaskIntent.addFlags(Intent.FLAG_ACTIVITY_RETAIN_IN_RECENTS)

        val appTaskThumbnailSize = activityManager.appTaskThumbnailSize

        Log.d(TAG, "appTaskThumbnailSize: $appTaskThumbnailSize")

        val bitmap = Bitmap.createBitmap(
            appTaskThumbnailSize.width, appTaskThumbnailSize.height, Bitmap.Config.ARGB_8888)

        activityManager.addAppTask(activity, addAppTaskIntent,
            ActivityManager.TaskDescription("addAppTask", R.drawable.ic_launcher), bitmap)

        val appTaskList = activityManager.appTasks

        val intent = Intent(targetContext, MainActivity::class.java)

        // The next code line fails with an exception:
        //
        // java.lang.NullPointerException: Attempt to invoke virtual method
        //  'void com.android.server.wm.DisplayContent.layoutAndAssignWindowLayersIfNeeded()'
        //  on a null object reference
        //    at android.os.Parcel.createExceptionOrNull(Parcel.java:2431)
        //    at android.os.Parcel.createException(Parcel.java:2409)
        //    at android.os.Parcel.readException(Parcel.java:2392)
        //    at android.os.Parcel.readException(Parcel.java:2334)
        //    at android.app.IAppTask$Stub$Proxy.startActivity(IAppTask.java:301)
        //    at android.app.Instrumentation.execStartActivityFromAppTask(Instrumentation.java:2072)
        //    at android.app.ActivityManager$AppTask.startActivity(ActivityManager.java:4810)
        //    at com.example.apptaskapplication.AppTaskTest.testStartActivity(AppTaskTest.kt:58)
        appTaskList[0].startActivity(targetContext, intent, null)

        // After running the test once, and failing above, a second test run will see
        // startActivity succeed, but the next code line will then fail with an exception
        // (operating on the 2nd element of appTaskList):
        //
        // java.lang.NullPointerException: Attempt to invoke virtual method
        //  'com.android.server.wm.ActivityRecord com.android.server.wm.TaskDisplayArea.topRunningActivity()'
        //  on a null object reference
        //    at android.os.Parcel.createExceptionOrNull(Parcel.java:2431)
        //    at android.os.Parcel.createException(Parcel.java:2409)
        //    at android.os.Parcel.readException(Parcel.java:2392)
        //    at android.os.Parcel.readException(Parcel.java:2334)
        //    at android.app.IAppTask$Stub$Proxy.finishAndRemoveTask(IAppTask.java:214)
        //    at android.app.ActivityManager$AppTask.finishAndRemoveTask(ActivityManager.java:4751)
        //    at com.example.apptaskapplication.AppTaskTest.testAppTask(AppTaskTest.kt:101)
        appTaskList.forEach { it.finishAndRemoveTask() }
    }
}
