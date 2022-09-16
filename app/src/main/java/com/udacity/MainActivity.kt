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

    private lateinit var download_Manager: DownloadManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var notification_Manager: NotificationManager
    private var download_ID: Long ?= null
    private var downloadedFileName :String ?= null
    private var downloadStatus : String ?= null
    private var downloadedFileNumber : Int ?= null
    private var url: String ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        setOnClickOnCustomDownloadButton() //Set On Click On Custom Button

    }

    private fun setOnClickOnCustomDownloadButton() {
        customDownloadButton_main.setOnClickListener {
            val checkedRadioId = radioGroupOption_main.checkedRadioButtonId
            if(checkedRadioId != -1)  //If One Of RadioButtons Checked
            {
                val radioButton: RadioButton = radioGroupOption_main.findViewById(checkedRadioId)
                val index = radioGroupOption_main.indexOfChild(radioButton)
                downloadedFileName = radioButton.text.toString()
                downloadedFileNumber = index + 1
                url = when (index) {
                    0 -> GLIDE_URL
                    1 -> LOAD_URL
                    2 -> RETROFIT_URL
                    else -> ""
                }
                checkInternetAndSendNotification()

            }else{
                customDownloadButton_main.buttonState = ButtonState.Completed
                Toast.makeText(this, R.string.select, Toast.LENGTH_SHORT).show()

            }

        }
    }

    private fun checkInternetAndSendNotification() {
        if (isInternetOnline(this)) { //Check Internet Connection
            customDownloadButton_main.buttonState = ButtonState.Loading
            try {
                download()
                createChannel() //Create Channel
            } catch (e: Exception) {
                Toast.makeText(this, getString(R.string.There_was_an_error_loading_Try_again_please), Toast.LENGTH_SHORT).show()
                Log.d("MainActivity", e.toString())
            }

        }else{
            customDownloadButton_main.buttonState = ButtonState.Completed
            Toast.makeText(this, getString(R.string.Check_internet_connection), Toast.LENGTH_SHORT).show()
        }

    }

    private val receiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)

            url = ""
            customDownloadButton_main.buttonState = ButtonState.Completed


            if (id != null) {
                val query = DownloadManager.Query()
                    .setFilterById(id)

                val cursor = download_Manager.query(query)

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
                notification_Manager = ContextCompat.getSystemService(this@MainActivity, NotificationManager::class.java) as NotificationManager
                //sendNotification
                notification_Manager.sendNotification("The Project $downloadedFileNumber repository is $downloadStatus", CHANNEL_ID, applicationContext
                )

            }

        }
    }


    private fun download() {
        val request =
            DownloadManager.Request(Uri.parse(url))
                .setTitle(getString(R.string.notification_title))
                .setDescription(getString(R.string.app_description))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)
        //Initialize downloadManager
        download_Manager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        download_ID = download_Manager.enqueue(request)// enqueue puts the download request in the queue.



    }

    private fun isInternetOnline(context: Context): Boolean {
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
        url = ""

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
        private const val NOTIFICATION_ID = 0
        private const val CHANNEL_ID = "my_channelId"

    }

}