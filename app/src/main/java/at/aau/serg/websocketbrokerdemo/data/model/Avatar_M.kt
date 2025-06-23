package at.aau.serg.websocketbrokerdemo.model

import com.example.myapplication.R

enum class Avatar_M(val id: Int, val drawableRes: Int) {
    BEAR(11, R.drawable.bear_m),
    PANDA(12, R.drawable.panda_m),
    FOX(13, R.drawable.fox_m),
    PIG(14, R.drawable.pig_m),
    CROCODILE(15, R.drawable.crocodile_m),
    DUCK(16, R.drawable.duck_m);

    companion object {
        fun fromId(id: Int): Avatar_M? = values().find { it.id == id }
        fun fromDrawableRes(drawableRes: Int): Avatar_M? = values().find { it.drawableRes == drawableRes }
    }
}
