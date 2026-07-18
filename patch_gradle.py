with open("app/build.gradle.kts", "r") as f:
    content = f.read()

content = content.replace('versionName = "1.0"', 'versionName = "1.0.0-Pourify"')
content = content.replace('isMinifyEnabled = false', 'isMinifyEnabled = true')

with open("app/build.gradle.kts", "w") as f:
    f.write(content)
