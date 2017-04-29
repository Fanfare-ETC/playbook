'use strict';
import * as PIXI from 'pixi.js';

interface ContainerParams {
  width: number,
  height: number,
  trayHeight: number,
  trayEdgeHeight: number
}

class TrayTip extends PIXI.Container {
  _contentScale: number;
  _containerParams: ContainerParams;
  _text: string = '';

  _background: PIXI.Graphics;
  _shadow: PIXI.extras.TilingSprite;
  _label: PIXI.Text;

  constructor(contentScale: number, containerParams: ContainerParams) {
    super();

    this._contentScale = contentScale;
    this._containerParams = containerParams;

    this._background = new PIXI.Graphics();
    this.addChild(this._background);

    const shadowTexture = PIXI.loader.resources['resources/Collection-Shadow-9x16.png'].texture;
    const shadowHeight = shadowTexture.height * contentScale;
    this._shadow = new PIXI.extras.TilingSprite(shadowTexture, window.innerWidth, shadowHeight);
    this.addChild(this._shadow);

    this._label = new PIXI.Text();
    this.addChild(this._label);

    this._invalidate();
  }

  set text(value: string) {
    this._text = value;
    this._invalidate();
  }

  private _invalidate() {
    const contentScale = this._contentScale;
    const containerParams = this._containerParams;
    const background = this._background;
    const shadow = this._shadow;
    const label = this._label;

    background.clear();
    background.beginFill(0x000000, 0.5);
    background.drawRect(0, 0, window.innerWidth, window.innerHeight - containerParams.trayHeight - containerParams.height);
    background.endFill();
    background.beginFill(0x00bf00);
    background.drawRect(
      0, window.innerHeight - containerParams.trayHeight - containerParams.height,
      window.innerWidth, containerParams.trayEdgeHeight
    );
    background.endFill();
    background.beginFill(0x007f00);
    background.drawRect(
      0, window.innerHeight - containerParams.trayHeight - containerParams.height + containerParams.trayEdgeHeight,
      window.innerWidth, containerParams.height
    );
    background.endFill();

    shadow.anchor.set(0.0, 1.0);
    shadow.tileScale.set(1.0, contentScale);

    label.position.set(
      window.innerWidth / 2,
      (window.innerHeight - containerParams.trayHeight - containerParams.height) + containerParams.height / 2 + containerParams.trayEdgeHeight
    );
    label.anchor.set(0.5, 0.5);
    label.text = this._text.toUpperCase();
    label.style.fontFamily = 'proxima-nova-excn';
    label.style.fill = 0xffffff;
    label.style.fontWeight = '900';
    label.style.fontSize = 104 * contentScale;
  }
}

export default TrayTip;