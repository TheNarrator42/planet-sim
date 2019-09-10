package org.othercraft.bot

class PermissionLevel private constructor(val name :String) {


    operator fun compareTo(other : PermissionLevel):Int{
        if(this === other)
            return 0
        return when {
            this == ANY -> -1
            this == PLAYER && other == MASTER_ADMIN -> -1
            this == PLAYER -> 1
            this == MASTER_ADMIN -> 1
            else -> error("fuck")
        }
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PermissionLevel

        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

    override fun toString(): String {
        return "PermissionLevel('$name')"
    }

    companion object {
        val PLAYER = PermissionLevel("PLAYER")
        val MASTER_ADMIN = PermissionLevel("MASTER_ADMIN")
        val ANY = PermissionLevel("ANY")
    }

}