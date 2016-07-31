package com.timursoft.imtranslator

import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import com.timursoft.imtranslator.entity.WrappedSub
import java.util.*

/**
 * Created by TuMoH on 05.06.2016.
 */
class SubtitleRecyclerAdapter(private val subtitles: List<WrappedSub>) : RecyclerView.Adapter<SubtitleRecyclerAdapter.SubtitleVH>() {

    val modified = ArrayList<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubtitleVH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.subtitle_item, parent, false)
        return SubtitleVH(v, TextChangeListener())
    }

    override fun onBindViewHolder(holder: SubtitleVH, position: Int) {
        holder.line.text = (position + 1).toString()
        holder.time.text = subtitles[position].time
        holder.content.text = subtitles[position].originalContent
        holder.textChangeListener.position = position
        holder.textChangeListener.mute = true
        holder.newContent.setText(subtitles[position].sub.content)
        holder.textChangeListener.mute = false
    }

    override fun getItemCount(): Int {
        return subtitles.size
    }

    class SubtitleVH internal constructor(itemView: View, internal val textChangeListener: TextChangeListener) : RecyclerView.ViewHolder(itemView) {
        internal var line: TextView
        internal var time: TextView
        internal var content: TextView
        internal var newContent: EditText

        init {
            line = itemView.findViewById(R.id.line) as TextView
            time = itemView.findViewById(R.id.time_text) as TextView
            content = itemView.findViewById(R.id.content) as TextView
            newContent = itemView.findViewById(R.id.newContent) as EditText
            newContent.addTextChangedListener(textChangeListener)
        }
    }

    inner class TextChangeListener : TextWatcher {
        var position: Int = -1
        var mute: Boolean = false
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (!mute) {
                subtitles[position].sub.content = s.toString()
                modified.add(position)
            }
        }

        override fun afterTextChanged(s: Editable?) {
        }
    }

}
