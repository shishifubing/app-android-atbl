package com.shishifubing.atbl

import android.util.Log
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class LauncherSettingsRepository(private val dataStore: DataStore<LauncherSettings>) {

    private val tag = LauncherSettingsRepository::class.simpleName

    val settingsFlow: Flow<LauncherSettings> = dataStore.data
        .catch { exception ->
            // dataStore.data throws an IOException when an error is encountered when reading data
            if (exception is IOException) {
                Log.e(tag, "Error reading preferences.", exception)
                emit(LauncherSettings.getDefaultInstance())
            } else {
                throw exception
            }
        }

    suspend fun update(action: (LauncherSettings.Builder) -> (LauncherSettings.Builder)) {
        dataStore.updateData { current -> action(current.toBuilder()).build() }
    }

    suspend fun fetchInitial() = dataStore.data.first()
}

object SettingsSerializer : Serializer<LauncherSettings> {
    override val defaultValue: LauncherSettings =
        LauncherSettings.getDefaultInstance()
            .toBuilder()
            .setAppCardPadding(0)
            .setAppCardLabelRemoveSpaces(true)
            .setAppCardTextStyle(LauncherTextStyle.HeadlineSmall)
            .setAppCardLabelLowercase(false)
            .setAppLayoutHorizontalArrangement(
                LauncherHorizontalArrangement.HorizontalCenter
            )
            .setAppLayoutVerticalArrangement(
                LauncherVerticalArrangement.VerticalTop
            )
            .setAppCardTextColor(LauncherTextColor.Unspecified)
            .setAppCardFontFamily(LauncherFontFamily.Default)
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