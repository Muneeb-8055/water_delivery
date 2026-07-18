with open("backend/public/index.html", "r") as f:
    html = f.read()

html = html.replace(
    "if(!document.getElementById('view-overview').classList.contains('hidden')) {",
    "if(tenantId && !document.getElementById('view-overview').classList.contains('hidden')) {"
)

with open("backend/public/index.html", "w") as f:
    f.write(html)
    print("Patched HTML fix")
