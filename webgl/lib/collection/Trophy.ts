'use strict';
import * as PIXI from 'pixi.js';
import GoalTypesMetadata from './GoalTypesMetadata';

function getTexture(goal?: string) : PIXI.Texture {
  if (goal === undefined) {
    return PIXI.loader.resources['resources/collection.json'].textures!['empty.png'];
  } else {
    const file = GoalTypesMetadata[goal].file;
    return PIXI.loader.resources['resources/collection.json'].textures![file];
  }
}

class Trophy extends PIXI.Sprite {
  private _goal?: string;

  constructor(goal?: string) {
    super(getTexture(goal));
    this._goal = goal;
  }

  set goal(value: string) {
    this.texture = getTexture(value);
    this._goal = value;
  }

  get goal() : string {
    return this._goal!;
  }
}

export default Trophy;