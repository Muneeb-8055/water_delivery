with open("app/src/main/java/com/pourify/distribution/ui/screens/SettingsScreen.kt", "r") as f:
    content = f.read()

import_statement = "import androidx.compose.material.icons.filled.Logout"
if import_statement not in content:
    content = content.replace("import androidx.compose.material.icons.filled.Storage", "import androidx.compose.material.icons.filled.Storage\n" + import_statement)

logout_button = """
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedButton(
                onClick = { navController.navigate("login") { popUpTo(0) } },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.error),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(imageVector = Icons.Filled.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Logout", style = Typography.labelMedium)
            }
"""

if "Logout" not in content.split("CLEAR LOCAL CACHE")[1]:
    content = content.replace("CLEAR LOCAL CACHE\", style = Typography.labelMedium)\n                        }\n                    }\n                }\n            }\n        }\n    }\n}", "CLEAR LOCAL CACHE\", style = Typography.labelMedium)\n                        }\n                    }\n                }\n            }\n" + logout_button + "        }\n    }\n}")

with open("app/src/main/java/com/pourify/distribution/ui/screens/SettingsScreen.kt", "w") as f:
    f.write(content)
