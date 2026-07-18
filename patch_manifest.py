with open("app/src/main/AndroidManifest.xml", "r") as f:
    content = f.read()

meta_data = """
        <meta-data
            android:name="com.google.android.geo.API_KEY"
            android:value="" />
"""

if "com.google.android.geo.API_KEY" not in content:
    content = content.replace("<application", "<application" + meta_data)

with open("app/src/main/AndroidManifest.xml", "w") as f:
    f.write(content)
