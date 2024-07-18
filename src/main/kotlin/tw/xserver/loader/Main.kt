package tw.xserver.loader

import asg.cliche.ShellFactory
import org.fusesource.jansi.AnsiConsole
import tw.xserver.loader.base.MainLoader
import tw.xserver.loader.cli.RootCLI
import tw.xserver.loader.util.Arguments


fun main(args: Array<String>) {
    configureSystem()
    Arguments.main(args)
    MainLoader.start()

    try {
        commandLoop()
    } catch (e: Exception) {
        System.err.println("An error occurred: ${e.message}")
        e.printStackTrace()
    } finally {
        AnsiConsole.systemUninstall()
    }
}

/**
 * Configures system properties and installs the necessary system tools for console management.
 */
fun configureSystem() {
    System.setProperty("jansi.passthrough", "true")
    AnsiConsole.systemInstall()
}

/**
 * Continuously runs the command loop, providing a shell interface for user interaction.
 */
fun commandLoop() {
    while (true) {
        ShellFactory.createConsoleShell("$", "type `?l` for help or `stop` to exit", RootCLI()).commandLoop()
        System.err.println(
            """
            If you want to stop the program, 
            you can type `shutdown` or `stop`.
            Or the program may be broken...
            """.trimIndent()
        )
    }
}
