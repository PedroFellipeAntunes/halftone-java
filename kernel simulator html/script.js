// Immediately-invoked function to keep scope local
(() => {
  // DOM references
  const canvas = document.getElementById('c');
  const ctx = canvas.getContext('2d');

  const angleInput = document.getElementById('angle');
  const angleVal = document.getElementById('angleVal');
  const kernelInput = document.getElementById('kernel');
  const kVal = document.getElementById('kVal');
  const expanded = document.getElementById('expanded');
  const drawFullGridInput = document.getElementById('drawFullGrid');
  const rectWInput = document.getElementById('rectW');
  const rectHInput = document.getElementById('rectH');
  const stats = document.getElementById('stats');

  // Canvas aspect ratio used to scale drawing region
  const ASPECT_RATIO = 820 / 520;

  // Convert degrees to radians
  function degToRad(d){ return d * Math.PI / 180; }

  // Rotate a point (x,y) around center (cx,cy) by theta radians
  function rotatePoint(x, y, cx, cy, theta) {
    const dx = x - cx;
    const dy = y - cy;
    const cos = Math.cos(theta), sin = Math.sin(theta);
    const rx = dx * cos - dy * sin;
    const ry = dx * sin + dy * cos;
    return { x: cx + rx, y: cy + ry };
  }

  // Inverse rotate (rotate by negative angle)
  function inverseRotatePoint(x, y, cx, cy, theta) {
    return rotatePoint(x, y, cx, cy, -theta);
  }

  // Resize canvas to fit the canvas-wrap while preserving aspect ratio,
  // and trigger a redraw at the new resolution.
  function resizeCanvas() {
    const wrap = document.querySelector('.canvas-wrap');
    const maxWidth = wrap.clientWidth;
    const maxHeight = wrap.clientHeight;

    let newWidth = maxWidth;
    let newHeight = newWidth / ASPECT_RATIO;

    if (newHeight > maxHeight) {
      newHeight = maxHeight;
      newWidth = newHeight * ASPECT_RATIO;
    }

    canvas.width = Math.floor(newWidth);
    canvas.height = Math.floor(newHeight);

    canvas.style.width = `${newWidth}px`;
    canvas.style.height = `${newHeight}px`;

    draw();
  }

  // Draw the rotated grid, sampled cells and stats
  function draw() {
    const angleDeg = Number(angleInput.value);
    const theta = degToRad(angleDeg);
    angleVal.textContent = angleDeg;

    const kernel = Number(kernelInput.value);
    kVal.textContent = kernel;

    const rectW = Math.max(1, Math.floor(Number(rectWInput.value) || 0));
    const rectH = Math.max(1, Math.floor(Number(rectHInput.value) || 0));
    const border = expanded.checked ? kernel : 0;
    const drawFullGrid = drawFullGridInput.checked;

    const imgW = rectW + 2 * border;
    const imgH = rectH + 2 * border;

    const canvasW = canvas.width;
    const canvasH = canvas.height;

    // Center the drawing inside the canvas
    const originX = Math.max(0, Math.round((canvasW - imgW) / 2));
    const originY = Math.max(0, Math.round((canvasH - imgH) / 2));

    const centerX = imgW / 2;
    const centerY = imgH / 2;

    ctx.clearRect(0, 0, canvasW, canvasH);

    ctx.save();
    ctx.translate(originX, originY);

    // Outline the expanded rectangle (with border)
    ctx.strokeStyle = '#fff';
    ctx.lineWidth = 1;
    ctx.strokeRect(0, 0, imgW, imgH);

    // Original blue rectangle inside border
    ctx.fillStyle = '#0000ff80';
    ctx.fillRect(border, border, rectW, rectH);

    // Compute rotated bounding box of the expanded rectangle
    const corners = [
      {x: 0, y: 0},
      {x: imgW, y: 0},
      {x: 0, y: imgH},
      {x: imgW, y: imgH}
    ];

    const rotatedCorners = corners.map(p => rotatePoint(p.x, p.y, centerX, centerY, theta));
    const xrVals = rotatedCorners.map(p => p.x);
    const yrVals = rotatedCorners.map(p => p.y);
    const minXr = Math.min(...xrVals);
    const maxXr = Math.max(...xrVals);
    const minYr = Math.min(...yrVals);
    const maxYr = Math.max(...yrVals);

    const Wb = maxXr - minXr;
    const Hb = maxYr - minYr;

    const numCols = Math.max(1, Math.ceil(Wb / kernel));
    const numRows = Math.max(1, Math.ceil(Hb / kernel));

    // Allocate occupancy and counts arrays
    const occupancy = new Array(numRows);
    const counts = new Array(numRows);
    for (let r = 0; r < numRows; r++) {
      occupancy[r] = new Array(numCols).fill(false);
      counts[r] = new Array(numCols).fill(0);
    }

    // Sample each pixel of the original (expanded) rect and mark occupancy
    let sampled = 0;
    for (let yy = border; yy < border + rectH; yy++) {
      for (let xx = border; xx < border + rectW; xx++) {
        const rp = rotatePoint(xx, yy, centerX, centerY, theta);
        const xr = rp.x, yr = rp.y;
        const col = Math.floor((xr - minXr) / kernel);
        const row = Math.floor((yr - minYr) / kernel);
        if (row >= 0 && row < numRows && col >= 0 && col < numCols) {
          occupancy[row][col] = true;
          counts[row][col] += 1;
        }
        sampled++;
      }
    }

    // Draw grid cells: optionally full grid (red) and occupied cells (white)
    ctx.lineWidth = 1;
    for (let r = 0; r < numRows; r++) {
      for (let c = 0; c < numCols; c++) {
        const x0 = minXr + c * kernel;
        const x1 = minXr + (c + 1) * kernel;
        const y0 = minYr + r * kernel;
        const y1 = minYr + (r + 1) * kernel;

        const rc0 = inverseRotatePoint(x0, y0, centerX, centerY, theta);
        const rc1 = inverseRotatePoint(x1, y0, centerX, centerY, theta);
        const rc2 = inverseRotatePoint(x1, y1, centerX, centerY, theta);
        const rc3 = inverseRotatePoint(x0, y1, centerX, centerY, theta);

        if (drawFullGrid) {
          ctx.beginPath();
          ctx.moveTo(rc0.x, rc0.y);
          ctx.lineTo(rc1.x, rc1.y);
          ctx.lineTo(rc2.x, rc2.y);
          ctx.lineTo(rc3.x, rc3.y);
          ctx.closePath();
          ctx.strokeStyle = 'rgba(255,0,0,1)';
          ctx.lineWidth = 1;
          ctx.stroke();
        }

        if (occupancy[r][c]) {
          ctx.beginPath();
          ctx.moveTo(rc0.x, rc0.y);
          ctx.lineTo(rc1.x, rc1.y);
          ctx.lineTo(rc2.x, rc2.y);
          ctx.lineTo(rc3.x, rc3.y);
          ctx.closePath();
          ctx.lineWidth = 2;
          ctx.strokeStyle = 'rgba(255,255,255,1)';
          ctx.stroke();
        }
      }
    }

    // Draw rotated bounding box (dashed green)
    const bb0 = inverseRotatePoint(minXr, minYr, centerX, centerY, theta);
    const bb1 = inverseRotatePoint(maxXr, minYr, centerX, centerY, theta);
    const bb2 = inverseRotatePoint(maxXr, maxYr, centerX, centerY, theta);
    const bb3 = inverseRotatePoint(minXr, maxYr, centerX, centerY, theta);

    ctx.beginPath();
    ctx.moveTo(bb0.x, bb0.y);
    ctx.lineTo(bb1.x, bb1.y);
    ctx.lineTo(bb2.x, bb2.y);
    ctx.lineTo(bb3.x, bb3.y);
    ctx.closePath();
    ctx.lineWidth = 2;
    ctx.setLineDash([6,4]);
    ctx.strokeStyle = '#00ff00';
    ctx.stroke();
    ctx.setLineDash([]);

    // Draw center point marker
    ctx.fillStyle = '#fff';
    ctx.beginPath();
    ctx.arc(centerX, centerY, 3, 0, Math.PI*2);
    ctx.fill();

    ctx.restore();

    // Compute stats and display them
    const totalCells = numCols * numRows;
    let occupiedCount = 0;
    let totalSamples = 0;
    for (let r=0;r<numRows;r++){
      for (let c=0;c<numCols;c++){
        if (occupancy[r][c]) occupiedCount++;
        totalSamples += counts[r][c];
      }
    }

    stats.innerHTML = `Expanded image: ${imgW}×${imgH} px (border=${border}) &nbsp; | &nbsp; Rotated bounding box: ${Wb.toFixed(2)}×${Hb.toFixed(2)} px<br>
      Grid: ${numRows} rows × ${numCols} cols = ${totalCells} cells &nbsp; | &nbsp; Occupied cells: ${occupiedCount} &nbsp; | &nbsp; Sampled points: ${totalSamples}`;
  }

  // Wire up UI events to redraw when controls change
  angleInput.addEventListener('input', draw);
  kernelInput.addEventListener('input', draw);
  expanded.addEventListener('change', draw);
  drawFullGridInput.addEventListener('change', draw);
  rectWInput.addEventListener('input', draw);
  rectHInput.addEventListener('input', draw);

  // Recompute canvas size on window resize
  window.addEventListener('resize', resizeCanvas);

  // Initialize canvas size and draw for the first time
  resizeCanvas();
})();