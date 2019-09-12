package org.othercraft.bot

object Config{
    lateinit var isMasterAdmin: AdminManager
    lateinit var prefix: String
    lateinit var defaultHelp: HelpCommand
    lateinit var badPermissionHandler: BadPermissionHandler
}