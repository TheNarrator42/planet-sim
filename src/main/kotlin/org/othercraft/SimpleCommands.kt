package org.othercraft

import org.othercraft.bot.command

fun initSimpleCommands(){
    command {
        name = "ping"
        desc = "Time a request to discord"
        execute {
            createMessage("Pinging")
                .flatMap { first ->
                    first.edit {
                        val time = first.timestamp.toEpochMilli() - event.message.timestamp.toEpochMilli()
                        it.setContent(":ping_pong: Pong! Time: `${time}ms`")
                    }
                }
        }
    }
    command {
        name = "perms"
        desc = "Query your permission level"
        execute { createMessage(permissionLevel.toString()) }
        hidden = true
    }
}