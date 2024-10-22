package code.theducation.appthemehelper.util

import android.os.Build

/**
 * @author nankai
 */

object VersionUtils {

    /**
     * @return true if device is running API >= 21
     */
    fun hasLollipop(): Boolean {
        return Build.VERSION.SDK_INT >= 21
    }

    /**
     * @return true if device is running API >= 23
     */
    fun hasMarshmallow(): Boolean {
        return Build.VERSION.SDK_INT >= 23
    }

    /**
     * @return true if device is running API >= 24
     */
    fun hasNougat(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
    }

    /**
     * @return true if device is running API >= 24
     */
    fun hasNougatMR(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1
    }

    /**
     * @return true if device is running API >= 26
     */
    fun hasOreo(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
    }

    /**
     * @return true if device is running API >= 27
     */
    fun hasP(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
    }

    /**
     * @return true if device is running API >= 28
     */
    @JvmStatic
    fun hasQ(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    }
}
