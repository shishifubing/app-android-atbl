package com.shishifubing.atbl.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.ImageBitmap
import com.shishifubing.atbl.Defaults
import com.shishifubing.atbl.Model
import com.shishifubing.atbl.Model.Settings

val defaultUISettingsLayout = UISettingsLayout(Defaults.LayoutSettings)
val defaultUISettingsAppCard = UISettingsAppCard(Defaults.AppCardSettings)
val defaultUISettings = UISettings(
    defaultUISettingsLayout,
    defaultUISettingsAppCard
)

@Immutable
data class UIModel<T>(val model: T)

typealias UISettingsLayout = UIModel<Settings.Layout>
typealias UIAppShortcut = UIModel<Model.AppShortcut>
typealias UIAppShortcuts = UIModel<List<UIAppShortcut>>
typealias UIApps = UIModel<List<UIApp>>
typealias UISplitScreenShortcuts = UIModel<List<UISplitScreenShortcut>>
typealias UISettingsAppCard = UIModel<Settings.AppCard>
typealias UIHomeDialogActionButtons = UIModel<List<Pair<String, () -> Unit>>>
typealias UIHomeDialogHeaders = UIModel<List<@Composable (ColumnScope.() -> Unit)>>

@Immutable
data class UISettings(val model: Settings) {
    val layout = UISettingsLayout(model.layout)
    val appCard = UISettingsAppCard(model.appCard)
}

@Immutable
data class UIApp(val model: Model.App) {
    val shortcuts = UIAppShortcuts(
        model = model.shortcutsList.map { UIAppShortcut(it) }
    )

    fun bitmapIcon(): Bitmap {
        val byteArray = model.icon.toByteArray()
        return BitmapFactory.decodeByteArray(
            byteArray,
            0,
            byteArray.size,
            BitmapFactory.Options().also { it.inMutable = true }
        )
    }
}


@Stable
sealed interface UIHomeScreenItem {
    data class Apps(val apps: UIApps) : UIHomeScreenItem
    data class Shortcuts(
        val shortcuts: UISplitScreenShortcuts
    ) : UIHomeScreenItem
}

@Stable
sealed interface UIHomeState {
    data class Success(
        val screens: UIModel<List<UiHomeStateScreen>>
    ) : UIHomeState

    object Loading : UIHomeState
}

data class UiHomeStateScreen(
    val items: UIModel<List<UIHomeScreenItem>>,
    val showHiddenApps: Boolean,
    val isHomeApp: Boolean,
    val settings: UISettings
)

@Immutable
data class UISplitScreenShortcut(val model: Model.SplitScreenShortcut) {
    val uiAppFirst = UIApp(model.appTop)
    val uiAppSecond = UIApp(model.appBottom)

    fun label(settings: Settings.AppCard): String {
        return model.appTop.label + settings.splitScreenSeparator + model.appBottom.label
    }
}

sealed class UI {

    @Immutable
    data class Screen(val items: List<ScreenItem>)

    @Immutable
    sealed interface ScreenItem {
        @Immutable
        data class Complex(val item: Model.ItemComplex) : ScreenItem
    }

    data class App(
        val label: String,
        val packageName: String,
        val shortcuts: AppShortcuts,
        val isHidden: Boolean,
        val icon: ImageBitmap
    )

    @Immutable
    data class Apps(val apps: List<App>)
    data class AppShortcut(
        val shortcutId: String,
        val packageName: String,
        val label: String
    )

    @Immutable
    data class AppShortcuts(val shortcuts: List<AppShortcut>)
    data class SplitScreenShortcut(
        val appFirst: App,
        val appSecond: App,
        val key: String
    )

    @Immutable
    data class SplitScreenShortcuts(val shortcuts: List<SplitScreenShortcut>)
    data class Settings(
        val appCard: AppCard,
        val layout: Layout
    ) {
        @Immutable
        data class AppCard(
            val padding: Int,
            val labelRemoveSpaces: Boolean,
            val labelLowercase: Boolean,
            val textStyle: Model.Settings.TextStyle,
            val textColor: Model.Settings.TextColor,
            val fontFamily: Model.Settings.FontFamily,
            val splitScreenSeparator: String
        )

        @Immutable
        data class Layout(
            val horizontalArrangement: Model.Settings.HorizontalArrangement,
            val verticalArrangement: Model.Settings.VerticalArrangement,
            val sortBy: Model.Settings.SortBy,
            val reverseOrder: Boolean,
            val horizontalPadding: Int,
            val verticalPadding: Int
        )
    }
}

