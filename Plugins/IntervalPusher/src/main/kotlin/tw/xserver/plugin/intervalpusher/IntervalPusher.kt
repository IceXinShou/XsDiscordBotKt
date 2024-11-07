package tw.xserver.plugin.intervalpusher

import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tw.xserver.loader.base.MainLoader.jdaBot
import java.io.IOException

class IntervalPusher(
    private val originUrl: String,
    private val intervalSeconds: Int,
    private val scope: CoroutineScope
) {
    private val client = OkHttpClient()
    private var job: Job? = null
    private val url = buildUrl(originUrl)

    fun start() {
        job = scope.launch(Dispatchers.IO) {
            while (isActive) {
                try {
                    val request = Request.Builder()
                        .url(buildUrl(url))
                        .build()

                    client.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) {
                            when (response.code) {
                                404 -> {
                                    logger.warn("Status Monitor refuse the connection! (Code: 404)")
                                    logger.warn("Break the heartbeat loop!")
                                    stop()
                                }

                                521 -> logger.warn("Status Monitor is OFFLINE! (Code: 521)") // reply from Cloudflare
                                else -> logger.error("Query URL $url failed, code: ${response.code}")
                            }
                        }
                    }
                } catch (e: IOException) {
                    logger.error("Query URL $originUrl internet error: ${e.message}")
                } catch (e: Exception) {
                    logger.error("Query URL $originUrl failed: ${e.message}")
                }
                delay(intervalSeconds * 1000L)
            }
        }
    }

    fun stop() {
        job?.cancel()
        client.dispatcher.executorService.shutdown()
        client.connectionPool.evictAll()
        client.cache?.close()
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(this::class.java)

        private fun buildUrl(url: String): String =
            url.replace("%ping%", jdaBot.gatewayPing.toString())
    }
}
