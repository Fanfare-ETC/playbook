'use strict';
import Trophy from './Trophy';

class NewTrophyCard extends PIXI.Container {
  constructor(contentScale: number, goal: string) {
    super();

    const title = new PIXI.Text();
    this.addChild(title);

    const trophy = new Trophy();
    this.addChild(trophy);

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

    // Relayout horizontally after measurement.
    const center = this.width / 2;
    title.position.x = center;
    trophy.position.x = center;
  }
}

export default NewTrophyCard;