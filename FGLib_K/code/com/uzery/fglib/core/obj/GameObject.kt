package com.uzery.fglib.core.obj

import com.uzery.fglib.core.obj.ability.AbilityBox
import com.uzery.fglib.core.obj.ability.ActionListener
import com.uzery.fglib.core.obj.ability.InputAction
import com.uzery.fglib.core.obj.bounds.BoundsBox
import com.uzery.fglib.core.obj.controller.Controller
import com.uzery.fglib.core.obj.controller.TempAction
import com.uzery.fglib.core.obj.modificator.Modificator
import com.uzery.fglib.core.obj.property.GameProperty
import com.uzery.fglib.core.obj.stats.Stats
import com.uzery.fglib.core.obj.visual.Visualiser
import com.uzery.fglib.utils.math.geom.PointN
import java.util.*

abstract class GameObject {
    val stats = Stats()
    val bounds = BoundsBox()

    private var controller: Controller? = null
    private var temp: TempAction? = null

    internal val visuals = LinkedList<Visualiser>()
    private val modificators = LinkedList<Modificator>()
    internal val abilities = LinkedList<AbilityBox>()
    private val listeners = LinkedList<ActionListener>()
    private val properties = LinkedList<GameProperty>()

    val children = LinkedList<GameObject>()
    val grabbed = LinkedList<GameObject>()
    var owner: GameObject? = null

    private val tags = LinkedList<String>()
    private val effects = LinkedList<TagEffect>()

    var name = "temp"
    val values = LinkedList<Any>()

    var dead = false
        private set

    var object_time = 0
        private set


    fun setController(controller: () -> () -> TempAction){
        this.controller=Controller { controller() }
    }
    fun setController(controller: Controller){
        this.controller=controller
    }
    fun addListener(listener: (InputAction) -> Unit) = listeners.add(ActionListener { listener(it) })
    fun addListener(listener: ActionListener) = listeners.add(listener)
    fun addAbility(ability: () -> Unit) = abilities.add(AbilityBox { ability() })
    fun addAbility(ability: AbilityBox) = abilities.add(ability)
    fun addProperty(property: () -> Unit) = properties.add(GameProperty { property() })
    fun addProperty(property: GameProperty) = properties.add(property)
    fun addMod(mod: () -> Unit) = modificators.add(Modificator { mod() })
    fun addMod(mod: Modificator) = modificators.add(mod)
    fun addVisual(visual: Visualiser) = visuals.add(visual)

    fun next() {
        if(object_time == 0) afterInit()

        if(temp == null || temp!!.ends) temp = controller?.get()?.invoke()
        temp?.next()

        abilities.forEach { it.run() }

        modificators.forEach { it.update() }
        properties.forEach { it.update() }

        effects.forEach { it.update() }
        effects.removeIf { it.dead }

        object_time++
    }

    open fun afterInit() {
        /* ignore */
    }

    fun draw(draw_pos: PointN) = visuals.forEach { it.draw(draw_pos) }

    protected fun produce(vararg os: GameObject) = children.addAll(os)
    protected fun produce(os: List<GameObject>) = children.addAll(os)

    fun grab(vararg os: GameObject) {
        grabbed.addAll(os)
        os.forEach { it.owner = this }
        os.forEach { it.onGrab() }
    }

    fun grab(os: List<GameObject>) {
        grabbed.addAll(os)
        os.forEach { o -> o.owner = this }
        os.forEach { o -> o.onGrab() }
    }

    open fun setValues() {
        name = "temp"
    }

    override fun toString(): String {
        values.clear()
        setValues()
        val s = StringBuilder(name)
        if(values.isNotEmpty()) {
            s.append(":")
            values.forEach { value ->
                val ss = value.toString()
                if(ss[ss.lastIndex] == ']') s.append(" $ss")
                else s.append(" [$ss]")
            }
        }
        return s.toString()
    }

    fun activate(action: InputAction) {
        listeners.forEach { a -> a.activate(action) }
        temp?.activate(action)
    }

    open fun interact() = false

    fun collapse() {
        onDeath()
        dead = true
    }

    open fun onDeath() {
        /* ignore */
    }

    open fun onGrab() {
        /* ignore */
    }

    fun tag(vararg tag: String) = tags.addAll(tag)
    fun tagged(tag: String) = tags.contains(tag)
    fun addEffect(vararg effect: TagEffect) = effects.addAll(effect)
    fun effected(effect: String) = effects.any { a -> a.name == effect }
    fun effectedAny(vararg effect: String) = effect.any { eff -> effected(eff) }
    fun effectedAll(vararg effect: String) = effect.all { eff -> effected(eff) }
}
