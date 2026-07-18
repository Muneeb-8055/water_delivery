with open("backend/public/index.html", "r") as f:
    html = f.read()

map_html = """
            <div class="mt-8 bg-white p-6 rounded-lg shadow-md border border-slate-200">
                <h3 class="text-xl font-bold text-brand-dark mb-4">Live Agent Map</h3>
                <div class="relative w-full h-96 bg-slate-200 rounded-lg overflow-hidden border border-slate-300 flex items-center justify-center">
                    <img src="https://upload.wikimedia.org/wikipedia/commons/thumb/3/3b/Paris_map.svg/1024px-Paris_map.svg.png" alt="Map Placeholder" class="absolute inset-0 w-full h-full object-cover opacity-50 grayscale" />
                    <div class="absolute inset-0 bg-brand-dark bg-opacity-10"></div>
                    <div class="absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 bg-white px-4 py-2 rounded-full shadow-lg text-sm font-bold text-brand-teal flex items-center gap-2">
                        <span class="w-2 h-2 rounded-full bg-brand-teal animate-ping"></span>
                        Live tracking active
                    </div>
                    <!-- Sample Markers -->
                    <div class="absolute top-1/4 left-1/3 bg-brand-blue text-white w-6 h-6 rounded-full flex items-center justify-center shadow-md font-bold text-xs">1</div>
                    <div class="absolute top-1/3 right-1/4 bg-brand-teal text-white w-6 h-6 rounded-full flex items-center justify-center shadow-md font-bold text-xs">2</div>
                </div>
            </div>
"""

# Insert it after the grid of metrics in view-overview
html = html.replace('                        </div>\n                    </div>\n                </div>\n            </div>', '                        </div>\n                    </div>\n                </div>\n            </div>\n' + map_html)

with open("backend/public/index.html", "w") as f:
    f.write(html)
