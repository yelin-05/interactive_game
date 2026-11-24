"""检查 JSP/JSPF 源文件的实际字节编码是否被双重编码。"""
import os

base = os.path.join("javaee", "src", "main", "webapp", "WEB-INF", "views")
targets = [
    os.path.join(base, "home.jsp"),
    os.path.join(base, "inc", "nav.jspf"),
    os.path.join(base, "inc", "foot.jspf"),
]

for path in targets:
    if not os.path.exists(path):
        print("missing:", path)
        continue
    data = open(path, "rb").read()
    print("---", path, len(data), "bytes, BOM:", data[:3] == b"\xef\xbb\xbf")
    for kw in ["开始答题", "首页", "魔识", "答题中心"]:
        u8 = kw.encode("utf-8") in data
        gb = kw.encode("gbk") in data
        # 双重编码特征：UTF-8 字节被当作 Latin-1 再编码
        dbl = kw.encode("utf-8").decode("latin-1").encode("utf-8") in data
        print(f"    {kw}: utf8={u8} gbk={gb} double-encoded={dbl}")
    print("    head:", data[:50])
