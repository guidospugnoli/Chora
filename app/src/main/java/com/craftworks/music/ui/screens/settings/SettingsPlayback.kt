package com.craftworks.music.ui.screens.settings

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.craftworks.music.R
import com.craftworks.music.managers.MediaProviderManager
import com.craftworks.music.managers.settings.PlaybackSettingsManager
import com.craftworks.music.ui.elements.dialogs.TranscodingBitrateDialog
import com.craftworks.music.ui.elements.dialogs.TranscodingFormatDialog
import com.craftworks.music.ui.elements.dialogs.dialogFocusable
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Preview(showSystemUi = false, showBackground = true)
@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class,
    ExperimentalMaterial3Api::class
)
@Composable
fun S_PlaybackScreen(navHostController: NavHostController = rememberNavController()) {
    val context = LocalContext.current

    val playbackSettingsManager = remember { PlaybackSettingsManager(context)}

    var showWifiTranscodingDialog by remember { mutableStateOf(false) }
    var showDataTranscodingDialog by remember { mutableStateOf(false) }

    var showTranscodingFormatDialog by remember { mutableStateOf(false) }

    val currentProvider by MediaProviderManager.currentProvider.collectAsStateWithLifecycle()

    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.settings_playback)) },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navHostController.popBackStack()
                        },
                        modifier = Modifier.size(56.dp, 70.dp),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            tint = MaterialTheme.colorScheme.onBackground,
                            contentDescription = stringResource(R.string.settings_playback),
                            modifier = Modifier
                                .size(24.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
    ) { innerPadding ->
        Box (
            modifier = Modifier
                .padding(
                    top = innerPadding.calculateTopPadding()
                )
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .dialogFocusable()
        ) {
            Column(
                Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Transcoding
                Column(
                    modifier = Modifier.clip(RoundedCornerShape(16.dp)),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    val transcodingBitrateWifi =
                        playbackSettingsManager.wifiTranscodingBitrateFlow.collectAsState("").value

                    SettingsDialogButton(
                        settingsName = stringResource(R.string.playback_max_bitrate_wifi),
                        settingsSubtitle = if (transcodingBitrateWifi != "No Transcoding") "$transcodingBitrateWifi Kbps" else stringResource(R.string.playback_no_transcoding),
                        settingsIcon = ImageVector.vectorResource(R.drawable.s_p_transcoding),
                        toggleEvent = { showWifiTranscodingDialog = true },
                        enabled = currentProvider != null
                    )


                    val transcodingBitrateData =
                        playbackSettingsManager.mobileDataTranscodingBitrateFlow.collectAsState("").value

                    SettingsDialogButton(
                        settingsName = stringResource(R.string.playback_max_bitrate_mobile_data),
                        settingsSubtitle = if (transcodingBitrateData != "No Transcoding") "$transcodingBitrateData Kbps" else stringResource(R.string.playback_no_transcoding),
                        settingsIcon = ImageVector.vectorResource(R.drawable.s_p_transcoding),
                        toggleEvent = { showDataTranscodingDialog = true },
                        enabled = currentProvider != null
                    )

                    val transcodingFormat =
                        playbackSettingsManager.transcodingFormatFlow.collectAsState("opus").value

                    val transcodingFormatEnabled =
                        (transcodingBitrateData != "No Transcoding" || transcodingBitrateWifi != "No Transcoding") && currentProvider != null

                    SettingsDialogButton(
                        settingsName = stringResource(R.string.playback_transcoding_format),
                        settingsSubtitle = transcodingFormat,
                        settingsIcon = ImageVector.vectorResource(R.drawable.s_p_transcoding),
                        enabled = transcodingFormatEnabled,
                        toggleEvent = { showTranscodingFormatDialog = true }
                    )
                }

                Column(
                    modifier = Modifier.clip(RoundedCornerShape(16.dp)),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    val audioOffload by playbackSettingsManager.audioOffloadEnabled.collectAsStateWithLifecycle(true)
                    SettingsSwitch(
                        audioOffload,
                        stringResource(R.string.playback_audio_offloading),
                        ImageVector.vectorResource(R.drawable.rounded_replace_audio_24),
                        toggleEvent = {
                            coroutineScope.launch {
                                playbackSettingsManager.setAudioOffload(!audioOffload)
                            }
                        }
                    )
                }

                // Scrobble Percent
                Column(
                    modifier = Modifier.clip(RoundedCornerShape(16.dp)),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    val sliderValue =
                        PlaybackSettingsManager(context).scrobblePercentFlow.collectAsState(7)

                    SettingsSlider(
                        settingsName = stringResource(R.string.playback_min_scrobble_percentage),
                        value = sliderValue.value.toFloat(),
                        steps = 8,
                        minValue = 1f, maxValue = 10f,
                        onValueChange = {
                            coroutineScope.launch {
                                PlaybackSettingsManager(context).setScrobblePercent(it.roundToInt())
                            }
                        }
                    )
                }
            }
        }

        if (showWifiTranscodingDialog) TranscodingBitrateDialog(setShowDialog = {
            showWifiTranscodingDialog = it
        }, true)
        if (showDataTranscodingDialog) TranscodingBitrateDialog(setShowDialog = {
            showDataTranscodingDialog = it
        }, false)
        if (showTranscodingFormatDialog) TranscodingFormatDialog(setShowDialog = {
            showTranscodingFormatDialog = it
        })
    }
}