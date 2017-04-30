'use strict';
import * as PIXI from 'pixi.js';
import 'pixi-action';
import Trophy from './Trophy';
import GoalTypesMetadata from './GoalTypesMetadata';
import PlaybookRenderer from './PlaybookRenderer';

class SetScoredOverlay extends PIXI.Container {
  private _contentScale: number;
  private _renderer: PlaybookRenderer;

  private _background: PIXI.Graphics;
  private _innerContainer: PIXI.Container;
  private _innerBackground: PIXI.Graphics;
  private _title: PIXI.Text;
  private _score: PIXI.Text;
  private _trophy: Trophy;

  constructor(contentScale: number, renderer: PlaybookRenderer) {
    super();

    this._contentScale = contentScale;
    this._renderer = renderer;

    this._background = new PIXI.Graphics();
    this.addChild(this._background);

    this._innerContainer = new PIXI.Container();
    this.addChild(this._innerContainer);

    this._innerBackground = new PIXI.Graphics();
    this._innerContainer.addChild(this._innerBackground);

    this._title = new PIXI.Text();
    this._innerContainer.addChild(this._title);

    this._trophy = new Trophy();
    this._innerContainer.addChild(this._trophy);

    this._score = new PIXI.Text();
    this._innerContainer.addChild(this._score);

    this._initEvents();
    this.visible = false;
  }

  private _initEvents() {
    this.interactive = true;
    const innerContainer = this._innerContainer;

    let origPosition: PIXI.Point | null = null;
    let startPosition: PIXI.Point | null = null;
    let startOffset: PIXI.Point | null = null;

    const onTouchStart = (e: PIXI.interaction.InteractionEvent) => {
      origPosition = new PIXI.Point(innerContainer.position.x, innerContainer.position.y);
      startPosition = new PIXI.Point(e.data.global.x, e.data.global.y);
      startOffset = e.data.getLocalPosition(innerContainer);
      startOffset.x -= innerContainer.width / 2;
      startOffset.y -= innerContainer.height / 2;
    }

    const onTouchMove = (e: PIXI.interaction.InteractionEvent) => {
      if (startPosition === null || startOffset === null) { return; }
      const deltaX = e.data.global.x - startPosition.x;
      const angle = deltaX / window.innerWidth * 100;
      innerContainer.rotation = angle * PIXI.DEG_TO_RAD;
      innerContainer.position.set(
        e.data.global.x - startOffset.x,
        e.data.global.y - startOffset.y
      );
      this._renderer.markDirty();
    }

    const onTouchEnd = (e: PIXI.interaction.InteractionEvent) => {
      if (origPosition === null) { return; }

      const bounds = new PIXI.Rectangle(
        window.innerWidth / 4, window.innerHeight / 4,
        window.innerWidth / 2, window.innerHeight / 2
      );

      if (bounds.contains(e.data.global.x, e.data.global.y)) {
        const rotateTo = new PIXI.action.RotateTo(0, 0.25);
        const moveTo = new PIXI.action.MoveTo(origPosition.x, origPosition.y, 0.25);
        PIXI.actionManager.runAction(innerContainer, rotateTo);
        PIXI.actionManager.runAction(innerContainer, moveTo);
      } else {
        this.hide();
      }
    }

    innerContainer.interactive = true;
    innerContainer
      .on('touchstart', onTouchStart)
      .on('touchmove', onTouchMove)
      .on('touchend', onTouchEnd)
      .on('touchendoutside', onTouchEnd)
      .on('touchcancel', onTouchEnd);
  }

  show(goal: string) {
    this.visible = true;
    this.alpha = 1.0;

    const contentScale = this._contentScale;
    const background = this._background;
    const innerContainer = this._innerContainer;
    const innerBackground = this._innerBackground;
    const title = this._title;
    const trophy = this._trophy;
    const score = this._score;

    // Needs to be cleared early to determine correct width for inner container.
    innerBackground.clear();

    background.clear();
    background.beginFill(0x000000);
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

    innerContainer.pivot.set(innerContainer.width / 2, innerContainer.height / 2);
    innerContainer.rotation = 0;
    innerContainer.position.set(
      (window.innerWidth) / 2,
      (window.innerHeight) / 2
    );

    // Relayout horizontally after measurement.
    const center = innerContainer.width / 2;
    title.position.x = center;
    trophy.position.x = center;
    score.position.x = center;

    innerBackground.beginFill(0xffffff);
    innerBackground.drawRoundedRect(
      -64.0 * contentScale, -64.0 * contentScale,
      innerContainer.width + 128.0 * contentScale,
      innerContainer.height + 128.0 * contentScale,
      64.0 * contentScale
    );
    innerBackground.endFill();

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
    const callFunc = new PIXI.action.CallFunc(() => this.visible = false);
    const sequence = new PIXI.action.Sequence([fadeOut, callFunc]);
    PIXI.actionManager.runAction(this, sequence);
  }
}

export default SetScoredOverlay;