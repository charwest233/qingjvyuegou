-- =========================================
-- 商品描述增强脚本
-- 为每个商品添加更详细的品类关键词描述，
-- 以便 RAG 模糊搜索能通过 description 字段
-- 匹配到相关商品（例如搜索"美妆"可匹配口红）
-- =========================================

-- ===========================
-- 手机数码 (category_id=1)
-- ===========================
UPDATE t_product SET description = '【手机数码】A18 Pro芯片，钛金属机身，4800万像素三摄，全新拍照旗舰手机，支持5G网络，高端智能手机数码产品' WHERE name = 'iPhone 16 Pro Max';
UPDATE t_product SET description = '【手机数码】麒麟芯片，卫星通信，鸿蒙OS，超强影像旗舰手机，国产高端智能手机代表' WHERE name = '华为 Mate 70 Pro';
UPDATE t_product SET description = '【手机数码】骁龙8至尊版，徕卡四摄，专业影像旗舰手机，高性能智能手机数码产品' WHERE name = '小米 15 Ultra';
UPDATE t_product SET description = '【手机数码】降噪长续航，Hi-Res认证，无线蓝牙耳机，适用手机平板听歌通话，数码配件' WHERE name = '漫步者蓝牙耳机';
UPDATE t_product SET description = '【手机数码】血氧检测，心率监测，运动追踪，智能手表，苹果穿戴数码设备' WHERE name = 'Apple Watch S10';
UPDATE t_product SET description = '【手机数码】骁龙8至尊版，蔡司三摄，6000mAh大电池，旗舰拍照智能手机，数码影音娱乐' WHERE name = 'vivo X200 Ultra';
UPDATE t_product SET description = '【手机数码】折叠屏旗舰，轻薄机身，哈苏影像，全新形态智能手机，高端数码产品' WHERE name = 'OPPO Find N5';
UPDATE t_product SET description = '【手机数码】骁龙8Gen3，2K直屏，120W快充，高性能性价比手机，电竞游戏数码装备' WHERE name = '红米 K80 Pro';
UPDATE t_product SET description = '【手机数码】13英寸OLED屏，M4芯片，轻薄设计，iPad平板电脑，移动办公学习数码设备' WHERE name = 'iPad Pro M4';
UPDATE t_product SET description = '【手机数码】旗舰降噪耳机，30小时续航，头戴式无线蓝牙耳机，HiFi音质数码影音' WHERE name = '索尼WH-1000XM6';
UPDATE t_product SET description = '【手机数码】双主摄，全向避障，46分钟续航，航拍无人机，智能飞行数码设备' WHERE name = '大疆Air 3S';
UPDATE t_product SET description = '【手机数码】新一代掌机，向后兼容，任天堂游戏主机，家庭娱乐数码产品' WHERE name = '任天堂Switch 2';

-- ===========================
-- 电脑办公 (category_id=2)
-- ===========================
UPDATE t_product SET description = '【电脑办公】M4芯片，16GB内存，轻薄长续航，苹果笔记本电脑，适合办公学习移动办公' WHERE name = 'MacBook Air M4';
UPDATE t_product SET description = '【电脑办公】商务旗舰，i7处理器，32GB内存，ThinkPad商务笔记本，适合办公商务人士' WHERE name = '联想 ThinkPad X1';
UPDATE t_product SET description = '【电脑办公】热插拔轴体，RGB背光，机械键盘，适合办公打字游戏电竞外设' WHERE name = '机械键盘 K Pro';
UPDATE t_product SET description = '【电脑办公】人体工学设计，静音按键，无线鼠标，办公外设电脑配件' WHERE name = '罗技 MX Master 3S';
UPDATE t_product SET description = '【电脑办公】4K分辨率，Type-C一线连接，27寸显示器，台式机电脑屏幕办公设备' WHERE name = '戴尔 27寸显示器';
UPDATE t_product SET description = '【电脑办公】13.2英寸柔光屏，天生会画，华为平板电脑，适合办公学习绘画创作' WHERE name = '华为 MatePad Pro';
UPDATE t_product SET description = '【电脑办公】2TB，USB3.0，便携存储，移动硬盘，电脑外设数据存储办公配件' WHERE name = '西部数据移动硬盘';
UPDATE t_product SET description = '【电脑办公】20000mAh，三口输出，快充，充电宝移动电源，手机电脑数码配件' WHERE name = '小米 67W充电宝';
UPDATE t_product SET description = '【电脑办公】无线彩色喷墨，墨仓式打印，佳能打印机，适合办公家用打印设备' WHERE name = '佳能G3830打印机';
UPDATE t_product SET description = '【电脑办公】32英寸4K，144Hz，HDR600，华硕ROG电竞显示器，适合游戏办公电脑屏幕' WHERE name = '华硕ROG 4K显示器';
UPDATE t_product SET description = '【电脑办公】2TB，1050MB/s，IP65防水，三星移动固态硬盘，高速便携存储电脑外设' WHERE name = '三星T7移动固态';
UPDATE t_product SET description = '【电脑办公】骁龙X Elite，二合一平板，微软Surface笔记本，适合移动办公商务人士' WHERE name = '微软Surface Pro 11';

-- ===========================
-- 家用电器 (category_id=3)
-- ===========================
UPDATE t_product SET description = '【家用电器】激光探测灰尘，强劲吸力，戴森无线吸尘器，家庭清洁家电除螨除尘' WHERE name = '戴森 V15 吸尘器';
UPDATE t_product SET description = '【家用电器】新一级能效，变频冷暖，格力壁挂式空调，家用制冷制热电器' WHERE name = '格力空调 1.5匹';
UPDATE t_product SET description = '【家用电器】CADR值500，OLED触控屏，小米空气净化器，家用净化和除甲醛电器' WHERE name = '小米空气净化器';
UPDATE t_product SET description = '【家用电器】一机多用，静音破壁，九阳破壁机豆浆机，厨房家用电器料理烹饪' WHERE name = '九阳破壁机';
UPDATE t_product SET description = '【家用电器】声波震动，智能计时，飞利浦电动牙刷，家用个护清洁电器口腔护理' WHERE name = '飞利浦电动牙刷';
UPDATE t_product SET description = '【家用电器】变频微波，智能解冻，平板加热，美的微波炉，厨房家用电器加热烹饪' WHERE name = '美的微波炉';
UPDATE t_product SET description = '【家用电器】扫拖一体，AI避障，自动集尘，科沃斯扫地机器人，家庭清洁智能家电' WHERE name = '科沃斯扫地机器人';
UPDATE t_product SET description = '【家用电器】1.5L，煮茶煮粥，保温预约，小熊养生壶电热水壶，厨房家用电器' WHERE name = '小熊养生壶';
UPDATE t_product SET description = '【家用电器】4L容量，球形内胆，柴火饭，苏泊尔电饭煲，厨房家用电器煮饭烹饪' WHERE name = '苏泊尔球釜电饭煲';
UPDATE t_product SET description = '【家用电器】520升，风冷无霜，干湿分储，海尔双开门冰箱，家用冷藏冷冻保鲜电器' WHERE name = '海尔双开门冰箱';
UPDATE t_product SET description = '【家用电器】纳米水离子，6档风温，护发，松下吹风机，家用美发护发个护电器' WHERE name = '松下人鱼姬吹风机';
UPDATE t_product SET description = '【家用电器】直流变频，12档风速，遥控定时，美的落地扇循环扇，家用夏季纳凉电器' WHERE name = '美的循环扇落地扇';

-- ===========================
-- 美妆护肤 (category_id=4)
-- ===========================
UPDATE t_product SET description = '【美妆护肤】肌底修护，保湿抗老，兰蔻精华液，美妆护肤精华，适合日常护肤保养抗衰老' WHERE name = '兰蔻小黑瓶精华';
UPDATE t_product SET description = '【美妆护肤】油皮亲妈，持妆遮瑕，雅诗兰黛粉底液，美妆彩妆底妆，打造无瑕妆容' WHERE name = '雅诗兰黛DW粉底液';
UPDATE t_product SET description = '【美妆护肤】PITERA精华，水油平衡，SK-II护肤精华水，高端美妆护肤产品，细腻肌肤' WHERE name = 'SK-II 神仙水';
UPDATE t_product SET description = '【美妆护肤】正红色，丝绒质地，迪奥口红唇膏，美妆彩妆唇部产品，打造精致唇妆约会妆容' WHERE name = '迪奥口红 999';
UPDATE t_product SET description = '【美妆护肤】极地保湿，锁水修护，科颜氏面霜，美妆护肤保湿面霜，适合干性肌肤' WHERE name = '科颜氏高保湿面霜';
UPDATE t_product SET description = '【美妆护肤】视黄醇抗皱，紧致修护，欧莱雅精华液，美妆护肤抗衰老产品，日常护肤保养' WHERE name = '欧莱雅复颜精华';
UPDATE t_product SET description = '【美妆护肤】维稳修护，强韧肌底，资生堂红腰子精华，美妆护肤精华液，敏感肌修护' WHERE name = '资生堂红腰子';
UPDATE t_product SET description = '【美妆护肤】经典色号，哑光质地，MAC子弹头口红唇膏，美妆彩妆唇部产品，持久显色唇妆' WHERE name = 'MAC 子弹头口红';
UPDATE t_product SET description = '【美妆护肤】奇迹修复，深层滋养，海蓝之谜面霜，高端美妆护肤保湿霜，修护受损肌肤' WHERE name = '海蓝之谜经典面霜';
UPDATE t_product SET description = '【美妆护肤】遮瑕保湿，自然光泽，SPF50+，雪花秀气垫粉底，美妆彩妆底妆产品，韩妆清透' WHERE name = '雪花秀气垫粉底';
UPDATE t_product SET description = '【美妆护肤】双管设计，抗氧焕亮，修护肌肤，娇韵诗精华液，美妆护肤抗衰老抗氧化产品' WHERE name = '娇韵诗双萃精华';
UPDATE t_product SET description = '【美妆护肤】深层清洁，控油去黑头，科颜氏白泥面膜，美妆护肤清洁面膜，毛孔护理产品' WHERE name = '科颜氏白泥面膜';

-- ===========================
-- 食品饮料 (category_id=5)
-- ===========================
UPDATE t_product SET description = '【食品饮料】每日坚果，8袋装，三只松鼠坚果混合礼包，休闲零食健康食品年货送礼' WHERE name = '三只松鼠坚果大礼包';
UPDATE t_product SET description = '【食品饮料】意式浓缩，12粒装，星巴克胶囊咖啡，咖啡饮品提神醒脑办公饮品' WHERE name = '星巴克胶囊咖啡';
UPDATE t_product SET description = '【食品饮料】混合零食，30包，良品铺子零食大礼包，休闲食品办公室零食追剧零食' WHERE name = '良品铺子零食箱';
UPDATE t_product SET description = '【食品饮料】丝滑牛奶巧克力，520g，德芙巧克力礼盒，休闲食品糖果甜点送礼佳品' WHERE name = '德芙巧克力礼盒';
UPDATE t_product SET description = '【食品饮料】0糖0脂0卡，整箱12瓶，元气森林苏打水，健康饮品无糖饮料解渴' WHERE name = '元气森林苏打水';
UPDATE t_product SET description = '【食品饮料】100%鲜榨，冷链运输，10瓶装，农夫山泉NFC果汁，健康饮品纯天然果汁' WHERE name = '农夫山泉NFC果汁';
UPDATE t_product SET description = '【食品饮料】靖江特产，蜜汁口味，500g，百草味猪肉脯，休闲零食肉类零食小吃' WHERE name = '百草味猪肉脯';
UPDATE t_product SET description = '【食品饮料】生牛乳发酵，0添加，12盒装，蒙牛纯甄酸奶，健康饮品乳制品早餐食品' WHERE name = '蒙牛纯甄酸奶';
UPDATE t_product SET description = '【食品饮料】3.6g乳蛋白，整箱12盒，特仑苏纯牛奶，健康饮品乳制品营养早餐食品' WHERE name = '特仑苏纯牛奶';
UPDATE t_product SET description = '【食品饮料】经典双夹心，混合装800g，奥利奥饼干，休闲零食饼干糕点办公室零食' WHERE name = '奥利奥饼干大礼包';
UPDATE t_product SET description = '【食品饮料】巧克力派+薯片+好多鱼，综合装，好丽友零食大礼包，休闲派对食品零食' WHERE name = '好丽友派对大礼包';
UPDATE t_product SET description = '【食品饮料】仙贝+雪饼+小小酥，年货综合装，旺旺零食大礼包，休闲食品过年送礼年货' WHERE name = '旺旺大礼包';

-- ===========================
-- 原始测试数据（6个）
-- ===========================
UPDATE t_product SET description = '【手机数码】全面屏旗舰机型，高性能智能手机，适合游戏影音办公，数码科技产品' WHERE name = '智能手机 Pro Max';
UPDATE t_product SET description = '【手机数码】降噪长续航，无线蓝牙耳机，适用手机平板听歌通话，数码配件影音' WHERE name = '无线蓝牙耳机';
UPDATE t_product SET description = '【电脑办公】轻薄办公性能本，笔记本电脑，适合商务办公学习编程，移动办公设备' WHERE name = '轻薄笔记本电脑';
UPDATE t_product SET description = '【家用电器】静音三档风量，空气循环扇，夏季家用纳凉电风扇，宿舍办公降温电器' WHERE name = '空气循环扇';
UPDATE t_product SET description = '【美妆护肤】氨基酸温和洁面，保湿洁面乳，美妆护肤洁面产品，日常清洁洗脸护肤' WHERE name = '保湿洁面乳';
UPDATE t_product SET description = '【食品饮料】每日坚果补充能量，混合坚果礼盒，休闲零食健康食品，年货送礼办公零食' WHERE name = '混合坚果礼盒';
