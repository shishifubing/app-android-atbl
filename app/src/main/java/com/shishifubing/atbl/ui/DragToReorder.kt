package com.shishifubing.atbl.ui

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.unit.IntOffset
import com.shishifubing.atbl.LauncherApp
import com.shishifubing.atbl.LauncherApps
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.math.sign

enum class SlideState {
    NONE,
    UP,
    DOWN
}

/*
 * https://developersbreach.com/drag-to-reorder-compose/
 **/
fun Modifier.dragToReorder(
    launcherApp: LauncherApp,
    launcherApps: LauncherApps,
    itemHeight: Int,
    updateSlideState: (launcherApp: LauncherApp, slideState: SlideState) -> Unit,
    isDraggedAfterLongPress: Boolean,
    onStartDrag: () -> Unit,
    onStopDrag: (currentIndex: Int, destinationIndex: Int) -> Unit,
): Modifier = composed {
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }
    pointerInput(Unit) {
        // Wrap in a coroutine scope to use suspend functions for touch events and animation.
        coroutineScope {
            val shoesArticleIndex = launcherApps.appsList.indexOf(launcherApp)
            val offsetToSlide = itemHeight / 4
            var numberOfItems = 0
            var previousNumberOfItems: Int
            var listOffset = 0

            val onDragStart = {
                // Interrupt any ongoing animation of other items.
                launch {
                    offsetX.stop()
                    offsetY.stop()
                }
                onStartDrag()
            }
            val onDrag = { change: PointerInputChange ->
                val horizontalDragOffset =
                    offsetX.value + change.positionChange().x
                launch {
                    offsetX.snapTo(horizontalDragOffset)
                }
                val verticalDragOffset =
                    offsetY.value + change.positionChange().y
                launch {
                    offsetY.snapTo(verticalDragOffset)
                    val offsetSign = offsetY.value.sign.toInt()
                    previousNumberOfItems = numberOfItems
                    numberOfItems = calculateNumberOfSlidItems(
                        offsetY.value * offsetSign,
                        itemHeight,
                        offsetToSlide,
                        previousNumberOfItems
                    )

                    if (previousNumberOfItems > numberOfItems) {
                        updateSlideState(
                            launcherApps.appsList[shoesArticleIndex + previousNumberOfItems * offsetSign],
                            SlideState.NONE
                        )
                    } else if (numberOfItems != 0) {
                        try {
                            updateSlideState(
                                launcherApps.appsList[shoesArticleIndex + numberOfItems * offsetSign],
                                if (offsetSign == 1) SlideState.UP else SlideState.DOWN
                            )
                        } catch (e: IndexOutOfBoundsException) {
                            numberOfItems = previousNumberOfItems
                            Log.i(
                                "DragToReorder",
                                "Item is outside or at the edge"
                            )
                        }
                    }
                    listOffset = numberOfItems * offsetSign
                }
                // Consume the gesture event, not passed to external
                if (change.positionChange() != Offset.Zero) change.consume()
            }
            val onDragEnd = {
                launch {
                    offsetX.animateTo(0f)
                }
                launch {
                    offsetY.animateTo(itemHeight * numberOfItems * offsetY.value.sign)
                    onStopDrag(
                        shoesArticleIndex,
                        shoesArticleIndex + listOffset
                    )
                }
            }
            if (isDraggedAfterLongPress) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { onDragStart() },
                    onDrag = { change, _ -> onDrag(change) },
                    onDragEnd = { onDragEnd() }
                )
            } else {
                while (true) {
                    awaitPointerEventScope {
                        drag(awaitFirstDown().id) { change ->
                            onDragStart()
                            onDrag(change)
                        }
                    }
                    onDragEnd()
                }
            }
        }
    }
        .offset {
            // Use the animating offset value here.
            IntOffset(offsetX.value.roundToInt(), offsetY.value.roundToInt())
        }
}

private fun calculateNumberOfSlidItems(
    offsetY: Float,
    itemHeight: Int,
    offsetToSlide: Int,
    previousNumberOfItems: Int
): Int {
    val numberOfItemsInOffset = (offsetY / itemHeight).toInt()
    val numberOfItemsPlusOffset =
        ((offsetY + offsetToSlide) / itemHeight).toInt()
    val numberOfItemsMinusOffset =
        ((offsetY - offsetToSlide - 1) / itemHeight).toInt()
    return when {
        offsetY - offsetToSlide - 1 < 0 -> 0
        numberOfItemsPlusOffset > numberOfItemsInOffset -> numberOfItemsPlusOffset
        numberOfItemsMinusOffset < numberOfItemsInOffset -> numberOfItemsInOffset
        else -> previousNumberOfItems
    }
}