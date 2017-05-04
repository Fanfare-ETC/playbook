'use strict';
import { FriendlyNames as PlaybookEventsFriendlyNames } from '../PlaybookEvents';

/**
 * Prediction correct overlay.
 */
class PredictionCorrectOverlay extends PIXI.Container {
  background: PIXI.Graphics;
  ball: PIXI.Sprite;
  textContainer: PIXI.Container;
  text: PIXI.Text;
  scoreContainer: PIXI.Container;
  score1: PIXI.Text;
  score2: PIXI.Text;
  score3: PIXI.Text;

  constructor(contentScale: number, event: string, addedScore: number) {
    super();

    this.background = new PIXI.Graphics();
    this.background.beginFill(0x000000, 0.75);
    this.background.drawRect(0, 0, window.innerWidth, window.innerHeight);
    this.background.endFill();
    this.addChild(this.background);

    this.ball = new PIXI.Sprite(PIXI.loader.resources['resources/Item-Ball-Rotated.png'].texture);
    const ballScale = window.innerWidth / this.ball.texture.width;
    this.ball.scale.set(ballScale, ballScale);
    this.ball.position.set(window.innerWidth / 2, window.innerHeight / 2);
    this.ball.anchor.set(0.5, 0.5);
    this.addChild(this.ball);

    const textStyle = new PIXI.TextStyle({
      fontFamily: 'rockwell',
      fontWeight: 'bold',
      fontSize: 72.0 * contentScale,
      align: 'center',
      fill: 0x002b65
    });

    this.textContainer = new PIXI.Container();

    this.text = new PIXI.Text();
    this.text.text = `Prediction Correct:\n ${PlaybookEventsFriendlyNames[event]}\n`;
    this.text.style = textStyle;
    const textMetrics = PIXI.TextMetrics.measureText(this.text.text, this.text.style);
    this.textContainer.addChild(this.text);

    this.scoreContainer = new PIXI.Container();

    this.score1 = new PIXI.Text();
    this.score1.text = 'You got ';
    this.score1.style = textStyle;
    this.score1.position.set(0, textMetrics.height);
    const textMetricsScore1 = PIXI.TextMetrics.measureText(this.score1.text, this.score1.style);
    this.scoreContainer.addChild(this.score1);

    this.score2 = new PIXI.Text();
    this.score2.text = addedScore.toString();
    this.score2.style.fontFamily = 'SCOREBOARD';
    this.score2.style.fontSize = 72.0 * contentScale;
    this.score2.style.align = 'center';
    const textMetricsScore2 = PIXI.TextMetrics.measureText(this.score2.text, this.score2.style);
    this.score2.position.set(
      textMetricsScore1.width,
      textMetrics.height + (textMetricsScore1.height - textMetricsScore2.height - textMetrics.fontProperties.descent)
    );
    this.scoreContainer.addChild(this.score2);

    this.score3 = new PIXI.Text();
    this.score3.text = ` ${addedScore > 1 ? 'points' : 'point'}!`;
    this.score3.style = textStyle;
    this.score3.position.set(textMetricsScore1.width + textMetricsScore2.width, textMetrics.height);
    this.scoreContainer.addChild(this.score3);

    this.textContainer.addChild(this.scoreContainer);
    this.textContainer.position.set(
      window.innerWidth / 2 - this.textContainer.width / 2,
      window.innerHeight / 2 - this.textContainer.height / 2
    );
    this.addChild(this.textContainer);

    // Reposition the text to the center of the window.
    const center = new PIXI.Point(window.innerWidth / 2, window.innerHeight / 2);
    const scoreContainerCenter = this.scoreContainer.toLocal(center);
    this.scoreContainer.position.x = scoreContainerCenter.x - (this.scoreContainer.width / 2);
  }
}

export default PredictionCorrectOverlay;