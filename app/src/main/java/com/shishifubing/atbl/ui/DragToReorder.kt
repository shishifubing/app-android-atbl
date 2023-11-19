package com.shishifubing.atbl.ui

enum class SlideState {
    NONE,
    UP,
    DOWN
}

/*
 * https://developersbreach.com/drag-to-reorder-compose/
 **/
/*
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
            val shoesArticleIndex =
                launcherApps.appsMap.values.indexOf(launcherApp)
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
                            launcherApps.appsMap.values[shoesArticleIndex + previousNumberOfItems * offsetSign],
                            SlideState.NONE
                        )
                    } else if (numberOfItems != 0) {
                        try {
                            updateSlideState(
                                launcherApps.appsMap.values[shoesArticleIndex +
                                        numberOfItems * offsetSign],
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

 */

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