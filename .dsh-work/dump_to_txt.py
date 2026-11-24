"""把 docx 结构导出成 UTF-8 文本文件，供阅读工具直接查看。"""
import sys
from docx import Document


def dump(path: str, out: str) -> None:
    doc = Document(path)
    lines = []
    lines.append("=" * 78)
    lines.append("FILE: " + path)
    lines.append(f"段落={len(doc.paragraphs)} 表格={len(doc.tables)} 图片={len(doc.inline_shapes)}")
    lines.append("=" * 78)
    for i, p in enumerate(doc.paragraphs):
        text = p.text.strip()
        if not text:
            continue
        style = p.style.name if p.style is not None else "?"
        lines.append(f"[{i:03d}][{style}] {text}")
    for ti, table in enumerate(doc.tables):
        lines.append(f"--- 表{ti}: {len(table.rows)}x{len(table.columns)} ---")
        for ri, row in enumerate(table.rows):
            cells = [c.text.strip().replace("\n", " / ") for c in row.cells]
            lines.append("  R%d: %s" % (ri, " | ".join(cells)))
    for i, s in enumerate(doc.inline_shapes):
        lines.append(f"  图片{i}: {s.width}x{s.height}")
    with open(out, "w", encoding="utf-8") as f:
        f.write("\n".join(lines))
    print("written:", out, len(lines), "lines")


if __name__ == "__main__":
    dump(sys.argv[1], sys.argv[2])
