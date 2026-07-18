with open("app/src/main/java/com/pourify/distribution/ui/screens/DriverProfileScreen.kt", "r") as f:
    content = f.read()

import_group = "import androidx.compose.material.icons.filled.Group"
if import_group not in content:
    content = content.replace("import androidx.compose.material.icons.filled.ChevronRight", import_group + "\nimport androidx.compose.material.icons.filled.ChevronRight")

action_item = '                    Divider(color = MaterialTheme.colorScheme.outlineVariant)\n                    ActionListItem(icon = Icons.Filled.Group, title = "Manage Customers", subtitle = "Register new customers with geo-tags", onClick = { navController.navigate("manage_customers") })'

if "Manage Customers" not in content:
    content = content.replace('ActionListItem(icon = Icons.Filled.History, title = "Route History"', action_item + '\n                    ActionListItem(icon = Icons.Filled.History, title = "Route History"')

with open("app/src/main/java/com/pourify/distribution/ui/screens/DriverProfileScreen.kt", "w") as f:
    f.write(content)
    print("Patched DriverProfile")
