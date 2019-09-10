package org.othercraft

import org.othercraft.bot.*
import java.io.File


fun main() {
    config {
        this.help = NormalHelpPrinter()
        admin {
            id = 279412525051674624
            id = 293853365891235841
        }
        prefix = "ps "
        permission {  c, command ->
            c.cache("Permission request denied")
            c.cache("You used a command that needs level ${command.permission}")
            c.cache("You only have permission level ${c.permissionLevel}")
            c.flush().last().then()
        }
    }
    val client = start(File("key.txt"))
    commands(client){
        command {
            name = "ping"
            desc = "Time a request to discord"
            execute { c ->
                c.sendMessage("Pinging")
                    .flatMap { first ->
                        first.edit {
                            val time = -c.event.message.timestamp.toEpochMilli() + first.timestamp.toEpochMilli()
                            it.setContent("Pong! Time:${time}ms")
                        }
                    }
            }
        }
        command {
            name = "perms"
            desc = "Query your permission level"
            execute { c ->
                c.sendMessage(c.permissionLevel.toString())
            }
        }
    }.subscribe()
    client.login().block()
}