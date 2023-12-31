package com.uzery.fglib.core.obj

import com.uzery.fglib.utils.data.file.ConstL
import com.uzery.fglib.utils.data.file.TextData
import com.uzery.fglib.utils.math.FGUtils

abstract class SmartGameObject<Type>(val filename: String, name: String = "temp"): GameObject(name) {
    protected val objs = ArrayList<Type>()

    init {
        tag("#smart_object")
    }

    protected fun execute() {
        start()
        addComponent(*construct(objs).toTypedArray())
    }

    private fun start() {
        val data = TextData[filename]

        for (next in data) {
            if (FGUtils.isComment(next)) continue
            objs.add(from(next))
        }
    }

    fun onSave() {
        TextData.write(filename, data(), true)
    }

    protected abstract fun from(s: String): Type

    protected abstract fun construct(objs: ArrayList<Type>): List<ObjectComponent>

    override fun setValues() {
        values.add(filename)
    }

    fun data(): String {
        var s = ""
        s += ConstL.FILES_COMMENT
        objs.forEach { s += it.toString()+"\n" }
        return s
    }
}
