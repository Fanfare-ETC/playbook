'use strict';
import * as PIXI from 'pixi.js';
import GoalTypes from './GoalTypes';
import GoalTypesMetadata from './GoalTypesMetadata';

interface ContainerParams {
  height: number,
  width: number
}

interface V4GoalInfo {
  position: PIXI.Point,
  backgroundColor: number,
  textColor: number,
  goal: string
}

class V4Goal extends PIXI.Graphics {
  _contentScale: number;
  _containerParams: ContainerParams;
  _info: V4GoalInfo;
  _active: boolean;

  _label: PIXI.Text;
  _example: PIXI.Sprite;
  _score: PIXI.Text;

  /**
   * Creates a goal tile.
   */
  constructor(contentScale: number, containerParams: ContainerParams, info: V4GoalInfo) {
    super();

    this._contentScale = contentScale;
    this._containerParams = containerParams;
    this._info = info;
    this._active = false;

    // If the goal type is unknown, we need to assign a random goal.
    if (info.goal === GoalTypes.UNKNOWN) {
      const visibleGoals = Object.keys(GoalTypesMetadata)
        .filter(goal => !GoalTypesMetadata[goal].isHidden);
      const randomChoice = Math.floor((Math.random() * visibleGoals.length));
      info.goal = visibleGoals[randomChoice];
    }

    if (info.goal !== null) {
      this._label = new PIXI.Text(GoalTypesMetadata[info.goal].description);
      this.addChild(this._label);

      if (GoalTypesMetadata[info.goal].example !== null) {
          const v4GoalExampleTexture = PIXI.loader.resources[`resources/${GoalTypesMetadata[info.goal].example}`].texture;
          this._example = new PIXI.Sprite(v4GoalExampleTexture);
          this.addChild(this._example);
      }

      this._score = new PIXI.Text(GoalTypesMetadata[info.goal].score.toString());
      this.addChild(this._score);
    }

    this.position.set(info.position.x, info.position.y);
    this._invalidate();
  }

  get active() {
    return this._active;
  }

  set active(value) {
    const oldValue = this._active;
    this._active = !!value;
    if (oldValue !== this._active) {
      this._invalidate();
    }
  }

  _invalidate() {
    const contentScale = this._contentScale;
    const info = this._info;
    let label = this._label;
    let example = this._example;
    let score = this._score;

    this.clear();
    this.lineStyle(12.0 * contentScale, info.backgroundColor);

    if (this._active) {
      this.beginFill(info.backgroundColor);
      this.drawRoundedRect(0, 0, this._containerParams.width, this._containerParams.height, 64.0 * contentScale);
      this.endFill();
    } else {
      this.drawRoundedRect(0, 0, this._containerParams.width, this._containerParams.height, 64.0 * contentScale);
    }

    if (info.goal !== null) {
      label.style.fontFamily = 'proxima-nova-excn';
      label.style.fontSize = 72.0 * contentScale;
      label.style.fill = this._active ? info.textColor : info.backgroundColor;
      label.style.align = 'center';
      label.anchor.set(0.5, 0.5);
      label.position.set(this._containerParams.width / 2, 64.0 * contentScale);

      if (GoalTypesMetadata[info.goal].example !== null) {
        example.tint = this._active ? info.textColor : info.backgroundColor;
        example.anchor.set(0.5, 0.5);
        example.position.set(this._containerParams.width / 2, this._containerParams.height / 2);
        example.scale.set(contentScale, contentScale);
      }

      score.style.fontFamily = 'SCOREBOARD';
      score.style.fontSize = 104.0 * contentScale;
      score.style.fill = this._active ? info.textColor : info.backgroundColor;
      score.position.set(
        64.0 * contentScale,
        this._containerParams.height - 64.0 * contentScale - score.height
      );
    }
  }
}

export default V4Goal;
