package tw.xserver.plugin.placeholder

import org.apache.commons.text.StringSubstitutor


class Substitutor(
    private val varStart: String, // TODO: config.yml
    private val varEnd: String,
    private val mapper: MutableMap<String, String> = HashMap()
) {
    private var substitutor: StringSubstitutor

    init {
        substitutor = StringSubstitutor(mapper, varStart, varEnd)
    }

    fun get(key: String): String = mapper[key] ?: key
    fun put(kv: Map<String, String>) {
        mapper.putAll(kv)
        substitutor = StringSubstitutor(mapper, varStart, varEnd)
    }

    fun put(key: String, value: String) {
        mapper[key] = value
        substitutor = StringSubstitutor(mapper, varStart, varEnd)
    }

    fun parse(content: String?): String? {
        return content.let { substitutor.replace(it) }
    }
}