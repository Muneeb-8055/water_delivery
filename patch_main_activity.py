with open("app/src/main/java/com/pourify/distribution/MainActivity.kt", "r") as f:
    content = f.read()

imports = """import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.pourify.distribution.worker.SyncWorker
"""
content = content.replace("import com.pourify.distribution.ui.theme.PourifyTheme", "import com.pourify.distribution.ui.theme.PourifyTheme\n" + imports)

# Find onSyncClick in RouteItineraryScreen and replace it with WorkManager call
# Actually, the RouteItineraryScreen is instantiated inside MainActivity without the WorkManager call being passed down currently if onSyncClick is just inside RouteItineraryScreen. Wait, no, looking at RouteItineraryScreen it doesn't take onSyncClick as a parameter. It has an internal PourifyTopAppBar where it sets onSyncClick = { /*TODO*/ }

