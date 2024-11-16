package com.me.fansarts

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
// import androidx.compose.foundation.pager.HorizontalPager
// import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
// import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
// import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
// import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import coil.compose.AsyncImage // 注意，没有coil图片库的请先导入这个包
// import com.withhim.cc.R
// import com.withhim.cc.R.drawable
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

// 这个文件，方案，使用 material3, 链接起来可能不太方便。。
/**
 * 这是一个使用NestedScroll和Pager的样品，同时包含粘性标题（这里是Tabs）。
 * 我们使用的工具栏偏移量更改布局效果，同时模拟了粘性标题的吸顶效果。
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class,
    ExperimentalPagerApi::class
)
@Composable
fun NestedScrollWithPagerAndStickyHeaderSample() {
    val coroutineScope = rememberCoroutineScope()
    val density: Density = LocalDensity.current
    val context: Context = LocalContext.current
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs by remember { mutableStateOf(AlbumFileTypeTabUiState.LocalState.getTabUiState().tabs) }
    val pagerState = rememberPagerState { tabs.size }
    LaunchedEffect(pagerState) {
        // Collect from the a snapshotFlow reading the currentPage
        snapshotFlow { pagerState.currentPage }.collect { pageIndex ->
            // Do something with each page change, for example:
            // viewModel.sendPageSelectedEvent(page)
            selectedTabIndex = pageIndex
            Log.d("Page change", "Page changed to $pageIndex")
        }
    }
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        color = MaterialTheme.colorScheme.background
    ) {
        // 用于记录应用栏的高度，以便在嵌套滚动中使用。会在onSizeChanged中获取
        var toolbarHeight by remember { mutableStateOf(0.dp) }
        var tabBarHeight by remember { mutableStateOf(0.dp) }
        //val maxUpPx by mutableStateOf(with(LocalDensity.current) { toolbarHeight.roundToPx().toFloat() - 50.dp.roundToPx().toFloat() - 56.dp.roundToPx().toFloat() }) // 此处会导致负数然后运行报错，这是错误的写法
        // ToolBar 最大向上位移量
        var maxUpPx by remember { mutableStateOf(0f) } // 使用属性初始化，而不是直接在mutableStateOf中初始化
        // 当工具栏高度变化时更新maxUpPx，并确保它不是负的

        LaunchedEffect(toolbarHeight) {
            maxUpPx = with(density) {
                (toolbarHeight.roundToPx().toFloat() - tabBarHeight.roundToPx()
                    .toFloat() - topAppBarHeight.roundToPx().toFloat()).coerceAtLeast(0f)
            }
        }
        // ToolBar 最小向上位移量
        val minUpPx = 0f
        // 偏移折叠工具栏上移高度
        val toolbarOffsetHeightPx = remember { mutableStateOf(0f) }
        // 现在，让我们创建与嵌套滚动系统的连接并聆听子 LazyColumn 中发生的滚动
        val nestedScrollConnection = remember {
            object : NestedScrollConnection {
                // 1、工具栏完全可见时，向上滚动先移动工具栏。
                // 2、工具栏部分不可见时，任意方向滚动都先移动工具栏。
                // 3、工具栏完全不可见且处于最大偏移时，向上滚动应滚动 LazyColumn。
                // 4、工具栏完全可见时，向下滚动应滚动 LazyColumn。
                override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                    // 如果向上滚动，或者toolbar已经有偏移（但未达到最大值）
                    if (available.y < 0 && toolbarOffsetHeightPx.value > -maxUpPx) {
                        val delta = available.y
                        val newOffset = toolbarOffsetHeightPx.value + delta
                        toolbarOffsetHeightPx.value = newOffset.coerceIn(-maxUpPx, minUpPx)
                        // 消费掉所有的y轴上的滚动
                        return Offset(0f, delta)
                    }
                    // 如果toolbar偏移已经是最大值，且我们正在向上滚动，允许LazyColumn处理滚动事件
                    if (available.y < 0 && toolbarOffsetHeightPx.value == -maxUpPx) {
                        return Offset.Zero
                    }
                    // 如果工具栏完全可见，且向下滚动，直接返回 Offset.Zero 不消费事件，允许 LazyColumn 滚动
                    if (toolbarOffsetHeightPx.value == 0f && available.y > 0) {
                        return Offset.Zero
                    }
                    // 在其他情况下（向下滚动且工具栏有偏移），消费滚动事件使工具栏先滚动
                    if (available.y > 0 && toolbarOffsetHeightPx.value != 0f) {
                        val delta = available.y
                        val newOffset = toolbarOffsetHeightPx.value + delta
                        toolbarOffsetHeightPx.value = newOffset.coerceIn(-maxUpPx, 0f)
                        // 消费掉所有的y轴上的滚动
                        return Offset(0f, delta)
                    }
                    // 在其他情况下，不消费滚动事件
                    return Offset.Zero
                }

                override fun onPostScroll(
                    consumed: Offset,
                    available: Offset,
                    source: NestedScrollSource
                ): Offset {
                    // 其他情况不消费滚动事件
                    return super.onPostScroll(consumed, available, source)
                }
            }
        }
        // 使用toolbarOffsetHeightPx的值来判断是否已经滑动到最大值
        val isScrolledToMax by derivedStateOf {
            toolbarOffsetHeightPx.value <= -maxUpPx
        }
        // 使用toolbarOffsetHeightPx的值来判断是否已经滑动到最小值
        val isScrolledToMin by derivedStateOf {
            toolbarOffsetHeightPx.value >= 0f
        }
        Box(
            Modifier
                .fillMaxSize()
                // 作为父级附加到嵌套滚动系统
                .nestedScroll(nestedScrollConnection)
        ) {
            FollowNestedScrollToolbar(
                title = "toolbar offset is ${toolbarOffsetHeightPx.value}",
                scrollableAppBarHeight = toolbarHeight,
                toolbarOffsetHeightPx = toolbarOffsetHeightPx,
                isScrolledToMax = isScrolledToMax,
                onNavigate = {
                    showToast(context, "事件：onNavigate")
                },
                onBoxSizeChanged = { size ->
                    coroutineScope.launch {
                        // 你可以在这里获取到应用栏的高度
                        toolbarHeight = if (toolbarHeight == 0.dp) pxToDp(
                            size.height,
                            context
                        ).dp else toolbarHeight
                    }
                },
                onTabSizeChanged = { size ->
                    coroutineScope.launch {
                        // 你可以在这里获取到TabRow的高度
                        tabBarHeight = if (tabBarHeight == 0.dp) pxToDp(
                            size.height,
                            context
                        ).dp else tabBarHeight
                    }
                },
                tabs = tabs,
                selectedTabIndex = selectedTabIndex,
                onSelectedTabChange = { tabIndex ->
                    coroutineScope.launch {
                        selectedTabIndex = tabIndex
                        // Call scroll to on pagerState
                        pagerState.scrollToPage(tabIndex) // or pagerState.animateScrollToPage(tabIndex)
                    }
                }
            )
            Text(text = if (isScrolledToMax) "达到最大偏移量" else "尚未达到最大偏移量")
            val paddingOffset =
                toolbarHeight + with(LocalDensity.current) { toolbarOffsetHeightPx.value.toDp() }
            HorizontalPager(
                modifier = Modifier.fillMaxSize(),
                state = pagerState,
                contentPadding = PaddingValues(top = paddingOffset)
            ) { pageIndex ->
                // page content
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    items(20) { itemIndex ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .padding(5.dp)
                                .background(if (itemIndex % 2 == 0) Color.Black else Color.Yellow),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "I'm item $itemIndex",
                                color = if (itemIndex % 2 != 0) Color.Black else Color.Yellow
                            )
                            Text(
                                text = "pageIndex: $pageIndex", // pagerState.currentPage
                                color = if (itemIndex % 2 != 0) Color.Black else Color.Yellow
                            )
                        }
                    }
                }
            }
        }
    }
}

// 跟随嵌套滚动的工具栏（应用栏 -> Header区）
@Composable
private fun FollowNestedScrollToolbar(
    modifier: Modifier = Modifier,
    title: String,
    tabs: List<AlbumFileTypeTab>,
    selectedTabIndex: Int,
    onSelectedTabChange: (Int) -> Unit,
    scrollableAppBarHeight: Dp, // 可滚动的应用栏高度
    toolbarOffsetHeightPx: MutableState<Float>, //向上偏移量
    isScrolledToMax: Boolean,
    onBoxSizeChanged: (IntSize) -> Unit,
    onTabSizeChanged: (IntSize) -> Unit,
    onNavigate: (String) -> Unit,
    nestedScrollConnection: NestedScrollConnection? = null,// 后期扩展多层次的嵌套滚动
) {
    // 应用栏最大向上偏移量
    val maxOffsetHeightPx = with(LocalDensity.current) {
        scrollableAppBarHeight.roundToPx().toFloat() - topAppBarHeight.roundToPx().toFloat()
    }
    Box(
        modifier = modifier
            .zIndex(1f) // 设置z轴高度
            .offset {
                IntOffset(x = 0, y = toolbarOffsetHeightPx.value.roundToInt()) //设置偏移量
            }
            .fillMaxWidth()
            .onSizeChanged { size ->
                onBoxSizeChanged(size)
            }
    ) {
        Column(
            modifier = Modifier.background(color = MaterialTheme.colorScheme.background),
        ) {
            // 站位高度（此处是TopAppBar的区域）
            Spacer(modifier = Modifier.height(topAppBarHeight))
            // TopAppBar下面的区域内容
            DetailedInfo()
            // 文件类型Tab（这里是模拟StickyHeader粘性页眉效果 == 模拟吸顶）
            AlbumFileTypeTabRow(
                tabs = tabs,
                selectedTabIndex = selectedTabIndex,
                onSelectedTabChange = onSelectedTabChange,
                onTabSizeChanged = onTabSizeChanged
            )
        }
        CustomTopAppBar(
            modifier = modifier
                .offset {
                    IntOffset(
                        x = 0,
                        y = -toolbarOffsetHeightPx.value.roundToInt() //保证应用栏是始终不动的
                    )
                }
                .fillMaxWidth(),
            onNavigate = onNavigate,
            title = "龍龍龍",
        )
        if (isScrolledToMax) {
            HorizontalDivider(
                modifier = modifier
                    .padding(top = topAppBarHeight)
                    .offset {
                        IntOffset(
                            x = 0,
                            y = -toolbarOffsetHeightPx.value.roundToInt() //保证应用栏是始终不动的
                        )
                    }
                    .fillMaxWidth(),
            )
        }
        // 反馈应用栏的偏移量
        Text(
            text = title,
            modifier = modifier
                .offset {
                    IntOffset(
                        x = 0,
                        y = -toolbarOffsetHeightPx.value.roundToInt() //保证应用栏是始终不动的
                    )
                }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomTopAppBar(
    modifier: Modifier = Modifier,
    onNavigate: (String) -> Unit,
    title: String,
) {
    TopAppBar(
        modifier = modifier.fillMaxWidth(),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        navigationIcon = {
            IconButton(onClick = { onNavigate(popBackStack) }) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    painter = painterResource(id = drawable.ic_goback),
                    contentDescription = "go back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        },
        title = {
            Text(
                text = title,
                fontSize = 16.sp,
                lineHeight = 16.sp,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                overflow = TextOverflow.Ellipsis,
            )
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AlbumFileTypeTabRow(
    tabs: List<AlbumFileTypeTab>,
    selectedTabIndex: Int,
    onSelectedTabChange: (Int) -> Unit,
    onTabSizeChanged: (IntSize) -> Unit,
) {
    SecondaryTabRow(
        modifier = Modifier
            .fillMaxWidth()
            .onSizeChanged {
                onTabSizeChanged(it)
            },
        selectedTabIndex = selectedTabIndex,
        containerColor = MaterialTheme.colorScheme.background,
        indicator = {
            TabRowDefaults.SecondaryIndicator(
                modifier = Modifier.tabIndicatorOffset(selectedTabIndex,
                    matchContentSize = false
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
        },
    ) {
        tabs.forEachIndexed { index, tab ->
            Tab(
                selected = selectedTabIndex == index,
                onClick = {
                    onSelectedTabChange(index)
                },
                text = {
                    CustomIcon(
                        modifier = Modifier.size(24.dp),
                        icon = tab.icon,
                        tint = if (selectedTabIndex == index) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                },
            )
        }
    }
}


@Composable
private fun CustomIcon(
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int,
    contentDescription: String? = null,
    tint: Color = MaterialTheme.colorScheme.onBackground,
) {
    Icon(
        modifier = modifier,
        painter = painterResource(id = icon),
        contentDescription = contentDescription,
        tint = tint
    )
}


/**
 * 详情信息
 */
@Composable
private fun DetailedInfo() {
    // 头像url
    val avatarUrl = "https://pcsdata.baidu.com/thumbnail/08d0a1fc9h192683fa744661468f0f7c?fid=1815907562-16051585-269945692813262&rt=pr&sign=FDTAER-yUdy3dSFZ0SVxtzShv1zcMqd-HzBwNUMl9VWHyWHKiLTNJFQHPC8%3D&expires=2h&chkv=0&chkbd=0&chkpc=&dp-logid=372938087446407281&dp-callid=0&time=1709719200&bus_no=26&size=c200_u200&quality=100&vuk=-&ft=video&cut_x=150&cut_y=29&cut_h=256&cut_w=256&autorotate=1&module=face"
    // 公共样式
    val commonModifier = Modifier.padding(horizontal = 20.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .then(commonModifier),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AuthorAvatar(
            modifier = Modifier
                .size(80.dp)
                .aspectRatio(1f),
            url = avatarUrl,
            shape = RoundedCornerShape(50),
            border = null,
            contentScale = ContentScale.Crop
        )
        FileTypeNumberText(
            fileTypeName = "全部",
            fileTypeNumber = 200,
            color = MaterialTheme.colorScheme.onBackground,
        )
        FileTypeNumberText(
            fileTypeName = "照片",
            fileTypeNumber = 100,
            color = MaterialTheme.colorScheme.onBackground,
        )
        FileTypeNumberText(
            fileTypeName = "视频",
            fileTypeNumber = 100,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
    Spacer(modifier = Modifier.size(5.dp))
    DoubleLineText(
        modifier = Modifier
            .fillMaxWidth()
            .then(commonModifier),
        nameText = "",
        subText = "Telegram上的官方Telegram。很多递归。非常电报。哇！",
        onSubTextClick = {

        }
    )
    LinkToDoubleLineText(
        modifier = Modifier
            .fillMaxWidth()
            .then(commonModifier),
        linkText = "WSU-QB-Fans.com/亲爱的表哥的活宝妹",
        onSubTextClick = {
        }
    )
}


// 作者头像
@Composable
private fun AuthorAvatar(
    modifier: Modifier = Modifier,
    url: Any,
    shape: RoundedCornerShape = RoundedCornerShape(0),
    border: BorderStroke? = BorderStroke(width = 2.dp, color = MaterialTheme.colorScheme.onTertiary.copy(alpha = 0.35f)),
    contentScale: ContentScale = ContentScale.Fit,
) {
    Surface(
        shape = shape,
        border = border,
    ) {
        // 使用coil加载图片
        AsyncImage(
            model = url,
            modifier = modifier,
            contentDescription = null,
            placeholder = null,
            error = null,
            contentScale = contentScale,
        )
    }
}


// 根据Link生成对应的双行文本
@Composable
private fun LinkToDoubleLineText(
    modifier: Modifier = Modifier,
    linkText: String,
    onSubTextClick: (subText: String) -> Unit,
) {
    // 这里的nameText需要处理成对应的文本，当nameText中包含如下变成如下格式
    // 示例1：nameText中的hostshome为t.me的网址 -> Telegram Link
    // 示例2：nameText中的hostshome为onlyfans.com的网址 -> OnlyFans Link
    // 示例3：nameText中的hostshome为x.com的网址或twitter.com -> X Link
    // 示例4：nameText中的hostshome为youtube.com -> YouTube Link
    val processedNameText = when {
        "t.me" in linkText -> "Telegram Link"
        "onlyfans.com" in linkText -> "OnlyFans Link"
        "x.com" in linkText || "twitter.com" in linkText -> "X Link"
        "youtube.com" in linkText -> "YouTube Link"
        else -> "Other Link"
    }
    DoubleLineText(
        modifier = modifier,
        nameText = processedNameText,
        subText = linkText,
        onSubTextClick = onSubTextClick
    )
}


/**
 * 双行文本
 */
@Composable
private fun DoubleLineText(
    modifier: Modifier = Modifier,
    nameText: String,
    subText: String,
    onSubTextClick: (subText: String) -> Unit,
) {
    val annotatedString = buildAnnotatedString {
        withStyle(style = SpanStyle(
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.SemiBold
        )
        ) {
            append(nameText)
        }
        append("\n") // Add a line break between the number and the file type
        withStyle(style = SpanStyle(
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Normal,
            baselineShift = BaselineShift(0.15f) // 向上偏移0.25倍行高
        )) {
            append(subText)
        }
    }

    Text(
        modifier = modifier,
        text = annotatedString
    )
}


/**
 * 文件类型数量
 */
@Composable
private fun FileTypeNumberText(
    modifier: Modifier = Modifier,
    fileTypeName: String = "照片",
    fileTypeNumber: Number = 100,
    formatType: String = "compact", // 添加一个参数以选择格式化类型
    color: Color = MaterialTheme.colorScheme.onBackground,
) {
    // 需要先对fileTypeNumber进行处理，如单位到了千、万，需要改变显示文本，之后转换成字符串
    // 示例：1354 -> 方式1：1.3k  方式2: 1,354; 13540 -> 方式1：13.5k  方式2: 13,540
    // 需要两种方式都支持，用户可以在设置中切换
    val formattedNumber by remember {
        mutableStateOf(formatNumber(fileTypeNumber, formatType))
    }
    val annotatedString = buildAnnotatedString {
        withStyle(style = SpanStyle(
            fontSize = 16.sp,
            color = color,
            fontWeight = FontWeight.SemiBold
        )
        ) {
            append(formattedNumber)
        }
        append("\n") // Add a line break between the number and the file type
        withStyle(style = SpanStyle(
            fontSize = 14.sp,
            color = color,
            fontWeight = FontWeight.Normal,
            baselineShift = BaselineShift(0.25f) // 向上偏移0.25倍行高
        )
        ) {
            append(fileTypeName)
        }
    }
    Text(
        text = annotatedString,
        modifier = modifier,
        overflow = TextOverflow.Ellipsis
    )
}


/**
 * 格式化数字
 */
private fun formatNumber(number: Number, formatType: String): String {
    return when (formatType) {
        "compact" -> {// 紧凑型，如：1.3k
            when {
                number.toDouble() < 1000 -> number.toString()
                else -> {
                    val dividedNumber = number.toDouble() / 1000
                    String.format("%.1fk", dividedNumber)
                }
            }
        }
        "comma" -> { // 逗号分隔，如：1,000
            "%,d".format(number.toInt())
        }
        else -> number.toString()
    }
}
// 封装 Toast 显示的函数
private fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}
/**
 * 将像素转换为dp
 */
fun pxToDp(px: Int, context: Context): Int {
    val density = context.resources.displayMetrics.density
    return (px / density).toInt()
}


private data class AlbumFileTypeTabUiState(
    val tabs: List<AlbumFileTypeTab>
) {
    // 默认数据
    object LocalState {
        val defaultTabs: List<AlbumFileTypeTab> = AlbumFileTypeTab.LocalState.getTabList()
        // 获取默认数据
        fun getTabUiState(): AlbumFileTypeTabUiState {
            return AlbumFileTypeTabUiState(
                tabs = defaultTabs
            )
        }
    }
}
