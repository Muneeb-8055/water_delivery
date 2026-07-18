with open("app/src/main/java/com/pourify/distribution/ui/screens/UnifiedDashboardScreen.kt", "r") as f:
    lines = f.readlines()

new_lines = lines[:61] + [
'''                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = "Pending Routes",
                        value = "$pendingRoutes",
                        icon = Icons.Filled.Route,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Expected Cash",
                        value = "PKR ${totalCash.toInt()}",
                        icon = Icons.Filled.Payments,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Box(modifier = Modifier.fillMaxSize().background(Color(0xFFE2E8F0)))
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = "Map Marker",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.align(Alignment.Center).size(48.dp)
                        )
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
'''
] + lines[114:]

with open("app/src/main/java/com/pourify/distribution/ui/screens/UnifiedDashboardScreen.kt", "w") as f:
    f.writelines(new_lines)

