'use strict';
import PlaybookRenderer from '../PlaybookRenderer';
import Ball from './Ball';
import FieldOverlayArea from './FieldOverlayArea';

/**
 * Field overlay.
 */
class FieldOverlay extends PIXI.Sprite {
  private _defaultTexture: PIXI.Texture;
  private _payoutsTexture: PIXI.Texture;
  private _balls: Ball[];

  /**
   * Constructs the field overlay given the overlay areas.
   * The balls are needed to determine the drop targets.
   * @param areas
   * @param balls
   */
  constructor(renderer: PlaybookRenderer,
              areas: { [name: string]: PIXI.Polygon }, balls: Ball[],
              initFieldOverlayAreaEvents: (area: FieldOverlayArea, fieldOverlay: FieldOverlay) => void) {
    const texture = PIXI.loader.resources['resources/Prediction-Overlay.png'].texture;
    super(texture);

    this._defaultTexture = texture;
    this._payoutsTexture = PIXI.loader.resources['resources/Prediction-Overlay-Payout.png'].texture;
    this._balls = balls;

    Object.keys(areas).forEach(event => {
      const points = areas[event].points.map((point, index) => {
        if (index % 2 === 0) {
          return point - texture.width / 2;
        } else {
          return point - texture.height / 2;
        }
      });

      const area = new FieldOverlayArea(renderer, new PIXI.Polygon(points));
      area.name = event;
      area.alpha = 0;
      area.beginFill(0x000000);
      area.drawPolygon(points);
      area.endFill();

      initFieldOverlayAreaEvents(area, this);
      this.addChild(area);
    });
  }

  clearHighlightAreas() {
    this.children.forEach((child: FieldOverlayArea) => child.clearHighlight());
  }

  /**
   * Are we showing the payouts or the labels?
   * @returns {boolean}
   */
  isShowingPayouts() {
    return this.texture === this._payoutsTexture;
  }

  /**
   * Toggles showing the payouts for the betting table.
   */
  showPayouts() {
    this._balls.filter(ball => ball.selectedTarget)
      .forEach(ball => ball.setHollow(true));
    this.texture = this._payoutsTexture;
  }

  /**
   * Hides the payouts.
   */
  hidePayouts() {
    this._balls.filter(ball => ball.selectedTarget)
      .forEach(ball => ball.setHollow(false));
    this.texture = this._defaultTexture;
  }

  /**
   * Retrieves an overlay area given a point in world space.
   * @param {PIXI.Point} point
   * @returns {FieldOverlayArea?}
   */
  getAreaByPoint(point: PIXI.Point) : FieldOverlayArea | undefined {
    return (this.children as FieldOverlayArea[]).find(child => {
      const local = this.toLocal(point);
      return child.hitArea.contains(local.x, local.y);
    });
  }

  /**
   * Retrieves an overlay area given a name.
   * @param {string} name
   * @returns {FieldOverlayArea?}
   */
  getAreaByName(name: string) : FieldOverlayArea | undefined {
    return (this.children as FieldOverlayArea[]).find(child => child.name === name);
  }

  update() {
    // Unhighlight all field overlays.
    this.clearHighlightAreas();

    // Highlight areas where ball is above.
    this._balls.filter(ball => ball.isBeingDragged && ball.dragTarget)
      .forEach(ball => ball.dragTarget!.highlight());
  }
}

export default FieldOverlay;