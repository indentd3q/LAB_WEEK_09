package com.example.lab_week_09

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.lab_week_09.ui.theme.LAB_WEEK_09Theme
import com.example.lab_week_09.ui.elements.OnBackgroundItemText
import com.example.lab_week_09.ui.elements.OnBackgroundTitleText
import com.example.lab_week_09.ui.elements.PrimaryTextButton
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

// Data class representing a Student
data class Student(
    var name: String
)

// Singleton object to hold the Moshi instance
object MoshiInstance {
    val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LAB_WEEK_09Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    App(navController = navController)
                }
            }
        }
    }
}

@Composable
fun Home(
    navigateFromHomeToResult: (String) -> Unit
) {
    val listData = remember { mutableStateListOf(Student("Tanu"), Student("Tina"), Student("Tono")) }
    var inputField by remember { mutableStateOf(Student("")) }
    var errorMessage by remember { mutableStateOf("") }

    HomeContent(
        listData = listData,
        inputField = inputField,
        errorMessage = errorMessage,
        onInputValueChange = { input ->
            inputField = inputField.copy(name = input)
            errorMessage = "" // Clear error when input changes
        },
        onButtonClick = {
            if (inputField.name.isBlank()) {
                errorMessage = "Name cannot be empty!"
            } else {
                listData.add(inputField)
                inputField = inputField.copy(name = "")
            }
        },
        navigateFromHomeToResult = {
            // Convert listData to JSON
            val type = Types.newParameterizedType(List::class.java, Student::class.java)
            val jsonAdapter = MoshiInstance.moshi.adapter<List<Student>>(type)
            val jsonString = jsonAdapter.toJson(listData.toList())
            navigateFromHomeToResult(jsonString)
        }
    )
}

@Composable
fun HomeContent(
    listData: SnapshotStateList<Student>,
    inputField: Student,
    errorMessage: String,
    onInputValueChange: (String) -> Unit,
    onButtonClick: () -> Unit,
    navigateFromHomeToResult: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OnBackgroundTitleText(text = "Enter Item")

        TextField(
            value = inputField.name,
            onValueChange = onInputValueChange,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            modifier = Modifier.padding(vertical = 8.dp)
        )

        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = Color.Red,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Row {
            PrimaryTextButton(text = "Add Item") {
                onButtonClick()
            }

            PrimaryTextButton(text = "Show Result") {
                navigateFromHomeToResult()
            }
        }

        LazyColumn {
            items(listData) { item ->
                Column(
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OnBackgroundItemText(text = item.name)
                }
            }
        }
    }
}

@Composable
fun App(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            Home { listData ->
                navController.navigate("resultContent/?listData=$listData")
            }
        }

        composable(
            "resultContent/?listData={listData}",
            arguments = listOf(navArgument("listData") {
                type = NavType.StringType
            })
        ) { backStackEntry ->
            ResultContent(listData = backStackEntry.arguments?.getString("listData").orEmpty())
        }
    }
}

@Composable
fun ResultContent(listData: String) {
    // Parse JSON back to a list of Student objects
    val type = Types.newParameterizedType(List::class.java, Student::class.java)
    val jsonAdapter = MoshiInstance.moshi.adapter<List<Student>>(type)
    val students = jsonAdapter.fromJson(listData) ?: emptyList()

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LazyColumn {
            items(students) { student ->
                OnBackgroundItemText(text = student.name)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewHome() {
    LAB_WEEK_09Theme {
        Home {}
    }
}
