'use strict';
import * as PIXI from 'pixi.js';
import 'pixi-action';
import * as particles from 'pixi-particles';
import Trophy from './Trophy';
import GoalTypesMetadata from './GoalTypesMetadata';
import PlaybookRenderer from './PlaybookRenderer';
import ICard from './ICard';

/**
 * A dismissable card is a UI element that can be dismissed from the screen
 * by means of swiping away.
 */
class DismissableCard extends PIXI.Container {
  protected _renderer: PlaybookRenderer;
  private _onDismiss: () => void;

  constructor(renderer: PlaybookRenderer, onDismiss: () => void) {
    super();
    this._renderer = renderer;
    this._onDismiss = onDismiss;
    this._initEvents();
  }

  private _initEvents() {
    let origPosition: PIXI.Point | null = null;
    let startPosition: PIXI.Point | null = null;
    let startOffset: PIXI.Point | null = null;

    const onTouchStart = (e: PIXI.interaction.InteractionEvent) => {
      origPosition = new PIXI.Point(this.position.x, this.position.y);
      startPosition = new PIXI.Point(e.data.global.x, e.data.global.y);
      startOffset = e.data.getLocalPosition(this);
      startOffset.x -= this.width / 2;
      startOffset.y -= this.height / 2;
    }

    const onTouchMove = (e: PIXI.interaction.InteractionEvent) => {
      if (startPosition === null || startOffset === null) { return; }
      const deltaX = e.data.global.x - startPosition.x;
      const angle = deltaX / window.innerWidth * 100;
      this.rotation = angle * PIXI.DEG_TO_RAD;
      this.position.set(
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
        PIXI.actionManager.runAction(this, rotateTo);
        PIXI.actionManager.runAction(this, moveTo);
      } else {
        this._onDismiss();
        this._dismiss();
      }
    }

    this.interactive = true;
    this.on('touchstart', onTouchStart)
      .on('touchmove', onTouchMove)
      .on('touchend', onTouchEnd)
      .on('touchendoutside', onTouchEnd)
      .on('touchcancel', onTouchEnd);
  }

  private _dismiss() {
    const fadeOut = new PIXI.action.FadeOut(0.5);
    PIXI.actionManager.runAction(this, fadeOut);
  }
}

class SetScoredCard extends DismissableCard {
  private _contentScale: number;

  private _background: PIXI.Graphics;
  private _title: PIXI.Text;
  private _trophy: Trophy;
  private _cardsContainer: PIXI.Container;
  private _scoreContainer: PIXI.Container;
  private _score: PIXI.Text;
  private _scorePointsEarned: PIXI.Text;

  constructor(contentScale: number, renderer: PlaybookRenderer,
              onDismiss: () => void) {
    super(renderer, onDismiss);

    this._contentScale = contentScale;

    this._background = new PIXI.Graphics();
    this.addChild(this._background);

    this._title = new PIXI.Text();
    this.addChild(this._title);

    this._cardsContainer = new PIXI.Container();
    this.addChild(this._cardsContainer);

    this._trophy = new Trophy();
    this.addChild(this._trophy);

    this._scoreContainer = new PIXI.Container();
    this.addChild(this._scoreContainer);

    this._score = new PIXI.Text();
    this._scoreContainer.addChild(this._score);

    this._scorePointsEarned = new PIXI.Text();
    this._scoreContainer.addChild(this._scorePointsEarned);
  }

  show(goal: string, cardSet: ICard[]) {
    const contentScale = this._contentScale;

    const background = this._background;
    const title = this._title;
    const trophy = this._trophy;
    const cardsContainer = this._cardsContainer;
    const scoreContainer = this._scoreContainer;
    const score = this._score;
    const scorePointsEarned = this._scorePointsEarned;

    // Needs to be cleared early to determine correct width for this container.
    background.clear();

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

    // Show cards in a fan.
    cardsContainer.removeChildren();
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

    if (goal) {
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
    }

    this.pivot.set(this.width / 2, this.height / 2);
    this.rotation = 0;
    this.position.set(
      (window.innerWidth) / 2,
      (window.innerHeight) / 2
    );

    // Relayout horizontally after measurement.
    const center = this.width / 2;
    title.position.x = center;
    cardsContainer.position.x = center - cardsContainer.width / 2;
    trophy.position.x = cardsContainer.position.x + cardsContainer.width;
    scoreContainer.position.x = center;

    background.beginFill(0xffffff);
    background.drawRoundedRect(
      -64.0 * contentScale, -64.0 * contentScale,
      this.width + 128.0 * contentScale,
      this.height + 128.0 * contentScale,
      64.0 * contentScale
    )
    background.endFill();

    // Perform animation.
    this.alpha = 0;
    this.position.y += this.height / 2;
    const fadeIn = new PIXI.action.FadeIn(0.5);
    const moveBy = new PIXI.action.MoveBy(0.0, -this.height / 2, 0.5);
    PIXI.actionManager.runAction(this, fadeIn);
    PIXI.actionManager.runAction(this, moveBy);
  }
}

class NewTrophyCard extends DismissableCard {
  private _contentScale: number;
  private _background: PIXI.Graphics;
  private _title: PIXI.Text;
  private _trophy: Trophy;

  constructor(contentScale: number, renderer: PlaybookRenderer,
              onDismiss: () => void) {
    super(renderer, onDismiss);

    this._contentScale = contentScale;

    this._background = new PIXI.Graphics();
    this.addChild(this._background);

    this._title = new PIXI.Text();
    this.addChild(this._title);

    this._trophy = new Trophy();
    this.addChild(this._trophy);
  }

  show(goal: string) {
    const contentScale = this._contentScale;
    const background = this._background;
    const title = this._title;
    const trophy = this._trophy;

    background.clear();

    title.position.set(0.0, 0.0);
    title.anchor.set(0.5, 0);
    title.text = 'Congratulations!\nYou got a new trophy!';
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
    trophy.scale.set(contentScale, contentScale);
    trophy.anchor.set(0.5, 0.0);
    trophy.position.set(0.0, title.position.y + title.height + 64.0 * contentScale);

    this.pivot.set(this.width / 2, this.height / 2);
    this.rotation = 0;
    this.position.set(
      (window.innerWidth) / 2,
      (window.innerHeight) / 2
    );

    // Relayout horizontally after measurement.
    const center = this.width / 2;
    title.position.x = center;
    trophy.position.x = center;

    background.beginFill(0xffffff);
    background.drawRoundedRect(
      -64.0 * contentScale, -64.0 * contentScale,
      this.width + 128.0 * contentScale,
      this.height + 128.0 * contentScale,
      64.0 * contentScale
    );
    background.endFill();

    // Perform animation.
    this.alpha = 0;
    this.position.y += this.height / 2;
    const fadeIn = new PIXI.action.FadeIn(0.5);
    const moveBy = new PIXI.action.MoveBy(0.0, -this.height / 2, 0.5);
    PIXI.actionManager.runAction(this, fadeIn);
    PIXI.actionManager.runAction(this, moveBy);
  }
}

class SetScoredOverlay extends PIXI.Container {
  private _contentScale: number;
  private _renderer: PlaybookRenderer;

  private _background: PIXI.Graphics;
  private _emitter: particles.Emitter;
  private _particlesContainer: PIXI.Container;
  private _setScoredCard: SetScoredCard;
  private _newTrophyCard: NewTrophyCard;
  private _cardQueue: (() => void)[];

  constructor(contentScale: number, renderer: PlaybookRenderer) {
    super();

    this._contentScale = contentScale;
    this._renderer = renderer;

    this._background = new PIXI.Graphics();
    this.addChild(this._background);

    this._particlesContainer = new PIXI.Container();
    this.addChild(this._particlesContainer);

    this._setScoredCard = new SetScoredCard(contentScale, renderer, () => this.hide());
    this.addChild(this._setScoredCard);

    this._newTrophyCard = new NewTrophyCard(contentScale, renderer, () => this.hide());
    this.addChild(this._newTrophyCard);

    this._cardQueue = [];

    this._emitter = new particles.Emitter(this._particlesContainer,
      [PIXI.loader.resources['resources/Item-Ball.png'].texture], {
					alpha: {
						start: 1,
						end: 1
					},
					scale: {
						start: 0.75,
						end: 1.0,
						minimumScaleMultiplier: 0.5
					},
					color: {
						start: 'ffffff',
						end: 'ffffff'
					},
					speed: {
						start: 200,
						end: 200
					},
					startRotation: {
						min: 80,
						max: 100
					},
					rotationSpeed: {
						min: 0,
						max: 50
					},
					lifetime: {
						min: 3.5,
						max: 4
					},
					blendMode: 'normal',
					frequency: 0.08,
					emitterLifetime: 0,
					maxParticles: 500,
					pos: {
						x: 0,
						y: 0
					},
					addAtBack: false,
					spawnType: 'rect',
					spawnRect: {
						x: 0,
						y: -64.0,
						w: window.innerWidth,
						h: 0
					}
      });

    this._initEvents();
    this.visible = false;
  }

  private _initEvents() {
    this._background.interactive = true;
    this._background.on('tap', () => this.hide());
  }

  show(goal: string, cardSet: ICard[], trophyGained: boolean) {
    this.visible = true;
    this.alpha = 1.0;

    const renderer = this._renderer;
    const background = this._background;
    const setScoredCard = this._setScoredCard;
    const newTrophyCard = this._newTrophyCard;
    const cardQueue = this._cardQueue;
    const emitter = this._emitter;

    emitter.cleanup();
    renderer.emitters.push(emitter);
    emitter.emit = true;

    background.clear();
    background.beginFill(0x000000);
    background.drawRect(0, 0, window.innerWidth, window.innerHeight);
    background.endFill();

    setScoredCard.show(goal, cardSet);
    if (trophyGained) {
      cardQueue.push(() => newTrophyCard.show(goal));
    }

    // Perform animation.
    const alphaTo = new PIXI.action.AlphaTo(0.75, 1.5);
    PIXI.actionManager.runAction(background, alphaTo);
  }

  hide() {
    if (this._cardQueue.length > 0) {
      const runnable = this._cardQueue.shift();
      runnable!();
    } else {
      const fadeOut = new PIXI.action.FadeOut(0.5);
      const callFunc = new PIXI.action.CallFunc(() => {
        this.visible = false;
        this._renderer.emitters.splice(this._renderer.emitters.indexOf(this._emitter), 1);
        this._emitter.emit = false;
      });
      const sequence = new PIXI.action.Sequence([fadeOut, callFunc]);
      PIXI.actionManager.runAction(this, sequence);
    }
  }
}

export default SetScoredOverlay;