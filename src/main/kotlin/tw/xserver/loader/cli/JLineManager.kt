package tw.xserver.loader.cli

import kotlinx.coroutines.*
import org.jline.reader.EndOfFileException
import org.jline.reader.LineReader
import org.jline.reader.LineReaderBuilder
import org.jline.reader.UserInterruptException
import org.jline.terminal.Terminal
import org.jline.terminal.TerminalBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tw.xserver.loader.base.MainLoader
import kotlin.coroutines.coroutineContext
import kotlin.system.exitProcess

object JLineManager {
    private lateinit var scope: CoroutineScope
    private val terminal: Terminal = TerminalBuilder.builder().system(true).build()
    val reader: LineReader = LineReaderBuilder.builder().terminal(terminal).build()

    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

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
                    val status = processLine(line)
                    if (status == 1) stopApp()
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

    private fun processLine(cmd: String): Int = when (cmd.lowercase()) {
        "reload" -> {
            MainLoader.reload()
            logger.info("Application reloaded successfully.")
            0
        }

        "stop", "exit", "shutdown" -> {
            1
        }

        else -> {
            logger.warn("Unknown command: {}", cmd)
            0
        }
    }

    private fun stopApp() {
        scope.cancel()
        MainLoader.stop()
        logger.info("Application stopped successfully.")
        exitProcess(0)
    }
}
