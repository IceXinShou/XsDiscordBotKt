package tw.xserver.loader.util

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import org.slf4j.LoggerFactory


object Arguments : CliktCommand() {
    val forceExportResources: Boolean
            by option(
                "-F",
                "--force-export-resources",
                help = "Force all plugins export theirs resources such as lang files."
            ).flag(default = false)

    val ignoreVersionCheck: Boolean
            by option(
                "-I",
                "--ignore-update",
                help = "Ignore the version check from GitHub"
            ).flag(default = false)

    private val logLevel: String
            by option(
                "-l",
                "--level",
                help = "Set logging level"
            ).default("INFO")

    override fun run() {
        (LoggerFactory.getILoggerFactory() as LoggerContext).getLogger(Logger.ROOT_LOGGER_NAME).level =
            Level.toLevel(logLevel)
    }
}
