package tw.xserver.loader

import com.github.ajalt.clikt.core.main
import tw.xserver.loader.base.MainLoader
import tw.xserver.loader.cli.JLineManager
import tw.xserver.loader.logger.LogBackManager
import tw.xserver.loader.util.Arguments


fun main(args: Array<String>) {
    try {
        LogBackManager.configureSystem()
        Arguments.main(args)
        MainLoader.main()
        JLineManager.main()
        Thread.currentThread().join()
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        LogBackManager.uninstall()
    }
}
