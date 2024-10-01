package tw.xserver.plugin.addons.ticket

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.decodeFromStream
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent
import net.dv8tion.jda.api.events.session.ReadyEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tw.xserver.loader.base.MainLoader.jdaBot
import tw.xserver.loader.plugin.PluginEvent
import tw.xserver.loader.util.FileGetter
import tw.xserver.plugin.addons.ticket.serializer.MainConfigSerializer
import java.io.File
import java.io.IOException

object Event : PluginEvent(true) {
    internal val PLUGIN_DIR_FILE = File("./plugins/TicketAddons/")
    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)
    internal lateinit var config: MainConfigSerializer
    internal lateinit var targetMember: Member
        private set

    override fun load() {
        reloadAll()
    }

    override fun unload() {}

    override fun reloadConfigFile() {
        fileGetter = FileGetter(PLUGIN_DIR_FILE, this.javaClass)

        try {
            fileGetter.readInputStream("./config.yml").use {
                config = Yaml().decodeFromStream<MainConfigSerializer>(it)
            }
        } catch (e: IOException) {
            logger.error("Please configure ${PLUGIN_DIR_FILE.canonicalPath}./config.yml", e)
        }

        logger.info("Setting file loaded successfully")
    }

    override fun reloadLang() {
        fileGetter.exportDefaultDirectory("./lang")
    }

    override fun onReady(event: ReadyEvent) {
        targetMember = jdaBot
            .getGuildById(config.guildId)!!
            .retrieveMemberById(config.userId).complete()
    }

    override fun onChannelCreate(event: ChannelCreateEvent) {
        TicketAddons.onCreate(event)
    }
}
