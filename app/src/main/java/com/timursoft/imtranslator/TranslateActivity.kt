package com.timursoft.imtranslator

import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import com.devbrackets.android.exomedia.core.exoplayer.EMExoPlayer
import com.devbrackets.android.exomedia.core.listener.ExoPlayerListener
import com.devbrackets.android.exomedia.listener.OnPreparedListener
import com.google.android.exoplayer.ExoPlayer
import com.jakewharton.rxbinding.view.touches
import com.nbsp.materialfilepicker.MaterialFilePicker
import com.nbsp.materialfilepicker.ui.FilePickerActivity
import com.timursoft.imtranslator.entity.SubFile
import com.timursoft.imtranslator.entity.SubFileEntity
import com.timursoft.suber.Suber.suber
import com.trello.rxlifecycle.components.support.RxAppCompatActivity
import com.trello.rxlifecycle.kotlin.bindToLifecycle
import io.requery.Persistable
import io.requery.rx.SingleEntityStore
import kotlinx.android.synthetic.main.activity_translate.*
import rx.Observable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subjects.PublishSubject
import java.io.File
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern
import javax.inject.Inject

open class TranslateActivity : RxAppCompatActivity() {

    companion object {
        val SUB_FILE = "SUB_FILE"
        val VIDEO_OFFSET = 300
        val FILE_PATH_PATTERN = Pattern.compile("^(.*)/([^/]*)\\.[^\\./]*$")!!
        val VIDEO_FILE_PATTERN = Pattern.compile(".*\\.(mp4|3gp|ts|webm|mkv)$")!!
    }

    private lateinit var adapter: SubtitleRecyclerAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var subFile: SubFile
    private val publishSubject: PublishSubject<Int> = PublishSubject.create()
    private var delaySubscription: Subscription? = null
    private var lastPlayedPosition = 0
    private var videoEnded = false

    @Inject
    lateinit var dataStore: SingleEntityStore<Persistable>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_translate)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        MyApplication.appComponent.inject(this)

        subFile = getSubFile()

        recycler_view.addItemDecoration(BottomDecorator(subFile.subs.size - 1))
        adapter = SubtitleRecyclerAdapter(subFile.subs)
        recycler_view.adapter = adapter
        layoutManager = recycler_view.layoutManager as LinearLayoutManager
        layoutManager.scrollToPositionWithOffset(subFile.lastPosition, 0)

        (recycler_layout.layoutParams as CoordinatorLayout.LayoutParams).behavior =
                StopVideoBehavior(touchListener = { view, motionEvent ->
                    if (MotionEvent.ACTION_DOWN == motionEvent.action && video_view.isPlaying) {
                        pause()
                    }
                })

        video_view.touches()
                .filter { MotionEvent.ACTION_DOWN == it.action }
                .subscribe {
                    if (video_view.isPlaying) {
                        pause()
                    } else {
                        play()
                    }
                }
        video_view.setMeasureBasedOnAspectRatioEnabled(true)
        video_view.setOnPreparedListener(OnPreparedListener {
            video_view.layoutParams.height = 10000
            if (!video_view.isPlaying) {
                ic_play_pause.visibility = View.VISIBLE
            }
        })
        video_view.setVideoUri(getVideoContent())
        video_view.addPlayerListener(object : ExoPlayerListener {
            override fun onStateChanged(playWhenReady: Boolean, playbackState: Int) {
                if (playbackState == ExoPlayer.STATE_ENDED) {
                    ic_play_pause.visibility = View.VISIBLE
                    videoEnded = true
                }
            }

            override fun onVideoSizeChanged(width: Int, height: Int, unAppliedRotationDegrees: Int, pixelWidthHeightRatio: Float) {
            }

            override fun onError(emExoPlayer: EMExoPlayer?, e: Exception?) {
            }

            override fun onSeekComplete() {
            }
        })
        video_view.requestFocus(View.FOCUSABLES_ALL)
    }

    override fun onResume() {
        super.onResume()

        publishSubject.filter { video_view.isPlaying }
                .doOnEach { lastPlayedPosition = it.value as Int }
                .observeOn(AndroidSchedulers.mainThread())
                .bindToLifecycle(this)
                .subscribe {
                    // todo нужен плавный скролл
                    layoutManager.scrollToPositionWithOffset(it, 0)
                }
    }

    override fun onPause() {
        super.onPause()
        pause()
        save()
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
                .filter { it < subFile.subs.size }
                .delay {
                    var delay = subFile.subs[it].sub.startTime - video_view.currentPosition - VIDEO_OFFSET
                    if (delay < 0) delay = 0
                    Observable.timer(delay.toLong(), TimeUnit.MILLISECONDS)
                }
                // добавить подсветку итема ???
                .subscribe { publishSubject.onNext(it) }

        if (videoEnded) {
            videoEnded = false
            lastPlayedPosition = 0
            video_view.restart()
        }
        val position = layoutManager.findFirstVisibleItemPosition()
        if (position != lastPlayedPosition) {
            video_view.seekTo(subFile.subs[position].sub.startTime - VIDEO_OFFSET)
        }
        ic_play_pause.visibility = View.GONE
        video_view.start()
        publishSubject.onNext(position)
    }

    protected open fun getSubFile(): SubFile {
        return dataStore.select(SubFile::class.java).where(SubFileEntity.ID.eq(intent.getIntExtra(SUB_FILE, 0))).get().first()
    }

    protected open fun getVideoContent(): Uri? {
        if (subFile.videoPath != null) {
            return Uri.parse(subFile.videoPath)
        }

        val subPath = subFile.filePath

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
                        .setAction(R.string.check) {
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
                    subFile.videoPath = videoFile.absolutePath
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
                subFile.videoPath = filePath
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
        Observable.just(layoutManager.findFirstVisibleItemPosition())
                .observeOn(Schedulers.computation())
                .subscribe({ position ->
                    subFile.lastPosition = position
                    MainActivity.updatePercent(subFile)

                    if (!subFile.name.equals(MainActivity.EXAMPLE_FILE_NAME)) {
                        val file = File(subFile.filePath)
                        val sfo = suber().parse(file)

                        adapter.modified.forEach { i ->
                            sfo.subs[i] = subFile.subs[i].sub
                        }
                        suber().serialize(sfo, file)
                        subFile.uptime = file.lastModified()
                        adapter.modified.clear()
                    }

                    dataStore.update(subFile.subs).subscribe()
                    dataStore.update(subFile)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe { Snackbar.make(app_bar, R.string.INFO_saved, Snackbar.LENGTH_SHORT).show() }
                }, {
                    Snackbar.make(app_bar, R.string.ERROR_save, Snackbar.LENGTH_SHORT).show()
                    Log.e(MainActivity.TAG, "Не удалось сохранить!", it)
                })
    }

    class StopVideoBehavior(var touchListener: ((View, MotionEvent) -> Unit)) : CoordinatorLayout.Behavior<View>() {
        override fun onInterceptTouchEvent(parent: CoordinatorLayout, child: View, ev: MotionEvent): Boolean {
            touchListener.invoke(child, ev)
            return false
        }
    }

    inner class BottomDecorator(val lastPosition: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            super.getItemOffsets(outRect, view, parent, state)
            if (lastPosition == parent.getChildAdapterPosition(view)) {
                val lp = (view.layoutParams as RecyclerView.LayoutParams)
                view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
                val itemHeight = view.measuredHeight + lp.topMargin + lp.bottomMargin
                outRect.bottom = parent.height - itemHeight
            }
        }
    }

}
