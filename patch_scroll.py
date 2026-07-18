import re

# Fix DriverProfileScreen
with open("app/src/main/java/com/pourify/distribution/ui/screens/DriverProfileScreen.kt", "r") as f:
    content = f.read()

if "import androidx.compose.foundation.rememberScrollState" not in content:
    content = content.replace("import androidx.compose.foundation.layout.*", "import androidx.compose.foundation.layout.*\nimport androidx.compose.foundation.verticalScroll\nimport androidx.compose.foundation.rememberScrollState")

content = content.replace("Modifier\n                .fillMaxSize()\n                .background(MaterialTheme.colorScheme.background)\n                .padding(innerPadding)\n                .padding(16.dp),", "Modifier\n                .fillMaxSize()\n                .background(MaterialTheme.colorScheme.background)\n                .padding(innerPadding)\n                .padding(16.dp)\n                .verticalScroll(rememberScrollState()),")

with open("app/src/main/java/com/pourify/distribution/ui/screens/DriverProfileScreen.kt", "w") as f:
    f.write(content)

# Fix SettingsScreen
with open("app/src/main/java/com/pourify/distribution/ui/screens/SettingsScreen.kt", "r") as f:
    content = f.read()

if "import androidx.compose.foundation.rememberScrollState" not in content:
    content = content.replace("import androidx.compose.foundation.layout.*", "import androidx.compose.foundation.layout.*\nimport androidx.compose.foundation.verticalScroll\nimport androidx.compose.foundation.rememberScrollState")

content = content.replace("Modifier\n                .fillMaxSize()\n                .background(MaterialTheme.colorScheme.background)\n                .padding(innerPadding)\n                .padding(16.dp),", "Modifier\n                .fillMaxSize()\n                .background(MaterialTheme.colorScheme.background)\n                .padding(innerPadding)\n                .padding(16.dp)\n                .verticalScroll(rememberScrollState()),")

with open("app/src/main/java/com/pourify/distribution/ui/screens/SettingsScreen.kt", "w") as f:
    f.write(content)

print("Patched scrolling")
