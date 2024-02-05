import net.minestom.server.MinecraftServer
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandExecutor
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.command.builder.suggestion.SuggestionEntry
import net.minestom.server.coordinate.Point
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.GameMode
import net.minestom.server.event.EventNode
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent
import net.minestom.server.event.player.PlayerSpawnEvent
import net.minestom.server.instance.Chunk
import net.minestom.server.instance.Instance
import net.minestom.server.instance.LightingChunk
import net.minestom.server.instance.block.Block
import net.minestom.server.world.DimensionType
import java.util.concurrent.CompletableFuture

fun Pos(x: Int, y: Int, z: Int) = Pos(x.toDouble(), y.toDouble(), z.toDouble())

private val WORLD_EVENTS = EventNode.all("routing node")
    .addListener(AsyncPlayerConfigurationEvent::class.java) { event ->
        val world = MinecraftServer.getInstanceManager().createInstanceContainer(DimensionType.OVERWORLD)
        world.setChunkSupplier(::LightingChunk)
        world.setGenerator {
            it.modifier().fillHeight(0, 40, Block.STONE)
        }

        event.spawningInstance = world
        event.player.respawnPoint = Pos(0, 40, 10000)
    }.addListener(PlayerSpawnEvent::class.java) {
        with(it.player) {
            gameMode = GameMode.CREATIVE
            permissionLevel = 4
        }
    }

fun main() {
    val minecraftServer = MinecraftServer.init()

    MinecraftServer.getGlobalEventHandler().addChild(WORLD_EVENTS)

    // This works fine but here no tab completion.
    MinecraftServer.getCommandManager().register(
        object : Command("example1") {
            init {
                val firstArg = ArgumentType.String("first arg")
                val secondArg = ArgumentType.Integer("second arg")

                defaultExecutor = CommandExecutor { sender, _ ->
                    sender.sendMessage("not enough arguments")
                }
                addSyntax({ sender, context ->
                    sender.sendMessage("${context[firstArg]} -|- ${context[secondArg]}")
                }, firstArg, secondArg)
            }
        }
    )

    // Now it is broken
    MinecraftServer.getCommandManager().register(
        object : Command("example2") {
            init {
                val firstArg = ArgumentType.Word("first arg").apply {
                    setSuggestionCallback { _, _, suggestion ->
                        suggestion.addEntry(SuggestionEntry("suggestion"))
                    }
                }
                val secondArg = ArgumentType.Integer("second arg").apply {
                    setSuggestionCallback { _, _, suggestion ->
                        suggestion.addEntry(SuggestionEntry("123"))
                    }
                }

                defaultExecutor = CommandExecutor { sender, _ ->
                    sender.sendMessage("not enough arguments")
                }
                addSyntax({ sender, context ->
                    sender.sendMessage("${context[firstArg]} -|- ${context[secondArg]}")
                }, firstArg, secondArg)
            }
        }
    )

    // Empty suggestion callbacks also will break syntax popup
    MinecraftServer.getCommandManager().register(
        object : Command("example3") {
            init {
                val firstArg = ArgumentType.Word("first arg").apply {
                    setSuggestionCallback { _, _, _ -> }
                }
                val secondArg = ArgumentType.Integer("second arg").apply {
                    setSuggestionCallback { _, _, _ -> }
                }

                defaultExecutor = CommandExecutor { sender, _ ->
                    sender.sendMessage("not enough arguments")
                }
                addSyntax({ sender, context ->
                    sender.sendMessage("${context[firstArg]} -|- ${context[secondArg]}")
                }, firstArg, secondArg)
            }
        }
    )

    // If command has syntax without args it will also break.
    MinecraftServer.getCommandManager().register(
        object : Command("example4") {
            init {
                val firstArg = ArgumentType.Word("first arg").apply {
                    setSuggestionCallback { _, _, suggestion ->
                        suggestion.addEntry(SuggestionEntry("suggestion"))
                    }
                }
                val secondArg = ArgumentType.Integer("second arg").apply {
                    setSuggestionCallback { _, _, suggestion ->
                        suggestion.addEntry(SuggestionEntry("123"))
                    }
                }

                addSyntax({ sender, _ ->
                    sender.sendMessage("not enough arguments")
                })
                addSyntax({ sender, context ->
                    sender.sendMessage("${context[firstArg]} -|- ${context[secondArg]}")
                }, firstArg, secondArg)
            }
        }
    )

    minecraftServer.start("::", 25565)
}