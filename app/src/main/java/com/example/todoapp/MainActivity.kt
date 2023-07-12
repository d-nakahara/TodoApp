package com.example.todoapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todoapp.ui.theme.TodoAppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat

class MainActivity : ComponentActivity() {
    private val dao = RoomApplication.database.todoDao()
    private var todoList = mutableStateListOf<Todo>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TodoAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    MainScreen(todoList)

                }
            }
        }
        loadTodo()
    }

    private fun loadTodo() {
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.Default) {
                dao.getAll().forEach { todo ->
                    todoList.add(todo)
                }
            }
        }
    }

    private fun postTodo(title: String) {
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.Default) {
                dao.post(Todo(title = title,id = 0))

                todoList.clear()
                loadTodo()
            }
        }
    }

    private fun deleteTodo(todo: Todo) {
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.Default) {
                dao.delete(todo)

                todoList.clear()
                loadTodo()
            }
        }
    }

    @Composable
    fun MainScreen(todoList: SnapshotStateList<Todo>) {
        var text: String by remember { mutableStateOf("") }

        Column {
            TopAppBar(
                title = { Text("Todo List") }
            )
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                items(todoList) { todo ->
                    key(todo.id) {
                        TodoItem(todo)
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { it -> text = it },
                    label = { Text("ToDo") },
                    modifier = Modifier.wrapContentHeight().weight(1f)
                )
                Spacer(Modifier.size(16.dp))
                Button(
                    onClick = {
                        if (text.isEmpty()) return@Button
                        postTodo(text)
                        text = ""
                    },
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) {
                    Text("ADD")
                }
            }

        }
    }

    @Composable
    fun TodoItem(todo: Todo) {
        val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clickable(onClick = { deleteTodo(todo) })
        ) {
            Text(
                text = todo.title,
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
            )
            Text(
                text = "created at: ${sdf.format(todo.created_at)}",
                fontSize = 12.sp,
                color = Color.LightGray,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}