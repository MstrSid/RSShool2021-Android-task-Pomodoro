package by.kos.pomodoro.model

data class PomodoroTimer(
    var id: Int,
    var currentMs: Long,
    val totalMs: Long,
    var isStarted: Boolean,
    var isOutScreen: Boolean
)
