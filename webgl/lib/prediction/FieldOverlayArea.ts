'use strict';
import PlaybookRenderer from '../PlaybookRenderer';
import BallCountSprite from './BallCountSprite';

/**
 * Field overlay highlighted area.
 */
class FieldOverlayArea extends PIXI.Graphics {
  private _highlighted: boolean = false;
  private _renderer: PlaybookRenderer;

  ballCountSprite: BallCountSprite | null = null;
  centroidOffset: PIXI.Point = new PIXI.Point(0, 0);

  /**
   * Constructs a field overlay area defined by the given polygon.
   * @param area
   */
  constructor(renderer: PlaybookRenderer, area: PIXI.Polygon) {
    super();
    this.hitArea = area;
    this._renderer = renderer;
  }

  highlight() {
    this.alpha = 0.2;
    this._highlighted = true;
    this._renderer.markDirty();
  }

  clearHighlight() {
    this.alpha = 0;
    this._highlighted = false;
    this._renderer.markDirty();
  }

  isHighlighted() {
    return this._highlighted;
  }

  /**
   * Returns the centroid of the overlay area. The center is defined in terms
   * of the local space of the field overlay.
   *
   * Computed using: https://en.wikipedia.org/wiki/Centroid#Centroid_of_polygon
   *
   * @returns {PIXI.Point}
   */
  getCentroid() {
    const points = (this.hitArea as PIXI.Polygon).points;
    const numPoints = points.length / 2;
    const getXAt = (i: number) => points[(i * 2)];
    const getYAt = (i: number) => points[(i * 2) + 1];

    // Compute the area. Note that this produces a negative result since we
    // have our points laid out in counter-clockwise fashion.
    let area = 0;
    for (let i = 0; i < numPoints; i++) {
      if (i === numPoints - 1) {
        area += (getXAt(i) * getYAt(0) - getXAt(0) * getYAt(i));
      } else {
        area += (getXAt(i) * getYAt(i + 1) - getXAt(i + 1) * getYAt(i));
      }
    }
    area /= 2;

    let centroid = new PIXI.Point();
    for (let i = 0; i < numPoints; i++) {
      if (i === numPoints - 1) {
        centroid.x += (getXAt(i) + getXAt(0)) * (getXAt(i) * getYAt(0) - getXAt(0) * getYAt(i));
        centroid.y += (getYAt(i) + getYAt(0)) * (getXAt(i) * getYAt(0) - getXAt(0) * getYAt(i));
      } else {
        centroid.x += (getXAt(i) + getXAt(i + 1)) * (getXAt(i) * getYAt(i + 1) - getXAt(i + 1) * getYAt(i));
        centroid.y += (getYAt(i) + getYAt(i + 1)) * (getXAt(i) * getYAt(i + 1) - getXAt(i + 1) * getYAt(i));
      }
    }
    centroid.x /= area * 6;
    centroid.y /= area * 6;

    centroid.x += this.centroidOffset.x;
    centroid.y += this.centroidOffset.y;
    return centroid;
  }
}

export default FieldOverlayArea;