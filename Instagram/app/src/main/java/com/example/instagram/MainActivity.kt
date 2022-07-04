package com.example.instagram

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.instagram.databinding.ActivityMainBinding
import com.example.instagram.navigation.*
import java.util.jar.Manifest

class MainActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 1)
        binding.bottomNav.run {
            setOnItemSelectedListener { item ->
                when(item.itemId){
                    R.id.action_home -> {
                        var detailViewFragment = DetailViewFragment()
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.main_content, detailViewFragment).commit()
                    }
                    R.id.ic_search -> {
                        var GridFragment = GridFragment()
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.main_content, GridFragment).commit()
                    }
                    R.id.action_add_photo -> {
                        if(ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                            startActivity(Intent(context, AddPhotoActivity::class.java))
                        }
                    }
                    R.id.action_favorite_alarm -> {
                        var alarmFragment = AlarmFragment()
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.main_content, alarmFragment).commit()
                    }

                    R.id.action_account -> {
                        var userFragment = UserFragment()
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.main_content, userFragment).commit()
                    }
                }
                true
            }
        }
    }
}