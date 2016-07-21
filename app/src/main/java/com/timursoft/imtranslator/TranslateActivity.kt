package com.timursoft.imtranslator

import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import com.devbrackets.android.exomedia.listener.OnPreparedListener
import com.timursoft.subtitleparser.FormatSRT
import com.timursoft.subtitleparser.IOHelper
import com.timursoft.subtitleparser.Subtitle
import com.timursoft.subtitleparser.SubtitleObject
import kotlinx.android.synthetic.main.activity_translate.*
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
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
    val publishSubject: PublishSubject<Int> = PublishSubject.create()
    var lastPlayedPosition = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_translate)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        try {
            // todo поддержка всех форматов
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

        (recycler_layout.layoutParams as CoordinatorLayout.LayoutParams).behavior =
                StopVideoBehavior(touchListener = { view, motionEvent ->
                    if (MotionEvent.ACTION_DOWN == motionEvent.action && video_view.isPlaying) {
                        video_view.pause()
                        ic_play_pause.visibility = View.VISIBLE
                    }
                })

        video_view.setOnTouchListener { view, motionEvent ->
            if (MotionEvent.ACTION_DOWN == motionEvent.action) {
                if (video_view.isPlaying) {
                    video_view.pause()
                    ic_play_pause.visibility = View.VISIBLE
                } else {
                    val position = layoutManager!!.findFirstVisibleItemPosition()
                    if (position != lastPlayedPosition) {
                        video_view.seekTo(subtitles[position].startTime - VIDEO_OFFSET)
                    }
                    ic_play_pause.visibility = View.GONE
                    video_view.start()
                    publishSubject.onNext(position)
                }
                return@setOnTouchListener true
            }
            return@setOnTouchListener false
        }
        video_view.setMeasureBasedOnAspectRatioEnabled(true)
        // todo 1000 мало
        video_view.setOnPreparedListener(OnPreparedListener { video_view.layoutParams.height = 1000 })
        setVideoContent(video_view)
        video_view.requestFocus(View.FOCUSABLES_ALL)

        val psShare = publishSubject.share()

        psShare.doOnEach { lastPlayedPosition = it.value as Int }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    // todo нужен плавный скролл
                    layoutManager?.scrollToPositionWithOffset(it, 0)
                }

        psShare.map { it + 1 }
                .filter { it < subtitles.size }
                .delay {
                    val delay = subtitles[it].startTime - video_view.currentPosition
                    Observable.timer(delay.toLong(), TimeUnit.MILLISECONDS)
                }
                // todo добавить подсветку итема ???
                // todo отписаться при стопе
                // todo rxLifeCycle
                .filter { video_view.isPlaying }
                .subscribe { publishSubject.onNext(it) }
    }

    open fun getSubtitleIS(): InputStream {
        return FileInputStream(intent.getStringExtra(FILE_PATH))
    }

    open fun setVideoContent(videoView: VideoView) {
        // todo
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

    class StopVideoBehavior(var touchListener: ((View, MotionEvent) -> Unit)) : CoordinatorLayout.Behavior<View>() {
        override fun onInterceptTouchEvent(parent: CoordinatorLayout, child: View, ev: MotionEvent): Boolean {
            touchListener.invoke(child, ev)
            return false
        }
    }

}
