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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.lz_saigao.snake_tab.SnakeScrollTab

class SnakeScrollTabActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val pagerData = mutableListOf<String>()
            val tabData = mutableListOf<String>()

            for (index in 1..20) {
                pagerData.add("${index}--page")
                tabData.add("tab$index")
            }

            val pagerWidth = this.resources.displayMetrics.widthPixels
            ShowContent(pagerData, tabData, pagerWidth)
        }
    }

    @OptIn(ExperimentalPagerApi::class)
    @Composable
    fun ShowContent(pagerData: List<String>, tabData: List<String>, pagerWidth: Int) {
        val pagerState = rememberPagerState()
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
                // 触发来源于pagerState
                tabSelectIndex = pagerState.currentPage
            } else {
                // 触发来源于tabSelect
                pagerState.animateScrollToPage(tabSelectIndex)
            }
            indicatorScrollPercentage = 0f
            lastTabSelectIndex = tabSelectIndex
        })
        val nestedScroll = remember {
            object : NestedScrollConnection {
                override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                    indicatorScrollPercentage += available.x / pagerWidth
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
            SnakeScrollTab(
                Modifier.background(Color.Blue),
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
                tabContentPadding = 10.dp,
                tabScrollPosition = pagerWidth / 2,
                indicator = {
                    Box(
                        Modifier
                            .width(80.dp)
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