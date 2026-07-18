with open("app/src/main/java/com/pourify/distribution/ui/MainViewModel.kt", "r") as f:
    content = f.read()

dummy_customers = """
                val dummyCustomers = listOf(
                    CustomerEntity("cust-1", "Shuaib Corp", "0300-1234567", 31.5204, 74.3587, 120.0, 480.0, 6, 2, "SCHEDULED"),
                    CustomerEntity("cust-2", "Ali Traders", "0300-7654321", 31.5210, 74.3590, 110.0, 0.0, 4, 0, "SCHEDULED"),
                    CustomerEntity("cust-3", "Zaid Mart", "0300-9998887", 31.5220, 74.3600, 130.0, 1500.0, 10, 5, "SCHEDULED"),
                )
                repository.insertCustomers(dummyCustomers)
"""

if "dummyCustomers" not in content:
    content = content.replace("repository.insertInventory(initialInventory)", "repository.insertInventory(initialInventory)\n" + dummy_customers)

with open("app/src/main/java/com/pourify/distribution/ui/MainViewModel.kt", "w") as f:
    f.write(content)
