package tw.xserver.loader.cli

import asg.cliche.ShellFactory

object CommandLineManager {
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
}