'use strict';
import * as PIXI from 'pixi.js';
import IGameState from './IGameState';

export interface IDiscardBarLayoutParams {
  whiteBanner: PIXI.Rectangle;
  height: number;
  heightExpanded: number;
}

class DiscardBar extends PIXI.Container {
  _contentScale: number;
  _expanded: boolean = false;

  _shadow: PIXI.extras.TilingSprite;
  _background: PIXI.Graphics;
  _label: PIXI.Text;

  constructor(state: IGameState, contentScale: number) {
    super();

    this._contentScale = contentScale;

    this._background = new PIXI.Graphics();
    this.addChild(this._background);

    this._label = new PIXI.Text();
    this.addChild(this._label);

    const shadowTexture = PIXI.loader.resources['resources/Collection-Shadow-Overturn.png'].texture;
    const shadowHeight = shadowTexture.height * contentScale;
    this._shadow = new PIXI.extras.TilingSprite(shadowTexture, window.innerWidth, shadowHeight);
    this.addChild(this._shadow);

    this._invalidate();
  }

  expand() {
    this._expanded = true;
    this._invalidate();
  }

  contract() {
    this._expanded = false;
    this._invalidate();
  }

  getChildrenLayoutParams() : IDiscardBarLayoutParams {
    const contentScale = this._contentScale;
    return {
      whiteBanner: new PIXI.Rectangle(0, 0, window.innerWidth, 32.0 * contentScale),
      height: 32.0 * contentScale,
      heightExpanded: 128.0 * contentScale
    };
  }

  private _invalidate() {
    const contentScale = this._contentScale;
    const expanded = this._expanded;
    const shadow = this._shadow;
    const background = this._background;
    const label = this._label;

    const layoutParams = this.getChildrenLayoutParams();
    const backgroundHeight = (expanded ? layoutParams.heightExpanded : layoutParams.height);
    const whiteBannerHeight = layoutParams.whiteBanner.height;

    background.clear();
    background.beginFill(0xffffff);
    background.drawRect(0, 0, window.innerWidth, whiteBannerHeight);
    background.endFill();
    background.beginFill(0x002b65);
    background.drawRect(0, whiteBannerHeight, window.innerWidth, backgroundHeight);
    background.endFill();

    label.position.set(window.innerWidth / 2, whiteBannerHeight + backgroundHeight / 2);
    label.anchor.set(0.5, 0.5);
    label.visible = expanded;
    label.text = 'drag cards up to discard'.toUpperCase();
    label.style.fontFamily = 'proxima-nova-excn';
    label.style.fill = 0xffffff;
    label.style.fontWeight = '900';
    label.style.fontSize = 104 * contentScale;
    label.style.align = 'center';

    shadow.anchor.set(0.0, 0.0);
    shadow.position.set(0, backgroundHeight + whiteBannerHeight);
    shadow.tileScale.set(contentScale);
  }
}

export default DiscardBar;