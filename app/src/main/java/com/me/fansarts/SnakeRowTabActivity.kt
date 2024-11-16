package com.me.fansarts

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.lz_saigao.snake_tab.SnakeRowTab
import com.lz_saigao.snake_tab.SnakeScrollTab

class SnakeRowTabActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // 【TODO：】重点：把每页内容，改装成，可滚动的第2 层Tab..
            val pagerData = mutableListOf<String>() // <<<<<<<<<<<<<<<<<<<< 
            val tabData = mutableListOf<String>()

            // for (index in 1..3) {
            //     pagerData.add("${index}--page")
            //     tabData.add("tab$index")
            // }
            var titles = listOf("Home","Info","About")
            for (index in 1..3) {
                pagerData.add("${index}--page")
                // tabData.add("tab$index")
                titles.elementAtOrNull(index-1)?.let { tabData.add(it) }
            } 
            val pagerWidth = with(LocalDensity.current){
                this@SnakeRowTabActivity.resources.displayMetrics.widthPixels.toDp()
            }
            ShowContent(pagerData, tabData,pagerWidth,pagerWidth)
        }
    }

    @OptIn(ExperimentalPagerApi::class)
    @Composable
    fun ShowContent(pagerData: List<String>, tabData: List<String>,tabWidth :Dp,pagerWidth:Dp){
        val pagerState = rememberPagerState()
        val pagerWidthPx = with(LocalDensity.current){
            pagerWidth.toPx()
        }
        var tabSelectIndex by remember {
            mutableStateOf(0)
        }
        var lastTabSelectIndex by remember {
            mutableStateOf(0)
        }
        var indicatorScrollPercentage by remember {
            mutableStateOf(0f)
        }
        LaunchedEffect(key1 = pagerState.currentPage + tabSelectIndex, block = {
            if (tabSelectIndex == lastTabSelectIndex) {
                //触发来源于pagerState
                tabSelectIndex = pagerState.currentPage
            } else {
                //触发来源于tabSelect
                pagerState.animateScrollToPage(tabSelectIndex)
            }
            indicatorScrollPercentage = 0f
            lastTabSelectIndex = tabSelectIndex
        })
        val nestedScroll = remember {
            object : NestedScrollConnection {
                override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                    indicatorScrollPercentage += available.x / pagerWidthPx
                    return Offset.Zero
                }
            }
        }
        Column(
            modifier = Modifier
                .background(Color.White)
                .fillMaxWidth()
                .fillMaxHeight()
        ) {
            SnakeRowTab(
                Modifier.background(Color.Blue),
                tabWidth=tabWidth,
                selectIndex = tabSelectIndex,
                tabs = {
                    tabData.forEachIndexed { index, s ->
                        Text(
                            text = s,
                            Modifier.clickable {
                                tabSelectIndex = index
                            },
                            fontSize = if (index == tabSelectIndex) 20.sp else 13.sp,
                            color = if (index == tabSelectIndex) Color.Red else Color.White
                        )
                    }
                },
                tabAlignment = SnakeTabSetting.tabAlignment,
                tabAxisAlignment=SnakeTabSetting.tabAxisAlignment,
                indicator = {
                    Box(
                        Modifier
                            .width(20.dp)
                            .height(4.dp)
                            .background(Color.Red)

                    )
                }, indicatorRule = SnakeTabSetting.indicatorRule,
                indicatorOffset = DpOffset(0.dp, 0.dp),
                indicatorScrollPercentage = if (SnakeTabSetting.moveByOther) indicatorScrollPercentage else 0f,
            )
            HorizontalPager(
                count = pagerData.size,
                modifier = Modifier
                    .background(Color.Gray)
                    .nestedScroll(nestedScroll),
                state = pagerState
            ) { index ->
                Text(text = pagerData[index], textAlign = TextAlign.Center, fontSize = 20.sp)

            }
        }
    }
}