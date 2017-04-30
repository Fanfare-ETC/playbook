'use strict';
import * as PIXI from 'pixi.js';
import 'pixi-action';
import ICard from './ICard';
import GoalChoice from './GoalChoice';

class GhostCards extends PIXI.Container {
  private _cards: ICard[] | null = null;
  private _goalTile: GoalChoice | null = null;
  private _repeatInterval: number | null = null;

  constructor() {
    super();
    this._invalidate();
  }

  set cards(value: ICard[] | null) {
    this._cards = value;
    this._invalidate();
  }

  set goalTile(value: GoalChoice | null) {
    this._goalTile = value;
    this._invalidate();
  }

  private _invalidate() {
    const cards = this._cards;
    const goalTile = this._goalTile;

    if (this._repeatInterval !== null) {
      clearInterval(this._repeatInterval);
    }

    this.removeChildren();
    if (cards === null || goalTile === null) { return; }

    for (const card of cards) {
      const ghostCard = new PIXI.Sprite(card.sprite.texture);
      ghostCard.scale.set(card.sprite.scale.x, card.sprite.scale.y);
      ghostCard.anchor.set(0.5, 0.5);
      ghostCard.visible = false;

      this._repeatInterval = setInterval(() => {
        const moveTo = new PIXI.action.MoveTo(
          goalTile.position.x + goalTile.width / 2,
          goalTile.position.y + goalTile.height / 2,
          0.5
        );
        const fadeOut = new PIXI.action.FadeOut(0.5);

        ghostCard.visible = true;
        ghostCard.alpha = 0.5;
        ghostCard.position.set(card.sprite.position.x, card.sprite.position.y);
        PIXI.actionManager.runAction(ghostCard, moveTo);
        PIXI.actionManager.runAction(ghostCard, fadeOut);
      }, 1500);

      this.addChild(ghostCard);
    }

    console.log(this.children);
  }
}

export default GhostCards;