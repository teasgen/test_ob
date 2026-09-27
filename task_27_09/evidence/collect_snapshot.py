#!/usr/bin/env python3
"""Read-only source inventory, version 1.0. No imports or execution of project code.

Usage: python3 collect_snapshot.py REPOSITORY OUTPUT_DIRECTORY
The output directory must be outside the repository. Selected excerpts are redacted.
"""
import ast
import hashlib
import json
import platform
import re
import sys
from pathlib import Path

VERSION = "1.0"
repo = Path(sys.argv[1]).resolve()
out = Path(sys.argv[2]).resolve()
if not repo.is_dir() or out == repo or repo in out.parents:
    raise SystemExit("Use an existing repository and an external output directory")
out.mkdir(parents=True, exist_ok=True)
excluded = {".git", ".idea", ".gradle", "node_modules", "__pycache__", ".venv", "build"}
files = sorted(p for p in repo.rglob("*") if p.is_file()
               and p.name != ".DS_Store"
               and not (set(p.relative_to(repo).parts) & excluded))
manifest = "".join(f"{hashlib.sha256(p.read_bytes()).hexdigest()}  {p.relative_to(repo).as_posix()}\n" for p in files)
(out / "snapshot.sha256").write_text(manifest, encoding="utf-8")
inventory = {
    "collector_version": VERSION,
    "python_version": platform.python_version(),
    "scope": "source inspection only; no runtime or network checks",
    "source_file_count": len(files),
    "manifest_sha256": hashlib.sha256(manifest.encode()).hexdigest(),
    "git_metadata_present": (repo / ".git").exists(),
    "root_readme_bytes": (repo / "README.md").stat().st_size,
    "test_files": [p.relative_to(repo).as_posix() for p in files if p.name.endswith(("Test.kt", ".test.js")) or p.name == "tests.py" or p.name.startswith("test_")],
    "ci_files": [p.relative_to(repo).as_posix() for p in files if p.name == ".gitlab-ci.yml" or ".github/workflows/" in p.as_posix()],
    "viewsets": [],
}
tree = ast.parse((repo / "backend/api/views.py").read_text())
for node in tree.body:
    if isinstance(node, ast.ClassDef) and node.name.endswith("ViewSet"):
        names = [n.name for n in node.body if isinstance(n, ast.FunctionDef)]
        assignments = [t.id for n in node.body if isinstance(n, ast.Assign) for t in n.targets if isinstance(t, ast.Name)]
        inventory["viewsets"].append({"name": node.name, "line": node.lineno,
                                      "get_queryset": "get_queryset" in names,
                                      "get_permissions": "get_permissions" in names,
                                      "permission_classes": "permission_classes" in assignments})
(out / "inventory.json").write_text(json.dumps(inventory, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
android = "HealthMonitor/app/src/main/"
kotlin = android + "java/com/example/healthmonitor/"
groups = [
    ("E01", "Доступ к API и аутентификация", [("backend/backend/settings.py",111,119), ("backend/api/views.py",167,186), ("backend/api/views.py",254,267), ("backend/api/views.py",311,324), ("backend/api/views.py",374,454), ("backend/api/urls.py",1,32), (kotlin+"data/auth/AuthManager.kt",32,36)]),
    ("E02", "Поля пациента", [("backend/api/serializers.py",119,133), ("backend/api/models.py",261,292)]),
    ("E03", "Секреты и настройки (значения скрыты)", [("backend/backend/settings.py",9,13), ("backend/backend/settings.py",66,75), ("backend/backend/settings.py",138,147), ("docker-compose.yml",1,38)]),
    ("E04", "HTTP и журналирование", [(kotlin+"data/network/NetworkModule.kt",13,24), (android+"AndroidManifest.xml",6,15), (android+"res/xml/network_security_config.xml",1,8), ("backend/api/views.py",339,350), ("frontend/src/LoginPage/LoginPage.js",12,32)]),
    ("E05", "Файлы и LLM", [("backend/api/serializers.py",136,168), ("backend/api/views.py",270,308), ("backend/api/views.py",320,363), ("backend/api/function/conv.py",40,79), ("backend/backend/urls.py",17,24), ("frontend/nginx.conf",1,21)]),
    ("E06", "Сборка и зависимости", [("backend/Dockerfile",1,30), ("backend/docker-cmd.sh",1,20), ("frontend/Dockerfile",1,20), ("backend/api/function/conv.py",7,11)]),
    ("E07", "Имеющиеся тесты", [("frontend/src/App.test.js",1,8), ("HealthMonitor/app/src/test/java/com/example/healthmonitor/ExampleUnitTest.kt",1,17), ("HealthMonitor/app/src/androidTest/java/com/example/healthmonitor/ExampleInstrumentedTest.kt",1,24)]),
    ("E08", "Показатели и прототип", [(kotlin+"viewmodel/MainViewModel.kt",330,345), ("backend/api/validators.py",48,52), ("backend/api/models.py",297,301)])
]
lines = ["# Свидетельства статического просмотра", "", "Дата подготовки: 27.09.2026. Сборщик 1.0. Исходное приложение не запускалось.", "", "Номера строк относятся к предоставленному снимку. Значения секретов и адрес почты скрыты; наличие строк подтверждено чтением файлов. Выдержки не заменяют анализ всего обработчика.", ""]
for ident, title, ranges in groups:
    lines += [f'<a id="{ident.lower()}"></a>', f"## {ident}. {title}", ""]
    for filename, start, end in ranges:
        source = (repo / filename).read_text().splitlines()
        lines += [f"`{filename}:{start}–{min(end,len(source))}`", "", "```text"]
        for number in range(start, min(end, len(source))+1):
            value = source[number-1]
            if re.search(r"SECRET_KEY|EMAIL_HOST_PASSWORD|EMAIL_HOST_USER|DEFAULT_FROM_EMAIL|POSTGRES_PASSWORD|DB_PASSWORD|[\"']PASSWORD[\"']", value):
                value = re.sub(r"([=:]).*", r"\1 [REDACTED]", value, count=1)
            lines.append(f"{number:4}: {value}")
        lines += ["```", ""]
(out / "source-review.md").write_text("\n".join(lines), encoding="utf-8")
print(json.dumps({k:v for k,v in inventory.items() if k != "viewsets"}, ensure_ascii=False, indent=2))
