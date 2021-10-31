package com.surendrasingh.connectivitytest

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import com.surendrasingh.connectivitytest.application.MyApplication
import com.surendrasingh.connectivitytest.databinding.ActivityMainBinding
import com.surendrasingh.connectivitytest.utils.ConnectionLiveData

class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ConnectionLiveData(this).observe(this, Observer {
            if (it){
                Snackbar.make(binding.container, "Internet connection available", Snackbar.LENGTH_LONG)
                    .setActionTextColor(Color.CYAN)
                    .show()
                findViewById<TextView>(R.id.textView).text = "Network available"
            }
            else{
                Snackbar.make(binding.container, "There is no active internet connection", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Ok") {

                    }
                    .setActionTextColor(Color.CYAN)
                    .show()

                findViewById<TextView>(R.id.textView).text = "Network unavailable"
            }
        })
    }
}