package com.example.yinfuapp.utils

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import java.io.File

class AudioPlayerHelper(context: Context) {
    
    private var player: ExoPlayer? = null
    private var onCompletionListener: (() -> Unit)? = null
    
    init {
        player = ExoPlayer.Builder(context).build().apply {
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_ENDED) {
                        onCompletionListener?.invoke()
                    }
                }
            })
        }
    }
    
    fun playFile(file: File) {
        player?.apply {
            setMediaItem(MediaItem.fromUri(file.toURI().toString()))
            prepare()
            play()
        }
    }
    
    fun playUrl(url: String) {
        player?.apply {
            setMediaItem(MediaItem.fromUri(url))
            prepare()
            play()
        }
    }
    
    fun stop() {
        player?.stop()
    }
    
    fun release() {
        player?.release()
        player = null
    }
    
    fun setOnCompletionListener(listener: () -> Unit) {
        onCompletionListener = listener
    }
}