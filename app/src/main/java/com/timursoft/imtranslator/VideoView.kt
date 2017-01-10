package com.timursoft.imtranslator

import android.content.Context
import android.graphics.SurfaceTexture
import android.net.Uri
import android.util.AttributeSet
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.widget.RelativeLayout
import kotlinx.android.synthetic.main.video_view.view.*
import tv.danmaku.ijk.media.exo.IjkExoMediaPlayer
import tv.danmaku.ijk.media.player.IMediaPlayer

class VideoView : RelativeLayout {

    private lateinit var mediaPlayer: IMediaPlayer

    constructor(context: Context) : super(context) {
        setup(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setup(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        setup(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        setup(context)
    }

    private fun setup(context: Context) {
        mediaPlayer = IjkExoMediaPlayer(context)

        inflate(context, R.layout.video_view, this)

        texture_view.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean {
                return true
            }

            override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture, width: Int, height: Int) {
                mediaPlayer.setSurface(Surface(surfaceTexture))
            }

            override fun onSurfaceTextureSizeChanged(surfaceTexture: SurfaceTexture, width: Int, height: Int) {
            }

            override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) {
            }
        }
    }

    fun setVideoUri(uri: Uri?) {
        mediaPlayer.setDataSource(context, uri)
        mediaPlayer.prepareAsync()
        mediaPlayer.setOnPreparedListener {
            start()
        }
        mediaPlayer.setOnVideoSizeChangedListener { iMediaPlayer, w, h, sar_num, sar_den ->
            if (w > 0 && h > 0) {
                texture_view.layoutParams.height = texture_view.width * h / w
            }
            pause()
        }
    }

    fun start() {
        mediaPlayer.start()
        ic_play_pause.visibility = View.GONE
    }

    fun pause() {
        mediaPlayer.pause()
        ic_play_pause.visibility = View.VISIBLE
    }

    fun isPlaying(): Boolean {
        return mediaPlayer.isPlaying
    }

    fun seekTo(time: Int) {
        mediaPlayer.seekTo(time.toLong())
    }

    fun getCurrentPosition(): Int {
        return mediaPlayer.currentPosition.toInt()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mediaPlayer.release()
    }

}
