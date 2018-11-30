package jackameister.android.broadcastsender

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Parcelable

/**
 * Sends broadcasts in the same format that Conduit does.
 */
object BroadcastSender {

    private const val ACTION_BROADCAST = "jackameister.android.conduit.broadcast"
    private const val TOP_LEVEL_TYPE = "conduit"
    private const val EXTRA_WRAPPED_INTENT = "jackameister.android.conduit.extra.intent"

    /**
     * Sends a broadcast intent with the following properties:
     *  - Its action is "jackameister.android.conduit.broadcast".
     *  - Its MIME type is "conduit/[name]".
     *  - It has an extra with key "intent" and value [intent]. ([intent] is made into a parcel.
     *  See [Parcelable].)
     *
     * Other applications can easily be built to receive all broadcasts with the correct action
     * and any MIME type. That means that Conduit can be used as a "funnel" for intents. For
     * example, an automation app might receive a broadcast sent through this method, allowing it
     * to perform an arbitrary action in response. (To be even more concrete, a user with an
     * automation app and Conduit installed could set things up so that their phone automatically
     * turns on Bluetooth and sets the volume to 25% whenever they open an MP3 file.)
     *
     * @param context A context from this application
     * @param intent The intent whose fields will be sent in the broadcast
     * @return Whether a broadcast was sent to at least one recipient; however, if this app doesn't
     * hold the proper permission for a given recipient, the system won't deliver the broadcast and
     * true will still be returned
     */
    fun send(context: Context, intent: Intent, name: String): Boolean {
        val broadcastIntent = wrapInBroadcastableIntent(intent, name)

        /*
         * Android O (API level 26) introduced restrictions that keep other apps from receiving
         * implicit broadcast intents. We can get around the restrictions by asking the package
         * manager for the installed components that match our broadcast intent's fields, then
         * sending an *explicit* broadcast to each of those components.
         */
        val matches = context.packageManager.queryBroadcastReceivers(broadcastIntent, 0)
        if (matches.isEmpty()) return false
        for (resolveInfo in matches) {
            val explicitBroadcastIntent = Intent(broadcastIntent)
            val componentName = ComponentName(
                resolveInfo.activityInfo.applicationInfo.packageName,
                resolveInfo.activityInfo.name
            )
            explicitBroadcastIntent.component = componentName
            context.sendBroadcast(explicitBroadcastIntent)
        }
        return true
    }

    /**
     * Wraps an intent inside a newly-created intent's data field. The old intent is stored as
     * a parcel inside the new intent's extras. Its key is [EXTRA_WRAPPED_INTENT]. The new intent's
     * action is [ACTION_BROADCAST] and its MIME type is "[TOP_LEVEL_TYPE]/[broadcastCode]".
     *
     * @param intent The intent to wrap up
     * @param broadcastCode A code that can be used to identify the new intent by its type field
     * @return The new, "broadcastable", intent, with the old intent's fields stored inside it
     */
    private fun wrapInBroadcastableIntent(intent: Intent, broadcastCode: String): Intent {
        val broadcastableIntent = Intent(ACTION_BROADCAST)
        broadcastableIntent.type = "$TOP_LEVEL_TYPE/$broadcastCode"
        broadcastableIntent.putExtra(EXTRA_WRAPPED_INTENT, intent)
        return broadcastableIntent
    }
}
