package org.othercraft.bot

import discord4j.core.`object`.util.Snowflake
import reactor.core.publisher.Mono

class ConfigBuilder internal constructor(){
    var isMasterAdmin : AdminManager? = null
    private var prefixProducer: PrefixProducer? = null
    var help: HelpCommand? = null
    private var badPermissionHandler : BadPermissionHandler? = null



    fun permission(block :(Context, Command) -> Mono<Void>){
        badPermissionHandler = object : BadPermissionHandler {
            override fun run(context: Context, command: Command): Mono<Void> {
                return block(context,command)
            }
        }
    }

    var prefix :String
        set(value) {
            prefixProducer = object : PrefixProducer {
                override fun getPrefixForGuild(guild: Snowflake) = getDefaultPrefix()
                override fun getDefaultPrefix() = value
            }
        }
        @Deprecated("", level = DeprecationLevel.ERROR) get() { error("oof") }



    fun admin(block : AdminManagerBuilder.() -> Unit){
        isMasterAdmin = AdminManagerBuilder().apply(block).build()
    }

    internal fun setupConfig(){
        Config.badPermissionHandler = badPermissionHandler ?: error("No permission handler specified")
        Config.defaultHelp = help ?: error("No help specified")
        Config.isMasterAdmin = isMasterAdmin ?: error("isMasterAdmin not specified")
        Config.prefix = prefixProducer ?: error("PrefixProducer not specified")
    }
}