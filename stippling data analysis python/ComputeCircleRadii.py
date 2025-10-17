# ==========================================================
# Batch compute maximal equal radii from cciN.txt files
# Output TXT is saved in the same folder as this script (.py)
# ==========================================================
import os
import math
import time
from datetime import datetime
import numpy as np

# ---------------------- Configuration ----------------------
START_N = 1                         # first N to process (inclusive)
END_N = 100                         # last N to process (inclusive)
FILE_DIR = "cci_coords"             # directory containing cci{N}.txt files (input)
FILE_PATTERN = "cci{N}.txt"         # filename pattern for inputs
CONTAINER_RADIUS = 1.0              # original container radius for which points were given
OUTPUT_TXT = "computed_radii.txt"   # output text file (single column, no header)
# -----------------------------------------------------------

def now_str():
    return datetime.now().strftime("%H:%M:%S")

def print_step(msg):
    print(f"{now_str()} - {msg}")

# Determine script directory (where the .py file is). Fallback to cwd.
if "__file__" in globals():
    SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
else:
    SCRIPT_DIR = os.getcwd()
print_step(f"Script directory (outputs will be saved here): {SCRIPT_DIR}")

# Input directory (can be relative to script dir or absolute)
INPUT_DIR = os.path.join(SCRIPT_DIR, FILE_DIR) if not os.path.isabs(FILE_DIR) else FILE_DIR
print_step(f"Input directory (where cci files are read from): {os.path.abspath(INPUT_DIR)}")

TXT_FULLPATH = os.path.join(SCRIPT_DIR, OUTPUT_TXT)

# ---------------------- helper functions -------------------------
def build_filename(n):
    return os.path.join(INPUT_DIR, FILE_PATTERN.format(N=n))

def read_cci_file(path):
    """Read a cci file with lines: index x y -> return list of (idx,x,y)."""
    pts = []
    if not os.path.exists(path):
        return pts
    with open(path, "r", encoding="utf-8") as f:
        for lineno, raw in enumerate(f, start=1):
            line = raw.strip()
            if not line:
                continue
            parts = line.split()
            if len(parts) < 3:
                print_step(f"DEBUG - skipping malformed line {lineno} in {path}: {line!r}")
                continue
            try:
                idx = int(parts[0])
                x = float(parts[1])
                y = float(parts[2])
            except ValueError:
                print_step(f"DEBUG - cannot parse line {lineno} in {path}: {line!r}")
                continue
            pts.append((idx, x, y))
    return pts

def compute_max_common_radius(points, container_radius=CONTAINER_RADIUS):
    """Compute r* for fixed centers. Return np.nan if no points."""
    if not points:
        return np.nan
    coords = np.array([[p[1], p[2]] for p in points], dtype=float)
    d_origin = np.linalg.norm(coords, axis=1)
    r_edge_allow = container_radius - d_origin
    min_edge_r = float(np.min(r_edge_allow))
    min_pair_r = float("inf")
    n = len(coords)
    for i in range(n):
        xi, yi = coords[i]
        for j in range(i+1, n):
            xj, yj = coords[j]
            d = math.hypot(xi - xj, yi - yj)
            r_allowed = d / 2.0
            if r_allowed < min_pair_r:
                min_pair_r = r_allowed
    r_star = min(min_edge_r, min_pair_r)
    return r_star

# ---------------------- main loop -------------------------
print_step("Starting batch computation")
start_time = time.time()

radii = []
missing = 0
total = END_N - START_N + 1

if not os.path.isdir(INPUT_DIR):
    print_step(f"WARNING: input directory does not exist: {INPUT_DIR}")

for i, N in enumerate(range(START_N, END_N + 1), start=1):
    fname = build_filename(N)
    print_step(f"[{i}/{total}] Reading {fname}")
    pts = read_cci_file(fname)
    if not pts:
        print_step(f"  WARNING: missing or invalid file for N={N} -> recording empty entry")
        radii.append(np.nan)
        missing += 1
        continue
    r_star = compute_max_common_radius(pts, CONTAINER_RADIUS)
    radii.append(r_star)
    print_step(f"  Computed r* = {r_star:.12f} for N={N}")

elapsed = time.time() - start_time
print_step(f"Finished processing in {elapsed:.2f}s. Missing: {missing}/{total}")

# ---------------------- save TXT in script folder -------------------------
print_step(f"Saving TXT (one value per line, no header) to: {TXT_FULLPATH}")
with open(TXT_FULLPATH, "w", encoding="utf-8") as f:
    for r in radii:
        if np.isnan(r):
            f.write("\n")
        else:
            f.write(f"{r:.12f}\n")

print_step("TXT saved successfully.")
print_step(f"Done. Output file: {TXT_FULLPATH}")
