package jackameister.android.broadcastsender

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val context = this as Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        buttonSend.setOnClickListener { sendBroadcast() }
    }

    private fun sendBroadcast() {
        val broadcastName: String = tilBroadcastCode.editText?.text.toString()
        val sentSuccessfully: Boolean = BroadcastSender.send(context, INTENT_TO_SEND, broadcastName)
        if (sentSuccessfully) {
            textViewSentStatus.text = getString(R.string.send_success)
        } else {
            textViewSentStatus.text = getString(R.string.send_failure)
        }
    }

    companion object {
        /**
         * In the real world, where Conduit is sending the broadcast, this intent could contain anything.
         */
        private val INTENT_TO_SEND: Intent by lazy {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.addCategory(Intent.CATEGORY_BROWSABLE)
            intent.addCategory("jackameister.android.broadcastsender.category.ARBITRARY_EXAMPLE")
            intent.data = Uri.parse("https://example.com")
            intent.putExtra("numWidgets", 25)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            intent
        }
    }
}
