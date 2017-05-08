'use strict';
import * as PIXI from 'pixi.js';

class PayoutsTab extends PIXI.Sprite {
  private _labelsContainer: PIXI.Container;
  private _labelsLabel: PIXI.Text;
  private _payoutsLabel: PIXI.Text;
  private _isShowingLabels: boolean;

  constructor(contentScale: number) {
    const texture = PIXI.loader.resources['resources/prediction.json'].textures!['Prediction-Oddstab.png'];
    super(texture);

    this._isShowingLabels = true;
    this.scale.set(contentScale, contentScale);

    this._labelsContainer = new PIXI.Container();
    this.addChild(this._labelsContainer);

    this._labelsLabel = new PIXI.Text('Labels'.toUpperCase());
    this._labelsContainer.addChild(this._labelsLabel);

    this._payoutsLabel = new PIXI.Text('Payout'.toUpperCase());
    this._labelsContainer.addChild(this._payoutsLabel);

    this._invalidate();
  }

  selectLabels() {
    this._isShowingLabels = true;
    this._invalidate();
  }

  selectPayouts() {
    this._isShowingLabels = false;
    this._invalidate();
  }

  private _invalidate() {
    const labelsLabel = this._labelsLabel;
    const payoutsLabel = this._payoutsLabel;

    const baseFontStyle = new PIXI.TextStyle({
      fill: 0xffffff,
      fontSize: 104.0,
      fontFamily: 'proxima-nova-excn',
      fontWeight: '900'
    });

    const baseFontMetrics = PIXI.TextMetrics.measureText('X', baseFontStyle);
    const realHeight = baseFontMetrics.fontProperties.ascent - baseFontMetrics.fontProperties.descent;

    labelsLabel.style = baseFontStyle;
    labelsLabel.anchor.set(1.0, 0.0);
    labelsLabel.position.set(this.texture.width - 64.0, 0);

    payoutsLabel.style = baseFontStyle;
    payoutsLabel.anchor.set(1.0, 0.0);
    payoutsLabel.position.set(this.texture.width - 64.0, labelsLabel.position.y + realHeight + 16.0);

    if (this._isShowingLabels) {
      labelsLabel.alpha = 1.0;
      payoutsLabel.alpha = 0.5;
    } else {
      labelsLabel.alpha = 0.5;
      payoutsLabel.alpha = 1.0;
    }

    this._labelsContainer.position.set(0, (this.texture.height - this._labelsContainer.height) / 2);
  }
}

export default PayoutsTab;