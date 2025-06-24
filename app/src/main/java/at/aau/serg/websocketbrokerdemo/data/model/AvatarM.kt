package at.aau.serg.websocketbrokerdemo.model

import com.example.myapplication.R

enum class AvatarM(val id: Int, val drawableRes: Int) {
    BEAR(1, R.drawable.bearm),
    PANDA(2, R.drawable.pandam),
    FOX(3, R.drawable.foxm),
    PIG(4, R.drawable.pigm),
    CROCODILE(5, R.drawable.crocodilem),
    DUCK(6, R.drawable.duckm);

    companion object {
        fun fromId(id: Int): AvatarM? = values().find { it.id == id }
        fun fromDrawableRes(drawableRes: Int): AvatarM? = values().find { it.drawableRes == drawableRes }
    }
}
