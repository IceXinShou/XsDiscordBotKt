package tw.xserver.loader.plugin.yaml

import tw.xserver.loader.plugin.Event

class Info(
    val name: String,
    val pluginInstance: Event,
    val depend: List<String>,
    val softDepend: List<String>
)
