package com.uzery.fglib.extension.room_editor.ui

import com.uzery.fglib.core.obj.GameObject
import com.uzery.fglib.core.program.Platform
import com.uzery.fglib.core.program.Platform.char_keyboard
import com.uzery.fglib.core.program.Platform.graphics
import com.uzery.fglib.core.program.Platform.keyboard
import com.uzery.fglib.core.program.Program
import com.uzery.fglib.extension.room_editor.DataRE
import com.uzery.fglib.extension.ui.UIElement
import com.uzery.fglib.utils.data.debug.DebugData
import com.uzery.fglib.utils.math.FGUtils
import com.uzery.fglib.utils.math.geom.PointN
import com.uzery.fglib.utils.math.geom.shape.RectN
import javafx.scene.input.KeyCode
import javafx.scene.paint.Color
import javafx.scene.text.FontWeight
import java.lang.Exception

class ObjectRedactBoxRE(val data: DataRE): UIElement() {
    var new_obj: String = ""

    override val pos = (Platform.CANVAS-size)/2
    override val size
        get() = PointN(500, 42)
    override val window: RectN
        get() = Platform.CANVAS_R

    override val priority = 2

    private var caret = 1

    override fun draw() {
        graphics.alpha = 0.5
        graphics.fill.rect(pos, size, Color.BEIGE)
        graphics.alpha = 1.0

        graphics.fill.font("TimesNewRoman", 10.0, FontWeight.BOLD)

        graphics.fill.textR(pos+PointN(35, 14), "old:", Color.gray(0.2, 0.9))
        graphics.fill.text(pos+PointN(40, 14), "${data.redact_obj}", Color.gray(0.2, 0.9))

        val sp = PointN(2, 2)
        graphics.fill.rect(pos+PointN(40, 23)-sp, PointN(416, 12)+sp*2, Color.gray(0.2, 0.9))

        graphics.fill.textR(pos+PointN(35, 33), "new:", Color.gray(0.2, 0.9))
        graphics.fill.text(pos+PointN(40, 33), new_obj, Color.gray(0.9, 0.8))

        if (caret in 0..new_obj.length && time/20%3 > 0) {
            val text_size = if (caret == 0) PointN.ZERO
            else graphics.fill.text_size(new_obj.substring(0, caret))

            graphics.fill.rect(pos+text_size.XP+PointN(40, 22), PointN(1.0, 14.0), Color.gray(0.9, 0.8))
        }


        val sp2 = PointN(5, 5)
        val size1 = PointN(1, 1)*size.Y-sp2*2
        graphics.fill.rect(pos+size.XP+PointN(-sp2.X-size1.X, sp2.Y), size1, Color.gray(0.2, 0.9))

        graphics.fill.font("TimesNewRoman", 14.0, FontWeight.BOLD)
        graphics.alpha = 0.5
        graphics.fill.textR(pos+size-PointN(10.5, 13.5)-sp, "OK", Color.WHITE)
        graphics.alpha = 1.0
    }

    override fun ifActive() {
        fun superPressed(code: KeyCode): Boolean {
            return keyboard.inPressed(code) ||
                    keyboard.timePressed(code) > 40 && keyboard.pressed(code) && keyboard.timePressed(code)%2 == 0L
        }

        if (superPressed(KeyCode.RIGHT)) {
            caret++
        }
        if (superPressed(KeyCode.LEFT)) {
            caret--
        }
        if (superPressed(KeyCode.BACK_SPACE) && caret != 0) {
            new_obj = new_obj.substring(0, caret-1)+new_obj.substring(caret, new_obj.length)
            caret--
        }

        if (!keyboard.anyPressed(KeyCode.BACK_SPACE, KeyCode.LEFT, KeyCode.RIGHT, KeyCode.ESCAPE)) {
            Platform.charArray.forEach { char ->
                if (char_keyboard.pressed(char)) {
                    new_obj = new_obj.substring(0, caret)+char+new_obj.substring(caret, new_obj.length)
                    caret++
                }
            }
        }

        caret = caret.coerceIn(0..new_obj.length)
    }

    private var old_redact_obj: GameObject? = null
    private var time = 0
    override fun update() {
        if (keyboard.inPressed(KeyCode.ESCAPE)) {
            data.redact_obj = null
        }

        if (data.redact_obj != old_redact_obj) {
            new_obj = data.redact_obj.toString()
            caret = new_obj.length
        }
        old_redact_obj = data.redact_obj
        time++
    }
}
