package com.udacity

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.content_detail.*

class DetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setSupportActionBar(toolbar)

        val fileName = intent.getStringExtra("File_Name") //Receive File Name From intent
        val status = intent.getStringExtra("STATUS") //Receive status From intent


        statusTextViewValue_Detail.text = status!!.split(" ")[1] //Split To Get Failed Or Success

        if (status == "downloaded Failed") {
            statusTextViewValue_Detail.setTextColor(Color.RED)    //Set Status Text Color

        } else {
            statusTextViewValue_Detail.setTextColor(Color.GREEN)  //Set Status Text Color
        }

        fileTextViewValue_Detail.text = fileName //Set Text Of File Name

        okButton_Detail.setOnClickListener { finish() } //Set On Click On Back Button
    }

}
