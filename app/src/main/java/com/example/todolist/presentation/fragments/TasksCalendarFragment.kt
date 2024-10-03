package com.example.todolist.presentation.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.events.calendar.views.EventsCalendar
import com.example.todolist.R
import com.example.todolist.data.model.TaskCategoryInfo
import com.example.todolist.databinding.FragmentTasksCalendarBinding
import com.example.todolist.presentation.MainActivity
import com.example.todolist.presentation.MainActivityViewModel
import com.example.todolist.presentation.adapter.TasksAdapter
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import javax.inject.Named

@AndroidEntryPoint
class TasksCalendarFragment : ParentFragment(), EventsCalendar.Callback {
    private lateinit var viewModel: MainActivityViewModel
    private lateinit var binding: FragmentTasksCalendarBinding
    private lateinit var tasks: List<TaskCategoryInfo>
    private var dateFilter: Date? = null

    @Inject
    @Named("task_calendar_fragment")
    lateinit var adapter: TasksAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_tasks_calendar, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as MainActivity).viewModel

        val today = Calendar.getInstance()
        dateFilter = today.time
        val end = Calendar.getInstance()
        end.add(Calendar.YEAR, 2)
        binding.eventsCalendar.setSelectionMode(binding.eventsCalendar.SINGLE_SELECTION)
            .setToday(today)
            .setMonthRange(today, end)
            .setWeekStartDay(Calendar.SUNDAY, false)
            .setCurrentSelectedDate(today)
            .setCallback(this)
            .build()

        viewModel = (activity as MainActivity).viewModel
        adapter.setOnTaskStatusChangedListener {
            updateTaskStatus(viewModel, it)
        }
        adapter.setOnItemClickListener {
            editTaskInformation(it)
        }
        initRecyclerView()

        viewModel.getAllTask().observe(viewLifecycleOwner) { tasks ->
            setEvents(tasks)
            this.tasks = tasks
            binding.eventsCalendar.build()
            val filteredTasks = dateFilter?.let { date ->
                tasks.filter {
                    compareDates(it.taskInfo.date, date)
                }
            } ?: tasks
            adapter.differ.submitList(filteredTasks)
            if (filteredTasks.isEmpty()) binding.noResultAnimationView.visibility = View.VISIBLE
            else binding.noResultAnimationView.visibility = View.GONE
        }

        binding.fab.setOnClickListener {
            val action = TasksCalendarFragmentDirections.actionCalendarFragmentToNewTaskFragment(
                newTaskArg = null,
                calendarArg = dateFilter
            )
            it.findNavController().navigate(action)
        }
    }

    private fun setEvents(dates: List<TaskCategoryInfo>) {
        dates.forEach {
            val calendar = Calendar.getInstance()
            calendar.time = it.taskInfo.date
            binding.eventsCalendar.addEvent(calendar)
        }
    }

    private fun initRecyclerView() {
        binding.tasksRv.adapter = adapter
        binding.tasksRv.layoutManager = LinearLayoutManager(requireContext())
        val itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
        itemTouchHelper.attachToRecyclerView(binding.tasksRv)
    }

    private fun editTaskInformation(taskCategoryInfo: TaskCategoryInfo) {
        val action = TasksCalendarFragmentDirections.actionCalendarFragmentToNewTaskFragment(
            newTaskArg = taskCategoryInfo,
            calendarArg = null
        )
        findNavController().navigate(action)
    }

    private val simpleItemTouchCallback: ItemTouchHelper.SimpleCallback = object :
        ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT
        ) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, swipeDir: Int) {
            val position = viewHolder.adapterPosition
            val taskInfo = adapter.differ.currentList[position]?.taskInfo
            val categoryInfo = adapter.differ.currentList[position]?.categoryInfo?.get(0)
            if (taskInfo != null && categoryInfo != null) {
                deleteTask(viewModel, taskInfo, categoryInfo)
                Snackbar.make(
                    binding.root,
                    getString(R.string.task_deleted_message),
                    Snackbar.LENGTH_LONG
                )
                    .apply {
                        setAction(getString(R.string.take_back_message)) {
                            viewModel.insertTaskAndCategory(taskInfo, categoryInfo)
                        }
                        show()
                    }
            }
        }
    }

    override fun onDayLongPressed(selectedDate: Calendar?) {
        //TODO
    }

    override fun onDaySelected(selectedDate: Calendar?) {
        dateFilter = selectedDate?.time
        dateFilter?.let { date ->
            val filteredTasks = tasks.filter {
                compareDates(it.taskInfo.date, date)
            }
            adapter.differ.submitList(filteredTasks)
            if (filteredTasks.isEmpty()) binding.noResultAnimationView.visibility = View.VISIBLE
            else binding.noResultAnimationView.visibility = View.GONE
        }
    }

    override fun onMonthChanged(monthStartDate: Calendar?) {
        //TODO
    }

    private fun compareDates(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance()
        val cal2 = Calendar.getInstance()
        cal1.time = date1
        cal2.time = date2

        val year1 = cal1.get(Calendar.YEAR)
        val month1 = cal1.get(Calendar.MONTH)
        val day1 = cal1.get(Calendar.DAY_OF_MONTH)

        val year2 = cal2.get(Calendar.YEAR)
        val month2 = cal2.get(Calendar.MONTH)
        val day2 = cal2.get(Calendar.DAY_OF_MONTH)

        return year1 == year2 && month1 == month2 && day1 == day2
    }
}