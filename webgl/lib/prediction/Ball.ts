'use strict';
import PlaybookRenderer from '../PlaybookRenderer';
import FieldOverlayArea from './FieldOverlayArea';

/**
 * Ball.
 */
class Ball {
  private _renderer: PlaybookRenderer;
  private _getBallPositionForSlot: (ballTexture: PIXI.Texture, ballSlot: PIXI.Sprite, i: number) => PIXI.Point;

  sprite: PIXI.Sprite | null = null;
  isBeingDragged: boolean = false;
  dragPointerId: number | null = null;
  dragOffset: PIXI.Point | null = null;
  dragOrigPosition: PIXI.Point | null = null;
  dragTarget: FieldOverlayArea | null = null;
  isAnimating: boolean = false;
  selectedTarget: FieldOverlayArea | null = null;

  constructor(renderer: PlaybookRenderer,
              getBallPositionForSlot: (ballTexture: PIXI.Texture, ballSlot: PIXI.Sprite, i: number) => PIXI.Point) {
    this._renderer = renderer;
    this._getBallPositionForSlot = getBallPositionForSlot;
  }

  /**
   * Moves this ball to a specific position in world space with animation.
   */
  private _moveToWithAnimation(position: PIXI.Point) {
    const moveTo = new PIXI.action.MoveTo(position.x, position.y, 0.25);
    const callFunc = new PIXI.action.CallFunc(() => this.isAnimating = false);
    const sequence = new PIXI.action.Sequence([moveTo, callFunc]);
    this.isAnimating = true;
    PIXI.actionManager.runAction(this.sprite, sequence);
    return sequence;
  }

  /**
   * Moves this ball to its original location.
   */
  moveToOrigPosition() {
    this._moveToWithAnimation(this.dragOrigPosition!);
  }

  /**
   * Moves this ball to a specific slot.
   */
  moveToSlot(ballSlot: PIXI.Sprite, slot: number) {
    this._moveToWithAnimation(this._getBallPositionForSlot(this.sprite!.texture, ballSlot, slot));
  }

  /**
   * Moves this ball to the field.
   */
  moveToField(area: FieldOverlayArea, withAnimation = true) {
    let center = area.parent.toGlobal(area.getCentroid());

    // Determine if we need to run an animation.
    if (withAnimation) {
      this._moveToWithAnimation(center);
    } else {
      this.sprite!.position.set(center.x, center.y);
      this._renderer.markDirty();
    }
  }

  /**
   * Sets this ball as hollow. This is used to show prediction odds
   * when the ball is covering it.
   */
  setHollow(hollow: boolean) {
    const sheet = PIXI.loader.resources['resources/prediction.json'];
    if (hollow) {
      this.sprite!.texture = sheet.textures!['Item-Ball-Hollow.png'];
    } else {
      this.sprite!.texture = sheet.textures!['Item-Ball.png'];
    }
  }
}

export default Ball;