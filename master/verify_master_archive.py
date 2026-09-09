import hashlib, pathlib, tarfile, sys
root = pathlib.Path(__file__).resolve().parent
archive = root / 'PROJECT_DARK_MASTER_DB.tar.gz'
expected = '4e478fea91ab5a5a8a86f0a0b31c886c3923797713e744b39c9f0a2b1652ee0b'
actual = hashlib.sha256(archive.read_bytes()).hexdigest()
if actual != expected:
    raise SystemExit(f'checksum mismatch: {actual}')
with tarfile.open(archive, 'r:gz') as tf:
    names = tf.getnames()
    csvs = [n for n in names if n.startswith('master/data/') and n.endswith('.csv')]
    if len(csvs) != 92:
        raise SystemExit(f'expected 92 CSV sheets, got {len(csvs)}')
print(f'OK sha256={actual} csv_sheets={len(csvs)} archive_members={len(names)}')
