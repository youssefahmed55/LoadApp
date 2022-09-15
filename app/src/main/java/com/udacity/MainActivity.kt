package com.udacity

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.lang.Exception


class MainActivity : AppCompatActivity() {

    private var downloadID: Long = 0
    private var loadURL: String = ""

    private lateinit var notificationManager: NotificationManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var downloadManager: DownloadManager
    private var downloadStatus = ""
    private var downloadedFileNumber :Int= 0
    private var downloadedFileName = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        setOnClickOnCustomButton() //Set On Click On Custom Button
        setOnClickOnRadioGroup()   //Set On Click On Radio Group Items

    }

    private fun setOnClickOnRadioGroup() {
        radioGroupOption_main.setOnCheckedChangeListener { _, checkedId ->
            custom_button.buttonState = ButtonState.Completed
            val radioButton: RadioButton = radioGroupOption_main.findViewById(checkedId)
            val index = radioGroupOption_main.indexOfChild(radioButton)
            downloadedFileName = radioButton.text.toString()
            downloadedFileNumber = index + 1
            loadURL = when (index) {
                0 -> GLIDE_URL
                1 -> LOAD_URL
                2 -> RETROFIT_URL
                else -> ""
            }
        }
    }

    private fun setOnClickOnCustomButton() {
        custom_button.setOnClickListener {
            custom_button.buttonState = ButtonState.Clicked
            if (loadURL.isEmpty()) {
                Toast.makeText(this, R.string.select, Toast.LENGTH_SHORT).show()
                custom_button.buttonState = ButtonState.Disabled
            } else {
                if (isOnline(this)) { //Check Internet Connection
                    custom_button.buttonState = ButtonState.Loading
                    try {
                        download()
                    } catch (e: Exception) {
                        Log.e("MainActivity", e.toString())
                        Toast.makeText(
                            this,
                            getString(R.string.There_was_an_error_loading_Try_again_please),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    custom_button.buttonState = ButtonState.Completed
                    Toast.makeText(this, getString(R.string.Check_internet_connection), Toast.LENGTH_SHORT).show()
                }
            }

            createChannel() //Create Channel

        }
    }

    private val receiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)

            loadURL = ""
            downloadGlide_main.isChecked = false
            downloadRetrofit_main.isChecked = false
            downloadLoadApp_main.isChecked = false
            custom_button.buttonState = ButtonState.Disabled


            if (id != null) {
                val query = DownloadManager.Query()
                    .setFilterById(id)

                val cursor = downloadManager.query(query)

                if (cursor.moveToFirst()) {
                    val status: Int = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                    cursor.close()

                    downloadStatus = if (status == DownloadManager.STATUS_SUCCESSFUL) {
                        "downloaded Success"
                    } else {
                        "downloaded Failed"
                    }
                }
                //Initialize notificationManager
                notificationManager = ContextCompat.getSystemService(this@MainActivity, NotificationManager::class.java) as NotificationManager
                //sendNotification
                notificationManager.sendNotification("The Project $downloadedFileNumber repository is $downloadStatus", CHANNEL_ID, applicationContext
                )

            }

        }
    }


    private fun download() {
        val request =
            DownloadManager.Request(Uri.parse(loadURL))
                .setTitle(getString(R.string.notification_title))
                .setDescription(getString(R.string.app_description))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)
        //Initialize downloadManager
        downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadID = downloadManager.enqueue(request)// enqueue puts the download request in the queue.



    }

    private fun isOnline(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (capabilities != null) {
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                return true
            }
        }
        return false
    }

    fun NotificationManager.sendNotification(
        message: String,
        channelId: String,
        applicationContext: Context
    ) {
        //Initialize Notification builder
        val builder = NotificationCompat.Builder(
            applicationContext,
            channelId
        )

        //Initialize detailIntent
        val detailIntent = Intent(applicationContext, DetailActivity::class.java)
        detailIntent.putExtra("File_Name", downloadedFileName)
        detailIntent.putExtra("STATUS", downloadStatus)
        detailIntent.putExtra("NOTIFICATION_ID", NOTIFICATION_ID)
        pendingIntent = PendingIntent.getActivity(applicationContext, NOTIFICATION_ID, detailIntent, PendingIntent.FLAG_UPDATE_CURRENT)


        builder
            .setSmallIcon(R.drawable.ic_assistant_black_24dp)
            .setContentTitle(
                getString(R.string.notification_title)
            )
            .setContentText(message)
            .setChannelId(channelId)
            .addAction(
                R.drawable.ic_launcher_foreground,
                getString(R.string.notification_button),
                pendingIntent
            )


        notify(NOTIFICATION_ID, builder.build())


        downloadedFileName = ""
        downloadedFileNumber = 0
        downloadStatus = ""
        loadURL = ""

    }


    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                "MyChannelID",
                NotificationManager.IMPORTANCE_HIGH
            )

            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.GREEN
            notificationChannel.enableVibration(true)
            notificationChannel.description = "File Downloaded Completely"


            val notificationManager = getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(notificationChannel)

        }
    }

    override fun onDestroy() {
        unregisterReceiver(receiver)
        super.onDestroy()
    }

    companion object {
        private const val GLIDE_URL = "https://github.com/bumptech/glide/archive/master.zip"
        private const val LOAD_URL = "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
        private const val RETROFIT_URL = "https://github.com/square/retrofit/archive/master.zip"
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "my_channelId"

    }

}