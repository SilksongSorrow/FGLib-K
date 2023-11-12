package com.uzery.fglib.extension.ui

import com.uzery.fglib.core.obj.DrawLayer
import com.uzery.fglib.core.program.Platform.graphics
import com.uzery.fglib.core.program.Platform.mouse
import com.uzery.fglib.utils.math.FGUtils
import com.uzery.fglib.utils.math.geom.PointN
import javafx.scene.input.MouseButton
import javafx.scene.paint.Color
import javafx.scene.text.FontWeight
import java.util.*

abstract class VBox: UIElement() {
    protected abstract val full: Int
    protected abstract val rows: Int

    private val pp = PointN(1.2, 1.4)

    abstract val sizeOne: PointN
    private val offset
        get() = sizeOne/5
    final override val size: PointN
        get() = PointN(sizeOne.X*rows, sizeOne.Y*((full-1)/rows+1))*pp+offset

    override fun update() {

    }

    open fun ifActiveUpdate() {
        /*ignore*/
    }

    var value = false

    var select = 0
        get() = field.coerceIn(0 until full)

    abstract fun setNames(id: Int): String

    abstract fun draw(pos: PointN, id: Int)

    override fun draw() {
        graphics.layer = DrawLayer.CAMERA_OFF

        val name = LinkedList<String>()
        for (id in 0 until full) {
            name.add(setNames(id))

            val ps = pos+fromID(id)
            graphics.fill.rect(ps, sizeOne, FGUtils.transparent(Color.DARKBLUE, 0.1))

            graphics.fill.font("TimesNewRoman", 12.0/2, FontWeight.BOLD)

            graphics.fill.textC(ps+sizeOne*PointN(0.5, 1.25), name[id], Color.DARKBLUE)
            draw(ps+sizeOne/2, id)

            graphics.setStroke(2.0)
            if (id == select) graphics.stroke.rect(ps, sizeOne, FGUtils.transparent(Color.DARKBLUE, 0.6))
        }
    }

    final override fun ifActive() {
        for (id in 0 until full) {
            val mouse_input = mouse.keys.anyInPressed(MouseButton.PRIMARY, MouseButton.SECONDARY) && isActive()
            if (mouse_input && isAt(pos+fromID(id), sizeOne)) select = id
        }
        ifActiveUpdate()
    }

    private fun fromID(id: Int): PointN {
        val line = id/rows
        val row = id%rows
        return offset+sizeOne*PointN(row, line)*pp
    }
}