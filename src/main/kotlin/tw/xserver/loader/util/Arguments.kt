package tw.xserver.loader.util

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option

object Arguments : CliktCommand() {
    val ignoreVersionCheck: Boolean
            by option("-I", "--ignore-update", help = "Ignore the version check from GitHub").flag()

    override fun run() {
    }
}
