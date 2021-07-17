package by.kos.pomodoro.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import by.kos.pomodoro.databinding.TimerItemBinding
import by.kos.pomodoro.interfaces.TimerListener
import by.kos.pomodoro.model.PomodoroTimer

class TimerAdapter(private val listener: TimerListener): ListAdapter<PomodoroTimer, TimerViewHolder>(itemComparator) {

    var isStopped: Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimerViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = TimerItemBinding.inflate(layoutInflater, parent, false)
        return TimerViewHolder(binding, listener, null)
    }

    override fun onBindViewHolder(holder: TimerViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onViewRecycled(holder: TimerViewHolder) {
        super.onViewRecycled(holder)
        if(holder.cTimer != null){
            holder.cTimer?.cancel()
            if(isStopped){
                holder.cTimer?.onFinish()
            }
        }

    }



    private companion object {

        private val itemComparator = object : DiffUtil.ItemCallback<PomodoroTimer>() {

            override fun areItemsTheSame(oldItem: PomodoroTimer, newItem: PomodoroTimer): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: PomodoroTimer, newItem: PomodoroTimer): Boolean {
                return oldItem.currentMs == newItem.currentMs &&
                        oldItem.isStarted == newItem.isStarted
            }

            override fun getChangePayload(oldItem: PomodoroTimer, newItem: PomodoroTimer) = Any()
        }
    }
}