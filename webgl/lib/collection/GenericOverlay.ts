'use strict';
import GenericCard from './GenericCard';

class GenericOverlay extends PIXI.Container {
  private _background: PIXI.Graphics;
  private _current: GenericCard | null = null;
  private _cardQueue: GenericCard[] = [];
  private _isAnimating: boolean = false;

  constructor() {
    super();

    this._background = new PIXI.Graphics();
    this.addChild(this._background);

    this._initEvents();

    this.visible = false;
  }

  private _initEvents() {
    this._background.interactive = true;
    this._background.on('tap', () => {
      if (this._isAnimating) { return; }
      this._hide();
    });
  }

  push(card: GenericCard) {
    this._cardQueue.push(card);
    if (this._current === null) {
      this._show();
    }
  }

  private _show() {
    this.visible = true;
    this.alpha = 1.0;

    const background = this._background;
    const cardQueue = this._cardQueue;

    background.clear();
    background.beginFill(0x000000, 0.75);
    background.drawRect(0, 0, window.innerWidth, window.innerHeight);
    background.endFill();
    background.alpha = 0.0;

    const card = cardQueue.shift();
    const dismissHandler = () => {
      this._hide();
      card!.emitter.off('dismiss', dismissHandler);
    };
    card!.emitter.on('dismiss', dismissHandler);
    this._current = card!;
    this.addChild(this._current);
    card!.show();

    // Perform animation
    this._isAnimating = true;
    const fadeIn = new PIXI.action.FadeIn(0.25);
    const callFunc = new PIXI.action.CallFunc(() => this._isAnimating = false);
    const sequence = new PIXI.action.Sequence([fadeIn, callFunc]);
    PIXI.actionManager.runAction(background, sequence);
  }

  private _hide() {
    this.removeChild(this._current!);
    if (this._cardQueue.length > 0) {
      const nextCard = this._cardQueue.shift();
      this._current = nextCard!;
      nextCard!.show();
    } else {
      this._isAnimating = true;
      this._current = null;
      const fadeOut = new PIXI.action.FadeOut(0.25);
      const callFunc = new PIXI.action.CallFunc(() => {
        this._isAnimating = false;
        this.visible = false;
      });
      const sequence = new PIXI.action.Sequence([fadeOut, callFunc]);
      PIXI.actionManager.runAction(this, sequence);
    }
  }
}

export default GenericOverlay;