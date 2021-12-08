package com.ssafy.jobis.presentation.chat.viewholder

import android.graphics.ImageDecoder
import android.graphics.drawable.AnimatedImageDrawable
import android.graphics.drawable.Drawable
import android.media.Image
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.ssafy.jobis.R

class GIFViewHolder(view: View, private val fragmentActivity: FragmentActivity): RecyclerView.ViewHolder(view) {

    interface OnClickGIFListener {
        fun chooseGIF(source: ImageDecoder.Source?, fileName: String)
    }
    private val img: ImageView = view.findViewById(R.id.img_gif)

    fun bind(source: ImageDecoder.Source?, fileName: String) {
        if (source != null) {
            img.setImageBitmap(ImageDecoder.decodeBitmap(source))
            img.setOnClickListener {
                if (fragmentActivity is OnClickGIFListener) {
                    fragmentActivity.chooseGIF(source, fileName)
                }
            }
        }
    }
}