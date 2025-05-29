package at.aau.serg.websocketbrokerdemo.model

import com.example.myapplication.R

enum class Avatar(val id: Int, val drawableRes: Int) {
    BEAR(1, R.drawable.bear),
    PANDA(2, R.drawable.panda),
    FOX(3, R.drawable.fox),
    PIG(4, R.drawable.pig),
    CROCODILE(5, R.drawable.crocodile),
    DUCK(6, R.drawable.duck);

    companion object {
        fun fromId(id: Int): Avatar? = values().find { it.id == id }
        fun fromDrawableRes(drawableRes: Int): Avatar? = values().find { it.drawableRes == drawableRes }
    }
}
