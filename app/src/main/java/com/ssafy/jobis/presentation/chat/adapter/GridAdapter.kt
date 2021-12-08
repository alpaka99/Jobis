package com.ssafy.jobis.presentation.chat.adapter

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.ssafy.jobis.R
import com.ssafy.jobis.presentation.chat.viewholder.GIFViewHolder
import com.waynejo.androidndkgif.GifDecoder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.lang.StringBuilder
import java.util.*
import kotlin.collections.ArrayList

class GridAdapter(private val fragmentActivity: FragmentActivity): RecyclerView.Adapter<GIFViewHolder>() {

    private var gifBitmap = ArrayList<ImageDecoder.Source>()
    private var filePathes = ArrayList<String>()

    init {
        getGif()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GIFViewHolder {
        return GIFViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_gif, parent, false), fragmentActivity)
    }

    override fun onBindViewHolder(holder: GIFViewHolder, position: Int) {
        if (position <  gifBitmap.size) {
            holder.bind(gifBitmap[position], filePathes[position])
        }
    }

    override fun getItemCount(): Int {
        return gifBitmap.size
    }

    fun getGif() {
        gifBitmap.clear()
        filePathes.clear()

        val pref = PreferenceManager.getDefaultSharedPreferences(fragmentActivity)
        val files = pref.getString("gif", "")
        val st = StringTokenizer(files, "#")
        val sb = StringBuilder()
        CoroutineScope(Dispatchers.IO).launch {

            while (st.hasMoreTokens()) {
                val path = st.nextToken()
                val file = File(path)

                val gifFile = Uri.fromFile(file)
                val fileName = "${gifFile.lastPathSegment}"

                if (file.exists()) {
                    sb.append(file).append("#")
                    val source = ImageDecoder.createSource(file)
                    gifBitmap.add(source)
                    filePathes.add(fileName)
                }
            }
            pref.edit().apply {
                putString("gif", sb.toString())
                apply()
            }
            CoroutineScope(Dispatchers.Main).launch {
                notifyDataSetChanged()
            }

            Log.d("이미지 모두 가져오기 성공","ㅇㅇㅇ")
        }
    }
}