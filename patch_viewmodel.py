with open("app/src/main/java/com/pourify/distribution/ui/MainViewModel.kt", "r") as f:
    content = f.read()

func = """
    fun insertCustomer(customer: CustomerEntity) {
        viewModelScope.launch {
            repository.insertCustomers(listOf(customer))
        }
    }
"""

if "fun insertCustomer(" not in content:
    content = content.replace("fun updateCustomerLocation(", func + "\n    fun updateCustomerLocation(")
    with open("app/src/main/java/com/pourify/distribution/ui/MainViewModel.kt", "w") as f:
        f.write(content)
        print("Patched MainViewModel")
