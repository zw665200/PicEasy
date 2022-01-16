package com.picfix.tools.controller

import android.app.Activity
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.picfix.tools.utils.JLog

/**
 * @author Herr_Z
 * @description:
 * @date : 2021/12/23 20:58
 */
object MediaPlayer {
    private var exoPlayer: ExoPlayer? = null
    private var status = Status.INIT

    fun getPlayer(activity: Activity): ExoPlayer {
        if (exoPlayer == null) {
            exoPlayer = ExoPlayer.Builder(activity).build()
            status = Status.START
        }

        return exoPlayer!!
    }

    fun play(uri: String) {
        when (status) {

            Status.START -> {
                if (exoPlayer != null) {
                    JLog.i("start")
                    val mediaItem = MediaItem.fromUri(uri)
                    exoPlayer!!.setMediaItem(mediaItem)
                    exoPlayer!!.repeatMode = Player.REPEAT_MODE_ONE
                    exoPlayer!!.prepare()
                    exoPlayer!!.playWhenReady = true
                    status = Status.PLAY
                }
            }

            Status.STOP -> {
                if (exoPlayer != null) {
                    JLog.i("stop")
                    JLog.i("count = ${exoPlayer!!.mediaItemCount}")

                    exoPlayer!!.removeMediaItem(0)
                    status = Status.START
                    play(uri)
                }
            }

            else -> {}
        }
    }

    fun stop() {
        when (status) {
            Status.PLAY -> {
                if (exoPlayer != null) {
                    exoPlayer!!.pause()
                    status = Status.STOP
                }
            }
        }
    }

    fun release() {
        when (status) {
            Status.PLAY -> {
                if (exoPlayer != null) {
                    exoPlayer!!.release()
                    status = Status.START
                }
            }
        }
    }

    enum class Status {
        INIT, START, PLAY, STOP
    }

}