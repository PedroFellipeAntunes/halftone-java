# ==========================================================
# @title Stippling density for each kernel size compared to the true image average
# ==========================================================
import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
from mpl_toolkits.mplot3d import Axes3D
import os

# Configuration
LOG_PATH = "log.txt"
OUT_DIR = "stippling_plots"
os.makedirs(OUT_DIR, exist_ok=True)

# Step 1: Load and prepare data
# The log file uses semicolons (;) as separators and commas (,) as decimal points.
df = pd.read_csv(LOG_PATH, sep=';', decimal=',', engine='python')
df['kernelSize'] = df['kernelSize'].astype(int)
df['density'] = df['density'].astype(int)
df['delta'] = df['delta'].astype(float)

print("Data loaded:", df.shape)
print(df.head())

# Step 2: Group and pivot data
# Creates a pivot table where rows = density, columns = kernelSize, values = delta
pivot = df.pivot(index='density', columns='kernelSize', values='delta')
unique_kernels = sorted(df['kernelSize'].unique())

# Step 3: 2D line plots per kernel size (show only min Δ point, scalable for many kernels)
import matplotlib.cm as cm

# Determine grid layout dynamically
cols = 10  # number of columns in the grid
rows = int(np.ceil(len(unique_kernels) / cols))
fig_width = 20  # base width
fig_height = rows * 2.5  # scale height by number of rows
fig2d, axes = plt.subplots(rows, cols, figsize=(fig_width, fig_height), sharex=True, sharey=True, constrained_layout=True)
axes = axes.flatten()

for idx, kernel in enumerate(unique_kernels):
    ax = axes[idx]
    data = df[df['kernelSize'] == kernel].copy()
    data = data.sort_values("density")

    # Draw the line connecting all delta values (in black)
    ax.plot(data['density'], data['delta'], color='black', linewidth=0.8, alpha=0.6)

    # Highlight only the minimum delta point in red
    min_row = data.loc[data['delta'].idxmin()]
    min_density = min_row['density']
    min_delta = min_row['delta']

    ax.scatter(
        min_density,
        min_delta,
        color='red',
        s=50,
        edgecolor='black',
        linewidth=0.5,
        zorder=3,
        label=f"min Δ = {min_delta:.4f}"
    )

    ax.set_title(f"Kernel = {kernel}", fontsize=8)
    ax.set_xlabel("Density", fontsize=7)
    ax.set_ylabel("Δ", fontsize=7)
    ax.tick_params(axis='both', which='major', labelsize=6)
    ax.grid(True)

# Hide any unused axes
for j in range(idx + 1, len(axes)):
    axes[j].set_visible(False)

fig2d.suptitle("Δ vs Stippling Density per Kernel Size (Minimum Δ Highlighted)", fontsize=12)
fig2d.savefig(os.path.join(OUT_DIR, "per_kernel_2D.png"), dpi=200)
plt.show()

# Step 4: 3D surface visualization
X, Y = np.meshgrid(pivot.columns, pivot.index)
Z = pivot.values

# Fill NaN values by interpolation
def fill_nan_grid(z):
    zf = z.copy().astype(float)
    for col in range(zf.shape[1]):
        colv = zf[:, col]
        nans = np.isnan(colv)
        if np.all(nans):
            continue
        if np.any(nans):
            xs = np.arange(len(colv))
            zf[nans, col] = np.interp(xs[nans], xs[~nans], colv[~nans])
    zf[np.isnan(zf)] = np.nanmean(zf)
    return zf

Z = fill_nan_grid(Z)

azim, elev = 45, 30
fig3d = plt.figure(figsize=(12, 8))
ax3d = fig3d.add_subplot(111, projection='3d')

surf = ax3d.plot_surface(X, Y, Z, cmap='viridis', edgecolor='none', alpha=0.95)
ax3d.set_xlabel("Kernel Size")
ax3d.set_ylabel("Stippling Density")
ax3d.set_zlabel("Δ (Delta)")
ax3d.set_title(f"3D Surface - Δ vs Kernel Size vs Density (azim={azim}, elev={elev})")
ax3d.view_init(elev=elev, azim=azim)

fig3d.colorbar(surf, ax=ax3d, shrink=0.6, aspect=10, label='Δ (Delta)')
fig3d.tight_layout()
out_path = os.path.join(OUT_DIR, f"surface_az{azim}_el{elev}.png")
fig3d.savefig(out_path, dpi=200)
plt.show()

print(f"3D surface saved: {out_path}")

# Step 5: Identify best stippling density per kernel
best_rows = (
    df.loc[df.groupby("kernelSize")["delta"].idxmin()]
    .sort_values("kernelSize")
    .reset_index(drop=True)
)

# Calculate mean and median of best densities
mean_density = best_rows['density'].mean()
median_density = best_rows['density'].median()

# Append summary rows
summary = pd.DataFrame({
    "kernelSize": ["MEAN", "MEDIAN"],
    "density": [mean_density, median_density],
    "delta": [np.nan, np.nan]
})

best_with_summary = pd.concat([best_rows, summary], ignore_index=True)

print("\nBest (lowest Δ) values per kernel size:")
print(best_with_summary)

# Save summary CSV
best_csv = os.path.join(OUT_DIR, "best_deltas.csv")
best_with_summary.to_csv(best_csv, sep=';', index=False)
print(f"\nSummary saved: {best_csv}")