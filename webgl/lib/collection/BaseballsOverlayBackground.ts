'use strict';
import * as particles from 'pixi-particles';
import PlaybookRenderer from '../PlaybookRenderer';
import { IOverlayBackground } from '../GenericOverlay';

class BaseballsOverlayBackground extends PIXI.Container implements IOverlayBackground {
  private _renderer: PlaybookRenderer;
  private _background: PIXI.Graphics;
  private _emitter: particles.Emitter;

  constructor(renderer: PlaybookRenderer) {
    super();

    this._renderer = renderer;

    this._background = new PIXI.Graphics();
    this.addChild(this._background);

    this._emitter = new particles.Emitter(this,
      [PIXI.loader.resources['resources/Item-Ball.png'].texture], {
					alpha: {
						start: 1,
						end: 1
					},
					scale: {
						start: 0.75,
						end: 1.0,
						minimumScaleMultiplier: 0.5
					},
					color: {
						start: 'ffffff',
						end: 'ffffff'
					},
					speed: {
						start: 200,
						end: 200
					},
					startRotation: {
						min: 80,
						max: 100
					},
					rotationSpeed: {
						min: 0,
						max: 50
					},
					lifetime: {
						min: 3.5,
						max: 4
					},
					blendMode: 'normal',
					frequency: 0.08,
					emitterLifetime: 0,
					maxParticles: 500,
					pos: {
						x: 0,
						y: 0
					},
					addAtBack: false,
					spawnType: 'rect',
					spawnRect: {
						x: 0,
						y: -64.0,
						w: window.innerWidth,
						h: 0
					}
      });
  }

  onShow() {
    const renderer = this._renderer;
    const background = this._background;
    const emitter = this._emitter;

    emitter.cleanup();
    renderer.emitters.push(emitter);
    emitter.emit = true;

    background.clear();
    background.beginFill(0x000000, 0.75);
    background.drawRect(0, 0, window.innerWidth, window.innerHeight);
    background.endFill();
  }

  onHide() {
    this._renderer.emitters.splice(this._renderer.emitters.indexOf(this._emitter), 1);
    this._emitter.emit = false;
  }
}

export default BaseballsOverlayBackground;