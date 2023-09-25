package com.uzery.fglib.core.world

import com.uzery.fglib.core.obj.GameObject
import com.uzery.fglib.core.program.Platform
import com.uzery.fglib.core.program.Platform.CANVAS
import com.uzery.fglib.core.program.Platform.graphics
import com.uzery.fglib.core.room.Room
import com.uzery.fglib.core.world.World.rooms
import com.uzery.fglib.utils.math.geom.PointN
import com.uzery.fglib.utils.math.geom.shape.RectN
import javafx.scene.paint.Color
import javafx.scene.text.FontWeight

class MovableWC(private val goal: GameObject, val room_p: PointN = PointN(10, 10)): WorldController {
    private val void = Room(PointN.ZERO, PointN.ZERO)
    var goal_room = void
        private set

    override fun init() {
    }

    override fun isActive(r: Room): Boolean {
        val rect = RectN(r.pos-room_p-CANVAS/2, r.size+room_p*2+CANVAS)
        return rect.into(World.camera!!.stats.POS+World.camera!!.stats.roomPOS)
    }

    override fun onAppear(r: Room) {
        moveObjs()
    }

    override fun onDisappear(r: Room) {
        /*for (o in r.objects) {
            if(roomFor(o)!=void){
                World.add(o)
            }
        }*/

        moveObjs()
    }

    private fun moveObjs() {
        fun moveGoal() {
            val newRoom = roomFor(goal)
            goal.stats.POS += goal_room.pos-newRoom.pos
            goal.stats.roomPOS = newRoom.pos
            World.camera!!.stats.roomPOS = goal.stats.roomPOS
            World.camera!!.move(goal_room.pos-newRoom.pos)

            goal_room.objects.remove(goal)
            goal_room = newRoom
            goal_room.objects.add(goal)
        }
        moveGoal()

        fun migrate(oldRoom: Room) {
            oldRoom.objects.filter { it.tagged("migrator") }.forEach { obj ->
                if (!isInArea(oldRoom, obj)) {
                    val newRoom = roomFor(obj)
                    oldRoom.remove(obj)
                    newRoom.objects.add(obj)
                    obj.stats.POS += oldRoom.pos-newRoom.pos
                    obj.stats.roomPOS = newRoom.pos
                    //obj.stats.POS=obj.stats.POS.round(1.0)
                }
            }
        }

        migrate(void)
        World.active_rooms.forEach { migrate(it) }

        //goal_room.objects.removeIf { o->o.tagged("#immovable") }

        /*if(goal.dead){
            goal=goal_f()
            goal.stats.POS -= goal_room.pos
            camera.move(goal_room.pos - roomFor(goal).pos)
        }*///todo
    }

    private fun isInArea(r: Room, obj: GameObject): Boolean {
        return r.main.into(obj.stats.POS+obj.stats.roomPOS) //+o.stats.roomPOS
    }

    override fun roomFor(obj: GameObject): Room {
        return rooms.firstOrNull { isInArea(it, obj) } ?: void
    }

    override fun update() {
        graphics.fill.font("TimesNewRoman", 12.0/2, FontWeight.BOLD)
        graphics.fill.textL(PointN(20, 60), "pos: "+goal.stats.POS, Color.BLACK)
        /*if(Platform.keyboard.pressed(KeyCode.CONTROL) && Platform.keyboard.inPressed(KeyCode.R)) {
            active_rooms.forEach { room->room.objects.removeIf { it.tagged("player") } }
            World.add(goal)
        }*/
        void.next()
        void.draw(PointN.ZERO)

        moveObjs()
    }

    override fun drawPOS(): PointN {
        return goal_room.pos
    }
}
