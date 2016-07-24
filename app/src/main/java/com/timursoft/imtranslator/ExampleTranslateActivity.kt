package com.timursoft.imtranslator

import android.net.Uri
import android.support.design.widget.Snackbar
import kotlinx.android.synthetic.main.activity_translate.*
import java.io.InputStream

class ExampleTranslateActivity : TranslateActivity() {

    override fun getSubtitlesContent(): InputStream {
        return assets.open("example_subtitle.srt")
    }

    override fun getVideoContent(): Uri {
        return Uri.parse("/android_asset/example_video.mp4")
    }

    override fun save() {
        Snackbar.make(app_bar, R.string.INFO_not_available_in_example, Snackbar.LENGTH_SHORT).show()
    }

}
