package com.timursoft.imtranslator

import android.net.Uri
import android.support.design.widget.Snackbar
import com.timursoft.suber.IOHelper
import com.timursoft.suber.ParserSRT
import com.timursoft.suber.SubFileObject
import kotlinx.android.synthetic.main.activity_translate.*

class ExampleTranslateActivity : TranslateActivity() {

    override fun getSubFileObject(): SubFileObject? {
        return ParserSRT().parse(IOHelper.stringFromIS(assets.open("example_subtitle.srt")))
    }

    override fun getVideoContent(): Uri {
        return Uri.parse("/android_asset/example_video.mp4")
    }

    override fun save() {
        Snackbar.make(app_bar, R.string.INFO_not_available_in_example, Snackbar.LENGTH_SHORT).show()
    }

}
