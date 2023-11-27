package com.shishifubing.atbl

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import com.google.protobuf.InvalidProtocolBufferException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import java.io.InputStream
import java.io.OutputStream

private val Context.dataStore by dataStore(
    fileName = "settings.pb",
    serializer = LauncherSettingsSerializer
)

val launcherSettingsDefault: LauncherSettings = LauncherSettings
    .getDefaultInstance()
    .toBuilder()
    .setAppCardPadding(0)
    .setAppCardLabelRemoveSpaces(true)
    .setAppCardTextStyle(LauncherTextStyle.HeadlineSmall)
    .setAppCardLabelLowercase(true)
    .setAppLayoutHorizontalArrangement(
        LauncherHorizontalArrangement.HorizontalStart
    )
    .setAppLayoutVerticalArrangement(
        LauncherVerticalArrangement.VerticalSpaceBetween
    )
    .setAppCardTextColor(LauncherTextColor.Unspecified)
    .setAppCardFontFamily(LauncherFontFamily.Monospace)
    .setAppLayoutSortBy(LauncherSortBy.SortByLabel)
    .setAppLayoutReverseOrder(false)
    .setAppLayoutHorizontalPadding(0)
    .setAppLayoutVerticalPadding(0)
    .setAppCardSplitScreenSeparator("/")
    .build()

class LauncherSettingsRepository(private val context: Context) {

    private val tag = LauncherSettingsRepository::class.simpleName

    companion object {
        val default = launcherSettingsDefault
    }

    val settingsFlow: Flow<LauncherSettings> = context.dataStore.data.catch {
        emit(LauncherSettingsSerializer.defaultValue)
    }


    suspend fun update(action: LauncherSettings.Builder.() -> Unit) {
        context.dataStore.updateData { it.toBuilder().apply(action).build() }
    }

    suspend fun updateFromBytes(bytes: ByteArray) = update { mergeFrom(bytes) }

}

private object LauncherSettingsSerializer : Serializer<LauncherSettings> {
    override val defaultValue: LauncherSettings = launcherSettingsDefault

    override suspend fun readFrom(input: InputStream): LauncherSettings {
        try {
            return LauncherSettings.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read settings.", exception)
        }
    }

    override suspend fun writeTo(t: LauncherSettings, output: OutputStream) =
        t.writeTo(output)
}