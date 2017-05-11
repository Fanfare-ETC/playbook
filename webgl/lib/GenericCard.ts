'use strict';
import PlaybookRenderer from './PlaybookRenderer';
import DismissableCard from './DismissableCard';
import { IOverlayCard } from './GenericOverlay';

class GenericCard extends DismissableCard implements IOverlayCard {
  private _contentScale: number;
  private _background: PIXI.Graphics;
  private _content: PIXI.Container;

  constructor(contentScale: number, renderer: PlaybookRenderer,
              content: PIXI.Container) {
    super(renderer);

    this._contentScale = contentScale;

    this._background = new PIXI.Graphics();
    this.addChild(this._background);

    this._content = content;
    this.addChild(this._content);

    this.visible = false;
  }

  show() {
    this.visible = true;

    const contentScale = this._contentScale;
    const background = this._background;
    const content = this._content;
    const padding = 64.0 * contentScale;

    background.clear();

    this.pivot.set(this.width / 2 + padding, this.height / 2 + padding);
    this.rotation = 0;
    this.position.set(window.innerWidth / 2, window.innerHeight / 2);

    background.beginFill(0xffffff);
    background.drawRoundedRect(
      0, 0,
      this.width + padding * 2,
      this.height + padding * 2,
      64.0 * contentScale
    )
    background.endFill();

    // Prevent events from going into the background.
    background.interactive = true;
    background.hitArea = background.getBounds();

    // Position the content.
    content.position.set(padding, padding);

    // Perform animation.
    this.alpha = 0;
    this.position.y += this.height / 2;
    const fadeIn = new PIXI.action.FadeIn(0.5);
    const moveBy = new PIXI.action.MoveBy(0.0, -this.height / 2, 0.5);
    PIXI.actionManager.runAction(this, fadeIn);
    PIXI.actionManager.runAction(this, moveBy);
  }
}

export default GenericCard;