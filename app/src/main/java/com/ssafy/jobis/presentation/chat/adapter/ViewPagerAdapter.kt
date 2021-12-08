package com.ssafy.jobis.presentation.chat.adapter

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.preference.PreferenceManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.firebase.auth.FirebaseAuth
import com.ssafy.jobis.presentation.chat.DrawingFragment
import com.waynejo.androidndkgif.GifEncoder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class ViewPagerAdapter(private val fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity){

    interface CanvasListener {
        fun onSuccess(file: File)
    }

    private val canvasList:Array<DrawingFragment> by lazy {
        Array(24) { DrawingFragment() }
    }
    private val used:Array<Boolean> by lazy {
        Array(24) { false }
    }
    val bitmapList = ArrayList<Bitmap>()

    override fun getItemCount(): Int {
        return 24
    }

    override fun createFragment(position: Int): DrawingFragment {
        return canvasList[position]
    }

    fun isAdded(position: Int): Boolean {
        return used[position]
    }

    fun addView(position: Int) {
        used[position] = true
    }
    fun removeView(position: Int) {
        used[position] = false
    }
    fun unDo(position: Int) {
        canvasList[position].unDo()
    }
    fun reDo(position: Int) {
        canvasList[position].reDo()
    }

    @SuppressLint("ShowToast", "CommitPrefEdits")
    suspend fun saveView() {
        bitmapList.clear()

        for (i in used.indices) {
            if (used[i]) {
                bitmapList.add(canvasList[i].getBitmap())
            }
        }

        val userId = FirebaseAuth.getInstance().currentUser?.email!!.split("@")[0]

        if (bitmapList.size == 0) {
            Toast.makeText(fragmentActivity, "프레임을 선택해주세요", Toast.LENGTH_SHORT).show()
            return
        }

        if (bitmapList.size == 1) {
            val fileName = userId + "_" + System.currentTimeMillis().toString()+".png"
            val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), fileName)

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val fo = FileOutputStream(file)
                    bitmapList[0].compress(Bitmap.CompressFormat.PNG, 100, fo)
                    fo.close()
                } catch (e: Exception) {
                } finally {
                    savePathAndUpdate(file)
                }
            }.join()
        } else {
            val fileName = userId + "_" + System.currentTimeMillis().toString()+".gif"
            val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), fileName)

            val filePath = file.absolutePath
            val delayMs = 100;

            val wh = canvasList[0].getWH()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    encodeGIF(wh[0], wh[1], filePath, delayMs)
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    savePathAndUpdate(file)
                }
            }.join()
        }
    }

    private fun encodeGIF(w: Int, h: Int, filePath: String, delayMs: Int) {
        val gifEncoder = GifEncoder()
        gifEncoder.init(w, h, filePath, GifEncoder.EncodingType.ENCODING_TYPE_NORMAL_LOW_MEMORY)
        gifEncoder.setDither(false)
        for (bm in bitmapList) {
            gifEncoder.encodeFrame(bm, delayMs)
        }
        gifEncoder.close()
    }

    private suspend fun savePathAndUpdate(file: File) {
        val pref = PreferenceManager.getDefaultSharedPreferences(fragmentActivity)
        val files = pref.getString("gif", "").toString() + file.absolutePath + "#"
        pref.edit().apply {
            putString("gif", files)
            apply()
        }
        if (fragmentActivity is CanvasListener) {
            fragmentActivity.onSuccess(file)
        }
        Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).also {
            it.data = Uri.fromFile(file)
            fragmentActivity.sendBroadcast(it)
        }
    }
}