with open("app/src/main/java/com/pourify/distribution/utils/WhatsAppDispatcher.kt", "r") as f:
    content = f.read()

content = content.replace('setPackage("com.whatsapp")', '// setPackage("com.whatsapp")')
content = content.replace("context.startActivity(intent)", 'context.startActivity(Intent.createChooser(intent, "Share Invoice via"))')
content = content.replace('val intent = Intent(Intent.ACTION_VIEW, uri)', 'val intent = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, message) }')

with open("app/src/main/java/com/pourify/distribution/utils/WhatsAppDispatcher.kt", "w") as f:
    f.write(content)
    print("Patched WhatsApp")
