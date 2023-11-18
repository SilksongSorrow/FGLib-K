package com.uzery.fglib.core.obj

import com.uzery.fglib.core.obj.ability.AbilityBox
import com.uzery.fglib.core.obj.ability.ActionListener
import com.uzery.fglib.core.obj.ability.InputAction
import com.uzery.fglib.core.obj.bounds.BoundsBox
import com.uzery.fglib.core.obj.bounds.BoundsBox.Companion.CODE
import com.uzery.fglib.core.obj.bounds.BoundsElement
import com.uzery.fglib.core.obj.controller.Controller
import com.uzery.fglib.core.obj.controller.TempAction
import com.uzery.fglib.core.obj.property.GameProperty
import com.uzery.fglib.core.obj.stats.Stats
import com.uzery.fglib.core.obj.visual.LayerVisualiser
import com.uzery.fglib.core.obj.visual.Visualiser
import com.uzery.fglib.utils.data.debug.DebugData
import com.uzery.fglib.utils.graphics.AffineGraphics
import com.uzery.fglib.utils.math.geom.PointN
import com.uzery.fglib.utils.math.geom.Shape
import java.util.*

abstract class GameObject(var name: String = "temp") {
    val stats = Stats()
    var bounds = BoundsBox()

    private var controller: Controller? = null
    private var temp: TempAction? = null

    val visuals = ArrayList<Visualiser>()
    private val abilities = ArrayList<AbilityBox>()
    private val listeners = ArrayList<ActionListener>()
    private val properties = ArrayList<GameProperty>()

    private val onBirth = ArrayList<() -> Unit>()
    private val onDeath = ArrayList<() -> Unit>()
    private val onGrab = ArrayList<() -> Unit>()

    internal val children = ArrayList<GameObject>()
    internal val followers = ArrayList<GameObject>()
    var owner: GameObject? = null

    private val tags = ArrayList<String>()
    private val effects = ArrayList<TagEffect>()

    val values = ArrayList<Any>()

    var dead = false
        private set

    var object_time = 0
        private set

    private fun addBounds(code: CODE, vararg bs: BoundsElement) = bounds[code.ordinal].add(*bs)
    private fun addBounds(code: CODE, shape: () -> Shape?) = bounds[code.ordinal].add(shape)
    private fun addBounds(code: CODE, name: String, shape: () -> Shape?) =
        bounds[code.ordinal].add(BoundsElement(name, shape))

    fun addRedBounds(vararg bs: BoundsElement) = addBounds(CODE.RED, *bs)
    fun addRedBounds(shape: () -> Shape?) = addBounds(CODE.RED, shape)
    fun addRedBounds(name: String, shape: () -> Shape?) = addBounds(CODE.RED, name, shape)

    fun addOrangeBounds(vararg bs: BoundsElement) = addBounds(CODE.ORANGE, *bs)
    fun addOrangeBounds(shape: () -> Shape?) = addBounds(CODE.ORANGE, shape)
    fun addOrangeBounds(name: String, shape: () -> Shape?) = addBounds(CODE.ORANGE, name, shape)

    fun addBlueBounds(vararg bs: BoundsElement) = addBounds(CODE.BLUE, *bs)
    fun addBlueBounds(shape: () -> Shape?) = addBounds(CODE.BLUE, shape)
    fun addBlueBounds(name: String, shape: () -> Shape?) = addBounds(CODE.BLUE, name, shape)

    fun addGreenBounds(vararg bs: BoundsElement) = addBounds(CODE.GREEN, *bs)
    fun addGreenBounds(shape: () -> Shape?) = addBounds(CODE.GREEN, shape)
    fun addGreenBounds(name: String, shape: () -> Shape?) = addBounds(CODE.GREEN, name, shape)


    fun setController(controller: () -> () -> TempAction) {
        setController(Controller { controller() })
    }

    fun setController(controller: Controller) {
        this.controller = controller
    }

    fun addListener(listener: (InputAction) -> Unit) = addListener(ActionListener { listener(it) })
    fun addListener(vararg listener: ActionListener) = listeners.addAll(listener)
    fun addAbility(ability: () -> Unit) = addAbility(AbilityBox { ability() })
    fun addAbility(vararg ability: AbilityBox) = abilities.addAll(ability)
    fun addProperty(property: () -> Unit) = addProperty(GameProperty { property() })
    fun addProperty(vararg property: GameProperty) = properties.addAll(property)
    fun addVisual(vis: Visualiser) = visuals.add(vis)

    fun addLayerVisual(layer: DrawLayer, vis: (agc: AffineGraphics, draw_pos: PointN) -> Unit){
        visuals.add(
            object: LayerVisualiser(layer) {
                override fun draw(draw_pos: PointN) { vis(agc, draw_pos) }
            }
        )
    }

    fun init(){
        onBirth.forEach { it() }
    }

    fun next() {
        if (temp == null || temp!!.ends) temp = controller?.get()?.invoke()
        temp?.next()

        abilities.forEach { it.run() }

        properties.forEach { it.update() }

        effects.forEach { it.update() }
        effects.removeIf { it.dead }

        object_time++
    }

    fun nextWithFollowers() {
        next()
        followers.removeIf { it.dead }

        followers.forEach { it.nextWithFollowers() }
    }

    fun draw(draw_pos: PointN) {
        visuals.sortBy { v -> v.drawLayer().sort }
        visuals.forEach { it.drawWithDefaults(draw_pos) }
    }

    protected fun produce(vararg os: GameObject) {
        children.addAll(os)
    }

    protected fun produce(os: List<GameObject>) {
        children.addAll(os)
    }

    fun grab(vararg os: GameObject) {
        followers.addAll(os)
        os.forEach { it.owner = this }
        os.forEach { o -> o.onGrab.forEach { it() } }
    }

    fun grab(os: List<GameObject>) {
        followers.addAll(os)
        os.forEach { it.owner = this }
        os.forEach { o -> o.onGrab.forEach { it() } }
    }

    protected open fun setValues() {}

    override fun toString(): String {
        values.clear()
        setValues()
        val res = StringBuilder(name)
        if (values.isNotEmpty()) {
            res.append(":")
            values.forEach { value ->
                val s = value.toString()
                if (s == "") throw DebugData.error("NULLABLE VALUE: $name: $values")
                res.append(if (s[s.lastIndex] == ']') " $s" else " [$s]")
            }
        }
        return res.toString()
    }

    fun activate(action: InputAction) {
        listeners.forEach { a -> a.activate(action) }
        temp?.activate(action)
    }

    open fun interact() = false

    open fun collapse() {
        if (dead) return
        onDeath.forEach { it() }
        dead = true
        followers.forEach { it.collapse() }
    }

    fun onBirth(f: () -> Unit) {
        onBirth.add(f)
    }

    fun onDeath(f: () -> Unit) {
        onDeath.add(f)
    }

    fun onGrab(f: () -> Unit) {
        onGrab.add(f)
    }

    fun tag(vararg tag: String) = tags.addAll(tag)
    fun untag(vararg tag: String) = tags.removeAll(tag.toSet())

    fun tagged(tag: String) = tag in tags
    fun addEffect(vararg effect: TagEffect) = effects.addAll(effect)
    fun effected(effect: String) = effects.any { a -> a.name == effect }
    fun effectedAny(vararg effect: String) = effect.any { eff -> effected(eff) }
    fun effectedAll(vararg effect: String) = effect.all { eff -> effected(eff) }
    fun equalsS(other: GameObject): Boolean {
        return this.toString() == other.toString()
    }

    fun equalsName(other: GameObject): Boolean {
        return this.name == other.name
    }

    open fun answerYN(question: String) = false
    open fun answer(question: String) = ""
}
