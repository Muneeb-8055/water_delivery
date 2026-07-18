const fs = require('fs');
let content = fs.readFileSync('backend/index.js', 'utf8');

if (!content.includes("express.static('public')")) {
    content = content.replace("app.use(express.json());", "app.use(express.json());\napp.use(express.static('public'));");
    fs.writeFileSync('backend/index.js', content);
    console.log("Patched index.js");
}
