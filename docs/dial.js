// The Detent dial for the web. This file ports the dial and the dot grid from the launcher.
// All sizes are design units. The phone screen is 412 units wide and 892 units high.
'use strict';

const W = 412, HD = 892, AX = 226, CY0 = 470, GAP = 54;
const G = 12, GO = 8, LX = 380, LENS_GAP = 24, RX = 20, RF = 6, RC = 62, RR = 14;
const CLOCK_TOP = 84, SEARCH_Y = 796;
const READ_Y = 80 + G * Math.ceil(Math.max(0, CLOCK_TOP - 84) / G);
const LENS_TOP = READ_Y + 112, LENS_BOTTOM = SEARCH_Y - 36;
const TAU = Math.PI * 2;
const FONT = '"JetBrains Mono", ui-monospace, monospace';

const mod = (a, n) => ((a % n) + n) % n;
const c01 = v => Math.min(1, Math.max(0, v));
const snap = v => GO + G * Math.round((v - GO) / G);
const inOutCubic = t => t < .5 ? 4 * t * t * t : 1 - Math.pow(-2 * t + 2, 3) / 2;
const approach = (v, t, dt, f) => Math.abs(t - v) < .003 ? t : v + (t - v) * (1 - Math.pow(f, dt / 16));

export const LAYOUTS = [
  { name: 'soft · radial · double', r: 330, m: 'double', tpa: 4, kind: 'radial' },
  { name: 'round · radial · double', r: 200, m: 'double', tpa: 5, kind: 'radial' },
  { name: 'soft · radial · ticks', r: 330, m: 'ticks', tpa: 4, kind: 'radial' },
  { name: 'flat · radial · dots', r: 560, m: 'dots', tpa: 6, kind: 'radial' },
  { name: 'flat · radial · nodes', r: 560, m: 'nodes', tpa: 4, kind: 'radial' },
  { name: 'grid · field', r: 300, m: 'double', tpa: 1, kind: 'field' },
  { name: 'grid · lens', r: 300, m: 'double', tpa: 1, kind: 'lens' },
  { name: 'grid · readout', r: 200, m: 'double', tpa: 1, kind: 'readout' },
];
const axOf = l => ({ radial: AX, field: 236, readout: 224, lens: LX })[l.kind];
const cyOf = l => l.kind === 'radial' ? CY0 : snap(476);

const rgb = c => [(c >> 16) & 255, (c >> 8) & 255, c & 255];
const T = (name, group, light, bg, fg, accent, wall, hand = 0x504945, wallLeft = null) =>
  ({ name, group, light, bg: rgb(bg), fg: rgb(fg), accent: rgb(accent), wall, hand: rgb(hand), wallLeft });
const om = (name, light, bg, fg, accent) => T(name, 'omarchy', light, bg, fg, accent, `walls/${name}.webp`);

export const THEMES = [
  T('dusk', 'phone', false, 0x1d2021, 0xebdbb2, 0xfe8019, null, 0x504945),
  T('ember', 'phone', false, 0x000000, 0xebe5dc, 0xdf6124, null, 0x3a3733),
  T('signal', 'phone', false, 0x000000, 0xffffff, 0xff453a, null, 0x3a3a3a),
  T('paper', 'phone', true, 0xf1ede4, 0x1c1a17, 0xc2410c, null, 0xcbc3b4),
  om('catppuccin', false, 0x1e1e2e, 0xcdd6f4, 0x89b4fa),
  om('catppuccin-latte', true, 0xeff1f5, 0x4c4f69, 0x1e66f5),
  om('ethereal', false, 0x060b1e, 0xffcead, 0x7d82d9),
  om('everforest', false, 0x2d353b, 0xd3c6aa, 0x7fbbb3),
  om('flexoki-light', true, 0xfffcf0, 0x100f0f, 0x205ea6),
  om('gruvbox', false, 0x282828, 0xd4be98, 0x7daea3),
  om('hackerman', false, 0x0b0c16, 0xddf7ff, 0x82fb9c),
  om('kanagawa', false, 0x1f1f28, 0xdcd7ba, 0xdcd7ba),
  T('last-horizon', 'omarchy', false, 0x0c0b0c, 0xfafcfb, 0xb59790, 'walls/last-horizon.webp', 0x504945,
    'walls/last-horizon-left.webp'),
  om('lumon', false, 0x16242d, 0xd6e2ee, 0x8bc9eb),
  om('lupine', true, 0xfafafa, 0x212121, 0x3264eb),
  om('matte-black', false, 0x121212, 0xbebebe, 0xe68e0d),
  om('miasma', false, 0x222222, 0xc2c2b0, 0x78824b),
  om('nord', false, 0x2e3440, 0xd8dee9, 0x81a1c1),
  om('osaka-jade', false, 0x111c18, 0xc1c497, 0x509475),
  om('retro-82', false, 0x05182e, 0xf6dcac, 0xfaa968),
  om('ristretto', false, 0x2c2525, 0xe6d9db, 0xf38d70),
  om('rose-pine', true, 0xfaf4ed, 0x575279, 0x56949f),
  om('solitude', false, 0x101315, 0xcacccc, 0x798186),
  om('starship', false, 0x1c1c1e, 0xf5f5f7, 0xff9f0a),
  om('tokyo-night', false, 0x1a1b26, 0xa9b1d6, 0x7aa2f7),
  T('vantablack', 'omarchy', false, 0x000000, 0xffffff, 0x8d8d8d, null, 0x505050),
  om('white', true, 0xffffff, 0x000000, 0x6e6e6e),
];

const mix = (a, b, t) => a.map((v, i) => Math.round(v * t + b[i] * (1 - t)));
export const css = (c, a = 1) => a === 1 ? `rgb(${c[0]},${c[1]},${c[2]})` : `rgba(${c[0]},${c[1]},${c[2]},${a})`;

function luminance(c) {
  const l = c.map(v => { v /= 255; return v <= .03928 ? v / 12.92 : Math.pow((v + .055) / 1.055, 2.4); });
  return .2126 * l[0] + .7152 * l[1] + .0722 * l[2];
}

export function palette(t) {
  return {
    light: t.light, bg: t.bg, fg: t.fg, accent: t.accent, hand: t.hand,
    onAccent: luminance(t.accent) > .33 ? [17, 17, 17] : [255, 255, 255],
    muted: mix(t.fg, t.bg, .5), muted2: mix(t.fg, t.bg, .68),
    sheet: mix(t.fg, t.bg, .05), card: mix(t.fg, t.bg, .1),
  };
}

export const APPS = ['calculator', 'calendar', 'camera', 'chrome', 'clock', 'contacts', 'drive', 'files', 'gmail',
  'keep', 'maps', 'messages', 'music', 'phone', 'photos', 'play store', 'settings', 'weather', 'youtube'];

const QUICK = 'camera';

const DAYS = ['sun', 'mon', 'tue', 'wed', 'thu', 'fri', 'sat'];
const MONTHS = ['jan', 'feb', 'mar', 'apr', 'may', 'jun', 'jul', 'aug', 'sep', 'oct', 'nov', 'dec'];
const H24 = (() => {
  try {
    const hc = new Intl.DateTimeFormat(undefined, { hour: 'numeric' }).resolvedOptions().hourCycle;
    return hc === 'h23' || hc === 'h24';
  } catch { return true; }
})();
const pad2 = v => String(v).padStart(2, '0');
const clockText = (d = new Date()) => (H24 ? pad2(d.getHours()) : String(d.getHours() % 12 || 12)) + ':' + pad2(d.getMinutes());
const dateText = (d = new Date()) => `${DAYS[d.getDay()]} ${d.getDate()} ${MONTHS[d.getMonth()]}`;

// The dot-matrix readout. 5 x 7 glyphs. "0" is an empty row, so short letters sit on the baseline.
const GLYPH_SRC = {
  a: '0 0 .###. ....# .#### #...# .####', b: '#.... #.... ####. #...# #...# #...# ####.',
  c: '0 0 .#### #.... #.... #.... .####', d: '....# ....# .#### #...# #...# #...# .####',
  e: '0 0 .###. #...# ##### #.... .###.', f: '..##. .#... ####. .#... .#... .#... .#...',
  g: '0 0 .#### #...# #...# #...# .#### ....# .###.', h: '#.... #.... ####. #...# #...# #...# #...#',
  i: '..#.. 0 .##.. ..#.. ..#.. ..#.. .###.', j: '...#. 0 ..##. ...#. ...#. ...#. ...#. #..#. .##..',
  k: '#.... #.... #..#. #.#.. ##... #.#.. #..#.', l: '.##.. ..#.. ..#.. ..#.. ..#.. ..#.. .###.',
  m: '0 0 ##.#. #.#.# #.#.# #.#.# #.#.#', n: '0 0 ####. #...# #...# #...# #...#',
  o: '0 0 .###. #...# #...# #...# .###.', p: '0 0 ####. #...# #...# #...# ####. #.... #....',
  q: '0 0 .#### #...# #...# #...# .#### ....# ....#', r: '0 0 #.##. ##..# #.... #.... #....',
  s: '0 0 .#### #.... .###. ....# ####.', t: '.#... .#... ####. .#... .#... .#..# ..##.',
  u: '0 0 #...# #...# #...# #...# .####', v: '0 0 #...# #...# #...# .#.#. ..#..',
  w: '0 0 #...# #...# #.#.# #.#.# .#.#.', x: '0 0 #...# .#.#. ..#.. .#.#. #...#',
  y: '0 0 #...# #...# #...# #...# .#### ....# .###.', z: '0 0 ##### ...#. ..#.. .#... #####',
  0: '.###. #...# #..## #.#.# ##..# #...# .###.', 1: '..#.. .##.. ..#.. ..#.. ..#.. ..#.. .###.',
  2: '.###. #...# ....# ...#. ..#.. .#... #####', 3: '##### ...#. ..#.. ...#. ....# #...# .###.',
  4: '...#. ..##. .#.#. #..#. ##### ...#. ...#.', 5: '##### #.... ####. ....# ....# #...# .###.',
  6: '..##. .#... #.... ####. #...# #...# .###.', 7: '##### ....# ...#. ..#.. .#... .#... .#...',
  8: '.###. #...# #...# .###. #...# #...# .###.', 9: '.###. #...# #...# .#### ....# ...#. .##..',
  ':': '. . # . # . .',
};
const GLYPHS = Object.fromEntries(Object.entries(GLYPH_SRC).map(([k, v]) =>
  [k, v.split(' ').map(r => r === '0' ? '.....' : r)]));

function bits(text, scale, rowOff) {
  const out = new Uint8Array(RC * RR);
  let cx = 0;
  for (const ch of text) {
    const g = GLYPHS[ch];
    if (!g) { cx += 3 * scale; continue; }
    const w = g[0].length;
    for (let r = 0; r < g.length; r++) for (let c = 0; c < w; c++) {
      if (g[r][c] !== '#') continue;
      for (let sy = 0; sy < scale; sy++) for (let sx = 0; sx < scale; sx++) {
        const x = cx + c * scale + sx, y = rowOff + r * scale + sy;
        if (x >= 0 && x < RC && y >= 0 && y < RR) out[y * RC + x] = 1;
      }
    }
    cx += (w + 1) * scale;
  }
  return out;
}

class Readout {
  constructor() {
    this.v = new Float32Array(RC * RR);
    this.accent = new Uint8Array(RC * RR);
    this.target = new Uint8Array(RC * RR);
    this.start = new Float64Array(RC * RR);
  }

  set(text, scale, rowOff, accentOn, now, instant) {
    const b = bits(text, scale, rowOff);
    for (let i = 0; i < b.length; i++) {
      const on = b[i];
      if (on) this.accent[i] = accentOn ? 1 : 0;
      if (on !== this.target[i]) {
        this.target[i] = on;
        this.start[i] = now + (i % RC) * 5 + (on ? 30 : 0);
      }
      if (instant) this.v[i] = on;
    }
  }

  /** Moves the dots one frame. Returns true while a dot has not reached its state. */
  advance(now, dt) {
    const k = 1 - Math.pow(.7, dt / 16);
    let busy = false;
    for (let i = 0; i < this.v.length; i++) {
      const t = this.target[i], x = this.v[i];
      if (x === t) continue;
      busy = true;
      if (now < this.start[i]) continue;
      const d = t - x;
      this.v[i] = Math.abs(d) < .004 ? t : x + d * k;
    }
    return busy;
  }
}

// Images. Each wallpaper loads once and every dial redraws when it arrives.
const images = new Map();
const onImage = new Set();
function image(src) {
  let e = images.get(src);
  if (!e) {
    e = { img: new Image(), ok: false };
    e.img.onload = () => { e.ok = true; onImage.forEach(f => f()); };
    e.img.src = src;
    images.set(src, e);
  }
  return e.ok ? e.img : null;
}

// The dot-hand art is white dots on black. The dots take the hand colour and the black takes the background.
const tints = new Map();
function tinted(t, img) {
  let c = tints.get(t.name);
  if (c) return c;
  c = document.createElement('canvas');
  c.width = img.naturalWidth;
  c.height = img.naturalHeight;
  const g = c.getContext('2d');
  g.drawImage(img, 0, 0);
  const d = g.getImageData(0, 0, c.width, c.height);
  const p = d.data;
  for (let i = 0; i < p.length; i += 4) {
    const v = p[i] / 255;
    p[i] = t.bg[0] + (t.hand[0] - t.bg[0]) * v;
    p[i + 1] = t.bg[1] + (t.hand[1] - t.bg[1]) * v;
    p[i + 2] = t.bg[2] + (t.hand[2] - t.bg[2]) * v;
  }
  g.putImageData(d, 0, 0);
  tints.set(t.name, c);
  return c;
}

const walls = new Map();
function wallLayer(t, left, cw, ch) {
  const key = `${t.name}|${left}|${cw}x${ch}`;
  const hit = walls.get(key);
  if (hit) return hit;
  let img = image(t.wall ? (left && t.wallLeft ? t.wallLeft : t.wall) : 'hands.webp');
  if (!img) return null;
  if (!t.wall) img = tinted(t, img);
  const c = document.createElement('canvas');
  c.width = cw;
  c.height = ch;
  const g = c.getContext('2d');
  const iw = img.naturalWidth || img.width, ih = img.naturalHeight || img.height;
  const s = Math.max(cw / iw, ch / ih);
  g.save();
  if (!t.wall && left) { g.translate(cw, 0); g.scale(-1, 1); }
  g.drawImage(img, (cw - iw * s) / 2, (ch - ih * s) / 2, iw * s, ih * s);
  g.restore();
  if (t.wall) {
    const b = t.bg;
    let gr = g.createLinearGradient(0, 0, 0, ch);
    gr.addColorStop(0, css(b, .6)); gr.addColorStop(.32, css(b, .15));
    gr.addColorStop(.7, css(b, .2)); gr.addColorStop(1, css(b, .75));
    g.fillStyle = gr;
    g.fillRect(0, 0, cw, ch);
    // The side with the dial is darker, so the labels stay readable.
    gr = g.createLinearGradient(0, 0, cw, 0);
    gr.addColorStop(0, css(b, left ? .86 : .2)); gr.addColorStop(.5, css(b, .5)); gr.addColorStop(1, css(b, left ? .2 : .86));
    g.fillStyle = gr;
    g.fillRect(0, 0, cw, ch);
  }
  if (walls.size > 60) walls.clear();
  walls.set(key, c);
  return c;
}

// Dial geometry for one rim radius.
function geo(r, cy, ax = AX) {
  const step = GAP / r;
  return { r, cy, cx: ax + r, step, span: Math.min(6.5, 1.55 / step) };
}
const fitScale = chars => Math.max(1, 150 / (chars * 9));
const labelScale = (a, chars) => { const cl = Math.max(0, 1 - a); return Math.min(1 + .45 * cl * cl, fitScale(chars)); };
const labelRadius = (g, m) => g.r - (m === 'ticks' ? 28 : m === 'double' ? 34 : 22);
const CH = 9.15;

function labelPoseAt(d, chars, s, g, r, left) {
  const ang = d * g.step;
  const x0 = g.cx - r * Math.cos(ang), y = g.cy + r * Math.sin(ang);
  const w = chars * CH * s;
  const rd = left ? r + 8 : r - w - 8;
  const bx = g.cx - rd * Math.cos(ang), by = g.cy + rd * Math.sin(ang);
  return { x: left ? W - x0 : x0, y, rot: left ? ang : -ang, tx: left ? -1 : 0, bx: left ? W - bx : bx, by };
}

const OPEN = { grid: [640, 700, 140, 540], radial: [420, 700, 0, 380] };

export class Dial {
  /**
   * @param canvas the canvas to draw on. Its width sets the scale.
   * @param o.layout layout index, o.theme theme index, o.still true for a preview that takes no input.
   */
  constructor(canvas, o = {}) {
    this.c = canvas;
    this.g = canvas.getContext('2d');
    this.frame = this.frame.bind(this);
    this.still = !!o.still;
    this.onChange = o.onChange || (() => {});
    this.names = APPS;
    this.unread = new Set(['gmail', 'messages']);
    this.pos = this.target = APPS.indexOf('gmail');
    this.lastIdx = Math.round(this.pos);
    this.vel = 0;
    this.spin = 'idle';
    this.pawl = 0;
    this.ls = this.lsT = .5;
    this.left = !!o.left;
    this.scrub = null;
    this.letter = '';
    this.drag = null;
    this.open = null;
    this.readout = new Readout();
    this.readName = false;
    this.a = { read: 1, labels: 1, clock: 1, search: 1, scrub: 0, wall: 1 };
    this.wall = null;
    this.prevWall = null;
    this.layoutIndex = o.layout ?? 0;
    this.radius = this.rT = LAYOUTS[this.layoutIndex].r;
    this.setTheme(o.theme ?? 0);
    this.showClock(true);
    this.resize();
    new ResizeObserver(() => this.resize()).observe(canvas);
    onImage.add(() => this.redraw());
    if (!this.still) this.listen();
  }

  get layout() { return LAYOUTS[this.layoutIndex]; }
  get kind() { return this.layout.kind; }
  get grid() { return this.kind !== 'radial'; }
  get cy() { return cyOf(this.layout); }

  resize() {
    const r = this.c.getBoundingClientRect();
    if (!r.width) return;
    const dpr = Math.min(3, window.devicePixelRatio || 1);
    const w = Math.round(r.width * dpr), h = Math.round(r.width * HD / W * dpr);
    if (w === this.c.width && h === this.c.height) return;
    this.c.width = w;
    this.c.height = h;
    this.k = w / W;
    this.redraw();
  }

  redraw() { if (this.raf) return; this.draw(performance.now()); }

  setTheme(i) {
    this.themeIndex = i;
    this.theme = THEMES[i];
    this.pal = palette(this.theme);
    this.run();
  }

  setLayout(i) {
    if (i === this.layoutIndex) return;
    const wasGrid = this.grid;
    this.layoutIndex = i;
    this.rT = this.layout.r;
    if (wasGrid !== this.grid) this.radius = this.rT;
    this.ls = this.lsT = .5;
    this.showClock(true);
    this.run();
  }

  setSide(left) { this.left = left; this.run(); }

  run() {
    if (this.still) { this.redraw(); return; }
    if (this.raf) return;
    this.last = performance.now();
    this.raf = requestAnimationFrame(this.frame);
  }

  frame(now) {
    this.raf = 0;
    const dt = Math.min(48, Math.max(1, now - this.last));
    const busy = this.step(dt, now);
    this.draw(now);
    if (busy) this.run();
    this.last = now;
  }

  step(dt, now) {
    let p = this.pos;
    if (this.spin === 'fling') {
      p += this.vel * dt;
      this.vel *= Math.pow(.9965, dt);
      if (Math.abs(this.vel) < .004) {
        this.target = Math.round(p + Math.sign(this.vel) * .3);
        this.spin = 'settle';
      }
    } else if (this.spin === 'settle') {
      const d = this.target - p;
      p += d * (1 - Math.pow(.78, dt / 16));
      if (Math.abs(d) < .002) { p = this.target; this.spin = 'idle'; }
    }
    this.pos = p;
    let morph = false;
    if (Math.abs(this.radius - this.rT) > .3) {
      this.radius += (this.rT - this.radius) * (1 - Math.pow(.84, dt / 16));
      morph = true;
    } else this.radius = this.rT;
    this.pawl *= Math.pow(.8, dt / 16);
    this.ls = approach(this.ls, this.lsT, dt, .82);
    const idx = Math.round(p);
    if (idx !== this.lastIdx) {
      this.lastIdx = idx;
      this.pawl = 1;
      this.followReadout(now);
    }
    const reading = this.grid && this.readout.advance(now, dt);
    const a = this.a, o = this.open;
    const tg = {
      read: this.scrub && this.kind !== 'readout' ? .25 : 1,
      labels: o ? 0 : 1,
      clock: this.scrub ? .25 : 1,
      search: this.scrub || o ? 0 : 1,
      scrub: this.scrub ? 1 : 0,
      wall: 1,
    };
    let fading = false;
    for (const key in tg) {
      a[key] = approach(a[key], tg[key], dt, key === 'wall' ? .9 : .8);
      if (a[key] !== tg[key]) fading = true;
    }
    if (o) {
      o.t += dt;
      const [i, h, d, out] = o.grid ? OPEN.grid : OPEN.radial;
      if (o.t >= i + h + d + out) {
        this.open = null;
        if (this.readName && !this.drag) this.scheduleClock();
      }
    }
    const done = this.spin === 'idle' && this.pawl < .01 && !morph && this.ls === this.lsT && !reading && !fading && !this.open;
    if (done) {
      this.pawl = 0;
      if (this.readName && !this.drag) this.scheduleClock();
    }
    return !done;
  }

  openProgress() {
    const o = this.open;
    if (!o) return 0;
    const [i, h, d, out] = o.grid ? OPEN.grid : OPEN.radial;
    let t = o.t;
    if (t < i) return inOutCubic(t / i);
    t -= i + h + d;
    if (t < 0) return 1;
    return t < out ? 1 - inOutCubic(t / out) : 0;
  }

  // The readout on the grid layouts.

  followReadout(now = performance.now()) {
    if (this.kind !== 'readout') return;
    if (this.scrub) {
      clearTimeout(this.clockTimer);
      this.readName = true;
      this.readout.set(this.scrub[mod(Math.round(this.pos), this.scrub.length)], 2, 0, true, now, false);
      this.run();
    } else this.showName();
  }

  showName() {
    clearTimeout(this.clockTimer);
    this.readName = true;
    this.readout.set(this.names[mod(Math.round(this.pos), this.names.length)], 1, 3, true, performance.now(), false);
    this.run();
  }

  showClock(instant = false) {
    clearTimeout(this.clockTimer);
    this.readName = false;
    this.clockShown = clockText();
    this.readout.set(this.clockShown, 2, 0, false, performance.now(), instant);
    this.run();
  }

  scheduleClock() {
    clearTimeout(this.clockTimer);
    this.clockTimer = setTimeout(() => {
      if (!this.drag && this.spin === 'idle' && !this.open) this.showClock();
    }, 1600);
  }

  /** Redraws when the minute changes. */
  tick() {
    if (clockText() === this.clockShown) return;
    if (!this.readName) this.showClock(); else this.clockShown = clockText();
    this.redraw();
  }

  // Gestures. All coordinates are design units.

  mirX(x) { return this.left ? W - x : x; }

  zoneAt(x, y) {
    if (this.kind === 'lens') {
      if (!this.onLens(x, y)) return 'empty';
      return this.lensRowAt(y) === Math.round(this.pos) ? 'selected' : 'dial';
    }
    if (y < 200 || y > HD - 102) return 'empty';
    const g = geo(this.radius, this.cy, axOf(this.layout));
    const dx = g.cx - this.mirX(x), dy = y - this.cy;
    const dist = Math.hypot(dx, dy);
    if (dist <= g.r - 200 || dist >= g.r + 40) return 'empty';
    const k = this.pos + Math.atan2(dy, dx) / g.step;
    if (Math.abs(k - this.pos) > g.span) return 'empty';
    return Math.round(k) === Math.round(this.pos) ? 'selected' : 'dial';
  }

  onLens(x, y) { return this.mirX(x) > 160 && y > LENS_TOP - 12 && y < LENS_BOTTOM + 12; }

  lensRowY(i) {
    const dy = (i - this.pos) * LENS_GAP, l = 70 + 60 * this.ls, ad = Math.abs(dy);
    if (ad >= l) return this.cy + dy;
    const t = 1 - ad / l;
    return this.cy + dy * (1 + .9 * this.ls * t * t);
  }

  lensRowAt(y) {
    const c = Math.round(this.pos + (y - this.cy) / LENS_GAP);
    let best = c, bd = Infinity;
    for (let i = c - 4; i <= c + 4; i++) {
      const d = Math.abs(this.lensRowY(i) - y);
      if (d < bd) { bd = d; best = i; }
    }
    return bd <= 16 ? best : null;
  }

  onLensName(i, x, y) {
    const dy = (i - this.pos) * LENS_GAP, l = 70 + 60 * this.ls;
    if (Math.abs(dy) >= l) return false;
    const name = this.names[mod(i, this.names.length)];
    const right = LX - 10, leftEdge = LX - 18 - name.length * CH * 1.7 - 8;
    const mx = this.mirX(x);
    return mx > leftEdge && mx < right && Math.abs(y - this.lensRowY(i)) < 14;
  }

  point(e) {
    const r = this.c.getBoundingClientRect();
    return [(e.clientX - r.left) / r.width * W, (e.clientY - r.top) / r.height * HD];
  }

  listen() {
    const c = this.c;
    c.addEventListener('pointerdown', e => {
      if (e.button !== 0) return;
      c.setPointerCapture(e.pointerId);
      this.onDown(...this.point(e));
    });
    c.addEventListener('pointermove', e => { if (this.drag) this.onMove(...this.point(e)); });
    c.addEventListener('pointerup', e => this.onUp(...this.point(e)));
    c.addEventListener('pointercancel', () => this.onCancel());
    c.addEventListener('wheel', e => {
      e.preventDefault();
      if (this.open || this.drag) return;
      this.wheelAcc = (this.wheelAcc || 0) + (e.deltaMode === 1 ? e.deltaY * 40 : e.deltaY);
      clearTimeout(this.wheelTimer);
      this.wheelTimer = setTimeout(() => { this.wheelAcc = 0; }, 220);
      while (Math.abs(this.wheelAcc) >= 50) {
        const s = Math.sign(this.wheelAcc);
        this.stepBy(s);
        this.wheelAcc -= 50 * s;
      }
    }, { passive: false });
    c.addEventListener('keydown', e => {
      if (this.open) return;
      if (e.key === 'j' || e.key === 'ArrowDown') this.stepBy(1);
      else if (e.key === 'k' || e.key === 'ArrowUp') this.stepBy(-1);
      else if (e.key === 'l' || e.key === 'Enter' || e.key === ' ') this.launchSelected();
      else return;
      e.preventDefault();
    });
  }

  onDown(x, y) {
    if (this.open) return;
    // The quick button and the voice button take their own taps.
    if (this.onQuick(x, y)) { this.btnDown = 'quick'; return; }
    if (this.onVoice(x, y)) { this.btnDown = 'voice'; return; }
    const t = performance.now();
    const d = {
      x0: x, y0: y, y, t0: t, moved: false, scrub: false, samples: [[t, this.pos]],
      caught: this.spin === 'fling' || (this.spin === 'settle' && Math.abs(this.target - this.pos) > .3),
    };
    const zone = this.zoneAt(x, y);
    this.drag = d;
    this.spin = 'drag';
    this.vel = 0;
    if (this.kind === 'lens') { clearTimeout(this.lsTimer); this.lsT = 1; }
    if (this.kind === 'readout') this.showName();
    if (zone !== 'empty') {
      this.press = setTimeout(() => { if (this.drag === d && !d.moved) this.startScrub(); }, 450);
    }
    this.run();
  }

  onMove(x, y) {
    if (this.btnDown) return;
    const d = this.drag;
    if (!d) return;
    if (!d.moved && (Math.abs(y - d.y0) > 6 || Math.abs(x - d.x0) > 6)) {
      d.moved = true;
      clearTimeout(this.press);
    }
    this.pos -= (y - d.y) / (this.kind === 'lens' ? LENS_GAP : GAP);
    d.y = y;
    const t = performance.now();
    d.samples.push([t, this.pos]);
    while (d.samples.length > 2 && t - d.samples[0][0] > 90) d.samples.shift();
    this.run();
  }

  onUp(x, y) {
    clearTimeout(this.press);
    if (this.btnDown) {
      const b = this.btnDown;
      this.btnDown = null;
      if (b === 'quick' && this.onQuick(x, y)) this.openQuick();
      if (b === 'voice' && this.onVoice(x, y)) this.onChange({ voice: true });
      return;
    }
    const d = this.drag;
    if (!d) return;
    this.drag = null;
    if (this.kind === 'lens') this.lsT = .5;
    if (d.scrub) { this.endScrub(); return; }
    const t = performance.now();
    this.target = Math.round(this.pos);
    if (!d.moved && t - d.t0 < 450) {
      this.spin = 'settle';
      if (!d.caught) this.tapAt(x, y);
      this.run();
      return;
    }
    const [t0, p0] = d.samples[0];
    this.vel = Math.max(-.06, Math.min(.06, (this.pos - p0) / Math.max(16, t - t0)));
    this.spin = Math.abs(this.vel) > .006 ? 'fling' : 'settle';
    this.run();
  }

  onCancel() {
    clearTimeout(this.press);
    this.btnDown = null;
    const d = this.drag;
    if (!d) return;
    this.drag = null;
    if (this.kind === 'lens') this.lsT = .5;
    if (d.scrub) { this.endScrub(); return; }
    this.target = Math.round(this.pos);
    this.spin = 'settle';
    this.run();
  }

  stepBy(n) {
    const base = this.spin === 'settle' ? this.target : Math.round(this.pos);
    if (this.kind === 'lens') {
      this.lsT = 1;
      clearTimeout(this.lsTimer);
      this.lsTimer = setTimeout(() => { this.lsT = .5; this.run(); }, 700);
    }
    if (this.kind === 'readout') this.showName();
    this.target = base + n;
    this.spin = 'settle';
    this.run();
  }

  startScrub() {
    const n = this.names.length;
    const letters = [...new Set(this.names.map(a => a[0]))];
    const cur = this.names[mod(Math.round(this.pos), n)];
    this.scrubBase = Math.round(this.pos);
    const li = letters.indexOf(cur[0]);
    this.pos = this.target = this.lastIdx = li;
    this.spin = 'drag';
    this.drag.scrub = true;
    this.scrub = letters;
    this.followReadout();
    this.run();
  }

  endScrub() {
    const letters = this.scrub;
    if (!letters) return;
    const letter = letters[mod(Math.round(this.pos), letters.length)];
    this.scrub = null;
    const n = this.names.length;
    const idx = this.names.findIndex(a => a[0] === letter);
    let d = idx - mod(this.scrubBase, n);
    if (d > n / 2) d -= n;
    if (d < -n / 2) d += n;
    this.pos = this.lastIdx = this.scrubBase;
    this.target = this.scrubBase + d;
    this.spin = 'settle';
    this.followReadout();
    this.run();
  }

  tapAt(x, y) {
    const cur = Math.round(this.pos);
    if (this.kind === 'readout' && y > READ_Y - 10 && y < READ_Y + 90) { this.launchSelected(); return; }
    if (this.kind === 'lens') {
      if (!this.onLens(x, y)) return;
      const k = this.lensRowAt(y);
      if (k === null) return;
      const named = this.onLensName(k, x, y);
      if (!named && this.mirX(x) < LX - 16) return;
      if (k === cur) this.launchSelected();
      else if (named) this.launchAt(k);
      else { this.target = k; this.spin = 'settle'; }
      return;
    }
    if (this.zoneAt(x, y) === 'empty') return;
    const g = geo(this.radius, this.cy, axOf(this.layout));
    const k = Math.round(this.pos + Math.atan2(y - this.cy, g.cx - this.mirX(x)) / g.step);
    if (k === cur) this.launchSelected();
    else if (this.kind !== 'readout') this.launchAt(k);
    else { this.target = k; this.spin = 'settle'; }
  }

  launchSelected() { this.openApp(Math.round(this.target)); }

  launchAt(k) {
    this.target = k;
    this.spin = 'settle';
    this.openApp(k);
  }

  openApp(k) {
    const name = this.names[mod(k, this.names.length)];
    this.unread.delete(name);
    let rx, ry;
    if (this.grid) {
      rx = this.mirX(axOf(this.layout));
      ry = this.cy;
    } else {
      const p = labelPoseAt(k - this.pos, name.length, 1, geo(this.radius, this.cy), labelRadius(geo(this.radius, this.cy), this.layout.m), this.left);
      rx = p.x;
      ry = p.y;
    }
    this.open = { name, t: 0, grid: this.grid, rx, ry };
    this.onChange({ open: name });
    this.run();
  }

  // The quick button sits in the bottom corner on the dial side. In the demo it holds the camera.

  quickBox() {
    this.font(13, 400, 0);
    const w = 14 + 14 + 10 + this.g.measureText(QUICK).width + 18;
    return { x: this.left ? 24 : W - 24 - w, y: SEARCH_Y, w, h: 44 };
  }

  onQuick(x, y) {
    if (this.a.search < .5) return false;
    const b = this.quickBox();
    return x >= b.x && x <= b.x + b.w && y >= b.y && y <= b.y + b.h;
  }

  openQuick() {
    const b = this.quickBox();
    // The grid ripple starts at the label. The radial sheet grows from it.
    this.open = { name: QUICK, t: 0, grid: this.grid, rx: b.x + 38 + (this.grid ? 6 : 0), ry: b.y + b.h / 2 };
    this.onChange({ open: QUICK });
    this.run();
  }

  // The voice button sits between search and the quick button. The demo has no microphone.

  searchBox() {
    this.font(13, 400, 0);
    const w = 14 + 14 + 10 + this.g.measureText('search').width + 18;
    return { x: this.left ? W - 24 - w : 24, y: SEARCH_Y, w, h: 44 };
  }

  voiceBox() {
    const s = this.searchBox();
    return { x: this.left ? s.x - 10 - 44 : s.x + s.w + 10, y: SEARCH_Y, w: 44, h: 44 };
  }

  onVoice(x, y) {
    if (this.a.search < .5) return false;
    const b = this.voiceBox();
    return x >= b.x && x <= b.x + b.w && y >= b.y && y <= b.y + b.h;
  }

  // Drawing.

  font(size, weight, spacing) {
    const g = this.g;
    g.font = `${weight} ${size}px ${FONT}`;
    if ('letterSpacing' in g) g.letterSpacing = `${spacing * size}px`;
  }

  draw(now) {
    const g = this.g, k = this.k, pal = this.pal;
    if (!k) return;
    g.setTransform(1, 0, 0, 1, 0, 0);
    g.globalAlpha = 1;
    g.fillStyle = css(pal.bg);
    g.fillRect(0, 0, this.c.width, this.c.height);
    if (!this.grid) {
      const wl = wallLayer(this.theme, this.left, this.c.width, this.c.height);
      if (wl && wl !== this.wall) {
        // A new wallpaper fades in over the old one. A resize swaps it at once.
        const fade = !this.still && this.wall && this.wall.width === wl.width;
        this.prevWall = this.wall;
        this.wall = wl;
        if (fade) { this.a.wall = 0; this.run(); }
      }
      if (this.prevWall && this.a.wall < 1 && this.prevWall.width === this.c.width) g.drawImage(this.prevWall, 0, 0);
      if (this.wall) {
        g.globalAlpha = this.still ? 1 : this.a.wall;
        g.drawImage(this.wall, 0, 0);
        g.globalAlpha = 1;
      }
    }
    g.setTransform(k, 0, 0, k, 0, 0);
    if (this.grid) this.drawGrid();
    else {
      this.drawClock();
      this.drawMarks();
      this.drawLabels();
      this.drawNotch();
    }
    if (this.kind !== 'readout') this.drawScrubLetter();
    this.drawSearch();
    this.drawVoice();
    this.drawQuick();
    this.drawOpen();
    this.drawStatus();
  }

  text(s, x, y, size, weight, color, alpha, spacing, align = 'left', base = 'middle') {
    const g = this.g;
    this.font(size, weight, spacing);
    g.textAlign = align;
    g.textBaseline = base;
    g.globalAlpha = alpha;
    g.fillStyle = css(color);
    g.fillText(s, x, y);
    g.globalAlpha = 1;
  }

  drawStatus() {
    const pal = this.pal, g = this.g;
    this.text(clockText(), 22, 24, 12, 500, pal.fg, .9, 0);
    g.globalAlpha = .9;
    g.strokeStyle = css(pal.fg);
    g.lineWidth = 1.2;
    g.beginPath();
    g.roundRect(W - 46, 19, 20, 10, 2.5);
    g.stroke();
    g.fillStyle = css(pal.fg);
    g.fillRect(W - 44, 21, 13, 6);
    g.fillRect(W - 25.5, 22, 1.6, 4);
    g.globalAlpha = 1;
  }

  drawClock() {
    const pal = this.pal, left = this.left, a = this.a.clock;
    const x = left ? W - 28 : 28, al = left ? 'right' : 'left';
    this.text(clockText(), x, CLOCK_TOP, 68, 300, pal.fg, a, -.05, al, 'top');
    this.text(dateText(), x, CLOCK_TOP + 68 + 8, 13, 400, pal.muted2, a, .02, al, 'top');
  }

  drawSearch() {
    const a = this.a.search;
    if (a <= .004) return;
    const g = this.g, pal = this.pal;
    const { x, w, h } = this.searchBox(), y = SEARCH_Y + (1 - a) * 8;
    g.globalAlpha = a;
    g.fillStyle = css(pal.bg, .55);
    g.beginPath();
    g.roundRect(x, y, w, h, h / 2);
    g.fill();
    g.strokeStyle = css(pal.fg, .22);
    g.lineWidth = 1;
    g.stroke();
    g.strokeStyle = css(pal.fg);
    g.lineWidth = 1.5;
    const ix = x + 14, iy = y + 15;
    g.beginPath();
    g.arc(ix + 5, iy + 5, 4.25, 0, TAU);
    g.moveTo(ix + 8.5, iy + 8.5);
    g.lineTo(ix + 12.74, iy + 12.74);
    g.stroke();
    this.text('search', ix + 24, y + h / 2, 13, 400, pal.fg, a, 0);
  }

  drawVoice() {
    const a = this.a.search;
    if (a <= .004) return;
    const g = this.g, pal = this.pal, b = this.voiceBox(), y = b.y + (1 - a) * 8;
    g.globalAlpha = a;
    g.fillStyle = css(pal.bg, .55);
    g.beginPath();
    g.arc(b.x + 22, y + 22, 22, 0, TAU);
    g.fill();
    g.strokeStyle = css(pal.fg, .22);
    g.lineWidth = 1;
    g.stroke();
    g.strokeStyle = css(pal.fg);
    g.lineWidth = 1.5;
    const ix = b.x + 15, iy = y + 15;
    g.beginPath();
    g.roundRect(ix + 4.5, iy + .75, 5, 8, 2.5);
    g.stroke();
    g.beginPath();
    g.ellipse(ix + 7, iy + 7.5, 5, 4, 0, 0, Math.PI);
    g.moveTo(ix + 7, iy + 11.5);
    g.lineTo(ix + 7, iy + 13.5);
    g.stroke();
    g.globalAlpha = 1;
  }

  drawQuick() {
    const a = this.a.search;
    if (a <= .004) return;
    const g = this.g, pal = this.pal, b = this.quickBox(), y = b.y + (1 - a) * 8;
    g.globalAlpha = a;
    g.fillStyle = css(pal.bg, .55);
    g.beginPath();
    g.roundRect(b.x, y, b.w, b.h, b.h / 2);
    g.fill();
    g.strokeStyle = css(pal.fg, .22);
    g.lineWidth = 1;
    g.stroke();
    g.globalAlpha = 1;
    this.dot(b.x + 21, y + b.h / 2, 3.5, pal.fg, a);
    this.text(QUICK, b.x + 38, y + b.h / 2, 13, 400, pal.fg, a, 0);
  }

  drawScrubLetter() {
    const a = this.a.scrub;
    if (this.scrub) this.letter = this.scrub[mod(Math.round(this.pos), this.scrub.length)];
    if (a <= .004 || !this.letter) return;
    const x = this.left ? W - 36 : 36;
    this.text(this.letter, x, this.cy + (1 - a) * 6, 112, 300, this.pal.accent, a, -.04, this.left ? 'right' : 'left');
  }

  drawOpen() {
    const o = this.open;
    if (!o) return;
    const v = this.openProgress(), g = this.g, pal = this.pal;
    if (!o.grid) {
      const far = Math.max(Math.hypot(o.rx, o.ry), Math.hypot(W - o.rx, o.ry), Math.hypot(o.rx, HD - o.ry), Math.hypot(W - o.rx, HD - o.ry));
      g.fillStyle = css(pal.sheet);
      g.beginPath();
      g.arc(o.rx, o.ry, Math.max(0, v * far), 0, TAU);
      g.fill();
    }
    const t = c01((v - .7) / .3);
    if (t > 0) this.text(o.name, W / 2, HD / 2 - 10, 30, 300, pal.fg, t, -.02, 'center');
  }

  drawMarks() {
    const g = this.g, pal = this.pal, L = this.layout, m = L.m, left = this.left, pos = this.pos;
    const gg = geo(this.radius, this.cy);
    const x = v => left ? W - v : v;
    if (m === 'ticks' || m === 'nodes') {
      g.strokeStyle = css(pal.fg, .14);
      g.lineWidth = 1;
      g.beginPath();
      g.arc(x(gg.cx), gg.cy, gg.r, 0, TAU);
      g.stroke();
    }
    const tpa = L.tpa;
    const first = Math.ceil((pos - gg.span) * tpa), last = Math.floor((pos + gg.span) * tpa);
    for (let i = first; i <= last; i++) {
      const d = i / tpa - pos, a = Math.abs(d);
      if (a > gg.span) continue;
      const ang = d * gg.step;
      const px = gg.cx - gg.r * Math.cos(ang), py = gg.cy + gg.r * Math.sin(ang);
      const major = mod(i, tpa) === 0, hot = major && a < .5;
      const fade = Math.pow(1 - a / gg.span, .8);
      if (m === 'ticks') {
        const w = major ? 14 : 6, sg = left ? -1 : 1;
        g.strokeStyle = css(pal.fg, (major ? .6 : .25) * fade);
        g.lineWidth = 1;
        g.beginPath();
        g.moveTo(x(px), py);
        g.lineTo(x(px) + sg * w * Math.cos(ang), py - w * Math.sin(ang));
        g.stroke();
      } else if (m === 'double') {
        // Outer dots swell toward the notch. A fainter inner arc marks each app.
        const f = Math.max(0, 1 - a / 3.6);
        const size = hot ? 9 : major ? 3 + 5 * f : 2 + 2.6 * f;
        this.dot(x(px), py, size / 2, hot ? pal.accent : pal.fg, hot ? 1 : .65 * (.3 + .7 * f) * fade);
        if (major) {
          const ri = gg.r - 16, s2 = 2.5 + 3 * f;
          this.dot(x(gg.cx - ri * Math.cos(ang)), gg.cy + ri * Math.sin(ang), s2 / 2, pal.fg, .65 * (.25 + .5 * f) * fade);
        }
      } else {
        const size = m === 'nodes' ? (major ? 6 : 2) : (major ? 4 : 2);
        const [c, al] = hot ? [pal.accent, 1] : major ? [pal.fg, .65] : m === 'dots' ? [pal.fg, .3] : [pal.fg, .22];
        this.dot(x(px), py, size / 2, c, al * fade);
      }
    }
  }

  dot(x, y, r, c, a) {
    const g = this.g;
    g.globalAlpha = c01(a);
    g.fillStyle = css(c);
    g.beginPath();
    g.arc(x, y, r, 0, TAU);
    g.fill();
    g.globalAlpha = 1;
  }

  drawLabels() {
    const names = this.scrub || this.names, n = names.length, pal = this.pal, g = this.g;
    const gg = geo(this.radius, this.cy), L = this.layout, la = this.a.labels;
    for (let i = 0; i < n; i++) {
      let d = mod(i - this.pos, n);
      if (d > n / 2) d -= n;
      const a = Math.abs(d);
      if (a >= gg.span) continue;
      const cl = Math.max(0, 1 - a), sel = cl > .5, name = names[i];
      const s = this.scrub ? 1 + .9 * cl * cl : labelScale(a, name.length);
      const p = labelPoseAt(d, name.length, s, gg, labelRadius(gg, L.m), this.left);
      const op = Math.pow(1 - a / gg.span, 1.35) * (this.open && this.open.name === name ? 0 : 1) * (this.open ? la : 1);
      if (op < .004) continue;
      this.label(name, p, s, sel, op);
      if (!this.scrub && this.unread.has(name)) this.dot(p.bx, p.by, 2.5, pal.accent, op);
    }
  }

  label(name, p, s, sel, op) {
    const g = this.g;
    g.save();
    g.translate(p.x, p.y);
    g.rotate(p.rot);
    g.scale(s, s);
    this.font(15, sel ? 500 : 400, .01);
    const w = g.measureText(name).width;
    g.textAlign = 'left';
    g.textBaseline = 'middle';
    g.globalAlpha = op;
    g.fillStyle = css(sel ? this.pal.accent : this.pal.fg);
    g.fillText(name, p.tx * w, 0);
    g.restore();
  }

  /** The fixed notch at the selected row. It kicks 4 units outward on every detent. */
  drawNotch() {
    const pal = this.pal, left = this.left, g = this.g, cy = this.cy;
    const off = (left ? 1 : -1) * this.pawl * 4;
    const x = v => (left ? W - v : v) + off;
    switch (this.layout.m) {
      case 'ticks':
        g.fillStyle = css(pal.accent);
        g.fillRect(x(left ? 242 : 204), cy - 1, 38, 2);
        break;
      case 'nodes':
        g.strokeStyle = css(pal.accent);
        g.lineWidth = 1.5;
        g.beginPath();
        g.arc(x(226), cy, 9.25, 0, TAU);
        g.stroke();
        break;
      case 'double':
        this.dot(x(226), cy, 8, pal.bg, 1);
        this.dot(x(226), cy, 5, pal.accent, 1);
        break;
      default:
        [[191.5, 3, .45], [200, 4, .7], [209.5, 5, 1]].forEach(([c, s, a]) => this.dot(x(c), cy, s / 2, pal.accent, a));
    }
  }

  drawGrid() {
    const g = this.g, pal = this.pal, L = this.layout, kind = L.kind, left = this.left, pos = this.pos, pawl = this.pawl;
    const mir = x => left ? W - x : x;
    const names = this.scrub || this.names, n = names.length, cur = Math.round(pos);
    const unread = this.scrub ? null : this.names.map(a => this.unread.has(a));
    const o = this.open && this.open.grid ? this.open : null;
    const rx = o ? o.rx : 0, ry = o ? o.ry : 0;
    const rr = o ? this.openProgress() * (Math.max(Math.hypot(rx, ry), Math.hypot(W - rx, ry), Math.hypot(rx, HD - ry),
      Math.hypot(W - rx, HD - ry)) + 150) : -1;
    const read = this.a.read, labels = this.a.labels;
    const base = [], dim = [], unlit = [], flood = [], floodRead = [], vary = [];
    const rg = [0, 0, 0];

    // Grows a dot inside the ripple: an accent ring at the front, then the sheet colour.
    const ripple = (x, y, r, a, front, full) => {
      const d = rr - Math.hypot(x - rx, y - ry);
      if (d <= 0) return 0;
      if (d < 30) { rg[0] = Math.max(r, front); rg[1] = Math.max(a, .85); rg[2] = 1; return 1; }
      const q = inOutCubic(c01((d - 30) / 110));
      if (q >= 1) return 2;
      rg[0] = r + (full - r) * q; rg[1] = a + (1 - a) * q; rg[2] = 2;
      return 1;
    };

    const r0 = this.radius, ax = axOf(L), cy = this.cy, cx = ax + r0, step = GAP / r0;
    const tmax = Math.min(6.5, 1.55 / step) * step;
    const lsE = this.ls, lensL = 70 + 60 * lsE, lensK = .9 * lsE;
    const rows = Math.floor((HD - GO) / G) + 2;

    for (let j = 0; j < rows; j++) {
      const y0 = GO + j * G;
      for (let i = 0; i < 34; i++) {
        const x0 = GO + i * G, sx = mir(x0);
        if (y0 > READ_Y - 4 && y0 < READ_Y + 80 && sx > 16 && sx < 390) continue;
        let x = x0, y = y0, r = 1.2, a = .16, col = 0, special = false, inner = false;
        if (kind === 'lens') {
          // The column moves, so its dots are drawn below.
          if (x0 === LX && y0 > LENS_TOP - 6 && y0 < LENS_BOTTOM + 6) continue;
          const dx = x0 - LX, dy = y0 - cy, dd = Math.sqrt(dx * dx + dy * dy);
          if (dd < lensL) {
            const t = 1 - dd / lensL, m = 1 + lensK * t * t;
            x = LX + dx * m;
            y = cy + dy * m;
            const tt = t * t * lsE;
            if (tt > .002) { r = 1.2 * (1 + 1.5 * tt); a = .16 + .3 * tt; special = true; }
          }
        } else {
          const dx = cx - x0, dy = y0 - cy, du = Math.sqrt(dx * dx + dy * dy) - r0;
          if (kind === 'field' && du < -34) inner = true;
          else if (du > -32 && du <= 9 && (du >= -9 || du < -14)) {
            const th = Math.atan2(dy, dx), env = c01(1 - Math.abs(th) / tmax);
            if (env > 0) {
              // A swell travels along the double arc as the dial turns.
              const aa = pos + th / step, dl = Math.abs(aa - Math.round(aa));
              const bump = Math.pow(Math.cos(Math.PI * dl), 8), f = c01(1 - Math.abs(th) / .8), e2 = Math.pow(env, .7);
              if (du >= -9) {
                r = 1.2 + e2 * (.4 + 1.9 * f * f + 1.8 * bump);
                a = .16 + e2 * (.18 + .5 * f + .45 * bump);
                // The readout dial has no names, so an unread app tints its swell.
                if (kind === 'readout' && unread && n > 0 && bump > .35 && unread[mod(Math.round(aa), n)]) col = 1;
              } else {
                r = 1.1 + e2 * (.25 + f * f + bump);
                a = .12 + e2 * (.1 + .3 * f + .3 * bump);
              }
              a = Math.min(a, 1);
              special = true;
            }
          }
          if (x0 === ax && y0 === cy) {
            r = 1.2 + 3.4 + pawl * 1.3;
            a = 1;
            col = 1;
            special = true;
            inner = false;
          }
        }
        const px = mir(x);
        if (rr > 0) {
          const q = ripple(px, y, r, a, 2.4, 8.9);
          if (q === 2) { flood.push(px, y); continue; }
          if (q === 1) { [r, a, col] = rg; special = true; inner = false; }
        }
        if (special) vary.push(px, y, r, a, col);
        else if (inner) dim.push(px, y);
        else base.push(px, y);
      }
    }

    // The lens column: one dot per app and a small dot between two apps. The rows repeat without end and flow
    // through the lens. The selected dot is the notch. The column fades out at both ends.
    if (kind === 'lens' && n > 0) {
      const first = Math.floor(pos + (LENS_TOP - cy) / LENS_GAP) - 1;
      const last = Math.ceil(pos + (LENS_BOTTOM - cy) / LENS_GAP) + 1;
      for (let i = first; i <= last; i++) for (let half = 0; half <= 1; half++) {
        const y0 = cy + (i - pos + half * .5) * LENS_GAP;
        if (y0 < LENS_TOP || y0 > LENS_BOTTOM) continue;
        const dy = y0 - cy, ad = Math.abs(dy);
        let y = y0, tt = 0;
        if (ad < lensL) {
          const t = 1 - ad / lensL;
          y = cy + dy * (1 + lensK * t * t);
          tt = t * t * lsE;
        }
        let r, a, col = 0;
        if (half === 1) {
          r = 1.2 * (1 + 1.5 * tt);
          a = .16 + .3 * tt;
        } else if (i === cur) {
          r = 3.4 + 1.6 * this.ls + pawl;
          a = 1;
          col = 1;
        } else {
          r = 1.8 * (1 + 1.3 * tt);
          a = .5 + .4 * tt;
          if (unread && unread[mod(i, n)]) col = 1;
        }
        a *= c01((y0 - LENS_TOP) / 36) * c01((LENS_BOTTOM - y0) / 36);
        const px = mir(LX);
        if (rr > 0) {
          const q = ripple(px, y, r, a, 2.4, 8.9);
          if (q === 2) { flood.push(px, y); continue; }
          if (q === 1) [r, a, col] = rg;
        }
        vary.push(px, y, r, a, col);
      }
    }

    // The dot-matrix readout.
    const rv = this.readout.v, racc = this.readout.accent;
    for (let jj = 0; jj < RR; jj++) for (let ii = 0; ii < RC; ii++) {
      const idx = jj * RC + ii, x = RX + ii * RF, y = READ_Y + jj * RF, v = rv[idx];
      let r = .85 + 1.75 * v, a = (.08 + .92 * v) * read, col = racc[idx], lit = v > .02;
      if (rr > 0) {
        const q = ripple(x, y, r, a, 1.6, 4.5);
        if (q === 2) { floodRead.push(x, y); continue; }
        if (q === 1) { [r, a, col] = rg; lit = true; }
      }
      if (lit) vary.push(x, y, r, a, col); else unlit.push(x, y);
    }

    const batch = (d, r, a, c) => {
      if (!d.length || a <= 0) return;
      g.globalAlpha = a;
      g.fillStyle = css(c);
      g.beginPath();
      for (let q = 0; q < d.length; q += 2) { g.moveTo(d[q] + r, d[q + 1]); g.arc(d[q], d[q + 1], r, 0, TAU); }
      g.fill();
    };
    batch(base, 1.2, .16, pal.fg);
    batch(dim, 1.1, .08, pal.fg);
    batch(unlit, .85, .08 * read, pal.fg);
    batch(flood, 8.9, 1, pal.sheet);
    batch(floodRead, 4.5, 1, pal.sheet);
    const colors = [css(pal.fg), css(pal.accent), css(pal.sheet)];
    for (let q = 0; q < vary.length; q += 5) {
      g.globalAlpha = c01(vary[q + 3]);
      g.fillStyle = colors[vary[q + 4]];
      g.beginPath();
      g.arc(vary[q], vary[q + 1], vary[q + 2], 0, TAU);
      g.fill();
    }
    g.globalAlpha = 1;

    if (labels > .004 && n > 0) {
      if (kind === 'field') this.drawFieldLabels(names, unread, geo(r0, cy, ax), labels);
      else if (kind === 'lens') {
        for (let i = cur - 8; i <= cur + 8; i++) {
          const dy = (i - pos) * LENS_GAP, ad = Math.abs(dy);
          if (ad >= lensL) continue;
          const t = 1 - ad / lensL, y = cy + dy * (1 + lensK * t * t), on = i === cur;
          const sc = 1 + .7 * t * t * lsE + (on ? .15 : 0);
          const op = Math.min(1, t * 1.8) * (on ? 1 : .55 + .45 * lsE) * labels;
          if (op < .004) continue;
          g.save();
          g.translate(mir(LX - 18 - 8 * t * lsE), y);
          g.scale(sc, sc);
          this.text(names[mod(i, n)], 0, 0, 15, on ? 500 : 400, on ? pal.accent : pal.fg, op, .01, left ? 'left' : 'right');
          g.restore();
        }
      }
    }

    // The date under the readout. The readout layout shows the position of the selected app while it reads a name.
    const date = this.readName && kind === 'readout' && !this.scrub && n > 0
      ? `${pad2(mod(cur, n) + 1)} / ${n} · tap to open` : dateText();
    this.text(date, RX, READ_Y + 92, 12, 400, pal.muted2, read * labels, .04, 'left', 'top');
  }

  drawFieldLabels(names, unread, gg, alpha) {
    const n = names.length, rl = gg.r - 46;
    // The names ride on spokes inside the arc.
    for (let i = 0; i < n; i++) {
      let d = mod(i - this.pos, n);
      if (d > n / 2) d -= n;
      const a = Math.abs(d);
      if (a >= gg.span) continue;
      const cl = Math.max(0, 1 - a), sel = cl > .5, name = names[i];
      const s = this.scrub ? 1 + .9 * cl * cl : 1 + (Math.min(1.4, Math.max(1, 116 / (name.length * CH))) - 1) * cl * cl;
      const p = labelPoseAt(d, name.length, s, gg, rl, this.left);
      const op = Math.pow(1 - a / gg.span, 1.35) * alpha;
      if (op < .004) continue;
      this.label(name, p, s, sel, op);
      if (unread && unread[i]) this.dot(p.bx, p.by, 2.5, this.pal.accent, op);
    }
  }
}
