package com.example.emotionstatusemoji

import android.text.InputFilter
import android.text.Spanned
import android.widget.Toast

class EmojiFilter:InputFilter {
    override fun filter(p0: CharSequence?, p1: Int, p2: Int, p3: Spanned?, p4: Int, p5: Int): CharSequence {
        if (p0==null || p0.isBlank()){
            return ""
            //Toast.makeText(this@EmojiFilter,"Emoji Blank Is Not Allowed !!",Toast.LENGTH_SHORT).
        }
        val validCharType= listOf(Character.SURROGATE,Character.OTHER_SYMBOL,Character.NON_SPACING_MARK).map { it.toInt() }
        for (input in p0){
            val type=Character.getType(input)
            if (!validCharType.contains(type)){
                return ""
            }
        }
        return p0
    }
}
