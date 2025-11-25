<script lang="ts">
  /**
   * Generic card stat badge rendered as an SVG.
   * Variants supported:
   *  - action: blue solid ring
   *  - power:  orange solid ring (supports negative numbers)
   *  - range:  green dashed ring
   * Only single-digit values are expected, optionally with a leading minus sign.
   */
  export let type: 'action' | 'power' | 'range' = 'action';
  export let value: number | string = '';
  export let size = 40; // px

  // Colors tuned to be close to the reference image
  const COLORS = {
		action: { ring: '#F59E0B', text: '#0F172A' }, // orange
		power: { ring: '#53A9D6', text: '#0F172A' }, // blue
    range: { ring: '#4CAF50', text: '#0F172A' }  // green
  } as const;

  $: v = typeof value === 'number' ? String(value) : value ?? '';
  // Accessibility label
  $: label = `${type} ${v}`;

  // Geometry
  const vb = 48; // viewBox square
  const cx = 24;
  const cy = 24;
  const rOuter = 22;
  const rInner = 17.5;
  const stroke = 3.5;

  $: color = COLORS[type];

  // Unique IDs for gradients/filters to avoid collisions across multiple components
  const uid = Math.random().toString(36).slice(2, 8);

  // Helpers to build an arc path for the ink sweep effect
  const toRad = (deg: number) => (deg * Math.PI) / 180;
  const polar = (r: number, angleDeg: number) => {
    const a = toRad(angleDeg);
    return {
      x: cx + r * Math.cos(a),
      y: cy + r * Math.sin(a)
    };
  };
  // Sweep angles (in degrees). Start slightly above right, end near bottom-left for a dynamic sweep.
  const sweepStart = -35; // where the thick sweep starts
  const sweepEnd = 245;   // where the thick sweep ends
  const rr = rOuter - stroke / 2; // stroke radius
  $: startPt = polar(rr, sweepStart);
  $: endPt = polar(rr, sweepEnd);
  // large-arc-flag: 1 because > 180deg, sweep-flag: 1 for clockwise
  $: sweepPathD = `M ${startPt.x.toFixed(3)} ${startPt.y.toFixed(3)} A ${rr} ${rr} 0 1 1 ${endPt.x.toFixed(3)} ${endPt.y.toFixed(3)}`;

  // Build a short inward spiral path (about 1/8th of a circle) used for action/power only.
  function buildSpiralPath(opts?: {
    startAngle?: number; // degrees
    spanDeg?: number; // degrees of spiral arc
    rStart?: number; // starting radius
    rEnd?: number; // ending radius (kept within ring so it stays visible)
    steps?: number; // resolution
  }): string {
    // Default placement: upper-right quadrant, clearly visible and within ring thickness
    const startAngle = opts?.startAngle ?? 10; // slightly above the rightmost point
    const spanDeg = opts?.spanDeg ?? 50; // a touch more than 1/8th turn for visibility
    // Keep the spiral entirely inside the ring band (between rr - stroke/2 and rr + stroke/2)
    const ringInner = rr - stroke / 2; // inner edge of the colored ring
    const ringOuter = rr + stroke / 2; // outer edge of the colored ring
    const rStart = opts?.rStart ?? (ringOuter - stroke * 0.25);
    const rEnd = opts?.rEnd ?? (ringInner + stroke * 0.35);
    const steps = Math.max(4, Math.min(64, Math.floor(opts?.steps ?? 20)));

    const pts: { x: number; y: number }[] = [];
    for (let i = 0; i <= steps; i++) {
      const t = i / steps;
      // ease slightly so it tightens a bit toward the end
      const ease = t * (2 - t);
      const a = startAngle + spanDeg * ease;
      const r = rStart + (rEnd - rStart) * ease;
      pts.push(polar(r, a));
    }
    if (pts.length === 0) return '';
    const [p0, ...rest] = pts;
    return `M ${p0.x.toFixed(3)} ${p0.y.toFixed(3)} ` + rest.map(p => `L ${p.x.toFixed(3)} ${p.y.toFixed(3)}`).join(' ');
  }

  $: spiralPathD = buildSpiralPath();
</script>

<svg
  xmlns="http://www.w3.org/2000/svg"
  viewBox={`0 0 ${vb} ${vb}`}
  width={size}
  height={size}
  role="img"
  aria-label={label}
>
  <defs>
    <!-- Slight organic roughen to make strokes feel inked; keep subtle to avoid blurriness -->
    <filter id={`rough-${uid}`} x="-10%" y="-10%" width="120%" height="120%">
      <feTurbulence type="fractalNoise" baseFrequency="0.7" numOctaves="1" seed="3" result="noise" />
      <feDisplacementMap in="SourceGraphic" in2="noise" scale="0.6" xChannelSelector="R" yChannelSelector="G" />
    </filter>

    <!-- Gradient along the sweep direction to emulate ink pickup and fade -->
    <linearGradient id={`grad-${uid}`} gradientUnits="userSpaceOnUse" x1={startPt.x} y1={startPt.y} x2={endPt.x} y2={endPt.y}>
      <stop offset="0%" stop-color={color.ring} stop-opacity="0.85" />
      <stop offset="55%" stop-color={color.ring} stop-opacity="1" />
      <stop offset="100%" stop-color={color.ring} stop-opacity="0.9" />
    </linearGradient>

    <!-- Range dashed gradient (simpler) -->
    <linearGradient id={`grad-range-${uid}`} x1="0" y1="0" x2={vb} y2="0">
      <stop offset="0%" stop-color={COLORS.range.ring} stop-opacity="0.95" />
      <stop offset="100%" stop-color={COLORS.range.ring} stop-opacity="0.85" />
    </linearGradient>

    <!-- Gentle gloss on inner disc -->
    <radialGradient id={`disc-gloss-${uid}`} cx="50%" cy="40%" r="70%">
      <stop offset="0%" stop-color="#FFFFFF" stop-opacity="1" />
      <stop offset="65%" stop-color="#FFFFFF" stop-opacity="0.96" />
      <stop offset="100%" stop-color="#EDEDED" stop-opacity="0.95" />
    </radialGradient>

    <!-- Slight inner shadow on ring (stroke-only circle uses this via soft blur) -->
    <filter id={`inner-shadow-${uid}`} x="-20%" y="-20%" width="140%" height="140%">
      <feGaussianBlur in="SourceAlpha" stdDeviation="0.7" result="blur" />
      <feOffset dy="0.4" dx="0" result="offset" />
      <feComponentTransfer in="offset">
        <feFuncA type="linear" slope="0.45" />
      </feComponentTransfer>
      <feComposite in2="SourceGraphic" operator="out" />
    </filter>

    <!-- Very subtle paper grain overlay -->
    <filter id={`grain-${uid}`} x="-10%" y="-10%" width="120%" height="120%">
      <feTurbulence type="fractalNoise" baseFrequency="0.9" numOctaves="1" seed="7" result="n" />
      <feColorMatrix in="n" type="saturate" values="0" />
      <feComponentTransfer>
        <feFuncA type="linear" slope="0.06" />
      </feComponentTransfer>
    </filter>

    <!-- Clip path to keep grain within badge -->
    <clipPath id={`clip-disc-${uid}`}>
      <circle cx={cx} cy={cy} r={rOuter} />
    </clipPath>
  </defs>

  <!-- Outer circle backing -->
  <circle cx={cx} cy={cy} r={rOuter} fill="white" />

  {#if type === 'range'}
    <!-- Organic dashed ring for range -->
    <circle
      cx={cx}
      cy={cy}
      r={rr}
      fill="none"
      stroke={`url(#grad-range-${uid})`}
      stroke-width={stroke}
      stroke-linecap="round"
      stroke-dasharray="6.8 9.2"
      filter={`url(#rough-${uid})`}
    />
    <!-- Secondary dash layer for depth -->
    <circle
      cx={cx}
      cy={cy}
      r={rr}
      fill="none"
      stroke={COLORS.range.ring}
      stroke-opacity="0.35"
      stroke-width={stroke - 1}
      stroke-linecap="round"
      stroke-dasharray="5.2 10.7"
      stroke-dashoffset="7.95"
      filter={`url(#rough-${uid})`}
    />
  {:else}
    <!-- Soft base ring under the sweep for fullness -->
    <circle
      cx={cx}
      cy={cy}
      r={rr}
      fill="none"
      stroke={color.ring}
      stroke-opacity="0.25"
      stroke-width={stroke}
      filter={`url(#rough-${uid})`}
    />

    <!-- Tapered ink sweep arc -->
    <path
      d={sweepPathD}
      fill="none"
      stroke={`url(#grad-${uid})`}
      stroke-width={stroke + 1.4}
      stroke-linecap="round"
      filter={`url(#rough-${uid})`}
    />
    <!-- Subtle inner edge shading to add depth -->
    <circle
      cx={cx}
      cy={cy}
      r={rr - (stroke * 0.42)}
      fill="none"
      stroke="#000"
      stroke-opacity="0.15"
      stroke-width="0.9"
      filter={`url(#rough-${uid})`}
    />
    <!-- Secondary sweep pass with slight width modulation for detail -->
    <path
      d={sweepPathD}
      fill="none"
      stroke={color.ring}
      stroke-opacity="0.35"
      stroke-width={stroke + 0.6}
      stroke-linecap="round"
      filter={`url(#rough-${uid})`}
    />

    <!-- Short inward spiral start (about 1/8th circle) to add character -->
    <path
      d={spiralPathD}
      fill="none"
      stroke={color.ring}
      stroke-opacity="0.95"
      stroke-width={stroke - 0.2}
      stroke-linecap="round"
      stroke-linejoin="round"
      filter={`url(#rough-${uid})`}
    />

    <!-- Small highlight notch (top-right) to mimic card art accent -->
    <path
      d="M30 8 C33 9, 36 12, 37 15"
      fill="none"
      stroke="#FFFFFF"
      stroke-opacity="0.9"
      stroke-width="2"
      stroke-linecap="round"
    />
    <!-- Tiny secondary highlight near sweep end for realism -->
    <path
      d={`M ${endPt.x - 3} ${endPt.y - 2} Q ${endPt.x - 1.2} ${endPt.y - 0.8}, ${endPt.x} ${endPt.y}`}
      fill="none"
      stroke="#FFFFFF"
      stroke-opacity="0.7"
      stroke-width="1.2"
      stroke-linecap="round"
    />
  {/if}

  <!-- Inner white disc -->
  <circle cx={cx} cy={cy} r={rInner} fill={`url(#disc-gloss-${uid})`} />
  <!-- Inner rim definition line -->
  <circle cx={cx} cy={cy} r={rInner} fill="none" stroke="#000" stroke-opacity="0.08" stroke-width="0.8" />

  <!-- Value text -->
  <text
    x="50%"
    y="50%"
    dominant-baseline="central"
    text-anchor="middle"
    font-family='ui-sans-serif, system-ui, -apple-system, Segoe UI, Roboto, Ubuntu, Cantarell, Noto Sans, Helvetica Neue, Arial, "Apple Color Emoji", "Segoe UI Emoji"'
    font-size="20"
    font-weight="700"
    fill={color.text}
    class="select-text"
    style="user-select: text; -webkit-user-select: text; -ms-user-select: text;"
  >{v}</text>

  <!-- Grain overlay, clipped to badge -->
  <g clip-path={`url(#clip-disc-${uid})`} filter={`url(#grain-${uid})`} pointer-events="none">
    <rect x="0" y="0" width={vb} height={vb} fill="#000" opacity="0.09" />
  </g>
</svg>

<style>
  :global(svg) { display: inline-block; }
</style>
