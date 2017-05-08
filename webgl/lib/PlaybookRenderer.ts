'use strict';
import * as PIXI from 'pixi.js';
import * as particles from 'pixi-particles';

class PlaybookRenderer {
  renderer: PIXI.CanvasRenderer | PIXI.WebGLRenderer;
  emitters: particles.Emitter[] = [];

  _dirty: boolean = false;

  constructor() {
    this.renderer = PIXI.autoDetectRenderer(1080, 1920, { resolution: window.devicePixelRatio });
  }

  render(displayObject: PIXI.DisplayObject) {
    this.renderer.render(displayObject);
    this._dirty = false;
  }

  markDirty() {
    this._dirty = true;
  }

  get dirty() {
    return this._dirty;
  }
}

export default PlaybookRenderer;