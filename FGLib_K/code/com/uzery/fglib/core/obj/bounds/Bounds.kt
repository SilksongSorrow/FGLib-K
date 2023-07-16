package com.uzery.fglib.core.obj.bounds

import com.uzery.fglib.utils.math.geom.PointN
import com.uzery.fglib.utils.math.geom.RectN
import com.uzery.fglib.utils.math.geom.Shape
import java.util.*
import kotlin.math.max
import kotlin.math.min

class Bounds {
    var main: Shape? = null
        private set
    val elements = LinkedList<BoundsElement>()
    val shades = LinkedList<BoundsShade>()

    fun add(vararg els: BoundsElement) = els.forEach { element -> elements.add(element) }

    fun add(els: List<BoundsElement>) = els.forEach { element -> elements.add(element) }

    fun add(vararg shapes: () -> Shape?) = shapes.forEach { shape -> elements.add(BoundsElement(shape)) }
    fun add(shape: () -> Shape?) = elements.add(BoundsElement(shape))
    fun add(name: String, shape: () -> Shape?) = elements.add(BoundsElement(name, shape))

    fun main(): RectN? {
        if(isEmpty()) return null

        var min = PointN.ZERO
        var max = PointN.ZERO
        var first = true

        for(element in elements) {
            val shape = element.shape() ?: continue
            if(first) {
                min = shape.L
                max = shape.R
                first = false
            }
            min = PointN.transform(min, shape.L) { a, b -> min(a, b) }
            max = PointN.transform(max, shape.R) { a, b -> max(a, b) }
        }
        return RectN.rectLR(min, max)
    }

    private fun mainShade(): RectN? {
        if(shades.isEmpty()) return null

        var min = PointN.ZERO
        var max = PointN.ZERO
        var first = true

        for(shade in shades) {
            val shape = shade.shape ?: continue
            if(first) {
                min = shape.L
                max = shape.R
                first = false
            }
            min = PointN.transform(min, shape.L) { a, b -> min(a, b) }
            max = PointN.transform(max, shape.R) { a, b -> max(a, b) }
        }
        return RectN.rectLR(min, max)
    }

    fun isEmpty() = elements.isEmpty()

    @Deprecated("it doesn't copy original manually")
    fun copy(pos: PointN): Bounds {
        val els = LinkedList<BoundsElement>()
        elements.indices.forEach { i -> els.add(elements[i].copy(pos)) }
        return Bounds().also { it.add(els) }
    }

    fun update(pos: PointN) {
        shades.clear()
        elements.forEach { el -> shades.add(BoundsShade(el.name, el.shape()?.copy(pos))) }
        main = mainShade()?.copy(pos)
    }
}
