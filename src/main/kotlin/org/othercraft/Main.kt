package org.othercraft

import org.othercraft.bot.*
import java.io.File

const val MENTION_ADMIN = "<@620577414287851530>"


fun main() {

    config {
        this.help = NormalHelpPrinter()
        admin {
            id = 279412525051674624
            id = 293853365891235841
        }
        prefix = "ps "
        permission {  c, command ->
            if (command.permission == PermissionLevel.PLAYER){
                c.clientError("You are not a player. Contact an admin to start")
            } else {
                c.error(
                    "You used a command that needs level ${command.permission}. You only have permission level ${c.permissionLevel}",
                    "Permission request denied")
            }.then()
        }
    }
    val client = start(File("key.txt"))
    initAllCommands()
    client.runCommands().and(client.login()).block()
}

fun initAllCommands() {
    initSimpleCommands()
}
