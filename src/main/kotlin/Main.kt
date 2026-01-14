import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Winner Points B93"
    ) {
        MaterialTheme {
            App()
        }
    }
}

@Composable
@Preview
fun App() {
    var text by remember { mutableStateOf("Hello World!") }
    var count by remember { mutableStateOf(0) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.h4
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Count: $count",
                style = MaterialTheme.typography.h5
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(onClick = {
                    count++
                    text = "Clicked $count times!"
                }) {
                    Text("Click Me")
                }

                Button(onClick = {
                    count = 0
                    text = "Reset!"
                }) {
                    Text("Reset")
                }
            }
        }
    }
}