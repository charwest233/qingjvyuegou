"""
import_products.py - E-commerce product batch import tool (Pexels real images)
Usage: pip install requests && python import_products.py
"""

import requests
import uuid
from pathlib import Path

# Configuration
PEXELS_KEY = 'u0YdoyFtSGoIxno3AAbFqFEH2gMpYKwKjTVYPONDsBp8poEMY0m7TzUf'
BASE_URL = 'https://api.pexels.com/v1/search'

# Product data: (category_id, name, price, stock, description, english_search_keyword)
PRODUCTS = [
    # 1-手机数码
    (1, 'iPhone 16 Pro Max', 9999.00, 50, 'A18 Pro芯片，钛金属机身，4800万像素三摄', 'iPhone 16 pro max'),
    (1, '华为 Mate 70 Pro', 8999.00, 60, '麒麟芯片，卫星通信，鸿蒙OS', 'Huawei smartphone'),
    (1, '小米 15 Ultra', 6999.00, 80, '骁龙8至尊版，徕卡四摄', 'Xiaomi smartphone'),
    (1, '漫步者蓝牙耳机', 299.00, 500, '降噪长续航，Hi-Res认证', 'bluetooth earphone'),
    (1, 'Apple Watch S10', 3199.00, 120, '血氧检测，心率监测，运动追踪', 'Apple Watch'),
    # 2-电脑办公
    (2, 'MacBook Air M4', 8999.00, 30, 'M4芯片，16GB内存，轻薄长续航', 'MacBook Air'),
    (2, '联想 ThinkPad X1', 10999.00, 25, '商务旗舰，i7处理器，32GB内存', 'ThinkPad laptop'),
    (2, '机械键盘 K Pro', 399.00, 200, '热插拔轴体，RGB背光', 'mechanical keyboard'),
    (2, '罗技 MX Master 3S', 699.00, 150, '人体工学设计，静音按键', 'Logitech mouse'),
    (2, '戴尔 27寸显示器', 2499.00, 40, '4K分辨率，Type-C一线连接', 'Dell monitor'),
    # 3-家用电器
    (3, '戴森 V15 吸尘器', 4999.00, 35, '激光探测灰尘，强劲吸力', 'Dyson vacuum cleaner'),
    (3, '格力空调 1.5匹', 3299.00, 40, '新一级能效，变频冷暖', 'air conditioner'),
    (3, '小米空气净化器', 1299.00, 60, 'CADR值500，OLED触控屏', 'air purifier'),
    (3, '九阳破壁机', 599.00, 90, '一机多用，静音破壁', 'blender kitchen'),
    (3, '飞利浦电动牙刷', 399.00, 150, '声波震动，智能计时', 'electric toothbrush'),
    # 4-美妆护肤
    (4, '兰蔻小黑瓶精华', 1080.00, 80, '肌底修护，保湿抗老', 'skincare product'),
    (4, '雅诗兰黛DW粉底液', 460.00, 120, '油皮亲妈，持妆遮瑕', 'foundation makeup'),
    (4, 'SK-II 神仙水', 1590.00, 50, 'PITERA精华，水油平衡', 'skincare essence'),
    (4, '迪奥口红 999', 380.00, 200, '正红色，丝绒质地', 'lipstick'),
    (4, '科颜氏高保湿面霜', 350.00, 100, '极地保湿，锁水修护', 'moisturizer cream'),
    # 5-食品饮料
    (5, '三只松鼠坚果大礼包', 168.00, 300, '每日坚果，8袋装', 'nuts snack pack'),
    (5, '星巴克胶囊咖啡', 139.00, 200, '意式浓缩，12粒装', 'coffee capsules'),
    (5, '良品铺子零食箱', 99.00, 400, '混合零食，30包', 'snack food'),
    (5, '德芙巧克力礼盒', 128.00, 250, '丝滑牛奶巧克力，520g', 'chocolate gift'),
    (5, '元气森林苏打水', 59.90, 500, '0糖0脂0卡，整箱12瓶', 'soda drink'),
]

FALLBACK_URL = 'https://placehold.co/400x400/EEE/31343C?text='


def download_image(product_name, search_keyword, save_dir, page):
    """Download product image from Pexels"""
    headers = {'Authorization': PEXELS_KEY}

    try:
        resp = requests.get(
            BASE_URL,
            headers=headers,
            params={'query': search_keyword, 'per_page': 1, 'page': page},
            timeout=15
        )
        data = resp.json()

        if data.get('photos') and len(data['photos']) > 0:
            img_url = data['photos'][0]['src']['medium']
            img_resp = requests.get(img_url, timeout=15)

            filename = f'{uuid.uuid4().hex}.jpg'
            filepath = save_dir / filename
            filepath.write_bytes(img_resp.content)

            print(f'  [OK] {product_name} -> {filename}')
            return f'/uploads/product/{filename}'
        else:
            print(f'  [--] {product_name} -> no photo found, using placeholder')
            return download_placeholder(product_name, save_dir)

    except Exception as e:
        print(f'  [!!] {product_name} -> download failed: {str(e)[:30]}, using placeholder')
        return download_placeholder(product_name, save_dir)


def download_placeholder(product_name, save_dir):
    """Fallback: download text placeholder image"""
    filename = f'{uuid.uuid4().hex}.jpg'
    filepath = save_dir / filename
    try:
        img_resp = requests.get(FALLBACK_URL + product_name, timeout=10)
        filepath.write_bytes(img_resp.content)
    except:
        pass
    return f'/uploads/product/{filename}'


def main():
    save_dir = Path('server/uploads/product')
    save_dir.mkdir(parents=True, exist_ok=True)

    print('=' * 60)
    print('  Product Import Tool - Downloading images...')
    print('=' * 60)
    print()

    values = []
    for i, (cat_id, name, price, stock, desc, keyword) in enumerate(PRODUCTS, 1):
        print(f'[{i}/{len(PRODUCTS)}] ', end='')
        img_path = download_image(name, keyword, save_dir, page=i)

        safe_name = name.replace("'", "\\'")
        safe_desc = desc.replace("'", "\\'")
        values.append(
            f"({cat_id}, '{safe_name}', {price}, {stock}, "
            f"'{img_path}', '{safe_desc}', 0)"
        )

    print()
    print('=' * 60)
    print('  SQL generated! Copy below to Navicat/DBeaver:')
    print('=' * 60)
    print()

    sql = (
        "DELETE FROM t_product;\n"
        "INSERT INTO `t_product` (`category_id`, `name`, `price`, `stock`, `main_image`, `description`, `sales_count`) VALUES\n" +
        ',\n'.join(values) + ';'
    )
    print(sql)

    print()
    print('=' * 60)
    print(f'  [DONE] Imported {len(PRODUCTS)} products')
    print(f'  [FILE] Images saved in: {save_dir}')
    print(f'  [SQL] Execute the SQL above in your database client')
    print('=' * 60)


if __name__ == '__main__':
    main()
