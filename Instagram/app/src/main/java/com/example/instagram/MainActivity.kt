package com.example.instagram

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.instagram.databinding.ActivityMainBinding
import com.example.instagram.navigation.AlarmFragment
import com.example.instagram.navigation.DetailViewFragment
import com.example.instagram.navigation.GridFragment
import com.example.instagram.navigation.UserFragment

class MainActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.bottomNav.run {
            setOnItemSelectedListener { item ->
                when(item.itemId){
                    R.id.action_home -> {
                        var detailViewFragment = DetailViewFragment()
                        supportFragmentManager.beginTransaction().replace(R.id.main_content, detailViewFragment).commit()
                    }
                    R.id.ic_search -> {
                        var GridFragment = GridFragment()
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.main_content, GridFragment).commit()
                    }
                    R.id.action_add_photo -> {

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