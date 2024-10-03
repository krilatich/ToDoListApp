package com.example.todolist.presentation.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.FileProvider
import com.example.todolist.R
import com.example.todolist.data.model.TaskInfo
import com.example.todolist.presentation.MainActivity
import com.example.todolist.presentation.MainActivityViewModel
import com.example.todolist.presentation.adapter.TasksAdapter
import com.example.todolist.presentation.mapper.toTaskInfo
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileWriter
import java.io.IOException
import javax.inject.Inject
import javax.inject.Named

@AndroidEntryPoint
class JsonToEmailFragment : ParentFragment() {

    @Inject
    @Named("task_calendar_fragment")
    lateinit var adapter: TasksAdapter
    private lateinit var viewModel: MainActivityViewModel

    private var tasks: List<TaskInfo> = listOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_json_email, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as MainActivity).viewModel
        viewModel.getAllTask().observe(viewLifecycleOwner) { tasks ->
            this.tasks = tasks.map { it.toTaskInfo() }
        }

        val sendUncompletedTaskButton = view.findViewById<Button>(R.id.btn_send_uncompleted_tasks)
        sendUncompletedTaskButton.setOnClickListener {
            val fileName = "uncompleted_tasks.json"
            val file = File(
                requireContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                fileName
            )
            try {
                FileWriter(file).use { writer ->
                    writer.write(Gson().toJson(tasks))
                }
            } catch (e: IOException) {
                e.printStackTrace()
                // Обработка ошибки записи файла
                return@setOnClickListener
            }
            val fileUri: Uri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                file
            )
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, fileUri)
                type = "application/json"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val shareIntent = Intent.createChooser(sendIntent, "Отправить задачи")
            startActivity(shareIntent)
        }
    }

    companion object {

        @JvmStatic
        fun newInstance() = JsonToEmailFragment()
    }
}