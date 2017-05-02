'use strict';
import ICard from './ICard';
import Trophy from './Trophy';
import GoalTypesMetadata from './GoalTypesMetadata';

class SetScoredCard extends PIXI.Container {
  constructor(contentScale: number, goal: string, cardSet: ICard[]) {
    super();

    const title = new PIXI.Text();
    this.addChild(title);

    const cardsContainer = new PIXI.Container();
    this.addChild(cardsContainer);

    const trophy = new Trophy();
    this.addChild(trophy);

    const scoreContainer = new PIXI.Container();
    this.addChild(scoreContainer);

    const score = new PIXI.Text();
    scoreContainer.addChild(score);

    const scorePointsEarned = new PIXI.Text();
    scoreContainer.addChild(scorePointsEarned);

    title.position.set(0.0, 0.0);
    title.anchor.set(0.5, 0);
    title.text = 'You Completed a Set!';
    title.style.fontFamily = 'rockwell';
    title.style.fontWeight = 'bold';
    title.style.fontSize = 96.0 * contentScale;
    title.style.align = 'center';
    title.style.wordWrap = true;
    title.style.wordWrapWidth = window.innerWidth - 128.0 * contentScale;

    cardSet.forEach((card, i) => {
      const replica = new PIXI.Sprite(card.sprite.texture);
      replica.rotation = ((cardSet.length - 1) * -3.5 + (i * 7.0)) * PIXI.DEG_TO_RAD;
      replica.scale.set(card.sprite.scale.x * 2.0, card.sprite.scale.y * 2.0);
      replica.position.set(0.0, replica.height);
      replica.anchor.set(0.0, 1.0);
      cardsContainer.addChild(replica);
    });
    cardsContainer.position.set(0.0, title.position.y + title.height + 128.0 * contentScale);

    trophy.goal = goal!;
    trophy.width = trophy.texture.width * contentScale * 2.0;
    trophy.height = trophy.texture.height * contentScale * 2.0;
    trophy.scale.set(contentScale, contentScale);
    trophy.anchor.set(1.0, 1.0);
    trophy.position.set(0.0, cardsContainer.position.y + cardsContainer.height);

    scoreContainer.position.set(0.0, trophy.position.y + 64.0 * contentScale);

    score.position.set(0.0, 0.0);
    score.text = GoalTypesMetadata[goal!].score.toString();
    score.style.fill = 0x002b65;
    score.style.fontFamily = 'SCOREBOARD';
    score.style.fontSize = 256.0 * contentScale;
    score.style.align = 'center';
    const scoreMetrics = PIXI.TextMetrics.measureText(score.text, score.style);

    scorePointsEarned.text = ' points earned.';
    scorePointsEarned.style.fill = 0x002b65;
    scorePointsEarned.style.fontFamily = 'rockwell';
    scorePointsEarned.style.fontWeight = 'bold';
    scorePointsEarned.style.fontSize = 96.0 * contentScale;
    scorePointsEarned.style.align = 'center';
    const scorePointsEarnedMetrics = PIXI.TextMetrics.measureText(scorePointsEarned.text, scorePointsEarned.style);
    scorePointsEarned.position.set(scoreMetrics.width, scoreMetrics.height / 2 - scorePointsEarnedMetrics.height / 2);

    // Relayout after measurement.
    scoreContainer.pivot.x = scoreContainer.width / 2;

    // Relayout horizontally after measurement.
    const center = this.width / 2;
    title.position.x = center;
    cardsContainer.position.x = center - cardsContainer.width / 2;
    trophy.position.x = cardsContainer.position.x + cardsContainer.width;
    scoreContainer.position.x = center;
  }
}

export default SetScoredCard;