package org.othercraft.bot

object Config{
    lateinit var isMasterAdmin: AdminManager
    lateinit var prefix: PrefixProducer
    lateinit var defaultHelp: HelpCommand
    lateinit var badPermissionHandler: BadPermissionHandler
}