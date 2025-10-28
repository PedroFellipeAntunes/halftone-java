(() => {
  // ---------- DOM References ----------
  const leftCanvas = document.getElementById('leftCanvas');
  const rightCanvas = document.getElementById('rightCanvas');
  const leftCtx = leftCanvas.getContext('2d');
  const rightCtx = rightCanvas.getContext('2d');

  const angleInput = document.getElementById('angle');
  const angleVal = document.getElementById('angleVal');
  const kernelInput = document.getElementById('kernel');
  const kVal = document.getElementById('kVal');
  const rectWInput = document.getElementById('rectW');
  const rectHInput = document.getElementById('rectH');
  const stats = document.getElementById('stats');
  const hoverInfo = document.getElementById('hoverInfo');

  // ---------- Visual Config Variables ----------
  const COLORS = {
    canvasBg: 'black', // Background color of canvases
    rectFill: 'rgba(30,144,255,0.95)', // Blue filled rectangle
    rectBorder: 'rgba(10,60,140,0.95)', // Expanded rectangle border
    rotatedBorder: 'white', // Rotated corners border
    centerDot: 'white', // Center dot color
    mouseStart: 'rgba(0,255,0,0.95)', // Original mouse point
    mouseEnd: 'rgba(255,0,0,0.95)', // Rotated mouse point
    arrow: 'blue', // Arrow color from green->red
    rightOccupied: 'rgba(32,255,64,0.95)', // Right canvas occupied cell
    rightEmpty: 'rgba(255,32,64,0.90)', // Right canvas empty cell
    rightHover: 'rgba(33,150,243,0.95)', // Hovered cell
    rightBorder: 'rgba(0,255,0,0.9)', // Border around rotated grid
    hoverStroke: 'white' // Stroke for hover cell
  };

  const LINE_WIDTHS = {
    rectBorder: 5,
    rotatedBorder: 2,
    centerDot: 2,
    arrow: 3,
    rightCell: 0.25,
    rightHover: 2
  };

  const DOT = {
    leftMultiplier: 2,
    leftMinPx: 2,
    centerMultiplier: 1.5,
    rightInsetFraction: 0
  };

  const ARROW = {
    headFactor: 5
  };

  const PADDING = 12;

  // ---------- Utility Functions ----------
  const toRad = d => d * Math.PI / 180;
  function rotatePoint(x, y, cx, cy, theta) {
    const dx = x - cx, dy = y - cy;
    const c = Math.cos(theta), s = Math.sin(theta);
    return { x: cx + (dx * c - dy * s), y: cy + (dx * s + dy * c) };
  }

  // ---------- State Variables ----------
  let occupancy = [], counts = [];
  let gridMeta = null, leftMeta = null;
  let hoverCell = null, mouseImage = null, rotatedMouse = null;

  // ---------- Canvas Resize ----------
  function resizeCanvases() {
    const wrap = document.querySelector('.canvas-wrap');
    const w = wrap.clientWidth, h = wrap.clientHeight;
    const gap = 16;

    const boxWidth = Math.floor((w - gap) / 2);
    const boxHeight = Math.floor(h - 24);

    leftCanvas.width = boxWidth;
    leftCanvas.height = boxHeight;
    leftCanvas.style.width = boxWidth + 'px';
    leftCanvas.style.height = boxHeight + 'px';

    rightCanvas.width = boxWidth;
    rightCanvas.height = boxHeight;
    rightCanvas.style.width = boxWidth + 'px';
    rightCanvas.style.height = boxHeight + 'px';

    draw();
  }

  // ---------- Main Draw Function ----------
  function draw() {
    // Read inputs
    const angleDeg = Number(angleInput.value);
    const theta = toRad(angleDeg);
    angleVal.textContent = angleDeg;

    const kernel = Math.max(1, Math.floor(Number(kernelInput.value) || 15));
    kVal.textContent = kernel;

    const rectW = Math.max(1, Math.floor(Number(rectWInput.value) || 200));
    const rectH = Math.max(1, Math.floor(Number(rectHInput.value) || 200));

    const border = kernel;
    const imgW = rectW + 2 * border;
    const imgH = rectH + 2 * border;
    const centerX = border + rectW / 2;
    const centerY = border + rectH / 2;

    // Compute rotated corners
    const cornersExpanded = [
      { x: 0, y: 0 },
      { x: imgW, y: 0 },
      { x: imgW, y: imgH },
      { x: 0, y: imgH }
    ];
    const rotatedCorners = cornersExpanded.map(p => rotatePoint(p.x, p.y, centerX, centerY, theta));
    const xrVals = rotatedCorners.map(p => p.x), yrVals = rotatedCorners.map(p => p.y);

    const minXr = Math.min(...xrVals), maxXr = Math.max(...xrVals);
    const minYr = Math.min(...yrVals), maxYr = Math.max(...yrVals);

    const Wb = maxXr - minXr, Hb = maxYr - minYr;
    const numCols = Math.max(1, Math.ceil(Wb / kernel));
    const numRows = Math.max(1, Math.ceil(Hb / kernel));

    occupancy = Array.from({length: numRows}, () => Array(numCols).fill(false));
    counts = Array.from({length: numRows}, () => Array(numCols).fill(0));

    for (let y = 0; y < imgH; y++) {
      for (let x = 0; x < imgW; x++) {
        const rp = rotatePoint(x, y, centerX, centerY, theta);
        const col = Math.floor((rp.x - minXr) / kernel);
        const row = Math.floor((rp.y - minYr) / kernel);
        if (row >= 0 && row < numRows && col >= 0 && col < numCols) {
          occupancy[row][col] = true;
          counts[row][col]++;
        }
      }
    }

    gridMeta = { kernel, rectW, rectH, border, imgW, imgH, centerX, centerY, minXr, minYr, Wb, Hb, numCols, numRows, theta, rotatedCorners };

    // ---------- LEFT CANVAS ----------
    leftCtx.clearRect(0, 0, leftCanvas.width, leftCanvas.height);
    leftCtx.fillStyle = COLORS.canvasBg;
    leftCtx.fillRect(0, 0, leftCanvas.width, leftCanvas.height);

    const scaleL = Math.max(0.0001, Math.min((leftCanvas.width - 2*PADDING)/imgW, (leftCanvas.height - 2*PADDING)/imgH));
    const leftOffsetX = PADDING + (leftCanvas.width - 2*PADDING - imgW*scaleL)/2;
    const leftOffsetY = PADDING + (leftCanvas.height - 2*PADDING - imgH*scaleL)/2;
    leftMeta = { scaleL, leftOffsetX, leftOffsetY, imgW, imgH, border, centerX, centerY, theta };

    leftCtx.fillStyle = COLORS.rectFill;
    leftCtx.fillRect(Math.round(leftOffsetX + border*scaleL),
                     Math.round(leftOffsetY + border*scaleL),
                     Math.max(1, Math.round(rectW*scaleL)),
                     Math.max(1, Math.round(rectH*scaleL)));

    leftCtx.strokeStyle = COLORS.rectBorder;
    leftCtx.lineWidth = LINE_WIDTHS.rectBorder;
    leftCtx.strokeRect(Math.round(leftOffsetX)+0.5, Math.round(leftOffsetY)+0.5,
                       Math.max(1, Math.round(imgW*scaleL))-1,
                       Math.max(1, Math.round(imgH*scaleL))-1);

    leftCtx.strokeStyle = COLORS.rotatedBorder;
    leftCtx.lineWidth = LINE_WIDTHS.rotatedBorder;
    leftCtx.beginPath();
    gridMeta.rotatedCorners.forEach((p, idx) => {
      const cx = p.x*scaleL + leftOffsetX, cy = p.y*scaleL + leftOffsetY;
      idx===0 ? leftCtx.moveTo(cx, cy) : leftCtx.lineTo(cx, cy);
    });
    leftCtx.closePath();
    leftCtx.stroke();

    leftCtx.fillStyle = COLORS.centerDot;
    leftCtx.beginPath();
    leftCtx.arc(centerX*scaleL + leftOffsetX, centerY*scaleL + leftOffsetY,
                Math.max(DOT.leftMinPx, scaleL*DOT.centerMultiplier),
                0, Math.PI*2);
    leftCtx.fill();

    // ---------- Mouse Drawing Left ----------
    if(mouseImage && leftMeta){
      const gx = mouseImage.ex*scaleL + leftOffsetX;
      const gy = mouseImage.ey*scaleL + leftOffsetY;
      leftCtx.fillStyle = COLORS.mouseStart;
      leftCtx.beginPath();
      leftCtx.arc(gx, gy, Math.max(DOT.leftMinPx, Math.ceil(scaleL*DOT.leftMultiplier)), 0, Math.PI*2);
      leftCtx.fill();

      if(rotatedMouse){
        const rx = rotatedMouse.x*scaleL + leftOffsetX;
        const ry = rotatedMouse.y*scaleL + leftOffsetY;
        leftCtx.fillStyle = COLORS.mouseEnd;
        leftCtx.beginPath();
        leftCtx.arc(rx, ry, Math.max(DOT.leftMinPx, Math.ceil(scaleL*DOT.leftMultiplier)), 0, Math.PI*2);
        leftCtx.fill();

        leftCtx.strokeStyle = COLORS.arrow;
        leftCtx.lineWidth = LINE_WIDTHS.arrow;
        leftCtx.beginPath();
        leftCtx.moveTo(gx, gy);
        leftCtx.lineTo(rx, ry);
        leftCtx.stroke();

        const angle = Math.atan2(ry - gy, rx - gx);
        const headLen = LINE_WIDTHS.arrow * ARROW.headFactor;
        leftCtx.beginPath();
        leftCtx.moveTo(rx, ry);
        leftCtx.lineTo(rx - headLen * Math.cos(angle - Math.PI/6), ry - headLen * Math.sin(angle - Math.PI/6));
        leftCtx.lineTo(rx - headLen * Math.cos(angle + Math.PI/6), ry - headLen * Math.sin(angle + Math.PI/6));
        leftCtx.lineTo(rx, ry);
        leftCtx.fillStyle = COLORS.arrow;
        leftCtx.fill();
      }
    }

    // ---------- RIGHT CANVAS ----------
    rightCtx.clearRect(0,0,rightCanvas.width,rightCanvas.height);
    rightCtx.fillStyle = COLORS.canvasBg;
    rightCtx.fillRect(0,0,rightCanvas.width,rightCanvas.height);

    const scaleR = Math.max(0.0001, Math.min((rightCanvas.width-2*PADDING)/Wb, (rightCanvas.height-2*PADDING)/Hb));
    const rightOffsetX = PADDING + (rightCanvas.width-2*PADDING-Wb*scaleR)/2 - minXr*scaleR;
    const rightOffsetY = PADDING + (rightCanvas.height-2*PADDING-Hb*scaleR)/2 - minYr*scaleR;

    for(let r=0;r<numRows;r++){
      for(let c=0;c<numCols;c++){
        const x0 = minXr + c*kernel, y0 = minYr + r*kernel;
        const sx = x0*scaleR + rightOffsetX, sy = y0*scaleR + rightOffsetY;
        const sw = kernel*scaleR, sh = kernel*scaleR;
        const inset = Math.max(0, Math.floor(Math.min(sw,sh)*DOT.rightInsetFraction));
        rightCtx.fillStyle = occupancy[r][c] ? COLORS.rightOccupied : COLORS.rightEmpty;
        rightCtx.fillRect(Math.round(sx)+inset, Math.round(sy)+inset, Math.max(1,Math.ceil(sw)-2*inset), Math.max(1,Math.ceil(sh)-2*inset));
        rightCtx.strokeStyle = COLORS.hoverStroke;
        rightCtx.lineWidth = LINE_WIDTHS.rightCell;
        rightCtx.strokeRect(Math.round(sx)+0.5, Math.round(sy)+0.5, Math.ceil(sw)-1, Math.ceil(sh)-1);
      }
    }

    if(hoverCell){
      const r = hoverCell.r, c = hoverCell.c;
      if(r>=0 && r<numRows && c>=0 && c<numCols){
        const x0 = minXr + c*kernel, y0 = minYr + r*kernel;
        const sx = x0*scaleR + rightOffsetX, sy = y0*scaleR + rightOffsetY;
        const sw = kernel*scaleR, sh = kernel*scaleR;
        rightCtx.fillStyle = COLORS.rightHover;
        rightCtx.fillRect(Math.round(sx), Math.round(sy), Math.ceil(sw), Math.ceil(sh));
        rightCtx.strokeStyle = COLORS.hoverStroke;
        rightCtx.lineWidth = LINE_WIDTHS.rightHover;
        rightCtx.strokeRect(Math.round(sx)+0.5, Math.round(sy)+0.5, Math.ceil(sw)-1, Math.ceil(sh)-1);
      }
    }

    rightCtx.setLineDash([6,4]);
    rightCtx.strokeStyle = COLORS.rightBorder;
    rightCtx.lineWidth = LINE_WIDTHS.rotatedBorder;
    rightCtx.strokeRect(Math.round(minXr*scaleR + rightOffsetX), Math.round(minYr*scaleR + rightOffsetY),
                        Math.round(Wb*scaleR), Math.round(Hb*scaleR));
    rightCtx.setLineDash([]);

    // ---------- Stats ----------
    let occupiedCount=0,totalSamples=0;
    for(let r=0;r<numRows;r++) for(let c=0;c<numCols;c++){
      if(occupancy[r][c]) occupiedCount++;
      totalSamples+=counts[r][c];
    }
    stats.innerHTML = `Original: ${rectW}×${rectH} px | Border: ${border} | Rotated box: ${Wb.toFixed(2)}×${Hb.toFixed(2)} px
      &nbsp; | &nbsp; Grid: ${numRows}×${numCols} = ${numRows*numCols} &nbsp; | &nbsp; Occupied: ${occupiedCount} &nbsp; | &nbsp; Samples: ${totalSamples}`;
  }

  // ---------- Mouse Mapping ----------
  function leftCanvasClientToImage(clientX, clientY){
    if(!leftMeta) return null;
    const rect = leftCanvas.getBoundingClientRect();
    const cssX = clientX - rect.left, cssY = clientY - rect.top;
    const scaleX = leftCanvas.width / rect.width, scaleY = leftCanvas.height / rect.height;
    const cx = cssX*scaleX, cy = cssY*scaleY;
    const ex = (cx - leftMeta.leftOffsetX)/leftMeta.scaleL;
    const ey = (cy - leftMeta.leftOffsetY)/leftMeta.scaleL;
    const ix = ex, iy = ey;
    return {ix,iy,ex,ey};
  }

  // ---------- Events ----------
  leftCanvas.addEventListener('mousemove', evt=>{
    if(!gridMeta || !leftMeta) return;
    const pos = leftCanvasClientToImage(evt.clientX,evt.clientY);
    if(!pos) return;
    const ex = pos.ex, ey = pos.ey;
    if(ex>=0 && ex<gridMeta.imgW && ey>=0 && ey<gridMeta.imgH){
      mouseImage = {ix:pos.ix,iy:pos.iy,ex,ey};
      const rp = rotatePoint(ex,ey,gridMeta.centerX,gridMeta.centerY,gridMeta.theta);
      rotatedMouse = {x: rp.x, y: rp.y};
      hoverCell = { r: Math.floor((rp.y-gridMeta.minYr)/gridMeta.kernel),
                    c: Math.floor((rp.x-gridMeta.minXr)/gridMeta.kernel) };
      hoverInfo.textContent = `XY: ${pos.ix.toFixed(2)}, ${pos.iy.toFixed(2)} | Kernel: row=${hoverCell.r}, col=${hoverCell.c}`;
    }else{
      mouseImage=null; rotatedMouse=null; hoverCell=null;
      hoverInfo.textContent='XY: — | Kernel: —';
    }
    draw();
  });

  leftCanvas.addEventListener('mouseleave', ()=>{
    mouseImage=null; rotatedMouse=null; hoverCell=null;
    hoverInfo.textContent='XY: — | Kernel: —';
    draw();
  });

  angleInput.addEventListener('input', draw);
  kernelInput.addEventListener('input', draw);
  rectWInput.addEventListener('input', draw);
  rectHInput.addEventListener('input', draw);
  window.addEventListener('resize', resizeCanvases);

  resizeCanvases();
})();