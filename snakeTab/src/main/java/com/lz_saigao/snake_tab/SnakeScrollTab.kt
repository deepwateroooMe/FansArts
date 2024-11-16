package com.lz_saigao.snake_tab

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.AlignmentLine
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import java.lang.RuntimeException
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * @author lzSaiGao
 * @exception SnakeScrollTab it's a could scroll tab
 * @param modifier
 * @param selectIndex the index when tab be selected
 * @param tabs all Tab
 * @param tabAlignment  tabs self alignment
 * @param tabContentPadding the padding in every tab
 * @param tabScrollPosition the location when tab scroll
 * @param indicator
 * @param indicatorRule the indicator width rule
 * @param indicatorOffset
 * @param indicatorScrollPercentage when indicator need scroll between tab,you need give a move percentage
 */
@Composable
fun SnakeScrollTab(
    modifier: Modifier = Modifier,
    selectIndex: Int,
    tabs: @Composable () -> Unit,
    tabAlignment: TabAlignment = TabAlignment.Center,
    tabContentPadding: Dp = 10.dp,
    tabScrollPosition: Int = 0,
    indicator: @Composable () -> Unit = @Composable {
        Box(
            Modifier
                .width(2.dp)
                .height(4.dp)
                .background(Color.Red)
        )
    },
    indicatorRule: IndicatorRule = IndicatorRule.FillTab,
    indicatorOffset: DpOffset = DpOffset(0.dp, 0.dp),
    indicatorScrollPercentage: Float = 0f,
) {

    val scrollState = rememberScrollState()
    var needScrollPosition by remember {
        mutableStateOf(0)
    }

    LaunchedEffect(key1 = needScrollPosition, block = {
        scrollState.animateScrollTo(needScrollPosition)
    })

    SubcomposeLayout(
        modifier.horizontalScroll(scrollState)
    ) { constraints ->
        val tabConstraints = constraints.copy()
        val indicatorConstraints = constraints.copy()

        val tabMeasurableList = subcompose("Tab", tabs)

        val indicatorMeasurableList = subcompose("Indicator", indicator)

        var tabWidth = 0
        var maxTabHeight = 0
        var maxTabBaseLine = 0
        val tabPlaceableList = mutableListOf<Placeable>()

        tabMeasurableList.forEach {
            val tabPlaceable = it.measure(tabConstraints)
            tabWidth += tabPlaceable.width
            maxTabHeight = maxOf(maxTabHeight, tabPlaceable.height)
            if (tabPlaceable[FirstBaseline] != AlignmentLine.Unspecified) {
                val firstBaseLine = tabPlaceable[FirstBaseline]
                maxTabBaseLine = maxOf(maxTabBaseLine, firstBaseLine)
            }
            tabPlaceableList.add(tabPlaceable)
        }

        val indicatorPlaceable =
            indicatorMeasurableList[0].measure(
                if (indicatorRule == IndicatorRule.FillTab)
                    indicatorConstraints.copy(
                        minWidth = tabPlaceableList[selectIndex].width,
                        maxWidth = tabPlaceableList[selectIndex].width
                    )
                else
                    indicatorConstraints
            )

        layout(
            tabWidth + (tabPlaceableList.size - 1) * (tabContentPadding.toPx()
                .roundToInt()) + indicatorOffset.x.toPx()
                .roundToInt() ,
            maxTabHeight + indicatorPlaceable.height + (if(indicatorOffset.y>0.dp)indicatorOffset.y.toPx().roundToInt() else 0)
        ) {
            var tabRelativeX = 0
            val tabPositionList = mutableListOf<Int>()

            tabPlaceableList.forEach {
                var tabRelativeY = 0
                if (it[FirstBaseline] != AlignmentLine.Unspecified) {
                    tabRelativeY = when (tabAlignment) {
                        TabAlignment.Bottom -> maxTabHeight - it[FirstBaseline]
                        TabAlignment.Center -> {
                            val maxContentHeight =
                                maxTabHeight - (maxTabHeight - maxTabBaseLine) * 2
                            val itemContentHeight =
                                it.height - (it.height - it[FirstBaseline]) * 2
                            val contentMoveY = (maxContentHeight - itemContentHeight) / 2
                            (maxTabHeight - maxTabBaseLine) - (it.height - it[FirstBaseline]) + contentMoveY
                        }
                        TabAlignment.Top -> (maxTabHeight - maxTabBaseLine) - (it.height - it[FirstBaseline])
                    }
                }
                it.placeRelative(tabRelativeX, tabRelativeY)
                tabPositionList.add(tabRelativeX)
                tabRelativeX += it.width + tabContentPadding.toPx().roundToInt()
            }

            val indicatorRelativeX =
                if (indicatorRule == IndicatorRule.FillTab)
                    tabPositionList[selectIndex] + indicatorOffset.x.toPx()
                        .roundToInt()
                else
                    tabPositionList[selectIndex] + indicatorOffset.x.toPx()
                        .roundToInt() + (tabPlaceableList[selectIndex].width - indicatorPlaceable.width) / 2

            val indicatorRelativeY = maxTabHeight + indicatorOffset.y.toPx().roundToInt()

            val futureIndicatorDistance = when (selectIndex) {
                0 -> {
                    if (indicatorScrollPercentage > 0) {
                        0
                    } else {
                        val indicatorRelativeXNext =
                            if (indicatorRule == IndicatorRule.FillTab)
                                tabPositionList[selectIndex + 1] + indicatorOffset.x.toPx()
                                    .roundToInt()
                            else
                                tabPositionList[selectIndex + 1] + indicatorOffset.x.toPx()
                                    .roundToInt() + (tabPlaceableList[selectIndex + 1].width - indicatorPlaceable.width) / 2
                        abs(indicatorRelativeXNext - indicatorRelativeX)
                    }
                }
                tabPlaceableList.size - 1 -> {
                    if (indicatorScrollPercentage < 0) {
                        0
                    } else {
                        val indicatorRelativeXNext =
                            if (indicatorRule == IndicatorRule.FillTab)
                                tabPositionList[selectIndex - 1] + indicatorOffset.x.toPx()
                                    .roundToInt()
                            else
                                tabPositionList[selectIndex - 1] + indicatorOffset.x.toPx()
                                    .roundToInt() + (tabPlaceableList[selectIndex - 1].width - indicatorPlaceable.width) / 2
                        abs(indicatorRelativeXNext - indicatorRelativeX)
                    }
                }
                else -> {
                    if (indicatorScrollPercentage > 0) {
                        val indicatorRelativeXNext =
                            if (indicatorRule == IndicatorRule.FillTab)
                                tabPositionList[selectIndex + 1] + indicatorOffset.x.toPx()
                                    .roundToInt()
                            else
                                tabPositionList[selectIndex + 1] + indicatorOffset.x.toPx()
                                    .roundToInt() + (tabPlaceableList[selectIndex + 1].width - indicatorPlaceable.width) / 2
                        abs(indicatorRelativeXNext - indicatorRelativeX)
                    } else {
                        val indicatorRelativeXNext =
                            if (indicatorRule == IndicatorRule.FillTab)
                                tabPositionList[selectIndex - 1] + indicatorOffset.x.toPx()
                                    .roundToInt()
                            else
                                tabPositionList[selectIndex - 1] + indicatorOffset.x.toPx()
                                    .roundToInt() + (tabPlaceableList[selectIndex - 1].width - indicatorPlaceable.width) / 2
                        abs(indicatorRelativeXNext - indicatorRelativeX)
                    }
                }
            }

            indicatorPlaceable.placeRelative(
                indicatorRelativeX + (futureIndicatorDistance * (-indicatorScrollPercentage)).roundToInt(),
                indicatorRelativeY
            )

            needScrollPosition =
                tabPositionList[selectIndex] - tabScrollPosition + tabPlaceableList[selectIndex].width / 2
        }
    }

}