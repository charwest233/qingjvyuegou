import os
from PIL import Image, ImageDraw

def create_placeholder_icon(path, color, text):
    img = Image.new('RGB', (81, 81), color=color)
    d = ImageDraw.Draw(img)
    # Just draw a simple square for now as placeholder
    d.rectangle([20, 20, 60, 60], outline="white", width=5)
    img.save(path)

icons = [
    ("home.png", (200, 200, 200), "H"),
    ("home-active.png", (212, 175, 55), "H"),
    ("category.png", (200, 200, 200), "C"),
    ("category-active.png", (212, 175, 55), "C"),
    ("cart.png", (200, 200, 200), "B"),
    ("cart-active.png", (212, 175, 55), "B"),
    ("user.png", (200, 200, 200), "U"),
    ("user-active.png", (212, 175, 55), "U")
]

os.makedirs("weixin/images", exist_ok=True)
for name, color, text in icons:
    create_placeholder_icon(f"weixin/images/{name}", color, text)

print("Placeholder icons created successfully.")
