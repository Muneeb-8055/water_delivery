with open("app/src/main/java/com/pourify/distribution/ui/screens/RouteItineraryScreen.kt", "r") as f:
    content = f.read()

# Row intrinsic size fix
old_card_row = """        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()"""

new_card_row = """        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .width(8.dp)
                    .fillMaxHeight()"""

content = content.replace(old_card_row, new_card_row)

with open("app/src/main/java/com/pourify/distribution/ui/screens/RouteItineraryScreen.kt", "w") as f:
    f.write(content)
