'use strict';

class ScoreTab extends PIXI.Sprite {
  private _contentScale: number;
  private _labelsContainer: PIXI.Container;
  private _scoreLabel: PIXI.Text;
  private _score: PIXI.Text;

  constructor(contentScale: number) {
    const texture = PIXI.loader.resources['resources/prediction.json'].textures!['Prediction-Scoretab.png'];
    super(texture);

    this._contentScale = contentScale;
    this.scale.set(contentScale, contentScale);

    this._labelsContainer = new PIXI.Container();
    this.addChild(this._labelsContainer);

    this._scoreLabel = new PIXI.Text('Score:'.toUpperCase());
    this._labelsContainer.addChild(this._scoreLabel);

    this._score = new PIXI.Text('000');
    this._labelsContainer.addChild(this._score);

    this._invalidate();
  }

  get score() {
    return this._score;
  }

  private _invalidate() {
    const contentScale = this._contentScale;
    const scoreLabel = this._scoreLabel;
    const score = this._score;

    scoreLabel.style.fill = 0xffffff;
    scoreLabel.style.fontSize = 104.0;
    scoreLabel.style.fontFamily = 'proxima-nova-excn';
    scoreLabel.style.fontWeight = '900';
    scoreLabel.position.set(64.0 * contentScale, 0);

    const baseFontMetrics = PIXI.TextMetrics.measureText('X', scoreLabel.style);
    const realHeight = baseFontMetrics.height - baseFontMetrics.fontProperties.descent;

    score.style.fill = 0xffffff;
    score.style.fontSize = 104.0;
    score.style.fontFamily = 'SCOREBOARD';
    score.position.set(64.0 * contentScale, scoreLabel.position.y + realHeight + 16.0);

    // The score font has to ascent, so to vertical align this, copy the descent
    // from the base font.
    const bottomPadding = baseFontMetrics.height - baseFontMetrics.fontProperties.ascent;
    this._labelsContainer.position.set(0, (this.texture.height - this._labelsContainer.height - bottomPadding) / 2);
  }
}

export default ScoreTab;