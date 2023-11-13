package com.uzery.fglib.extension.room_editor

import com.uzery.fglib.core.obj.DrawLayer
import com.uzery.fglib.core.obj.GameObject
import com.uzery.fglib.core.program.Platform
import com.uzery.fglib.core.room.Room
import com.uzery.fglib.utils.data.getter.AbstractClassGetter
import com.uzery.fglib.utils.math.FGUtils
import com.uzery.fglib.utils.math.geom.PointN
import com.uzery.fglib.utils.math.num.StringN
import java.util.*

class DataRE(getter_pair: Pair<AbstractClassGetter<GameObject>, Array<out String>>) {
    var time = 0.0
    var world_play = false
    var last_world_play = false
    var hide_ui = false
    var chosen_entry = 0
    lateinit var edit: Room
    var groupsValues: LinkedList<LinkedList<StringN>> = LinkedList<LinkedList<StringN>>()
    var titles: LinkedList<LinkedList<String>> = LinkedList<LinkedList<String>>()
    var groupsSelect: LinkedList<Int> = LinkedList<Int>()
    var draw_bounds = false
    var select_objs = LinkedList<GameObject>()
    var chosen_obj: GameObject? = null
    var draw_pos = PointN.ZERO

    var edit_n = 0
    lateinit var last_edit_room: Room
    var last_edit_n: Int = 0

    var layers = LinkedList<DrawLayer>()
    var select_layer = 0
    var select_group = 0

    val OFFSET = 40.0
    val GRID
        get() = 32.0/Platform.scale
    val GRID_P
        get() = PointN(GRID, GRID)

    val names = LinkedList<StringN>()
    val ids = TreeMap<StringN, Int>()

    val getter = getter_pair.first
    val filenames = getter_pair.second
    fun init() {
        val entries = LinkedList<StringN>()

        for (i in 0 until getter.entries_size()) entries.add(getter.getEntryName(i))
        for (id in entries.indices) ids[entries[id]] = id
        entries.removeIf { "#" !in it.s }

        val groups_map = TreeMap<StringN, LinkedList<StringN>>()

        for (entry in entries) {
            val name = StringN(FGUtils.subBefore(entry.s, "#"), entry.n)
            if (groups_map[name] == null) groups_map[name] = LinkedList<StringN>()
            groups_map[name]!!.add(entry)
        }
        for ((id, key) in groups_map.keys.withIndex()) {
            groupsValues.add(LinkedList())
            titles.add(LinkedList())

            groups_map[key]!!.forEach { value ->
                groupsValues[id].add(value)
                titles[id].add(FGUtils.subAfter(value.s, "#"))
            }

            names.add(key)


            groupsSelect.add(0)
        }
    }
}
