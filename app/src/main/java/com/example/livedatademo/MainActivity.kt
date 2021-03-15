package com.example.livedatademo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels

class MainActivity : AppCompatActivity() {

    val vm by viewModels<TestViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


//        vm.user.observe(this) {
//            Toast.makeText(applicationContext, it, Toast.LENGTH_SHORT).show()
//        }

        findViewById<Button>(R.id.btnTest).setOnClickListener {
            vm.user.observe(this) {
                Toast.makeText(applicationContext, it, Toast.LENGTH_SHORT).show()
            }
        }
    }
}