package tw.xserver.plugin.intervalpusher

import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException

class IntervalPusher(
    private val url: String,
    private val intervalSeconds: Int,
    private val scope: CoroutineScope
) {
    private val client = OkHttpClient()
    private var job: Job? = null

    fun start() {
        job = scope.launch(Dispatchers.IO) {
            while (isActive) {
                try {
                    val request = Request.Builder()
                        .url(url)
                        .build()

                    client.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) {
                            logger.error("Query URL $url failed, code: ${response.code}")
                        }
                    }
                } catch (e: IOException) {
                    logger.error("Query URL $url internet error: ${e.message}")
                } catch (e: Exception) {
                    logger.error("Query URL $url failed: ${e.message}")
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
    }
}
