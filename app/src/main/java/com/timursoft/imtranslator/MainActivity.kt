package com.timursoft.imtranslator

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View

import com.nbsp.materialfilepicker.MaterialFilePicker
import com.nbsp.materialfilepicker.ui.FilePickerActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.util.regex.Pattern
import permissions.dispatcher.RuntimePermissions
import permissions.dispatcher.NeedsPermission
import android.Manifest

@RuntimePermissions
class MainActivity : AppCompatActivity() {

    companion object {
        val TAG = "#ImTrans"
        val FILE_PICKER_RESULT_CODE = 1
        val SUB_FILE_PATTERN = Pattern.compile(".*\\.(srt|ass|ssa)$")!!
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
    }

    fun onClick(view: View) {
        when (view.id) {
            R.id.button -> {
                MainActivityPermissionsDispatcher.showFilePickerWithCheck(this)
            }
            R.id.button_example -> {
                startActivity(Intent(this, ExampleTranslateActivity::class.java))
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILE_PICKER_RESULT_CODE) {
            if (resultCode == RESULT_OK) {
                val filePath = data?.getStringExtra(FilePickerActivity.RESULT_FILE_PATH)
                val intent = Intent(this, TranslateActivity::class.java)
                intent.putExtra(TranslateActivity.FILE_PATH, filePath)
                startActivity(intent)
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

}
