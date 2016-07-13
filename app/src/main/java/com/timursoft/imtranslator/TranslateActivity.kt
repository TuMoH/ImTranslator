package com.timursoft.imtranslator

import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
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
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subjects.PublishSubject
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.util.*
import java.util.concurrent.TimeUnit

open class TranslateActivity : AppCompatActivity() {

    companion object {
        val FILE_PATH = "FILE_PATH"
        val EDITED = "EDITED"
        val VIDEO_OFFSET = 80
    }

    var subtitleObject: SubtitleObject? = null
    var adapter: SubtitleRecyclerAdapter? = null
    var layoutManager: LinearLayoutManager? = null
    val subtitles = ArrayList<Subtitle>()
    val scrollListener = MyScrollListener()
    val publishSubject: PublishSubject<Int> = PublishSubject.create()

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
        recycler_view.adapter = adapter
        layoutManager = recycler_view.layoutManager as LinearLayoutManager
        fast_scroller.setRecyclerView(recycler_view)
        recycler_view.addOnScrollListener(fast_scroller.onScrollListener)
        fast_scroller.listener = { position -> videoGoTo(position) }
        recycler_view.addOnScrollListener(scrollListener)

        video_view.setOnTouchListener { view, motionEvent ->
            if (MotionEvent.ACTION_DOWN == motionEvent.action) {
                if (video_view.isPlaying) {
                    video_view.pause()
                    ic_play_pause.visibility = View.VISIBLE
                } else {
                    ic_play_pause.visibility = View.GONE
                    video_view.start()
                    publishSubject.onNext(getCurrentSubtitlePosition())
                }
                return@setOnTouchListener true
            }
            return@setOnTouchListener false
        }
        setVideoContent(video_view)
        video_view.requestFocus(0)

        val psShare = publishSubject.share()

        psShare.observeOn(AndroidSchedulers.mainThread())
                .subscribe { layoutManager?.scrollToPositionWithOffset(it, 0) }

        psShare.map { it + 1 }
                .filter { it < subtitles.size }
                .delay {
                    val delay = subtitles[it].startTime - video_view.currentPosition
                    Observable.timer(delay.toLong(), TimeUnit.MILLISECONDS)
                }
                .filter { video_view.isPlaying }
                // todo проверить не было ли скролла
                // todo добавить подсветку итема
                .filter { scrollListener.currentState == RecyclerView.SCROLL_STATE_IDLE }
//                .filter {
//                    val subtitle = subtitles[it]
//                    val videoTime = video_view.currentPosition
//                    videoTime >= subtitle.startTime && videoTime < subtitle.endTime
//                }
                .subscribe { publishSubject.onNext(it) }
    }

    open fun getSubtitleIS(): InputStream {
        return FileInputStream(intent.getStringExtra(FILE_PATH))
    }

    open fun setVideoContent(videoView: VideoView) {
        video_view.setVideoPath(Environment.getExternalStorageDirectory().absolutePath + "/example.mp4")
    }

    inner class MyScrollListener : RecyclerView.OnScrollListener() {
        var lastPosition = 0
        var currentState = 0
        override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            currentState = newState
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                val firstVisibleItem = (recyclerView?.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                if (firstVisibleItem != lastPosition) {
                    lastPosition = firstVisibleItem
                    videoGoTo(firstVisibleItem)
                }
            }
        }
    }

    fun getCurrentSubtitlePosition(): Int {
        val videoTime = video_view.currentPosition
        for (i in 0..subtitles.size - 1) {
            if (videoTime <= subtitles[i].endTime) {
                return if (i > 0) i - 1 else 0
            }
        }
        return 0
    }

    fun videoGoTo(position: Int) {
        publishSubject.onNext(position)
        video_view.seekTo(subtitles[position].startTime)
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

}
