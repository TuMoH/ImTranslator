package com.timursoft.imtranslator

import android.Manifest
import android.content.Intent
import android.databinding.ViewDataBinding
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jakewharton.rxbinding.view.clicks
import com.nbsp.materialfilepicker.MaterialFilePicker
import com.nbsp.materialfilepicker.ui.FilePickerActivity
import com.timursoft.imtranslator.databinding.FileItemBinding
import com.timursoft.imtranslator.entity.SubFile
import com.timursoft.imtranslator.entity.SubFileEntity
import com.timursoft.imtranslator.entity.WrappedSubEntity
import com.timursoft.suber.ParserASS
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
import rx.lang.kotlin.onError
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
        example.clicks().subscribe { startActivity(Intent(this, ExampleTranslateActivity::class.java)) }

        bookshelf.adapter = SubFileAdapter()
    }

    override fun onResume() {
        super.onResume()
        (bookshelf.adapter as SubFileAdapter).queryAsync()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILE_PICKER_RESULT_CODE) {
            if (resultCode == RESULT_OK) {
                Observable.just(data?.getStringExtra(FilePickerActivity.RESULT_FILE_PATH))
//                        .doOnEach { } // todo progress
                        .subscribeOn(Schedulers.computation())
                        .map { File(it) }
                        .map { file ->
                            val subFile = SubFileEntity()
                            subFile.filePath = file.absolutePath
                            subFile.name = file.name
                            subFile.uptime = file.lastModified()

                            suber().parse(file).subs.forEach { sub ->
                                val wrappedSub = WrappedSubEntity()
                                wrappedSub.sub = sub
                                wrappedSub.time = ParserASS.serializeTime(sub.startTime) +
                                        " - " + ParserASS.serializeTime(sub.endTime)
                                wrappedSub.originalContent = sub.content
                                wrappedSub.subFile = subFile
                                subFile.subs.add(wrappedSub)
                            }
                            dataStore.insert(subFile).subscribe()
                            subFile
                        }
                        .onError { Log.e(TAG, "Не удалось распарсить Файл субтитров", it) }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            // todo hide progress
                            toTranslateActivity(it)
                        }
            } else {
                Log.e(TAG, "File not found. resultCode = " + resultCode)
            }
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
        intent.putExtra(TranslateActivity.SUB_FILE, subFile)
        intent.putExtra(TranslateActivity.FILE_PATH, subFile.filePath)
        startActivity(intent)
    }

    private inner class SubFileAdapter internal constructor() : QueryRecyclerAdapter<SubFileEntity,
            BindingHolder<FileItemBinding>>(SubFileEntity.`$TYPE`), View.OnClickListener {

        override fun performQuery(): Result<SubFileEntity> {
            // this is all persons in the db sorted by their name
            // note this method in executed in a background thread.
            // (Alternatively RxJava w/ RxBinding could be used)
            return dataStore.select(SubFileEntity::class.java).get()
        }

        override fun onBindViewHolder(item: SubFileEntity, holder: BindingHolder<FileItemBinding>,
                                      position: Int) {
            holder.binding.subFile = item
            holder.binding.position = position
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingHolder<FileItemBinding> {
            val inflater = LayoutInflater.from(parent.context)
            val binding = FileItemBinding.inflate(inflater)
            binding.root.tag = binding
            binding.root.setOnClickListener(this)
            return BindingHolder(binding)
        }

        override fun onClick(v: View) {
            val binding = v.tag as? FileItemBinding
            if (binding != null) {
                toTranslateActivity(binding.subFile)
            }
        }
    }

    internal class BindingHolder<B : ViewDataBinding>(val binding: B) : RecyclerView.ViewHolder(binding.root)

}
