"""抓取页面字节，判断哪些中文在服务端就已损坏。"""
import urllib.request

url = "http://localhost:8080/moshi/home"
with urllib.request.urlopen(url, timeout=10) as r:
    raw = r.read()
    print("status:", r.status, "content-type:", r.headers.get("Content-Type"))
    print("bytes:", len(raw))

for kw in ["魔识", "开始答题", "看排行榜", "答题中心", "首页", "登录", "注册", "互动答题"]:
    u8 = kw.encode("utf-8") in raw
    # 双重编码：UTF-8 -> latin-1 -> UTF-8
    dbl = kw.encode("utf-8").decode("latin-1", errors="ignore").encode("utf-8") in raw
    print(f"  {kw!r}: 正常UTF8={u8} 双重编码={dbl}")

idx = raw.find(b'class="btn primary lg"')
if idx > 0:
    print("按钮附近原始字节:", raw[idx:idx + 120])
