package command

import com.mojang.brigadier.CommandDispatcher
import dev.steyn.brigadierkt.*

class CommandSource {

}

val dispatcher = CommandDispatcher<CommandSource>()

fun register() {
    dispatcher.command("test") {
        literal("test2") {
            executes {
                //code
                0
            }
        }
    }
}