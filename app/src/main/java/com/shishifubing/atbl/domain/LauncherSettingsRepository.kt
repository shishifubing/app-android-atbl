package com.shishifubing.atbl.domain

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import com.shishifubing.atbl.LauncherFontFamily
import com.shishifubing.atbl.LauncherHorizontalArrangement
import com.shishifubing.atbl.LauncherSettings
import com.shishifubing.atbl.LauncherSortBy
import com.shishifubing.atbl.LauncherTextColor
import com.shishifubing.atbl.LauncherTextStyle
import com.shishifubing.atbl.LauncherVerticalArrangement
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import java.io.InputStream
import java.io.OutputStream

class LauncherSettingsRepository(private val dataStore: DataStore<LauncherSettings>) {

    private val tag = LauncherSettingsRepository::class.simpleName

    val settingsFlow: Flow<LauncherSettings> = dataStore.data.catch {
        emit(LauncherSettingsSerializer.defaultValue)
    }

    fun getDefault(): LauncherSettings.Builder =
        LauncherSettingsSerializer.defaultValue.toBuilder()

    suspend fun update(action: LauncherSettings.Builder.() -> Unit) {
        dataStore.updateData { it.toBuilder().apply(action).build() }
    }

    suspend fun updateFromBytes(bytes: ByteArray) = update { mergeFrom(bytes) }

}

object LauncherSettingsSerializer : Serializer<LauncherSettings> {
    override val defaultValue: LauncherSettings =
        LauncherSettings.getDefaultInstance()
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