package com.uzery.fglib.core.program

import com.uzery.fglib.core.program.Program.WINDOW_SIZE
import com.uzery.fglib.core.program.Program.gc
import com.uzery.fglib.utils.graphics.AffineGraphics
import com.uzery.fglib.utils.graphics.AffineTransform
import com.uzery.fglib.utils.graphics.GeometryGraphics
import com.uzery.fglib.utils.graphics.ImageGraphics
import com.uzery.fglib.utils.input.KeyActivator
import com.uzery.fglib.utils.input.MouseActivator
import com.uzery.fglib.utils.math.geom.PointN
import com.uzery.fglib.utils.math.geom.shape.RectN
import javafx.scene.Cursor
import javafx.scene.image.Image
import javafx.scene.input.KeyCode
import javafx.scene.input.MouseButton
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import kotlin.math.min

object Platform {
    fun options() = Program.options

    fun exit() {
        Program.close()
    }

    val charArray = Array(Char.MAX_VALUE.code) { i -> Char(i) }

    internal fun update() {
        keyboard.update()
        mouse.keys.update()
    }

    var develop_mode = false

    var whole_draw = false

    var global_view_scale = 1.0

    var cursor: Cursor? = null
        set(value) {
            field = value
            if (Program.inited) Program.setCursor()
        }

    //todo scale 3 spaces
    var scale = 1
        get() {
            return if (field == -1) {
                val size = WINDOW/CANVAS
                min(size.X.toInt(), size.Y.toInt())
            } else field
        }

    val WINDOW
        get() = PointN(WINDOW_SIZE)
    val CANVAS
        get() = PointN(options().size)
    val CANVAS_REAL
        get() = CANVAS*scale

    val WINDOW_R
        get() = RectN(PointN.ZERO, WINDOW)
    val CANVAS_R
        get() = RectN(PointN.ZERO, CANVAS)
    val CANVAS_REAL_R
        get() = RectN(PointN.ZERO, CANVAS_REAL)

    var global_alpha = 1.0
        set(input) {
            gc.globalAlpha = input*graphics.alpha
            field = input
        }

    val keyboard = object: KeyActivator<KeyCode>(KeyCode.values().size) {
        override fun pressed0(code: Int): Boolean = Program.pressed[code]
        override fun from(key: KeyCode): Int = key.ordinal
    }
    val char_keyboard = object: KeyActivator<Char>(charArray.size) {
        override fun pressed0(code: Int): Boolean = Program.char_pressed(code)
        override fun from(key: Char): Int = Program.char_pressedID[key]!!
    }

    val mouse = object: MouseActivator(RectN(PointN.ZERO, CANVAS)) {
        override fun pos0(): PointN = Program.mouseP/scale
        override fun scroll0() = Program.scrollP

        override val keys = object: KeyActivator<MouseButton>(KeyCode.values().size) {
            override fun pressed0(code: Int): Boolean = Program.mouse_pressed[code]
            override fun from(key: MouseButton): Int = key.ordinal
        }
    }

    var oob = 0
    val graphics = object: AffineGraphics() {
        private fun isOutOfBounds(pos: PointN, size: PointN): Boolean {
            val b = pos.more(CANVAS_REAL) || (pos+size).less(PointN.ZERO)
            if (b) oob++
            return b
        }

        private val transform =
            AffineTransform {
                var x = (it-drawPOS*layer.z)*scale
                if (whole_draw) x = x.roundC(1.0)
                x *= view_scale*global_view_scale
                x
            }
        private val transformSize = AffineTransform { it*scale*view_scale*global_view_scale }

        override fun setStroke(size: Double) {
            gc.lineWidth = transformSize.transform(PointN(size)).X
        }

        override val image: ImageGraphics = object: ImageGraphics(transform) {
            override fun draw0(image: Image, pos: PointN, size: PointN) {
                if (isOutOfBounds(pos, size)) return
                gc.drawImage(image, pos.X, pos.Y, size.X, size.Y)
            }
        }

        override val fill: GeometryGraphics = object: GeometryGraphics(transform, transformSize) {
            override var color: Paint = Color(0.0, 0.0, 0.0, 1.0)
                set(value) {
                    field = value
                    gc.fill = value
                }

            override fun rect0(pos: PointN, size: PointN) {
                if (isOutOfBounds(pos, size)) return
                gc.fillRect(pos.X, pos.Y, size.X, size.Y)
            }

            override fun oval0(pos: PointN, size: PointN) {
                if (isOutOfBounds(pos, size)) return
                gc.fillOval(pos.X, pos.Y, size.X, size.Y)
            }

            override fun line0(pos1: PointN, pos2: PointN) {
                if (isOutOfBounds(pos1, pos2-pos1)) return
                gc.strokeLine(pos1.X, pos1.Y, pos2.X, pos2.Y)
            }

            override fun text0(pos: PointN, text: String) {
                if (isOutOfBounds(pos, text_size(text))) return

                gc.font = font
                gc.fillText(text, pos.X, pos.Y)
            }
        }
        override val stroke: GeometryGraphics = object: GeometryGraphics(transform, transformSize) {
            override var color: Paint = Color(0.0, 0.0, 0.0, 1.0)
                set(value) {
                    field = value
                    gc.stroke = value
                }

            override fun rect0(pos: PointN, size: PointN) {
                if (isOutOfBounds(pos, size)) return
                gc.strokeRect(pos.X, pos.Y, size.X, size.Y)
            }

            override fun oval0(pos: PointN, size: PointN) {
                if (isOutOfBounds(pos, size)) return
                gc.strokeOval(pos.X, pos.Y, size.X, size.Y)
            }

            override fun line0(pos1: PointN, pos2: PointN) {
                if (isOutOfBounds(pos1, pos2-pos1)) return
                gc.strokeLine(pos1.X, pos1.Y, pos2.X, pos2.Y)
            }

            override fun text0(pos: PointN, text: String) {
                if (isOutOfBounds(pos, text_size(text))) return

                gc.font = font
                gc.strokeText(text, pos.X, pos.Y)
            }
        }
    }
}
