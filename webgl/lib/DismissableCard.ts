'use strict';
import * as PIXI from 'pixi.js';
import 'pixi-action';
import PlaybookRenderer from './PlaybookRenderer';

/**
 * A dismissable card is a UI element that can be dismissed from the screen
 * by means of swiping away.
 */
class DismissableCard extends PIXI.Container {
  protected _renderer: PlaybookRenderer;
  private _isAnimating: boolean = false;

  emitter: PIXI.utils.EventEmitter = new PIXI.utils.EventEmitter();

  constructor(renderer: PlaybookRenderer) {
    super();
    this._renderer = renderer;
    this._initEvents();
  }

  protected _initEvents() {
    let origPosition: PIXI.Point | null = null;
    let startPosition: PIXI.Point | null = null;
    let startOffset: PIXI.Point | null = null;

    const onTouchStart = (e: PIXI.interaction.InteractionEvent) => {
      if (this._isAnimating) { return; }
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
      if (this._isAnimating) { return; }
      if (origPosition === null) { return; }

      const bounds = new PIXI.Rectangle(
        window.innerWidth / 4, window.innerHeight / 4,
        window.innerWidth / 2, window.innerHeight / 2
      );

      if (bounds.contains(e.data.global.x, e.data.global.y)) {
        this._isAnimating = true;
        const rotateTo = new PIXI.action.RotateTo(0, 0.25);
        const moveTo = new PIXI.action.MoveTo(origPosition.x, origPosition.y, 0.25);
        const callFunc = new PIXI.action.CallFunc(() => {
          this._isAnimating = false;
        });
        const sequence = new PIXI.action.Sequence([moveTo, callFunc]);
        PIXI.actionManager.runAction(this, rotateTo);
        PIXI.actionManager.runAction(this, sequence);
      } else {
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
    const callFunc = new PIXI.action.CallFunc(() => {
      this.visible = false;
      this.emitter.emit('dismiss');
    });
    const sequence = new PIXI.action.Sequence([fadeOut, callFunc]);
    PIXI.actionManager.runAction(this, sequence);
  }
}

export default DismissableCard;
