package com.timursoft.imtranslator

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View

import com.nbsp.materialfilepicker.MaterialFilePicker
import com.nbsp.materialfilepicker.ui.FilePickerActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object {
        val TAG = "#ImTrans"
        private val CHOOSE_FILE_RESULT_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        startActivity(Intent(this, TranslateActivity::class.java))
    }

    fun onClick(view: View) {
        // todo check permission
        MaterialFilePicker()
                .withActivity(this)
                .withRequestCode(CHOOSE_FILE_RESULT_CODE)
//                .withFilter(Pattern.compile(".*\\.txt$")) // Filtering files and directories by file name using regexp
//                .withFilterDirectories(true) // Set directories filterable (false by default)
//                .withHiddenFiles(true) // Show hidden files and folders
                .start();
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CHOOSE_FILE_RESULT_CODE) {
            if (resultCode == RESULT_OK) {
                val filePath = data?.getStringExtra(FilePickerActivity.RESULT_FILE_PATH)
                Log.e(TAG, "Filepath = " + filePath)
                val intent = Intent(this, TranslateActivity::class.java)
                intent.putExtra(TranslateActivity.FILE_PATH, filePath)
                startActivity(intent)
            } else {
                Log.e(TAG, "File not found. resultCode = " + resultCode)
            }
        }
    }

}
