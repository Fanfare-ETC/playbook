'use strict';
import * as PIXI from 'pixi.js';
import 'pixi-action';
import Trophy from './Trophy';
import GoalTypesMetadata from './GoalTypesMetadata';

class SetScoredOverlay extends PIXI.Container {
  private _contentScale: number;

  private _background: PIXI.Graphics;
  private _innerContainer: PIXI.Container;
  private _title: PIXI.Text;
  private _score: PIXI.Text;
  private _trophy: Trophy;

  constructor(contentScale: number) {
    super();

    this.interactive = true;
    this.on('tap', () => this.hide());
    this._contentScale = contentScale;

    this._background = new PIXI.Graphics();
    this.addChild(this._background);

    this._innerContainer = new PIXI.Container();
    this.addChild(this._innerContainer);

    this._title = new PIXI.Text();
    this._innerContainer.addChild(this._title);

    this._trophy = new Trophy();
    this._innerContainer.addChild(this._trophy);

    this._score = new PIXI.Text();
    this._innerContainer.addChild(this._score);

    this.visible = false;
  }

  show(goal: string) {
    this.visible = true;
    this.alpha = 1.0;

    const contentScale = this._contentScale;
    const background = this._background;
    const innerContainer = this._innerContainer;
    const title = this._title;
    const trophy = this._trophy;
    const score = this._score;

    background.beginFill(0xffffff);
    background.drawRect(0, 0, window.innerWidth, window.innerHeight);
    background.endFill();

    title.position.set(0.0, 0.0);
    title.anchor.set(0.5, 0);
    title.text = 'You Completed a Set!';
    title.style.fill = 0x002b65;
    title.style.fontFamily = 'rockwell';
    title.style.fontWeight = 'bold';
    title.style.fontSize = 96.0 * contentScale;
    title.style.align = 'center';
    title.style.wordWrap = true;
    title.style.wordWrapWidth = window.innerWidth - 128.0 * contentScale;

    trophy.goal = goal!;
    trophy.width = trophy.texture.width * contentScale * 2.0;
    trophy.height = trophy.texture.height * contentScale * 2.0;
    trophy.position.set(0.0, title.position.y + title.height + 64.0 * contentScale);
    trophy.anchor.set(0.5, 0);

    if (goal) {
      score.position.set(0.0, trophy.position.y + trophy.height + 64.0 * contentScale);
      score.anchor.set(0.5, 0);
      score.text = GoalTypesMetadata[goal!].score.toString();
      score.style.fill = 0x002b65;
      score.style.fontFamily = 'SCOREBOARD';
      score.style.fontSize = 256.0 * contentScale;
      score.style.align = 'center';
    }

    innerContainer.position.set(
      (window.innerWidth - innerContainer.width) / 2,
      (window.innerHeight - innerContainer.height) / 2
    );

    // Relayout horizontally after measurement.
    const center = innerContainer.width / 2;
    title.position.x = center;
    trophy.position.x = center;
    score.position.x = center;

    // Perform animation.
    const alphaTo = new PIXI.action.AlphaTo(0.75, 1.5);
    PIXI.actionManager.runAction(background, alphaTo);

    innerContainer.alpha = 0;
    innerContainer.position.y += innerContainer.height / 2;
    const fadeIn = new PIXI.action.FadeIn(0.5);
    const moveBy = new PIXI.action.MoveBy(0.0, -innerContainer.height / 2, 0.5);
    PIXI.actionManager.runAction(innerContainer, fadeIn);
    PIXI.actionManager.runAction(innerContainer, moveBy);
  }

  hide() {
    const fadeOut = new PIXI.action.FadeOut(0.5);
    PIXI.actionManager.runAction(this, fadeOut);
  }
}

export default SetScoredOverlay;