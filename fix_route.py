import re

with open("app/src/main/java/com/pourify/distribution/ui/screens/RouteItineraryScreen.kt", "r") as f:
    content = f.read()

# Fix Icons import
if "import androidx.compose.material.icons.Icons" not in content:
    content = content.replace("import androidx.compose.material3.*", "import androidx.compose.material.icons.Icons\nimport androidx.compose.material.icons.filled.Add\nimport androidx.compose.material3.*")
else:
    # already there but let's make sure
    pass

# Replace CustomerEntity creation
old_cust = """                                CustomerEntity(
                                    customerId = UUID.randomUUID().toString(),
                                    businessName = businessName,
                                    contactPhone = phone,
                                    geoLatitude = 0.0,
                                    geoLongitude = 0.0,
                                    balanceReceivable = bal,
                                    companyOwnedBottles = 0,
                                    visitStatus = "SCHEDULED",
                                    tenantId = "b1234567-89ab-cdef-0123-456789abcdef",
                                    syncStatus = 0
                                )"""
new_cust = """                                CustomerEntity(
                                    customerId = UUID.randomUUID().toString(),
                                    businessName = businessName,
                                    contactPhone = phone,
                                    geoLatitude = 0.0,
                                    geoLongitude = 0.0,
                                    cachedHistoricalRate = 0.0,
                                    balanceReceivable = bal,
                                    companyOwnedBottles = 0,
                                    depositBackedBottles = 0,
                                    visitStatus = "SCHEDULED"
                                )"""

content = content.replace(old_cust, new_cust)

with open("app/src/main/java/com/pourify/distribution/ui/screens/RouteItineraryScreen.kt", "w") as f:
    f.write(content)
