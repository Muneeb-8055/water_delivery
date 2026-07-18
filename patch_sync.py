with open("app/src/main/java/com/pourify/distribution/ui/screens/RouteItineraryScreen.kt", "r") as f:
    content = f.read()

imports = """import androidx.compose.ui.platform.LocalContext
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.pourify.distribution.worker.SyncWorker
"""

if "import androidx.work.WorkManager" not in content:
    content = content.replace("import com.pourify.distribution.ui.theme.Typography", "import com.pourify.distribution.ui.theme.Typography\n" + imports)

context_code = "val customers by viewModel.customers.collectAsStateWithLifecycle()\n    val context = LocalContext.current"
content = content.replace("val customers by viewModel.customers.collectAsStateWithLifecycle()", context_code)

old_sync = "onSyncClick = { /*TODO*/ }"
new_sync = """onSyncClick = { 
                    val workRequest = OneTimeWorkRequestBuilder<SyncWorker>().build()
                    WorkManager.getInstance(context).enqueue(workRequest)
                }"""
content = content.replace(old_sync, new_sync)

with open("app/src/main/java/com/pourify/distribution/ui/screens/RouteItineraryScreen.kt", "w") as f:
    f.write(content)
