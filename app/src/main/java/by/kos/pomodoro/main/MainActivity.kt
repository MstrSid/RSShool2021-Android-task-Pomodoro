package by.kos.pomodoro.main

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import by.kos.pomodoro.R
import by.kos.pomodoro.databinding.ActivityMainBinding
import by.kos.pomodoro.foreground.ForegroundService
import by.kos.pomodoro.interfaces.TimerListener
import by.kos.pomodoro.model.PomodoroTimer
import by.kos.pomodoro.utils.COMMAND_ID
import by.kos.pomodoro.utils.COMMAND_START
import by.kos.pomodoro.utils.COMMAND_STOP
import by.kos.pomodoro.utils.STARTED_TIMER_TIME_MS
import by.kos.pomodoro.view.TimerAdapter

class MainActivity : AppCompatActivity(), TimerListener, LifecycleObserver {

    private lateinit var binding: ActivityMainBinding
    private val timerAdapter = TimerAdapter(this)
    private val timers = mutableListOf<PomodoroTimer>()
    private var nextId = 0
    private var mCounter: CountDownTimer? = null
    private var isRunning: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = timerAdapter
        }

        binding.recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {


            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                val firstVisiblePosition =
                    (recyclerView.layoutManager as LinearLayoutManager?)!!.findFirstVisibleItemPosition()
                val lastVisiblePosition =
                    (recyclerView.layoutManager as LinearLayoutManager?)!!.findLastVisibleItemPosition()

                Log.d("stopwatch", "$firstVisiblePosition $lastVisiblePosition")

                for (i in 0 until timers.size) {
                    if ((i < firstVisiblePosition || i > lastVisiblePosition) && timers[i].isStarted
                    ) {
                        //mCounter?.cancel()
                        timers[i].isOutScreen = true
                        Log.d(
                            "stopwatch",
                            "$isRunning"
                        )
                        if (!isRunning) {
                            isRunning = true
                            mCounter = object : CountDownTimer(timers[i].currentMs, 10L) {
                                override fun onTick(millisUntilFinished: Long) {
                                    if (millisUntilFinished >= 10L) {
                                        timers[i].currentMs -= 10L + System.currentTimeMillis() - System.currentTimeMillis()
                                        //currentMsOut += millisUntilFinished
                                        Log.d(
                                            "stopwatch",
                                            "muf: $millisUntilFinished"
                                        )
                                    } else {
                                        onFinish()
                                    }
                                }

                                override fun onFinish() {
                                    isRunning = false
                                    cancel()
                                    stop(timers[i].id, 0L)
                                }

                            }.start()
                        }
                    } else {
                        if (timers[i].isOutScreen) {
                            mCounter?.cancel()
                            isRunning = false
                            mCounter = null
                            Log.d("stopwatch", "${timers[i]}")
                            //Log.d("stopwatch", "${currentMsOut}")
                            //stopwatches[i].currentMs = currentMsOut
                            timers[i].isOutScreen = false
                        }
                    }
                }
            }

        })


        binding.addNewStopwatchButton.setOnClickListener {
            if (binding.inputMinutes.text.isNullOrBlank()) {
                Toast.makeText(applicationContext, "Input time!", Toast.LENGTH_SHORT).show()
            } else {
                val minutes = binding.inputMinutes.text.toString().toLong() * 60000
                nextId = if (timers.isEmpty()) {
                    -1
                } else {
                    timers.last().id
                }
                timers.add(PomodoroTimer(++nextId, minutes, minutes, false, false))
                timerAdapter.submitList(timers.toList())
                Log.d("stopwatch", nextId.toString())
                Log.d("stopwatch", timerAdapter.getItemId(nextId).toString())
            }
        }
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun start(id: Int) {
        changeTimer(id, null, true)
        mCounter?.cancel()
    }

    override fun stop(id: Int, currentMs: Long) {
        changeTimer(id, currentMs, false)
    }

    override fun delete(id: Int) {
        timers.remove(timers.find {
            it.id == id
        })
        timers.forEach {
            if (it.id > id) {
                it.id--
            }
        }
        timerAdapter.submitList(timers.toList())
        timerAdapter.notifyDataSetChanged()

    }

    private fun changeTimer(id: Int, currentMs: Long?, isStarted: Boolean) {
        timers.forEach {
            if (it.id == id) {
                it.isStarted = isStarted
                it.currentMs = currentMs ?: it.currentMs
                timers.set(it.id, it)
            } else {
                it.isStarted = false
                timers.set(it.id, it)
            }
        }
        timerAdapter.submitList(timers)
        timerAdapter.notifyDataSetChanged()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        try {
            val timer = timers.first { it.isStarted }
            val startIntent = Intent(this, ForegroundService::class.java)
            startIntent.putExtra(COMMAND_ID, COMMAND_START)
            startIntent.putExtra(STARTED_TIMER_TIME_MS, timer.currentMs)
            startService(startIntent)
        } catch (e: java.lang.Exception) {

        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        try {
            val stopIntent = Intent(this, ForegroundService::class.java)
            stopIntent.putExtra(COMMAND_ID, COMMAND_STOP)
            startService(stopIntent)
        } catch (e: Exception) {
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopService(Intent(this, ForegroundService::class.java))
    }
}