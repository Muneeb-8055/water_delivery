with open("app/build.gradle.kts", "r") as f:
    content = f.read()

maps_deps = """
    implementation("com.google.maps.android:maps-compose:4.3.0")
    implementation("com.google.android.gms:play-services-maps:18.2.0")
"""

if "maps-compose" not in content:
    content = content.replace('implementation(libs.androidx.core.ktx)', 'implementation(libs.androidx.core.ktx)' + maps_deps)

with open("app/build.gradle.kts", "w") as f:
    f.write(content)
