package com.timursoft.imtranslator

import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.VideoView
import com.timursoft.subtitleparser.FormatSRT
import com.timursoft.subtitleparser.IOHelper
import com.timursoft.subtitleparser.Subtitle
import com.timursoft.subtitleparser.SubtitleObject
import kotlinx.android.synthetic.main.activity_translate.*
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.util.*

open class TranslateActivity : AppCompatActivity() {

    companion object {
        val FILE_PATH = "FILE_PATH"
        val EDITED = "EDITED"
    }

    var timer: Timer? = null
    var subtitleObject: SubtitleObject? = null
    var adapter: SubtitleRecyclerAdapter? = null
    val subtitles = ArrayList<Subtitle>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_translate)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        try {
            val formatSRT = FormatSRT()
            subtitleObject = formatSRT.parse(IOHelper.streamToString(getSubtitleIS()))
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
        fast_scroller.listener = { position -> videoGoTo(position) }
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
                    scrollRecycler()
                }
                return@setOnTouchListener true
            }
            return@setOnTouchListener false
        }
        setVideoContent(video)
        video.seekTo(28000)
        video.requestFocus(0)
    }

    open fun getSubtitleIS(): InputStream {
        return FileInputStream(intent.getStringExtra(FILE_PATH))
    }

    open fun setVideoContent(videoView: VideoView) {
        video.setVideoPath(Environment.getExternalStorageDirectory().absolutePath + "/example.mp4")
    }

    inner class MyScrollListener : RecyclerView.OnScrollListener() {
        var lastPosition = 0
        override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == 0) {
                val firstVisibleItem = (recyclerView?.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition();
                if (firstVisibleItem != lastPosition) {
                    lastPosition = firstVisibleItem
                    videoGoTo(firstVisibleItem)
                }
            }
        }
    }

    fun scrollRecycler() {
        timer?.cancel()
        timer = null

        val videoTime = video.currentPosition
        for (i in 0..subtitles.size) {
            val subtitle = subtitles[i]
            if (videoTime < subtitle.endTime) {
                val delay: Int
                if (videoTime >= subtitle.startTime) {
                    runOnUiThread({ (recycler.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(i, 0) })
                    delay = subtitles[i + 1].startTime - videoTime
                } else {
                    delay = subtitle.startTime - videoTime
                }
                timer = Timer()
                timer!!.schedule(ScrollTimerTask(), delay.toLong())
                return
            }
        }
    }

    fun videoGoTo(position: Int) {
        video.seekTo(subtitles[position].startTime)
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
            scrollRecycler()
        }
    }

}
