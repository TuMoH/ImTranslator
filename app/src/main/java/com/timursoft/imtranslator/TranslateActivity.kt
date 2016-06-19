package com.timursoft.imtranslator

import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import com.timursoft.subtitleparser.FormatSRT
import com.timursoft.subtitleparser.IOHelper
import com.timursoft.subtitleparser.Subtitle
import com.timursoft.subtitleparser.SubtitleObject
import kotlinx.android.synthetic.main.activity_translate.*
import java.io.IOException
import java.util.*

class TranslateActivity : AppCompatActivity() {

    companion object {
        val FILE_PATH = "FILE_PATH"
        val EDITED = "EDITED"
    }

    var timer: Timer? = null
    var subtitleObject: SubtitleObject? = null
    var adapter: SubtitleRecyclerAdapter? = null
    var lastPosition = 0
    val subtitles = ArrayList<Subtitle>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_translate)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        try {
//            val filePath = intent.getStringExtra(FILE_PATH)
            val formatSRT = FormatSRT()
            subtitleObject = formatSRT.parse(IOHelper.streamToString(assets.open("example.srt")))
            if (subtitleObject != null) {
                subtitles.addAll(subtitleObject!!.subtitles.values)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        adapter = SubtitleRecyclerAdapter(subtitles)
        recycler.adapter = adapter
        fast_scroller.setRecyclerView(recycler)
        recycler.addOnScrollListener(fast_scroller.onScrollListener)
        recycler.addOnScrollListener(MyScrollListener())

        video.setOnTouchListener { view, motionEvent ->
            if (MotionEvent.ACTION_DOWN == motionEvent.action) {
                if (video.isPlaying) {
                    video.pause()
                    ic_play_pause.visibility = View.VISIBLE
                    timer?.cancel()
                    timer = null
                } else {
                    ic_play_pause.visibility = View.GONE
                    video.start()
//                    createTimer()
                }
                return@setOnTouchListener true
            }
            return@setOnTouchListener false
        }
        video.setVideoPath(Environment.getExternalStorageDirectory().absolutePath + "/example.mp4")
        video.requestFocus(0)
    }

    inner class MyScrollListener : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val firstVisibleItem = (recyclerView?.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition();
            if (firstVisibleItem != lastPosition) {
                lastPosition = firstVisibleItem
                video.seekTo(subtitles[lastPosition].startTime)
            }
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
        }
    }

    fun createTimer() {
        timer?.cancel()
        timer = null

        var pos = -1
        val videoTime = video.currentPosition
        subtitles.forEach {
            if (videoTime >= it.startTime && videoTime < it.endTime) {
                val delay = it.endTime - videoTime

                runOnUiThread({ (recycler.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(pos, 0) })

                timer = Timer()
                timer!!.schedule(ScrollTimerTask(), delay.toLong())
                return@forEach
            } else if (videoTime < it.startTime) {
                val delay = it.startTime - videoTime
                timer = Timer()
                timer!!.schedule(ScrollTimerTask(), delay.toLong())
                return@forEach
            }
            pos++
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_translate, menu)
        return true
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            android.R.id.home -> onBackPressed()
//            R.id.action_save ->
        }
        return super.onOptionsItemSelected(menuItem)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putSerializable(EDITED, adapter?.edited)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        val edited = savedInstanceState?.getSerializable(EDITED) as HashMap<Int, String>
        adapter?.edited = edited
    }

    inner class ScrollTimerTask : TimerTask() {
        override fun run() {
            createTimer()
        }
    }

}
