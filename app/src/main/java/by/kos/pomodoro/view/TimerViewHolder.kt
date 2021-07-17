package by.kos.pomodoro.view

import android.annotation.SuppressLint
import android.graphics.drawable.AnimationDrawable
import android.media.MediaPlayer
import android.os.CountDownTimer
import android.widget.Toast
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView
import by.kos.pomodoro.R
import by.kos.pomodoro.databinding.TimerItemBinding
import by.kos.pomodoro.interfaces.TimerListener
import by.kos.pomodoro.model.PomodoroTimer
import by.kos.pomodoro.utils.displayTime
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Job

class TimerViewHolder(
    private val binding: TimerItemBinding,
    private val listener: TimerListener,
    private var job: Job?,
    private val mediaPlayer: MediaPlayer? = MediaPlayer.create(
        binding.root.context,
        R.raw.darthskills
    )
) : RecyclerView.ViewHolder(binding.root) {

    var cTimer: CountDownTimer? = null
    private var timerOut: PomodoroTimer? = null

    @DelicateCoroutinesApi
    fun bind(timer: PomodoroTimer) {
        binding.pomodoreTimer.text = timer.currentMs.displayTime()
        if (timer.isStarted) {
            startTimer(timer)
        } else {
            stopTimer(timer)
        }
        initButtonsListeners(timer)
    }

    private fun initButtonsListeners(timer: PomodoroTimer) {
        binding.startPauseButton.setOnClickListener {
            if (timer.isStarted) {
                listener.stop(timer.id, timer.currentMs)
            } else {
                if (timer.currentMs > 0L) {
                    listener.start(timer.id)
                }
            }
        }
        binding.deleteButton.setOnClickListener { listener.delete(timer.id) }
    }


    @DelicateCoroutinesApi
    @SuppressLint("UseCompatLoadingForDrawables")
    private fun startTimer(timer: PomodoroTimer) {
        timerOut = timer
        binding.startPauseButton.text = itemView.context.getString(R.string.stop)
        cTimer?.cancel()
        cTimer = getCountDownTimer(timer)
        cTimer?.start()
        binding.blinkingIndicator.isInvisible = false
        (binding.blinkingIndicator.background as? AnimationDrawable)?.start()
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun stopTimer(timer: PomodoroTimer) {
        binding.startPauseButton.text = itemView.context.getString(R.string.start)
        job?.cancel()
        cTimer?.cancel()
        binding.customView.setPeriod(timer.totalMs)
        binding.customView.setCurrent(timer.currentMs)
        binding.blinkingIndicator.isInvisible = true
        (binding.blinkingIndicator.background as? AnimationDrawable)?.stop()
    }

    private fun getCountDownTimer(timer: PomodoroTimer): CountDownTimer {
        TimerListener.Current.currentMs = timer.currentMs
        val countDownTimer = object : CountDownTimer(PERIOD, UNIT_TEN_MS) {
            val interval = UNIT_TEN_MS
            override fun onTick(millisUntilFinished: Long) {
                timer.currentMs -= interval + System.currentTimeMillis() - System.currentTimeMillis()
                TimerListener.Current.currentMs = timer.currentMs
                binding.customView.setPeriod(timer.totalMs)
                binding.customView.setCurrent(timer.currentMs)
                binding.pomodoreTimer.text = timer.currentMs.displayTime()
                if (timer.currentMs < 10L) {
                    onFinish()
                }
            }

            override fun onFinish() {
                listener.stop(timer.id, timer.currentMs)
                mediaPlayer?.setScreenOnWhilePlaying(true)
                mediaPlayer?.start()
                cTimer?.cancel()
                job?.cancel()
                Toast.makeText(binding.root.context, "Time is out!!!", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        return countDownTimer
    }


    private companion object {
        private const val UNIT_TEN_MS = 1000L
        private const val PERIOD = 1000L * 60L * 60L * 24L // Day
    }
}