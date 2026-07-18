with open("app/src/main/java/com/pourify/distribution/ui/screens/DriverProfileScreen.kt", "r") as f:
    content = f.read()

import_map = "import androidx.compose.material.icons.filled.Map"
if import_map not in content:
    content = content.replace("import androidx.compose.material.icons.filled.Group", import_map + "\nimport androidx.compose.material.icons.filled.Group")

action_item = '                    Divider(color = MaterialTheme.colorScheme.outlineVariant)\n                    ActionListItem(icon = Icons.Filled.Map, title = "Live Agent Map", subtitle = "View agent locations and verify proximity", onClick = { navController.navigate("agent_map") })'

if "Live Agent Map" not in content:
    content = content.replace('ActionListItem(icon = Icons.Filled.Group, title = "Manage Customers"', action_item + '\n                    ActionListItem(icon = Icons.Filled.Group, title = "Manage Customers"')

with open("app/src/main/java/com/pourify/distribution/ui/screens/DriverProfileScreen.kt", "w") as f:
    f.write(content)
