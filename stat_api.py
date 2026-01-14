# -*- coding: utf-8 -*-
import re

with open('openapi_new.yaml', 'r', encoding='utf-8') as f:
    lines = f.readlines()

interfaces = []
i = 0
while i < len(lines):
    line = lines[i]
    # 匹配路径定义
    path_match = re.match(r'^  (/(?:user|tea|shop|order|forum|message|upload)[^\s:]+):', line)
    if path_match:
        path = path_match.group(1)
        path_line = i + 1
        
        # 查找该路径下的所有HTTP方法
        methods = []
        j = i + 1
        while j < len(lines):
            method_line = lines[j].strip()
            if method_line in ['get:', 'post:', 'put:', 'delete:', 'patch:']:
                method = method_line.replace(':', '').upper()
                method_line_num = j + 1
                
                # 查找summary
                summary = ""
                k = j + 1
                while k < len(lines) and k < j + 10:
                    if lines[k].strip().startswith('summary:'):
                        summary = lines[k].split('summary:')[1].strip().strip('"').strip("'")
                        break
                    k += 1
                
                methods.append((method_line_num, method, summary))
                j += 1
            elif re.match(r'^  /', lines[j]) and not lines[j].strip().startswith('#'):
                # 遇到下一个路径，停止
                break
            else:
                j += 1
        
        if methods:
            interfaces.append((path_line, path, methods))
        i = j if j > i else i + 1
    else:
        i += 1

# 按模块分类
modules = {
    '用户模块': [],
    '茶叶模块': [],
    '店铺模块': [],
    '订单模块': [],
    '论坛模块': [],
    '消息模块': [],
    '公共模块': []
}

for path_line, path, methods in interfaces:
    if path.startswith('/user'):
        module = '用户模块'
    elif path.startswith('/tea'):
        module = '茶叶模块'
    elif path.startswith('/shop'):
        module = '店铺模块'
    elif path.startswith('/order'):
        module = '订单模块'
    elif path.startswith('/forum'):
        module = '论坛模块'
    elif path.startswith('/message'):
        module = '消息模块'
    elif path.startswith('/upload'):
        module = '公共模块'
    else:
        continue
    
    for method_line, method, summary in methods:
        modules[module].append((path_line, method, path, summary))

# 生成Markdown文档
output = """# openapi_new.yaml 接口统计文档

本文档统计了 `openapi_new.yaml` 中的所有接口，按模块分类，包含行号、HTTP方法、接口路径和简单描述。

## 统计概览

"""

total = sum(len(interfaces) for interfaces in modules.values())
output += f"- **总接口数**: {total}个\n"
for module_name, interfaces in modules.items():
    output += f"- **{module_name}**: {len(interfaces)}个\n"

output += "\n---\n\n"

module_names = ['用户模块', '茶叶模块', '店铺模块', '订单模块', '论坛模块', '消息模块', '公共模块']
module_numbers = ['一', '二', '三', '四', '五', '六', '七']

for idx, module_name in enumerate(module_names):
    interfaces = modules[module_name]
    if not interfaces:
        continue
    
    output += f"## {module_numbers[idx]}、{module_name}（{len(interfaces)}个接口）\n\n"
    output += "| 序号 | 行号 | HTTP方法 | 接口路径 | 接口描述 |\n"
    output += "|------|------|----------|----------|----------|\n"
    
    for seq, (path_line, method, path, summary) in enumerate(interfaces, 1):
        summary = summary if summary else "（无描述）"
        output += f"| {seq} | {path_line} | {method} | {path} | {summary} |\n"
    
    output += "\n---\n\n"

output += """## 统计说明

1. **行号**：指接口路径定义所在的行号（即路径开始的行）
2. **HTTP方法**：接口使用的HTTP方法（GET、POST、PUT、DELETE）
3. **接口路径**：完整的API路径，包含路径参数
4. **接口描述**：来自OpenAPI文档中的 `summary` 字段

## 备注

- 部分接口路径包含路径参数（如 `{id}`、`{userId}` 等），在实际调用时需要替换为具体值
- 部分接口需要认证（Bearer Token），在请求头中需要添加 `Authorization: Bearer <token>`
- 所有接口统一返回HTTP 200状态码，通过响应体中的 `code` 字段判断业务结果

"""

with open('API接口统计文档_new.yaml.md', 'w', encoding='utf-8') as f:
    f.write(output)

print(f"统计完成！共 {total} 个接口")
for module_name, interfaces in modules.items():
    print(f"{module_name}: {len(interfaces)}个")

