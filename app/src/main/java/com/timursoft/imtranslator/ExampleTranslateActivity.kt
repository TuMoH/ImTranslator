package com.timursoft.imtranslator

import android.net.Uri
import android.view.Menu
import java.io.InputStream

class ExampleTranslateActivity : TranslateActivity() {

    override fun getSubtitleIS(): InputStream {
        return assets.open("example_subtitle.srt")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val result = super.onCreateOptionsMenu(menu)
        menu.findItem(R.id.action_save).isVisible = false
        return result
    }

    override fun setVideoContent(videoView: VideoView) {
        val uri = Uri.parse("/android_asset/example_video.mp4")
        videoView.setVideoUri(uri)
    }

}
