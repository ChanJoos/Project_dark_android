#!/usr/bin/env python3
"""Verify retained reference bytes; never certify runtime or original fidelity."""
import hashlib
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "master/source/rucesion_coast_20261014"

def verify():
    manifest = json.loads((SOURCE / "image_sources.json").read_text())
    change = json.loads((ROOT / "master/changes/RUCESION-COAST-V155-SOURCE-ADMISSION.json").read_text())
    errors = []
    for row in manifest["images"]:
        path = SOURCE / row.get("path", "MISSING")
        if not path.is_file() or hashlib.sha256(path.read_bytes()).hexdigest() != row.get("sha256"):
            errors.append(f"Source checksum mismatch: {row['index']}")
        if row.get("runtimeUse") != "NOT_ADMITTED":
            errors.append(f"Unreviewed runtime admission: {row['index']}")
    if change["runtimeChanges"] or change["sourceBaselineModified"]:
        errors.append("Reference-only revision cannot change production or baseline")
    if change["wholeMapTopologyStatus"] != "PENDING" or change["originalAnimationStatus"] != "PENDING":
        errors.append("Static screenshots cannot close original topology/animation")
    report = {"status": "FAIL" if errors else "REFERENCE_BYTES_VERIFIED", "sourceImages": len(manifest["images"]), "runtimeVerified": False, "originalFidelityVerified": False, "errors": errors}
    print(json.dumps(report, ensure_ascii=False))
    return bool(errors)

if __name__ == "__main__":
    raise SystemExit(verify())
