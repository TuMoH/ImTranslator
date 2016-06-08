package com.timursoft.imtranslator

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.animation.OvershootInterpolator
import com.timursoft.subtitleparser.FormatSRT
import com.timursoft.subtitleparser.Subtitle
import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter
import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter
import jp.wasabeef.recyclerview.adapters.SlideInLeftAnimationAdapter
import jp.wasabeef.recyclerview.adapters.SlideInRightAnimationAdapter
import jp.wasabeef.recyclerview.animators.FadeInAnimator
import jp.wasabeef.recyclerview.animators.LandingAnimator
import jp.wasabeef.recyclerview.animators.SlideInDownAnimator
import kotlinx.android.synthetic.main.activity_translate.*
import java.io.IOException
import java.util.*

class TranslateActivity : AppCompatActivity() {

    companion object {
        val FILE_PATH = "FILE_PATH"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_translate)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val subtitles = ArrayList<Subtitle>()
        try {
            val formatSRT = FormatSRT()
            val timedTextObject = formatSRT.parseFile("name", assets.open("example.srt"))
            subtitles.addAll(timedTextObject.subtitles.values)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        val adapter = SubtitleRecyclerAdapter(subtitles)
        val alphaAdapter = ScaleInAnimationAdapter(adapter);
        alphaAdapter.setFirstOnly(true);
        alphaAdapter.setDuration(500);
        alphaAdapter.setInterpolator(OvershootInterpolator(.5f));
        recycler.adapter = alphaAdapter
        recycler.itemAnimator = FadeInAnimator()

        fast_scroller.setRecyclerView(recycler)
        recycler.addOnScrollListener(fast_scroller.onScrollListener)

        //        String filePath = getIntent().getStringExtra(FILE_PATH)
        //
        //
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_translate, menu)
        return true
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        when(menuItem.itemId) {
            android.R.id.home -> onBackPressed()
//            R.id.action_save ->
        }
        return super.onOptionsItemSelected(menuItem)
    }

}
