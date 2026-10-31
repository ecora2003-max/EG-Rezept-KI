#!/usr/bin/env python3
"""Restore independently-tested Kotlin sources from SHA-256-verified staging data.

No network or repository mutation occurs outside this checkout. Requires
the free Python 'brotli' package (pip install brotli) for this ONE-TIME export.
The resulting Kotlin sources have no third-party runtime dependencies.
"""
from __future__ import annotations

import base64
import hashlib
import io
import sys
import tarfile
from pathlib import Path

EXPECTED = "a5c53a77dfdf74d0cfc6497db5f415cf2abb0a32e7a3a0363ca2dc9f59581838"
MEMBERS = {
    "src/main/kotlin/de/eg/recipe/RecipeEngine.kt",
    "src/test/kotlin/de/eg/recipe/RecipeEngineTest.kt",
}
ROOT = Path(__file__).resolve().parents[1]


def main() -> None:
    try:
        import brotli
    except ImportError as exc:
        raise SystemExit("Install free brotli once: python -m pip install brotli") from exc
    data = b"".join((ROOT / ("transfer/bundle.part%d.b64" % i)).read_bytes() for i in (1, 2))
    packed = base64.b64decode(data, validate=True)
    checksum = hashlib.sha256(packed).hexdigest()
    if checksum != EXPECTED:
        raise SystemExit("Archive checksum mismatch: refuse to extract")
    with tarfile.open(fileobj=io.BytesIO(brotli.decompress(packed)), mode="r:") as archive:
        found = archive.getmembers()
        if {member.name for member in found} != MEMBERS or not all(member.isfile() for member in found):
            raise SystemExit("Archive contains unexpected paths: refuse to extract")
        for member in found:
            out = ROOT / member.name
            out.parent.mkdir(parents=True, exist_ok=True)
            out.write_bytes(archive.extractfile(member).read())
            print("restored:", member.name)


if __name__ == "__main__":
    main()
