package com.uzery.fglib.utils.data.audio

import com.uzery.fglib.utils.data.CollectDataClass
import javafx.scene.media.Media
import java.io.File

object AudioData: CollectDataClass() {
    private val origins = HashMap<String, Media>()
    operator fun get(name: String): Media {
        return origins[resolvePath(name)]!!
    }

    fun set(name: String) {
        val decode = resolvePath(name)

        if (origins[decode] != null) return

        origins[decode] = from(decode)
    }

    fun from(name: String): Media {
        return Media(File(name).toURI().toString())
    }
}
