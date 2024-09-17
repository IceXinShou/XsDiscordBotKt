package tw.xserver.loader

import tw.xserver.loader.base.MainLoader
import tw.xserver.loader.cli.CommandLineManager
import tw.xserver.loader.logger.LogBackManager
import tw.xserver.loader.util.Arguments


fun main(args: Array<String>) {
    LogBackManager.configureSystem()
    Arguments.main(args)
    MainLoader.main()

    try {
        CommandLineManager.commandLoop()
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        LogBackManager.uninstall()
    }
}
