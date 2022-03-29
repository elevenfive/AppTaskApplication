package com.example.apptaskapplication

import android.app.ActivityManager
import android.app.ActivityManager.TaskDescription
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.apptaskapplication.databinding.ActivityBinding

private const val TAG = "SecondaryActivity"

class SecondaryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
    }

    override fun onResume() {
        super.onResume()

        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val addAppTaskIntent = Intent(this, MainActivity::class.java)
        addAppTaskIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
        addAppTaskIntent.addFlags(Intent.FLAG_ACTIVITY_RETAIN_IN_RECENTS)

        val appTaskThumbnailSize = activityManager.appTaskThumbnailSize

        Log.d(TAG, "appTaskThumbnailSize: $appTaskThumbnailSize")

        val bitmap = Bitmap.createBitmap(
            appTaskThumbnailSize.width, appTaskThumbnailSize.height, Bitmap.Config.ARGB_8888)

        activityManager.addAppTask(this, addAppTaskIntent, TaskDescription(), bitmap)

        val appTaskList = activityManager.appTasks

        val intent = Intent(this, MainActivity::class.java)

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
        //    at com.example.apptaskapplication.SecondaryActivity.onResume(SecondaryActivity.kt:59)
        appTaskList[0].startActivity(this, intent, null)

        // After launching the activity once, and failing above, a second launch will see
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
        //    at com.example.apptaskapplication.SecondaryActivity.onResume(SecondaryActivity.kt:75)
        appTaskList.forEach { it.finishAndRemoveTask() }
    }
}
