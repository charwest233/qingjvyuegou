"""
import_products_3.py — 为5个分类各追加4个商品（共20个）
Usage: pip install requests && python import_products_3.py
"""

import requests
import uuid
from pathlib import Path

PEXELS_KEY = 'u0YdoyFtSGoIxno3AAbFqFEH2gMpYKwKjTVYPONDsBp8poEMY0m7TzUf'
BASE_URL = 'https://api.pexels.com/v1/search'
IMG_PREFIX = 'http://localhost:8080'
FALLBACK_URL = 'https://placehold.co/400x400/EEE/31343C?text='

PRODUCTS = [
    # 1-手机数码 (+4)
    (1, 'iPad Pro M4', 8499.00, 40, '13英寸OLED屏，M4芯片，轻薄设计', 'iPad Pro'),
    (1, '索尼WH-1000XM6', 2999.00, 80, '旗舰降噪耳机，30小时续航', 'Sony headphones'),
    (1, '大疆Air 3S', 6988.00, 25, '双主摄，全向避障，46分钟续航', 'DJI drone'),
    (1, '任天堂Switch 2', 3499.00, 60, '新一代掌机，向后兼容', 'Nintendo Switch'),
    # 2-电脑办公 (+4)
    (2, '佳能G3830打印机', 899.00, 50, '无线彩色喷墨，墨仓式打印', 'Canon printer'),
    (2, '华硕ROG 4K显示器', 3999.00, 30, '32英寸4K，144Hz，HDR600', 'ASUS monitor'),
    (2, '三星T7移动固态', 699.00, 150, '2TB，1050MB/s，IP65防水', 'Samsung SSD'),
    (2, '微软Surface Pro 11', 9999.00, 20, '骁龙X Elite，二合一平板', 'Surface Pro'),
    # 3-家用电器 (+4)
    (3, '苏泊尔球釜电饭煲', 399.00, 100, '4L容量，球形内胆，柴火饭', 'rice cooker'),
    (3, '海尔双开门冰箱', 4599.00, 25, '520升，风冷无霜，干湿分储', 'refrigerator'),
    (3, '松下人鱼姬吹风机', 1999.00, 60, '纳米水离子，6档风温，护发', 'hair dryer'),
    (3, '美的循环扇落地扇', 299.00, 120, '直流变频，12档风速，遥控定时', 'electric fan'),
    # 4-美妆护肤 (+4)
    (4, '海蓝之谜经典面霜', 2980.00, 30, '奇迹修复，深层滋养，60ml', 'La Mer cream'),
    (4, '雪花秀气垫粉底', 450.00, 100, '遮瑕保湿，自然光泽，SPF50+', 'cushion foundation'),
    (4, '娇韵诗双萃精华', 990.00, 50, '双管设计，抗氧焕亮，修护肌肤', 'Clarins serum'),
    (4, '科颜氏白泥面膜', 330.00, 80, '深层清洁，控油去黑头，125ml', 'Kiehl''s clay mask'),
    # 5-食品饮料 (+4)
    (5, '特仑苏纯牛奶', 68.00, 500, '3.6g乳蛋白，整箱12盒', 'milk'),
    (5, '奥利奥饼干大礼包', 39.90, 400, '经典双夹心，混合装800g', 'Oreo cookies'),
    (5, '好丽友派对大礼包', 45.00, 350, '巧克力派+薯片+好多鱼，综合装', 'snack mix'),
    (5, '旺旺大礼包', 88.00, 300, '仙贝+雪饼+小小酥，年货综合装', '旺旺 snack gift'),
]


def download_image(product_name, search_keyword, save_dir, page):
    headers = {'Authorization': PEXELS_KEY}
    try:
        resp = requests.get(
            BASE_URL, headers=headers,
            params={'query': search_keyword, 'per_page': 1, 'page': page + 50},
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
    print('  Adding 20 more products (4 per category)...')
    print('=' * 60)
    print()

    values = []
    for i, (cat_id, name, price, stock, desc, keyword) in enumerate(PRODUCTS, 1):
        print(f'[{i}/20] ', end='')
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
    print(f'  [DONE] Added 20 products')
    print(f'  [FILE] Images in: {save_dir}')
    print('=' * 60)


if __name__ == '__main__':
    main()
