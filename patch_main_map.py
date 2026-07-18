with open("app/src/main/java/com/pourify/distribution/MainActivity.kt", "r") as f:
    content = f.read()

route = """
                    composable("agent_map") {
                        AgentMapScreen(
                            viewModel = viewModel,
                            navController = navController
                        )
                    }
"""

if "agent_map" not in content:
    content = content.replace('composable("manage_customers") {', route + '\n                    composable("manage_customers") {')

with open("app/src/main/java/com/pourify/distribution/MainActivity.kt", "w") as f:
    f.write(content)
