package org.othercraft.bot

import reactor.core.publisher.Mono

class ConfigBuilder internal constructor(){
    var isMasterAdmin : AdminManager? = null
    var help: HelpCommand? = null
    private var badPermissionHandler : BadPermissionHandler? = null



    fun permission(block :(Context, Command) -> Mono<Void>){
        badPermissionHandler = object : BadPermissionHandler {
            override fun run(context: Context, command: Command): Mono<Void> {
                return block(context,command).then()
            }
        }
    }

    var prefix :String? = null

    fun admin(block : AdminManagerBuilder.() -> Unit){
        isMasterAdmin = AdminManagerBuilder().apply(block).build()
    }

    internal fun setupConfig(){
        Config.badPermissionHandler = badPermissionHandler ?: error("No permission handler specified")
        Config.defaultHelp = help ?: error("No help specified")
        Config.isMasterAdmin = isMasterAdmin ?: error("isMasterAdmin not specified")
        Config.prefix = prefix ?: error("PrefixProducer not specified")
    }
}