package game.objects.enemy

import com.uzery.fglib.core.obj.GameObject
import com.uzery.fglib.core.obj.ability.AbilityBox
import com.uzery.fglib.core.obj.ability.InputAction
import com.uzery.fglib.core.obj.bounds.Bounds
import com.uzery.fglib.core.obj.controller.Controller
import com.uzery.fglib.core.obj.controller.TempAction
import com.uzery.fglib.core.obj.controller.TimeTempAction
import com.uzery.fglib.core.obj.visual.LayerVisualiser
import com.uzery.fglib.core.world.World
import com.uzery.fglib.utils.data.image.Data
import com.uzery.fglib.utils.math.MathUtils
import com.uzery.fglib.utils.math.geom.PointN
import com.uzery.fglib.utils.math.geom.RectN
import com.uzery.fglib.utils.math.getter.Drop
import com.uzery.fglib.utils.math.num.IntI
import game.Game
import game.objects.items.EffectItem
import kotlin.math.cos
import kotlin.math.sin

class Goblin(pos: PointN): Enemy(4) {

    private val SPEED = 0.6

    init {
        stats.POS = pos
        controller = object: Controller {
            override fun get(): () -> TempAction {
                if(Math.random()>0.5) return attack
                return stay
            }
        }
        abilities.add(object: AbilityBox {
            override fun activate(action: InputAction) {
                if(action.code == InputAction.CODE.DAMAGE) {
                    LIFE -= 2
                }
            }
        })
        val filename = "mob|goblin.png"
        Data.set(filename, IntI(16, 16), 2)
        visuals.add(object: LayerVisualiser(Game.layer("OBJ")) {
            override fun draw(draw_pos: PointN) {
                agc().image.drawC(Data.get(filename, IntI(object_time/10%2, 0)), draw_pos)
            }
        })
        bounds.orange = { Bounds(RectN(-Game.STEP*7, Game.STEP*14)) }
    }

    var attack: () -> TempAction = {
        object: TimeTempAction() {
            var goal: GameObject? = null
            override fun start() {
                goal = World.allTagged("player").firstOrNull()
            }

            override fun update() {
                goal?.let {
                    val d = MathUtils.getDegree(stats.POS, it.stats.POS)
                    stats.nPOS += Game.X*cos(d)*SPEED
                    stats.nPOS += Game.Y*sin(d)*SPEED
                }
            }

            override fun ends() = false
        }
    }

    var stay: () -> TempAction = {
        object: TempAction {
            var t = 0

            override fun next() {
                val r = Math.random()
                stats.nPOS -= Game.X*r*2
                stats.nPOS -= Game.Y*r*2
                t++
            }

            override val ends: Boolean
                get() = t>5
        }
    }

    override val drop: Drop<GameObject?>
        get() {
            val drop = Drop<GameObject?>()
            drop.setFull(100.0)
            drop.add(2.0) { EffectItem(stats.POS, 3, "coin", 400) }
            drop.add(0.3) { EffectItem(stats.POS, 4, "5-coin", 400) }
            drop.add(1.0) { EffectItem(stats.POS, 5, "wheel_bullets", 400) }
            drop.add(1.0) { EffectItem(stats.POS, 6, "fast_bullets", 400) }
            drop.add(1.0) { EffectItem(stats.POS, 9, "coffee", 400) }
            drop.add(1.0) { EffectItem(stats.POS, 10, "three_bullets", 400) }
            drop.add(1.0) { EffectItem(stats.POS, 11, "life", 400) }
            drop.add(1.0) { EffectItem(stats.POS, 13, "master", 400) }
            return drop
        }
}
