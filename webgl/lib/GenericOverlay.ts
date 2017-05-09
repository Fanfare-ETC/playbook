'use strict';
export interface IOverlayBackground extends PIXI.DisplayObject {
  onShow: () => void;
  onHide: () => void;
}

export interface IOverlayCard extends PIXI.DisplayObject {
  show: () => void;
  emitter: PIXI.utils.EventEmitter;
}

class GenericOverlay extends PIXI.Container {
  private _background: PIXI.Graphics;
  private _customBackground: IOverlayBackground | null = null;
  private _current: IOverlayCard | null = null;
  private _cardQueue: IOverlayCard[] = [];
  private _isAnimating: boolean = false;

  constructor(background?: IOverlayBackground) {
    super();

    if (background !== undefined) {
      this._customBackground = background;
      this.addChild(this._customBackground);
    }

    this._background = new PIXI.Graphics();
    this.addChild(this._background);

    this._initEvents();

    this.visible = false;
  }

  private _initEvents() {
    const background = this._customBackground !== null ? this._customBackground : this._background;
    background.interactive = true;
    background.on('tap', () => {
      if (this._isAnimating) { return; }
      this._hide();
    });
  }

  push(card: IOverlayCard) {
    this._cardQueue.push(card);
    this.emit('push');
    if (this._current === null) {
      this._show();
    }
  }

  pop() {
    if (this._isAnimating) { return; }
    this._hide();
  }

  get active() {
    return this._current !== null;
  }

  private _show() {
    this.visible = true;
    this.alpha = 1.0;

    const background = this._background;

    if (this._customBackground === null) {
      background.clear();
      background.beginFill(0x000000, 0.75);
      background.drawRect(0, 0, window.innerWidth, window.innerHeight);
      background.endFill();
      background.alpha = 0.0;
    } else {
      this._customBackground.onShow();
    }

    this._showNextCard();

    // Perform animation
    this._isAnimating = true;
    const fadeIn = new PIXI.action.FadeIn(0.25);
    const callFunc = new PIXI.action.CallFunc(() => this._isAnimating = false);
    const sequence = new PIXI.action.Sequence([fadeIn, callFunc]);
    if (this._customBackground === null) {
      PIXI.actionManager.runAction(background, sequence);
    } else {
      PIXI.actionManager.runAction(this._customBackground, sequence);
    }
  }

  private _showNextCard() {
    const card = this._cardQueue.shift();
    const dismissHandler = () => {
      this._hide();
      card!.emitter.off('dismiss', dismissHandler);
    };
    card!.emitter.on('dismiss', dismissHandler);
    this._current = card!;
    this.addChild(this._current);
    card!.show();
  }

  private _hide() {
    this.removeChild(this._current!);
    if (this._cardQueue.length > 0) {
      this._showNextCard();
    } else {
      this._isAnimating = true;
      this._current = null;
      const fadeOut = new PIXI.action.FadeOut(0.25);
      const callFunc = new PIXI.action.CallFunc(() => {
        this._isAnimating = false;
        this.visible = false;
        if (this._customBackground !== null) {
          this._customBackground.onHide();
        }
      });
      const sequence = new PIXI.action.Sequence([fadeOut, callFunc]);
      PIXI.actionManager.runAction(this, sequence);
    }
    this.emit('pop');
  }
}

export default GenericOverlay;