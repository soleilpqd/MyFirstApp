package sharedlib.httpconnection.apiconnection

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.sendBlocking

/** Manager of Tasks */
class ConnSession {

    /** Additional headers for this session (apply before ConnTaskRequestBuilder)  */
    var additionalHeaders: Map<ConnRequestHeaderField, List<String>>? = null
    var charSet = Charsets.UTF_8

    companion object {
        /** Default `ConnSession` to init `ConnTask` */
        var default = ConnSession()
    }

    private val taskScope = CoroutineScope(Dispatchers.IO)
    private val myScope = CoroutineScope(Dispatchers.IO)
    private val myChannel = Channel<Job>(capacity = Channel.UNLIMITED).apply {
        myScope.launch { consumeEach { it.join() } }
    }
    private var tasks = mutableListOf<ConnTask>()

    internal val componentsSettings: MutableSet<Any> = mutableSetOf()

    override fun equals(other: Any?): Boolean {
        if (other is ConnSession) {
            return other === this
        }
        return false
    }

    internal fun start(task: ConnTask) {
        if (tasks.contains(task)) {
            return
        }
        val thisMe = this
        myChannel.sendBlocking(
            thisMe.myScope.launch {
                thisMe.tasks.add(task)
                task.job = thisMe.taskScope.launch {
                    task.perform()
                    clean(task)
                }
            }
        )
    }

    private fun clean(task: ConnTask) {
        val thisMe = this
        myChannel.sendBlocking(
            thisMe.myScope.launch {
                thisMe.tasks.remove(task)
            }
        )
    }

    internal fun stop(task: ConnTask) {
        if (!tasks.contains(task)) {
            return
        }
        task.job?.cancel()
        clean(task)
    }

    fun stopAll() {
        for (task in tasks) {
            task.stop()
        }
    }

    internal inline fun <reified SettingType: Any> setComponentSettings(settings: SettingType) {
        try {
            componentsSettings.removeIf { it is SettingType }
        } catch (exception: Exception) {

        }
        componentsSettings.add(settings)
    }

    internal inline fun <reified SettingType: Any> getComponentSettings(settingType: Class<SettingType>): SettingType? {
        return try {
            componentsSettings.first { it is SettingType } as? SettingType
        } catch (exception: Exception) {
            null
        }
    }

}
