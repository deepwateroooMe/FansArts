package com.me.fansarts

import android.annotation.SuppressLint
import androidx.annotation.DrawableRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search

data class AlbumFileTypeTab(
    // @DrawableRes val icon: Int,
    val name: String,
) {
    // 默认数据
    object LocalState {
        // @SuppressLint("SupportAnnotationUsage")
        val defaultTabs = listOf(
            // AlbumFileTypeTab(icon = R.drawable.ic_total, name = "全部"),

            // AlbumFileTypeTab(icon = R.drawable.ic_tabbar_reels_small_default, name = "相册"),
            // AlbumFileTypeTab(icon = R.drawable.ic_label, name = "视频"),
            // "Home" -> Icons.Default.Home
            // "Search" -> Icons.Default.Search
            // "Profile"-> Icons.Default.Person
            // else -> Icons.Default.Build
            AlbumFileTypeTab(name = "全部"),
            AlbumFileTypeTab(name = "相册"),
            AlbumFileTypeTab(name = "视频"),
        )
        // 获取默认数据
        fun getTabList(): List<AlbumFileTypeTab> {
            return LocalState.defaultTabs
        }
    }
}