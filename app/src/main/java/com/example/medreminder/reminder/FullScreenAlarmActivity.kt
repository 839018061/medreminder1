package com.example.medreminder.reminder

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.medreminder.data.entity.AdherenceRecord
import com.example.medreminder.util.DateUtils
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FullScreenAlarmActivity : ComponentActivity() {

    private var scheduleId: Long = -1
    private var drugName: String = ""
    private var dosage: String = ""
    private var relation: String = ""
    private var tts: TtsSpeaker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showOnLockScreen()

        scheduleId = intent.getLongExtra(AlarmScheduler.EXTRA_SCHEDULE_ID, -1)
        drugName = intent.getStringExtra(AlarmScheduler.EXTRA_DRUG_NAME) ?: ""
        dosage = intent.getStringExtra(AlarmScheduler.EXTRA_DOSAGE) ?: ""
        relation = intent.getStringExtra(AlarmScheduler.EXTRA_RELATION) ?: ""

        tts = TtsSpeaker(this)
        tts?.speak("该吃药了，$drugName，$dosage")

        setContent {
            AlarmScreen(
                drugName = drugName,
                dosage = dosage,
                relation = relation,
                onTake = { mark(AdherenceRecord.TAKEN) },
                onLate = { mark(AdherenceRecord.LATE) },
                onDismiss = { finish() }
            )
        }
    }

    private fun showOnLockScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val km = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            km.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }
    }

    private fun mark(status: String) {
        val app = application as com.example.medreminder.MedReminderApp
        MainScope().launch {
            app.repository.insertOrUpdateRecord(
                AdherenceRecord(
                    scheduleId = scheduleId,
                    drugName = drugName,
                    planDate = DateUtils.today(),
                    planTime = System.currentTimeMillis(),
                    actualTime = System.currentTimeMillis(),
                    status = status
                )
            )
            finish()
        }
    }

    override fun onDestroy() {
        tts?.shutdown()
        super.onDestroy()
    }

    companion object {
        fun start(context: Context, scheduleId: Long, drugName: String, dosage: String, relation: String) {
            context.startActivity(intent(context, scheduleId, drugName, dosage, relation))
        }

        fun intent(context: Context, scheduleId: Long, drugName: String, dosage: String, relation: String): Intent =
            Intent(context, FullScreenAlarmActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra(AlarmScheduler.EXTRA_SCHEDULE_ID, scheduleId)
                putExtra(AlarmScheduler.EXTRA_DRUG_NAME, drugName)
                putExtra(AlarmScheduler.EXTRA_DOSAGE, dosage)
                putExtra(AlarmScheduler.EXTRA_RELATION, relation)
            }
    }
}

@androidx.compose.runtime.Composable
private fun AlarmScreen(
    drugName: String,
    dosage: String,
    relation: String,
    onTake: () -> Unit,
    onLate: () -> Unit,
    onDismiss: () -> Unit
) {
    val time = remember { mutableStateOf(currentHm()) }
    LaunchedEffect(Unit) {
        while (true) {
            time.value = currentHm()
            delay(30_000)
        }
    }
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.primary) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(time.value, fontSize = 72.sp, color = MaterialTheme.colorScheme.onPrimary)
            Spacer(Modifier.height(8.dp))
            Text("该吃药了", fontSize = 32.sp, color = MaterialTheme.colorScheme.onPrimary)
            Spacer(Modifier.height(32.dp))
            Text(
                drugName,
                fontSize = 40.sp,
                color = MaterialTheme.colorScheme.onPrimary,
                textAlign = TextAlign.Center
            )
            Text(dosage, fontSize = 28.sp, color = MaterialTheme.colorScheme.onPrimary)
            if (relation.isNotBlank() && relation != "无") {
                Text("（$relation）", fontSize = 22.sp, color = MaterialTheme.colorScheme.onPrimary)
            }
            Spacer(Modifier.height(48.dp))
            Button(onClick = onTake, modifier = Modifier.fillMaxWidth().height(64.dp)) {
                Text("已服药", fontSize = 24.sp)
            }
            Spacer(Modifier.height(16.dp))
            OutlinedButton(onClick = onLate, modifier = Modifier.fillMaxWidth().height(56.dp)) {
                Text("补服", fontSize = 20.sp)
            }
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onDismiss) {
                Text("稍后提醒", color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}

private fun currentHm(): String {
    val f = SimpleDateFormat("HH:mm", Locale.getDefault())
    return f.format(Date())
}
