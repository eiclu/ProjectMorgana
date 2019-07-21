package interfaces

interface ChatInterface {
    fun onUserUpdateChannels(userId: Long)
    fun onChannelsUpdated()
    fun onUserUpdateRole(userId: Long)
}