"""
import_products_2.py — 追加导入15个商品
Usage: pip install requests && python import_products_2.py
"""

import requests
import uuid
from pathlib import Path

PEXELS_KEY = 'u0YdoyFtSGoIxno3AAbFqFEH2gMpYKwKjTVYPONDsBp8poEMY0m7TzUf'
BASE_URL = 'https://api.pexels.com/v1/search'
IMG_PREFIX = 'http://localhost:8080'
FALLBACK_URL = 'https://placehold.co/400x400/EEE/31343C?text='

PRODUCTS = [
    # 1-手机数码 (3)
    (1, 'vivo X200 Ultra', 5999.00, 60, '骁龙8至尊版，蔡司三摄，6000mAh大电池', 'vivo smartphone'),
    (1, 'OPPO Find N5', 8999.00, 30, '折叠屏旗舰，轻薄机身，哈苏影像', 'foldable phone'),
    (1, '红米 K80 Pro', 3599.00, 150, '骁龙8Gen3，2K直屏，120W快充', 'Xiaomi smartphone'),
    # 2-电脑办公 (3)
    (2, '华为 MatePad Pro', 4299.00, 40, '13.2英寸柔光屏，天生会画', 'Huawei tablet'),
    (2, '西部数据移动硬盘', 499.00, 200, '2TB，USB3.0，便携存储', 'external hard drive'),
    (2, '小米 67W充电宝', 199.00, 300, '20000mAh，三口输出，快充', 'power bank'),
    # 3-家用电器 (3)
    (3, '美的微波炉', 899.00, 80, '变频微波，智能解冻，平板加热', 'microwave oven'),
    (3, '科沃斯扫地机器人', 2999.00, 45, '扫拖一体，AI避障，自动集尘', 'robot vacuum'),
    (3, '小熊养生壶', 159.00, 150, '1.5L，煮茶煮粥，保温预约', 'electric kettle'),
    # 4-美妆护肤 (3)
    (4, '欧莱雅复颜精华', 289.00, 120, '视黄醇抗皱，紧致修护', 'skincare serum'),
    (4, '资生堂红腰子', 760.00, 70, '维稳修护，强韧肌底', 'skincare essence'),
    (4, 'MAC 子弹头口红', 240.00, 180, '经典色号，哑光质地', 'lipstick'),
    # 5-食品饮料 (3)
    (5, '农夫山泉NFC果汁', 89.90, 400, '100%鲜榨，冷链运输，10瓶装', 'orange juice'),
    (5, '百草味猪肉脯', 49.90, 350, '靖江特产，蜜汁口味，500g', 'dried meat snack'),
    (5, '蒙牛纯甄酸奶', 69.90, 500, '生牛乳发酵，0添加，12盒装', 'yogurt'),
]


def download_image(product_name, search_keyword, save_dir, page):
    headers = {'Authorization': PEXELS_KEY}
    try:
        resp = requests.get(
            BASE_URL, headers=headers,
            params={'query': search_keyword, 'per_page': 1, 'page': page + 25},
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
            return f'{IMG_PREFIX}/uploads/product/{filename}'
        else:
            print(f'  [--] {product_name} -> no photo, using placeholder')
            return download_placeholder(product_name, save_dir)
    except Exception as e:
        print(f'  [!!] {product_name} -> {str(e)[:30]}, using placeholder')
        return download_placeholder(product_name, save_dir)


def download_placeholder(product_name, save_dir):
    filename = f'{uuid.uuid4().hex}.jpg'
    filepath = save_dir / filename
    try:
        r = requests.get(FALLBACK_URL + product_name, timeout=10)
        filepath.write_bytes(r.content)
    except:
        pass
    return f'{IMG_PREFIX}/uploads/product/{filename}'


def main():
    save_dir = Path('server/uploads/product')
    save_dir.mkdir(parents=True, exist_ok=True)

    print('=' * 60)
    print('  Adding 15 more products...')
    print('=' * 60)
    print()

    values = []
    for i, (cat_id, name, price, stock, desc, keyword) in enumerate(PRODUCTS, 1):
        print(f'[{i}/15] ', end='')
        img_path = download_image(name, keyword, save_dir, page=i)
        safe_name = name.replace("'", "\\'")
        safe_desc = desc.replace("'", "\\'")
        values.append(
            f"({cat_id}, '{safe_name}', {price}, {stock}, "
            f"'{img_path}', '{safe_desc}', 0)"
        )

    print()
    sql = "INSERT INTO `t_product` (`category_id`, `name`, `price`, `stock`, `main_image`, `description`, `sales_count`) VALUES\n" + \
          ',\n'.join(values) + ';'
    print(sql)

    print()
    print('=' * 60)
    print(f'  [DONE] Added 15 products')
    print(f'  [FILE] Images in: {save_dir}')
    print('=' * 60)


if __name__ == '__main__':
    main()
