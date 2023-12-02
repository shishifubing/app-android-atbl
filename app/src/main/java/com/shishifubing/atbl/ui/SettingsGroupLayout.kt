package com.shishifubing.atbl.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.shishifubing.atbl.Model
import com.shishifubing.atbl.R
import com.shishifubing.atbl.data.UISettingsLayout

private val choices = object {
    val layoutHorizontalPadding = (0..150 step 10).map { it.toString() }
    val layoutVerticalPadding = (0..150 step 10).map { it.toString() }
}

@Composable
fun SettingsGroupLayout(
    settings: UISettingsLayout,
    setReverseOrder: (Boolean) -> Unit,
    setHorizontalPadding: (Int) -> Unit,
    setVerticalPadding: (Int) -> Unit,
    setHorizontalArrangement: (Model.Settings.HorizontalArrangement) -> Unit,
    setVerticalArrangement: (Model.Settings.VerticalArrangement) -> Unit,
    setSortBy: (Model.Settings.SortBy) -> Unit
) {
    val model = settings.model
    SettingsGroup(R.string.settings_group_layout) {
        LayoutReverseOrder(
            reverse = model.reverseOrder,
            setReverse = setReverseOrder
        )
        LayoutHorizontalPadding(
            padding = model.horizontalPadding,
            setPadding = setHorizontalPadding
        )
        LayoutVerticalPadding(
            padding = model.verticalPadding,
            setPadding = setVerticalPadding
        )
        LayoutHorizontalArrangement(
            arrangement = model.horizontalArrangement,
            setArrangement = setHorizontalArrangement
        )
        LayoutVerticalArrangement(
            arrangement = model.verticalArrangement,
            setArrangement = setVerticalArrangement
        )
        LayoutSortBy(
            sortBy = model.sortBy,
            setSortBy = setSortBy
        )
    }
}

@Composable
private fun LayoutVerticalPadding(
    padding: Int,
    setPadding: (Int) -> Unit
) {
    var curOption by remember {
        mutableIntStateOf(choices.layoutVerticalPadding.indexOf(
            padding.toString()
        ).let { if (it != -1) it else 0 })
    }
    SettingsFieldSingleChoice(
        name = R.string.settings_layout_vertical_padding,
        selectedOption = curOption,
        options = choices.layoutVerticalPadding,
        onConfirm = { choice ->
            curOption = choice
            setPadding(choices.layoutVerticalPadding[choice].toInt())
        }
    )
}

@Composable
private fun LayoutHorizontalPadding(
    padding: Int,
    setPadding: (Int) -> Unit
) {
    var curOption by remember {
        mutableIntStateOf(choices.layoutHorizontalPadding.indexOf(
            padding.toString()
        ).let { if (it != -1) it else 0 })
    }
    SettingsFieldSingleChoice(
        name = R.string.settings_layout_horizontal_padding,
        selectedOption = curOption,
        options = choices.layoutHorizontalPadding,
        onConfirm = { choice ->
            curOption = choice
            setPadding(choices.layoutHorizontalPadding[choice].toInt())
        }
    )
}

@Composable
private fun LayoutSortBy(
    sortBy: Model.Settings.SortBy,
    setSortBy: (Model.Settings.SortBy) -> Unit
) {
    SettingsFieldSingleChoiceEnum(
        name = R.string.settings_layout_sort_by,
        selectedOption = sortBy,
        onConfirm = { setSortBy(it) }
    )
}

@Composable
private fun LayoutReverseOrder(
    reverse: Boolean,
    setReverse: (Boolean) -> Unit
) {
    SettingsFieldSwitch(
        name = R.string.settings_layout_reverse_order,
        label = R.string.settings_layout_reverse_order_label,
        isToggled = reverse,
        onClick = { setReverse(reverse.not()) }
    )
}

@Composable
private fun LayoutVerticalArrangement(
    arrangement: Model.Settings.VerticalArrangement,
    setArrangement: (Model.Settings.VerticalArrangement) -> Unit
) {
    SettingsFieldSingleChoiceEnum(
        name = R.string.settings_layout_vertical_arrangement,
        selectedOption = arrangement,
        onConfirm = setArrangement
    )
}


@Composable
private fun LayoutHorizontalArrangement(
    arrangement: Model.Settings.HorizontalArrangement,
    setArrangement: (Model.Settings.HorizontalArrangement) -> Unit
) {
    SettingsFieldSingleChoiceEnum(
        name = R.string.settings_layout_horizontal_arrangement,
        selectedOption = arrangement,
        onConfirm = setArrangement
    )
}