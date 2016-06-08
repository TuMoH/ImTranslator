package com.timursoft.imtranslator

import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView

import com.timursoft.subtitleparser.Subtitle
import com.timursoft.subtitleparser.Time

/**
 * Created by TuMoH on 05.06.2016.
 */
class SubtitleRecyclerAdapter(private val subtitles: List<Subtitle>) : RecyclerView.Adapter<SubtitleRecyclerAdapter.SubtitleVH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubtitleVH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.subtitle_item, parent, false)
        return SubtitleVH(v, TextChangeListener())
    }

    override fun onBindViewHolder(holder: SubtitleVH, position: Int) {
        holder.line.text = (position + 1).toString()
        holder.start.text = subtitles[position].start.getTime(Time.ASS_FORMAT) +
                " - " + subtitles[position].end.getTime(Time.ASS_FORMAT)
        holder.content.text = subtitles[position].content
        holder.textChangeListener.position = position
        holder.newContent.setText(subtitles[position].translatedContent ?: subtitles[position].content)
    }

    override fun getItemCount(): Int {
        return subtitles.size
    }

    class SubtitleVH internal constructor(itemView: View, internal val textChangeListener: TextChangeListener) : RecyclerView.ViewHolder(itemView) {
        internal var line: TextView
        internal var start: TextView
        internal var content: TextView
        internal var newContent: EditText

        init {
            line = itemView.findViewById(R.id.line) as TextView
            start = itemView.findViewById(R.id.start) as TextView
            content = itemView.findViewById(R.id.content) as TextView
            newContent = itemView.findViewById(R.id.newContent) as EditText
            newContent.addTextChangedListener(textChangeListener)
        }
    }

    inner class TextChangeListener : TextWatcher {
        var position: Int = -1
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            subtitles[position].translatedContent = s.toString()
        }

        override fun afterTextChanged(s: Editable?) {
        }

    }

}
