with open("app/src/main/AndroidManifest.xml", "r") as f:
    content = f.read()

content = content.replace("""    <application
        <meta-data
            android:name="com.google.android.geo.API_KEY"
            android:value="" />

        android:allowBackup="true\"""", """    <application
        android:allowBackup="true\"""")

content = content.replace("<activity", """        <meta-data
            android:name="com.google.android.geo.API_KEY"
            android:value="" />
        <activity""")

with open("app/src/main/AndroidManifest.xml", "w") as f:
    f.write(content)
