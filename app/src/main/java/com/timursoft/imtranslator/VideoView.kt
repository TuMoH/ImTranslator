package com.timursoft.imtranslator

import android.content.Context
import android.util.AttributeSet
import com.devbrackets.android.exomedia.core.EMListenerMux
import com.devbrackets.android.exomedia.core.exoplayer.EMExoPlayer
import com.devbrackets.android.exomedia.core.video.ExoVideoView
import com.devbrackets.android.exomedia.listener.OnPreparedListener

/**
 * Created by TuMoH on 21.07.2016.
 */
class VideoView : ExoVideoView {

    constructor(context: Context) : super(context) {
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
    }

    override fun setup() {
        super.setup()
        setListenerMux(EMListenerMux(MuxNotifier()))
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (!isInEditMode) {
            release()
        }
    }

    fun setOnPreparedListener(listener: OnPreparedListener) {
        listenerMux.setOnPreparedListener(listener)
    }

    private inner class MuxNotifier : EMListenerMux.EMListenerMuxNotifier() {
        override fun shouldNotifyCompletion(endLeeway: Long): Boolean {
            return currentPosition + endLeeway >= duration
        }

        override fun onExoPlayerError(emExoPlayer: EMExoPlayer?, e: Exception) {
            stopPlayback()
            emExoPlayer?.forcePrepare()
        }

        override fun onMediaPlaybackEnded() {
            stopPlayback()
            keepScreenOn = false
        }

        //        @SuppressWarnings("SuspiciousNameCombination")
        override fun onVideoSizeChanged(width: Int, height: Int, unAppliedRotationDegrees: Int, pixelWidthHeightRatio: Float) {
            //NOTE: Android 5.0+ will always have an unAppliedRotationDegrees of 0 (ExoPlayer already handles it)
            setVideoRotation(unAppliedRotationDegrees, false)
            this@VideoView.onVideoSizeChanged(width, height)
        }
    }

}
