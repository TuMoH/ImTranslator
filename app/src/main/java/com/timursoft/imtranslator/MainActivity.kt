package com.timursoft.imtranslator

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.jakewharton.rxbinding.view.clicks
import com.nbsp.materialfilepicker.MaterialFilePicker
import com.nbsp.materialfilepicker.ui.FilePickerActivity
import com.timursoft.imtranslator.entity.SubFile
import com.timursoft.imtranslator.entity.SubFileEntity
import com.timursoft.imtranslator.entity.WrappedSubEntity
import com.timursoft.suber.IOHelper
import com.timursoft.suber.ParserASS
import com.timursoft.suber.ParserSRT
import com.timursoft.suber.SubFileObject
import com.timursoft.suber.Suber.suber
import io.requery.Persistable
import io.requery.android.QueryRecyclerAdapter
import io.requery.query.Result
import io.requery.rx.SingleEntityStore
import kotlinx.android.synthetic.main.activity_main.*
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.RuntimePermissions
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.io.File
import java.util.regex.Pattern
import javax.inject.Inject

@RuntimePermissions
class MainActivity : AppCompatActivity() {

    companion object {
        val TAG = "#ImTrans"
        val FILE_PICKER_RESULT_CODE = 1
        val SUB_FILE_PATTERN = Pattern.compile(".*\\.(srt|ass|ssa)$")!!
    }

    @Inject
    lateinit var dataStore: SingleEntityStore<Persistable>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        MyApplication.appComponent.inject(this)

        button.clicks().subscribe { MainActivityPermissionsDispatcher.showFilePickerWithCheck(this) }

        bookshelf.addItemDecoration(DividerItemDecoration(this))
        bookshelf.adapter = SubFileAdapter()

        val count = dataStore.count(SubFile::class.java).get().value()
        if (count == 0) {
            val subFile = SubFileEntity()
            subFile.filePath = "example"
            subFile.videoPath = "/android_asset/example_video.mp4"
            subFile.name = "example"
            subFile.uptime = 0

            val subFileObject = ParserSRT().parse(IOHelper.stringFromIS(assets.open("example_subtitle.srt")))
            wrapSubsAndFillSubFile(subFileObject, subFile)
            dataStore.insert(subFile).subscribe { (bookshelf.adapter as SubFileAdapter).queryAsync() }
        }
    }

    override fun onResume() {
        super.onResume()
        (bookshelf.adapter as SubFileAdapter).queryAsync()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILE_PICKER_RESULT_CODE) {
            if (resultCode == RESULT_OK) {
                val filePath = data?.getStringExtra(FilePickerActivity.RESULT_FILE_PATH)

                val oldSubFile = dataStore.select(SubFile::class.java)
                        .where(SubFileEntity.FILE_PATH.eq(filePath)).get().firstOrNull()
                if (oldSubFile != null) {
                    toTranslateActivity(oldSubFile)
                    return
                }
                Observable.just(filePath)
                        .observeOn(Schedulers.computation())
                        .map { File(it) }
                        .subscribe({ file ->
                            val subFile = SubFileEntity()
                            subFile.filePath = file.absolutePath
                            subFile.name = file.name
                            subFile.uptime = file.lastModified()

                            wrapSubsAndFillSubFile(suber().parse(file), subFile)

                            dataStore.insert(subFile)
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe {
                                        toTranslateActivity(it)
                                    }
                        }, { Log.e(TAG, "Не удалось распарсить Файл субтитров", it) })
            } else {
                Log.e(TAG, "File not found. resultCode = " + resultCode)
            }
        }
    }

    private fun wrapSubsAndFillSubFile(subFileObject: SubFileObject, subFile: SubFile) {
        subFileObject.subs.forEach { sub ->
            val wrappedSub = WrappedSubEntity()
            wrappedSub.sub = sub
            wrappedSub.time = ParserASS.serializeTime(sub.startTime) +
                    " - " + ParserASS.serializeTime(sub.endTime)
            wrappedSub.originalContent = sub.content
            wrappedSub.subFile = subFile
            subFile.subs.add(wrappedSub)
        }
    }

    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun showFilePicker() {
        MaterialFilePicker()
                .withActivity(this)
                .withRequestCode(FILE_PICKER_RESULT_CODE)
                .withFilter(SUB_FILE_PATTERN)
                .start()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults)
    }

    fun toTranslateActivity(subFile: SubFile) {
        val intent = Intent(this, TranslateActivity::class.java)
        intent.putExtra(TranslateActivity.SUB_FILE, subFile.id)
        subFile.subs
        startActivity(intent)
    }

    private inner class SubFileAdapter() : QueryRecyclerAdapter<SubFileEntity,
            ViewHolder>(SubFileEntity.`$TYPE`), View.OnClickListener {

        override fun performQuery(): Result<SubFileEntity> {
            return dataStore.select(SubFileEntity::class.java).orderBy(SubFileEntity.ID.desc()).get()
        }

        override fun onBindViewHolder(item: SubFileEntity, holder: ViewHolder, position: Int) {
            holder.name.text = item.name
            holder.percent.text = item.percent.toString() + "%"
            holder.itemView.tag = item
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.file_item, parent, false)
            view.setOnClickListener(this)
            return ViewHolder(view)
        }

        override fun onClick(v: View) {
            val subFile = v.tag as? SubFile
            if (subFile != null) {
                toTranslateActivity(subFile)
            }
        }
    }

    internal class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView
        val percent: TextView

        init {
            name = view.findViewById(R.id.file_item_name) as TextView
            percent = view.findViewById(R.id.file_item_percent) as TextView
        }
    }

    private inner class DividerItemDecoration(context: Context) : RecyclerView.ItemDecoration() {
        lateinit var divider: Drawable

        init {
            val styledAttributes = context.obtainStyledAttributes(intArrayOf(android.R.attr.listDivider))
            divider = styledAttributes.getDrawable(0)
            styledAttributes.recycle()
        }

        override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
            val left = parent.paddingLeft
            val right = parent.width - parent.paddingRight

            val childCount = parent.childCount
            for (i in 0..childCount - 1) {
                val child = parent.getChildAt(i)

                val params = child.layoutParams as RecyclerView.LayoutParams

                val top = child.bottom + params.bottomMargin
                val bottom = top + divider.intrinsicHeight

                divider.setBounds(left, top, right, bottom)
                divider.draw(c)
            }
        }
    }

}
