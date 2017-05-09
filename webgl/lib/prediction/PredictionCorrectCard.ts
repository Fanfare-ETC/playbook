'use strict';
import PlaybookRenderer from '../PlaybookRenderer';
import { FriendlyNames } from '../PlaybookEvents';
import { IOverlayCard } from '../GenericOverlay';

class PredictionCorrectCard extends PIXI.Container implements IOverlayCard {
  private _contentScale: number;
  private _event: string;
  private _addedScore: number;

  private _ball: PIXI.Sprite;
  private _textContainer: PIXI.Container;
  private _text: PIXI.Text;
  private _scoreContainer: PIXI.Container;
  private _score1: PIXI.Text;
  private _score2: PIXI.Text;
  private _score3: PIXI.Text;

  emitter: PIXI.utils.EventEmitter = new PIXI.utils.EventEmitter();

  constructor(contentScale: number, renderer: PlaybookRenderer, event: string,
              addedScore: number) {
    super();

    this._contentScale = contentScale;
    this._event = event;
    this._addedScore = addedScore;

    this._ball = new PIXI.Sprite(PIXI.loader.resources['resources/Item-Ball-Rotated.png'].texture);
    this.addChild(this._ball);

    this._textContainer = new PIXI.Container();
    this.addChild(this._textContainer);

    this._text = new PIXI.Text();
    this._textContainer.addChild(this._text);

    this._scoreContainer = new PIXI.Container();
    this._textContainer.addChild(this._scoreContainer);

    this._score1 = new PIXI.Text();
    this._scoreContainer.addChild(this._score1);

    this._score2 = new PIXI.Text();
    this._scoreContainer.addChild(this._score2);

    this._score3 = new PIXI.Text();
    this._scoreContainer.addChild(this._score3);

    this.visible = false;
  }

  show() {
    this.visible = true;

    const contentScale = this._contentScale;
    const event = this._event;
    const addedScore = this._addedScore;
    const ball = this._ball;
    const text = this._text;
    const score1 = this._score1;
    const score2 = this._score2;
    const score3 = this._score3;
    const textContainer = this._textContainer;
    const scoreContainer = this._scoreContainer;

    const ballScale = window.innerWidth / ball.texture.width;
    ball.scale.set(ballScale, ballScale);
    ball.position.set(window.innerWidth / 2, window.innerHeight / 2);
    ball.anchor.set(0.5, 0.5);

    const textStyle = new PIXI.TextStyle({
      fontFamily: 'rockwell',
      fontWeight: 'bold',
      fontSize: 104.0 * contentScale,
      align: 'center',
      fill: 0x002b65
    });

    text.text = `Prediction Correct:\n ${FriendlyNames[event]}\n`;
    text.style = textStyle;
    const textMetrics = PIXI.TextMetrics.measureText(text.text, text.style);

    score1.text = 'You got ';
    score1.style = textStyle;
    score1.position.set(0, textMetrics.height);
    const textMetricsScore1 = PIXI.TextMetrics.measureText(score1.text, score1.style);

    score2.text = addedScore.toString();
    score2.style.fontFamily = 'SCOREBOARD';
    score2.style.fontSize = 104.0 * contentScale;
    score2.style.align = 'center';
    const textMetricsScore2 = PIXI.TextMetrics.measureText(score2.text, score2.style);
    score2.position.set(
      textMetricsScore1.width,
      textMetrics.height + (textMetricsScore1.height - textMetricsScore2.height - textMetrics.fontProperties.descent)
    );

    score3.text = ` ${addedScore > 1 ? 'points' : 'point'}!`;
    score3.style = textStyle;
    score3.position.set(textMetricsScore1.width + textMetricsScore2.width, textMetrics.height);

    textContainer.position.set(
      window.innerWidth / 2 - textContainer.width / 2,
      window.innerHeight / 2 - textContainer.height / 2
    );

    // Reposition the text to the center of the window.
    const center = new PIXI.Point(window.innerWidth / 2, window.innerHeight / 2);
    const scoreContainerCenter = scoreContainer.toLocal(center);
    scoreContainer.position.x = scoreContainerCenter.x - (scoreContainer.width / 2);
  }
}

export default PredictionCorrectCard;