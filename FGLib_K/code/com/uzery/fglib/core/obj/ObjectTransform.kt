package com.uzery.fglib.core.obj

import com.uzery.fglib.utils.math.geom.PointN

abstract class ObjectTransform(protected val o: GameObject) {
    open val move: (d_pos: PointN)->Unit = { d_pos->
        pos += d_pos
    }
    open val resize: (d_size: PointN)->Unit = { d_size->
        size += d_size
    }
    open val resize_move: (d_size: PointN) -> Unit
        get() = this.move

    open val turnTo: ((a: Double)->Unit)? = { a ->
        alpha = a
    }

    open val addIn: ((pos: PointN)->Unit)? = null
    open val removeIn: ((pos: PointN)->Unit)? = null

    var pos = PointN.ZERO
        get() = o.stats.POS
        private set(value){
            o.stats.POS = value
            field = value
        }
    var size = PointN.ZERO
        get() = o.stats.SIZE
        private set(value){
            o.stats.SIZE = value
            field = value
        }
    var alpha = 0.0
        get() = o.stats.ALPHA
        private set(value){
            o.stats.ALPHA = value
            field = value
        }


    open val show_pos: PointN
        get() = pos
    open val show_size: PointN
        get() = size
}
