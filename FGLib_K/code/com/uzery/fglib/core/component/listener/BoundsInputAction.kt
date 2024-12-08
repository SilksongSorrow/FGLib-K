package com.uzery.fglib.core.component.listener

import com.uzery.fglib.core.obj.GameObject

class BoundsInputAction(val code: String, val prime: GameObject, val our: String, val their: String) {
    constructor(action: InputAction): this(action.code, action.prime, action.args[0] as String, action.args[1] as String)

    val stats = prime.stats

    override fun toString() = "BoundsInputAction[$code, $prime, $our, $their]"

    fun toInputAction(): InputAction {
        return InputAction(code, prime, our, their)
    }
}
