with open("app/src/main/java/com/pourify/distribution/ui/screens/UnifiedDashboardScreen.kt", "r") as f:
    content = f.read()

bad_nest = """                Spacer(modifier = Modifier.height(8.dp))
            item {"""

good_nest = """                Spacer(modifier = Modifier.height(8.dp))
            }
            item {"""

if bad_nest in content:
    content = content.replace(bad_nest, good_nest)
    
    # But wait, now I have an extra closing brace at the end of the first Row? Let's check the rest of that block.
    # The original block ended like:
    #                     modifier = Modifier.weight(1f)
    #                 )
    #             }
    #         }
    # Let's just fix it properly by parsing it. 
