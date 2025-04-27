package eki.dore.flabbygame.game_obj

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class ColorSet(val bird: Int, val column: Int, val background: Int) : Parcelable
