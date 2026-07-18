import glob

for filename in glob.glob("app/src/main/java/com/pourify/distribution/ui/screens/*.kt"):
    with open(filename, "r") as f:
        content = f.read()
    
    if "onNavigateToSync = { }," in content:
        content = content.replace("onNavigateToSync = { },", 'onNavigateToSync = { navController.navigate("reconciliation") },')
        with open(filename, "w") as f:
            f.write(content)
            print(f"Patched {filename}")

