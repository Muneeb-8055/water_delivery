with open("app/src/main/java/com/pourify/distribution/ui/screens/UnifiedDashboardScreen.kt", "r") as f:
    content = f.read()

if "Live Area Map" not in content:
    map_code = """
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Dummy map background
                        Box(modifier = Modifier.fillMaxSize().background(Color(0xFFE2E8F0)))
                        
                        // Map marker
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = "Map Marker",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.align(Alignment.Center).size(48.dp)
                        )
                        
                        // Overlay text
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(16.dp)
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Live Area Map", style = Typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
"""
    content = content.replace('Spacer(modifier = Modifier.height(8.dp))', 'Spacer(modifier = Modifier.height(8.dp))\n' + map_code, 1)
    with open("app/src/main/java/com/pourify/distribution/ui/screens/UnifiedDashboardScreen.kt", "w") as f:
        f.write(content)
