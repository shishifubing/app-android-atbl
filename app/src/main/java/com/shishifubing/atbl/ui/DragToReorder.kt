package com.shishifubing.atbl.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@Preview(showBackground = true)
@Composable
fun Check() {
    var items by remember { mutableStateOf((0..10).toList()) }
    var move by remember { mutableStateOf("") }
    LauncherTheme {
        Column {
            if (move.isNotEmpty()) {
                Text(move)
            }
            DragDropList(
                items = items,
                onMove = { from, to ->
                    move = "$from -> $to"
                    val fromItem = items[from]
                    val toItem = items[to]
                    val newList = items.toMutableList()
                    newList[from] = toItem
                    newList[to] = fromItem
                    items = newList // Update the mutable state
                },
                onDragFinished = { }
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .height(64.dp),
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            it.toString(),
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

}

@Composable
fun <T : Any> DragDropList(
    items: List<T>,
    onMove: (Int, Int) -> Unit,
    onDragFinished: () -> Unit,
    modifier: Modifier = Modifier,
    itemComposable: @Composable (item: T) -> Unit
) {

    val scope = rememberCoroutineScope()
    var overscrollJob by remember { mutableStateOf<Job?>(null) }
    val dragDropListState = rememberDragDropListState(onMove = onMove)

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
            .pointerInput(Unit) {
                detectDragGesturesAfterLongPress(
                    onDrag = { change, offset ->
                        change.consume()
                        dragDropListState.onDrag(offset)

                        if (overscrollJob?.isActive == true) {
                            return@detectDragGesturesAfterLongPress
                        }

                        val checkOver = dragDropListState.checkForOverScroll()
                        if (checkOver != 0f)
                            overscrollJob = scope.launch {
                                dragDropListState.lazyListState.scrollBy(
                                    checkOver
                                )
                            }
                    },
                    onDragStart = { offset ->
                        dragDropListState.onDragStart(
                            offset
                        )
                    },
                    onDragEnd = {
                        onDragFinished()
                        dragDropListState.onDragInterrupted()

                    },
                    onDragCancel = { dragDropListState.onDragInterrupted() }
                )
            },
        state = dragDropListState.lazyListState
    ) {
        itemsIndexed(items) { index, item ->
            val isCurrent = index == dragDropListState.currentIndexOfDraggedItem
            val offset = dragDropListState.elementDisplacement.takeIf { isCurrent }
            val translationYaxis by animateFloatAsState(
                targetValue = offset ?: 0f, label = ""
            )
            Column(
                modifier = Modifier
                    .graphicsLayer { translationY = translationYaxis }
                    .background(
                        if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.background,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .fillMaxWidth()
                    .zIndex(if (isCurrent) 1f else 0f)
            ) {
                itemComposable(item)
            }
        }
    }
}

@Composable
fun rememberDragDropListState(
    lazyListState: LazyListState = rememberLazyListState(),
    onMove: (Int, Int) -> Unit,
): DragDropListState {
    return remember {
        DragDropListState(
            lazyListState = lazyListState,
            onMove = onMove
        )
    }
}

class DragDropListState(
    val lazyListState: LazyListState,
    private val onMove: (Int, Int) -> Unit
) {
    private var draggedDistance by mutableFloatStateOf(0f)

    // used to obtain initial offsets on drag start
    private var initiallyDraggedElement by mutableStateOf<LazyListItemInfo?>(
        null
    )

    var currentIndexOfDraggedItem by mutableStateOf<Int?>(null)

    private val initialOffsets: Pair<Int, Int>?
        get() = initiallyDraggedElement?.let { Pair(it.offset, it.offsetEnd) }

    val elementDisplacement: Float?
        get() = currentIndexOfDraggedItem
            ?.let { lazyListState.getVisibleItemInfoFor(absoluteIndex = it) }
            ?.let { item ->
                (initiallyDraggedElement?.offset
                    ?: 0f).toFloat() + draggedDistance - item.offset
            }

    private val currentElement: LazyListItemInfo?
        get() = currentIndexOfDraggedItem?.let {
            lazyListState.getVisibleItemInfoFor(absoluteIndex = it)
        }

    private var overscrollJob by mutableStateOf<Job?>(null)

    fun onDragStart(offset: Offset) {
        lazyListState.layoutInfo.visibleItemsInfo
            .firstOrNull { item -> offset.y.toInt() in item.offset..(item.offset + item.size) }
            ?.also {
                currentIndexOfDraggedItem = it.index
                initiallyDraggedElement = it
            }
    }

    fun onDragInterrupted() {
        draggedDistance = 0f
        currentIndexOfDraggedItem = null
        initiallyDraggedElement = null
        overscrollJob?.cancel()
    }

    fun onDrag(offset: Offset) {
        draggedDistance += offset.y

        initialOffsets?.let { (topOffset, bottomOffset) ->
            val startOffset = topOffset + draggedDistance
            val endOffset = bottomOffset + draggedDistance

            currentElement?.let { hovered ->
                lazyListState.layoutInfo.visibleItemsInfo
                    .filterNot { item -> item.offsetEnd < startOffset || item.offset > endOffset || hovered.index == item.index }
                    .firstOrNull { item ->
                        val delta = startOffset - hovered.offset
                        when {
                            delta > 0 -> (endOffset > item.offsetEnd)
                            else -> (startOffset < item.offset)
                        }
                    }
                    ?.also { item ->
                        currentIndexOfDraggedItem?.let { current ->
                            onMove.invoke(
                                current,
                                item.index
                            )
                        }
                        currentIndexOfDraggedItem = item.index
                    }
            }
        }
    }

    fun checkForOverScroll(): Float {
        return initiallyDraggedElement?.let {
            val startOffset = it.offset + draggedDistance
            val endOffset = it.offsetEnd + draggedDistance

            return@let when {
                draggedDistance > 0 -> (endOffset - lazyListState.layoutInfo.viewportEndOffset).takeIf { diff -> diff > 0 }
                draggedDistance < 0 -> (startOffset - lazyListState.layoutInfo.viewportStartOffset).takeIf { diff -> diff < 0 }
                else -> null
            }
        } ?: 0f
    }
}

/*
    LazyListItemInfo.index is the item's absolute index in the list

    Based on the item's "relative position" with the "currently top" visible item,
    this returns LazyListItemInfo corresponding to it
*/
fun LazyListState.getVisibleItemInfoFor(absoluteIndex: Int): LazyListItemInfo? {
    return this.layoutInfo.visibleItemsInfo.getOrNull(absoluteIndex - this.layoutInfo.visibleItemsInfo.first().index)
}

/*
  Bottom offset of the element in Vertical list
*/
val LazyListItemInfo.offsetEnd: Int
    get() = this.offset + this.size

/*
   Moving element in the list
*/
fun <T> MutableList<T>.move(from: Int, to: Int) {
    if (from == to) {
        return
    }
    val element = removeAt(from) ?: return
    add(to, element)
}