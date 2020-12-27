package code.theducation.music.interfaces

import com.afollestad.materialcab.MaterialCab

interface ICabHolder {

    fun openCab(menuRes: Int, callback: MaterialCab.Callback): MaterialCab
}
