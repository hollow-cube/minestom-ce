package net.bytemc.minestom.server.instance

import net.minestom.server.instance.IChunkLoader
import net.minestom.server.instance.InstanceContainer
import net.minestom.server.world.DimensionType
import java.util.*

class ByteInstance: InstanceContainer {

    constructor(name: String, dimensionType: DimensionType) : super(UUID.randomUUID(), dimensionType)
    constructor(name: String, dimensionType: DimensionType, loader: IChunkLoader) : super(UUID.randomUUID(), dimensionType, loader)

    fun save() {
        TODO("Not implemented yet.")
    }

    fun setTime() {
        TODO("Not implemented yet.")
    }

    fun setWeather() {
        TODO("Not implemented yet.")
    }
}