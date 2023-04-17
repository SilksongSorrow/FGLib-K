package com.uzery.fglib.core.obj.bounds

import com.uzery.fglib.utils.data.DebugData

class BoundsBox{
    operator fun get(index: Int): (() -> Bounds)? {
        return when(index){
            0-> red
            1-> orange
            2-> blue
            3-> green
            else -> throw DebugData.error("index: $index")
        }
    }

    var red: (() -> Bounds)? = null
    var orange: (() -> Bounds)? = null
    var blue: (() -> Bounds)? = null
    var green: (() -> Bounds)? = null

    companion object {
        const val size=4
    }


}