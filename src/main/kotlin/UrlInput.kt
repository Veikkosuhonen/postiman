import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import components.TextLabel
import java.net.http.HttpRequest

private fun completeUrl(url: String): String {
    var result: String = url
    if (!url.matches(Regex("^https?://.*"))) {
        result = "https://$result"
    }
    return result
}

@Composable
fun RequestForm(onSubmit: (String, String, String, String) -> Unit) {
    Box {
        val state = rememberScrollState()

        Column(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp).verticalScroll(state),
            verticalArrangement = Arrangement.spacedBy(12.dp)

        ) {
            var urlInput by remember { mutableStateOf("") }
            var urlResult by remember { mutableStateOf(completeUrl(urlInput)) }
            var validUrl by remember { mutableStateOf(false) }
            var validHeaders by remember { mutableStateOf(true) }
            var headers by remember { mutableStateOf("") }
            var body by remember { mutableStateOf("") }
            var method by remember { mutableStateOf("GET") }
            var expanded by remember { mutableStateOf(false) }

            MethodSelector(method, { method = it }, expanded, { expanded = it })
            UrlInput(urlInput, urlResult, validUrl) {
                urlInput = it
                urlResult = completeUrl(urlInput)
                validUrl = Request.validateUrl(urlResult)
            }
            HeadersInput(headers, validHeaders) {
                headers = it
                validHeaders = Request.validateHeaders(headers)
            }
            BodyInput(body, validUrl) {
                body = it
            }
            Button(onClick = { onSubmit(method, urlResult, headers, body) }, enabled = validUrl && validHeaders) {
                Text("Send request")
            }
        }
        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd),
            adapter = rememberScrollbarAdapter(
                scrollState = state
            ),
        )
    }
}

@Composable
private fun MethodSelector(method: String, onChange: (String) -> Unit, expanded: Boolean, onOpen: (Boolean) -> Unit) {

    Row(verticalAlignment = Alignment.CenterVertically) {
        FormLabel("Method")
        Spacer(modifier = Modifier.width(4.dp))
        Box {
            OutlinedButton(onClick = { onOpen(true) }) {
                Text(method)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { onOpen(false) }) {
                Request.METHODS.forEach {
                    DropdownMenuItem(onClick = { onChange(it); onOpen(false) }) {
                        TextLabel(it)
                    }
                }
            }
        }
    }
}

@Composable
private fun UrlInput(input: String, result: String, valid: Boolean, onChange: (String) -> Unit) {
    val fieldColor: Color by animateColorAsState(if (valid) colors.secondary else colors.error)

    Column {
        FormTextField(input, { onChange(it) }, isError = !valid, placeholder = "url", singleLine = true)
        Spacer(modifier = Modifier.height(4.dp))
        Text(result,
            color = fieldColor,
            modifier = Modifier.border(1.dp, shape = MaterialTheme.shapes.small, color = fieldColor).padding(4.dp),
            style = MaterialTheme.typography.subtitle2
        )
    }
}

@Composable
private fun HeadersInput(input: String, valid: Boolean, onChange: (String) -> Unit) {
    Column {
        FormLabel("Headers")
        FormTextField(input, { onChange(it) }, isError = !valid, placeholder = "no headers")
    }
}

@Composable
private fun BodyInput(input: String, valid: Boolean, onChange: (String) -> Unit) {
    Column {
        FormLabel("Body")
        FormTextField(input, { onChange(it) }, isError = false, placeholder = "empty body")
    }
}

@Composable
fun PlaceHolder(text: String) = Text(text, fontStyle = FontStyle.Italic, color = Color.DarkGray)

@Composable
fun FormTextField(
    value: String,
    onChange: (String) -> Unit,
    isError: Boolean,
    placeholder: String,
    singleLine: Boolean = false
) = OutlinedTextField(
    value, onChange, isError = isError, placeholder = { PlaceHolder(placeholder) }, singleLine = singleLine
)

@Composable
fun FormLabel(text: String) {
    Text(text, fontWeight = FontWeight.Light)
}
