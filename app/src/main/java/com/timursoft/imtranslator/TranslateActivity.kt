package com.timursoft.imtranslator

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import com.devbrackets.android.exomedia.listener.OnPreparedListener
import com.nbsp.materialfilepicker.MaterialFilePicker
import com.nbsp.materialfilepicker.ui.FilePickerActivity
import com.timursoft.subtitleparser.FormatSRT
import com.timursoft.subtitleparser.IOHelper
import com.timursoft.subtitleparser.Subtitle
import kotlinx.android.synthetic.main.activity_translate.*
import rx.Observable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.subjects.PublishSubject
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

open class TranslateActivity : AppCompatActivity() {

    companion object {
        val FILE_PATH = "FILE_PATH"
        val EDITED = "EDITED"
        val VIDEO_OFFSET = 300
        val FILE_PATH_PATTERN = Pattern.compile("^(.*)/([^/]*)\\.[^\\./]*$")!!
        val VIDEO_FILE_PATTERN = Pattern.compile(".*\\.(mp4|3gp|ts|webm|mkv)$")!!
    }

    private var adapter: SubtitleRecyclerAdapter? = null
    private var layoutManager: LinearLayoutManager? = null
    private val subtitles = ArrayList<Subtitle>()
    private val publishSubject: PublishSubject<Int> = PublishSubject.create()
    private var delaySubscription: Subscription? = null
    private var lastPlayedPosition = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_translate)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        try {
            // todo поддержка всех форматов
            val formatSRT = FormatSRT()
            val subtitleObject = formatSRT.parse(IOHelper.streamToString(getSubtitlesContent()))
            if (subtitleObject != null) {
                subtitles.addAll(subtitleObject.subtitles.values)
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
                        pause()
                    }
                })

        video_view.setOnTouchListener { view, motionEvent ->
            if (MotionEvent.ACTION_DOWN == motionEvent.action) {
                if (video_view.isPlaying) {
                    pause()
                } else {
                    play()
                }
                return@setOnTouchListener true
            }
            return@setOnTouchListener false
        }
        video_view.setMeasureBasedOnAspectRatioEnabled(true)
        video_view.setOnPreparedListener(OnPreparedListener {
            video_view.layoutParams.height = 10000
            ic_play_pause.visibility = View.VISIBLE
        })
        video_view.setVideoUri(getVideoContent())
        video_view.requestFocus(View.FOCUSABLES_ALL)

        publishSubject.filter { video_view.isPlaying }
                .doOnEach { lastPlayedPosition = it.value as Int }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    // todo нужен плавный скролл
                    layoutManager?.scrollToPositionWithOffset(it, 0)
                }
    }

    private fun pause() {
        video_view.pause()
        ic_play_pause.visibility = View.VISIBLE
        delaySubscription?.unsubscribe()
    }

    private fun play() {
        if (delaySubscription != null && !delaySubscription!!.isUnsubscribed) {
            delaySubscription?.unsubscribe()
        }
        delaySubscription = publishSubject.map { it + 1 }
                .filter { it < subtitles.size }
                .delay {
                    var delay = subtitles[it].startTime - video_view.currentPosition - VIDEO_OFFSET
                    if (delay < 0) delay = 0
                    Observable.timer(delay.toLong(), TimeUnit.MILLISECONDS)
                }
                // todo добавить подсветку итема ???
                // todo rxLifeCycle
                .subscribe { publishSubject.onNext(it) }

        val position = layoutManager!!.findFirstVisibleItemPosition()
        if (position != lastPlayedPosition) {
            video_view.seekTo(subtitles[position].startTime - VIDEO_OFFSET)
        }
        ic_play_pause.visibility = View.GONE
        video_view.start()
        publishSubject.onNext(position)
    }

    protected open fun getSubtitlesContent(): InputStream? {
        return FileInputStream(intent.getStringExtra(FILE_PATH))
    }

    protected open fun getVideoContent(): Uri? {
        val subPath = intent.getStringExtra(FILE_PATH)

        val matcher = FILE_PATH_PATTERN.matcher(subPath)
        if (matcher.find()) {
            val dir = matcher.group(1)
            val fileName = matcher.group(2)

            val videoFile = File(dir).listFiles().find {
                fileName.equals(it.nameWithoutExtension, true)
                        && !MainActivity.SUB_FILE_PATTERN.matcher(it.name).matches()
            }
            if (videoFile == null) {
                Snackbar.make(app_bar, R.string.ERROR_video_not_found, Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.ACTION_check) {
                            MaterialFilePicker()
                                    .withActivity(this)
                                    .withRequestCode(MainActivity.FILE_PICKER_RESULT_CODE)
                                    .withFilter(VIDEO_FILE_PATTERN)
                                    .start()
                        }
                        .show()
            } else {
                if (!VIDEO_FILE_PATTERN.matcher(videoFile.name).matches()) {
                    Snackbar.make(app_bar, R.string.ERROR_video_format_not_supported,
                            Snackbar.LENGTH_INDEFINITE).show()
                } else {
                    return Uri.parse(videoFile.absolutePath)
                }
            }
        }
        return null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MainActivity.FILE_PICKER_RESULT_CODE) {
            if (resultCode == RESULT_OK) {
                val filePath = data?.getStringExtra(FilePickerActivity.RESULT_FILE_PATH)
                video_view.setVideoUri(Uri.parse(filePath))
            } else {
                Log.e(MainActivity.TAG, "File not found. resultCode = " + resultCode)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_translate, menu)
        return true
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            android.R.id.home -> onBackPressed()
            R.id.action_save -> save()
        }
        return super.onOptionsItemSelected(menuItem)
    }

    protected open fun save() {
        // todo реализовать
        Snackbar.make(app_bar, R.string.INFO_saved, Snackbar.LENGTH_SHORT).show()
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
