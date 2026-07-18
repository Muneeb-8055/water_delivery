with open("backend/index.js", "r") as f:
    lines = f.readlines()

new_lines = []
for line in lines:
    if "app.use(express.static" not in line:
        new_lines.append(line)

# find express.json and insert static
for i, line in enumerate(new_lines):
    if "app.use(express.json());" in line:
        new_lines.insert(i+1, "app.use(express.static('public'));\n")
        break

with open("backend/index.js", "w") as f:
    f.writelines(new_lines)
