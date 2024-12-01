package tw.xserver.loader.cli

import kotlinx.coroutines.*
import org.jline.reader.*
import org.jline.terminal.Terminal
import org.jline.terminal.TerminalBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tw.xserver.loader.base.MainLoader
import kotlin.coroutines.coroutineContext
import kotlin.system.exitProcess

class CustomCompleter : Completer {
    override fun complete(reader: LineReader, line: ParsedLine, candidates: MutableList<Candidate>) {
        val buffer = line.line()
        val tokens = buffer.split(" ")

        if (tokens.size == 1) {
            val commands = listOf("reload", "stop", "exit", "shutdown")
            commands.filter { it.startsWith(tokens[0], ignoreCase = true) }
                .forEach { candidates.add(Candidate(it)) }
        }
//        else if (tokens.size == 2) {
//            when (tokens[0].lowercase()) {
//                "reload" -> {
//                    val reloadOptions = listOf("force", "soft")
//                    reloadOptions.filter { it.startsWith(tokens[1], ignoreCase = true) }
//                        .forEach { candidates.add(Candidate(it)) }
//                }
//            }
//        }
    }
}


object JLineManager {
    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)
    private lateinit var scope: CoroutineScope
    private val terminal: Terminal = TerminalBuilder.builder().system(true).build()
    private val completer: Completer = CustomCompleter()
    val reader: LineReader = LineReaderBuilder.builder()
        .terminal(terminal)
        .completer(completer)
        .build()


    fun main() {
        scope = CoroutineScope(Dispatchers.IO).apply {
            launch {
                mainLoop()
            }
        }
    }

    private suspend fun mainLoop() {
        while (coroutineContext.isActive) {
            try {
                val line = withContext(Dispatchers.IO) {
                    reader.readLine("console > ").trim()
                }
                if (line.isNotEmpty()) {
                    processLine(line)
                }
            } catch (e: UserInterruptException) {
                logger.info("Interrupted by user: ^C")
            } catch (e: EndOfFileException) {
                logger.info("Interrupted by user: ^D")
            } catch (e: Exception) {
                logger.error("An error occurred: ", e)
            }
        }
    }

    private fun processLine(cmd: String) {
        when (cmd.lowercase()) {
            "reload" -> {
                MainLoader.reload()
                logger.info("Application reloaded successfully.")
            }

            "stop", "exit", "shutdown" -> {
                stopApp()
            }

            else -> {
                logger.warn("Unknown command: {}", cmd)
            }
        }
    }

    private fun stopApp() {
        scope.cancel()
        MainLoader.stop()
        logger.info("Application stopped successfully.")
        exitProcess(0)
    }
}
