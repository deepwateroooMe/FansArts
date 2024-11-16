package com.me.fansarts

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lz_saigao.snake_tab.TabAlignment
import com.lz_saigao.snake_tab.TabAxisAlignment

// 【改装】：成了 SplashScreenActivity... 闪一秒。。
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SnakeTabSetting.tabAxisAlignment= TabAxisAlignment.SpaceEvenly
        startActivity(Intent(this@MainActivity, SnakeRowTabActivity::class.java))
        // SnakeTabSetting.tabAlignment = TabAlignment.Bottom
        // startActivity(Intent(this@MainActivity, SnakeScrollTabActivity::class.java))
    }

    override fun onResume() {
        super.onResume()
        SnakeTabSetting.moveByOther=false
    }
}


