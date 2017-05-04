'use strict';
import FieldOverlay from './FieldOverlay';
import FieldOverlayArea from './FieldOverlayArea';

/**
 * Ball count sprite.
 */
class BallCountSprite extends PIXI.Sprite {
  _count: number = 0;

  /**
   * Creates a sprite that is used to represent the number of balls in a
   * specific area.
   * @param {FieldOverlay} fieldOverlay
   * @param {FieldOverlayArea} area
   * @param {PIXI.Sprite} ballSprite
   */
  constructor(fieldOverlay: FieldOverlay, area: FieldOverlayArea, ballSprite: PIXI.Sprite) {
    super();

    // Setup the sprite with an appropriate scale and location.
    const centroid = fieldOverlay.toGlobal(area.getCentroid());
    this.scale.set(ballSprite.scale.x, ballSprite.scale.y);
    this.position.set(centroid.x, centroid.y);
    this.anchor.set(0.5, 0.5);

    this._count = 0;
  }

  set count(value: number) {
    if (value > 1) {
      const texture = PIXI.loader.resources['resources/prediction.json'].textures![`Ball-Text-x${value}.png`];
      this.texture = texture;
    } else {
      this.texture = null!;
    }

    this._count = value;
  }
}

export default BallCountSprite;