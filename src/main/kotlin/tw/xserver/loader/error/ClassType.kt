package tw.xserver.loader.error

enum class ClassType(val type: String) {
    Guild("Guild"),
    Channel("Channel"),
    GuildChannel("GuildChannel"),
    TextChannel("TextChannel"),
    VoiceChannel("VoiceChannel"),
    ForumChannel("ForumChannel"),
    User("User"),
    Member("Member"),
    PrivateChannel("PrivateChannel")
}
