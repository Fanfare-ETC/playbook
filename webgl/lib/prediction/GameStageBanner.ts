'use strict';
export interface IGameStageBannerLayoutParams {
  height: number;
}

class GameStageBanner extends PIXI.Graphics {
  private _label: PIXI.Text;

  constructor(contentScale: number, layoutParams: IGameStageBannerLayoutParams) {
    super();

    this.beginFill(0xc53626);
    this.drawRect(0, 0, window.innerWidth, layoutParams.height);
    this.endFill();
    this.position.set(0, window.innerHeight - layoutParams.height);

    this._label = new PIXI.Text();
    this._label.style.fontFamily = 'proxima-nova-excn';
    this._label.style.fontWeight = '900';
    this._label.style.fontSize = 104.0 * contentScale;
    this._label.style.fill = 0xffffff;
    this._label.anchor.set(0.5, 0.5);
    this._label.position.set(window.innerWidth / 2, this.height / 2);
    this.addChild(this._label);

    this.visible = false;
  }

  set text(value: string) {
    this._label.text = value;
  }
}

export default GameStageBanner;