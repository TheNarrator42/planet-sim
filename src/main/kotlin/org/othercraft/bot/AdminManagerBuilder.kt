package org.othercraft.bot

import discord4j.core.`object`.entity.User
import discord4j.core.`object`.util.Snowflake
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono

class AdminManagerBuilder{
    private val list = mutableListOf<Snowflake>()

    var str :String
        set(value) {
            list.add(Snowflake.of(value))
        }
        @Deprecated("", level = DeprecationLevel.ERROR) get() { error("oof") }

    var id :Long
        set(value) {
            list.add(Snowflake.of(value))
        }
        @Deprecated("", level = DeprecationLevel.ERROR) get() { error("oof") }


    internal fun build(): AdminManager {
        return object : AdminManager {
            override fun isMasterAdmin(user: User): Boolean {
                return list.any { id -> user.id == id }
            }
        }
    }
}