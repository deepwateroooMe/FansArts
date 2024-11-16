package com.lz_saigao.snake_tab

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.AlignmentLine
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * @author lzSaiGao
 * @exception SnakeRowTab is a fixed width Tab,and so you can set up it's axisAlignment
 * @param modifier
 * @param tabWidth the SnakeRowTab width,Please set it to be smaller than one screen
 * @param selectIndex the index when tab be selected
 * @param tabs all Tab
 * @param tabAlignment  tabs self alignment
 * @param tabAxisAlignment  tabs alignment in axis
 * @param indicator
 * @param indicatorRule the indicator width rule
 * @param indicatorOffset
 * @param indicatorScrollPercentage when indicator need scroll between tab,you need give a move percentage
 */
@Composable
fun SnakeRowTab(
    modifier: Modifier = Modifier,
    tabWidth: Dp,
    selectIndex: Int,
    tabs: @Composable () -> Unit,
    tabAlignment: TabAlignment = TabAlignment.Center,
    tabAxisAlignment: TabAxisAlignment = TabAxisAlignment.SpaceEvenly,
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

    SubcomposeLayout(modifier) { constraints ->
        val tabConstraints = constraints.copy()
        val indicatorConstraints = constraints.copy()
        val tabRealWidth = tabWidth.toPx().roundToInt()

        val tabMeasurableList = subcompose("Tab", tabs)

        val indicatorMeasurableList = subcompose("Indicator", indicator)

        var maxTabHeight = 0
        var maxTabBaseLine = 0
        val tabPlaceableList = mutableListOf<Placeable>()

        var tabTextTotalWidth = 0
        tabMeasurableList.forEach {
            val tabPlaceable = it.measure(tabConstraints)
            maxTabHeight = maxOf(maxTabHeight, tabPlaceable.height)
            if (tabPlaceable[FirstBaseline] != AlignmentLine.Unspecified) {
                val firstBaseLine = tabPlaceable[FirstBaseline]
                maxTabBaseLine = maxOf(maxTabBaseLine, firstBaseLine)
            }
            tabTextTotalWidth += tabPlaceable.width
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
            tabRealWidth,
            maxTabHeight + indicatorPlaceable.height + (if (indicatorOffset.y > 0.dp) indicatorOffset.y.toPx()
                .roundToInt() else 0)
        ) {
            var tabPositionList = mutableListOf<Int>()
            var tabRelativeX = 0

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

                when (tabAxisAlignment) {
                    TabAxisAlignment.SpaceEvenly -> {
                        val spaceEvenlyWidth =
                            (tabRealWidth - tabTextTotalWidth) / (tabPlaceableList.size + 1)
                        tabRelativeX += spaceEvenlyWidth
                        tabPositionList.add(tabRelativeX)
                        it.placeRelative(tabRelativeX, tabRelativeY)
                        tabRelativeX += it.width
                    }
                    TabAxisAlignment.SpaceBetween -> {
                        val spaceBetweenWidth =
                            (tabRealWidth - tabTextTotalWidth) / (tabPlaceableList.size - 1)
                        tabPositionList.add(tabRelativeX)
                        it.placeRelative(tabRelativeX, tabRelativeY)
                        tabRelativeX += (spaceBetweenWidth + it.width)
                    }
                    TabAxisAlignment.SpaceAround -> {
                        val spaceAroundWidth = tabRealWidth / (tabPlaceableList.size)
                        tabRelativeX += (spaceAroundWidth / 2 - it.width / 2)
                        tabPositionList.add(tabRelativeX)
                        it.placeRelative(tabRelativeX, tabRelativeY)
                        tabRelativeX += (spaceAroundWidth / 2 + it.width / 2)
                    }
                }
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

        }
    }
}