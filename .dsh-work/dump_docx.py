"""把 docx 的文本结构（段落样式 + 表格 + 图片数量）打印出来，便于了解报告模板。"""
import sys
from docx import Document


def dump(path: str) -> None:
    doc = Document(path)
    print("=" * 78)
    print("FILE:", path)
    print("=" * 78)
    print(f"段落数={len(doc.paragraphs)}  表格数={len(doc.tables)}  "
          f"内联图片={len(doc.inline_shapes)}")
    print("-" * 78)
    for i, p in enumerate(doc.paragraphs):
        text = p.text.strip()
        if not text:
            continue
        style = p.style.name if p.style is not None else "?"
        print(f"[{i:03d}][{style}] {text}")
    for ti, table in enumerate(doc.tables):
        print(f"--- 表格 {ti}: {len(table.rows)} 行 x {len(table.columns)} 列 ---")
        for ri, row in enumerate(table.rows):
            cells = [c.text.strip().replace("\n", " / ") for c in row.cells]
            print(f"  R{ri}: " + " | ".join(cells))
    if doc.inline_shapes:
        print(f"--- 内联图片 {len(doc.inline_shapes)} 张 ---")
        for i, s in enumerate(doc.inline_shapes):
            print(f"  img{i}: {s.width}x{s.height}")


if __name__ == "__main__":
    for f in sys.argv[1:]:
        dump(f)
