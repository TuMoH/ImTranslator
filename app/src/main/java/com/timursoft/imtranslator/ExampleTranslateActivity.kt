package com.timursoft.imtranslator

import android.net.Uri
import android.view.Menu
import android.widget.VideoView
import java.io.InputStream

class ExampleTranslateActivity : TranslateActivity() {

    override fun getSubtitleIS(): InputStream {
        return resources.openRawResource(R.raw.example_subtitle)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val result = super.onCreateOptionsMenu(menu)
        menu.findItem(R.id.action_save).isVisible = false
        return result
    }

    override fun setVideoContent(videoView: VideoView) {
        val uri = Uri.parse("android.resource://" + packageName + "/" + R.raw.example_video)
        videoView.setVideoURI(uri)
    }

}
