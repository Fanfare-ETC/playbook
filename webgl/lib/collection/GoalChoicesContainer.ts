'use strict';
import * as PIXI from 'pixi.js';
import GoalChoice from './GoalChoice';
import GoalTypes from './GoalTypes';
import IGameState from './IGameState';

interface ContainerParams {
  width: number,
  height: number
}

class GoalChoicesContainer extends PIXI.Container {
  _state: IGameState;
  _containerParams: ContainerParams;
  _contentScale: number;
  _scoreFunc: (choice: GoalChoice, goal: string) => void;
  _choices: GoalChoice[];

  _goal: string | null = null;

  constructor(state: IGameState, contentScale: number, containerParams: ContainerParams,
              scoreFunc: (choice: GoalChoice, goal: string) => void) {
    super();

    this._state = state;
    this._containerParams = containerParams;
    this._contentScale = contentScale;
    this._scoreFunc = scoreFunc;

    this._invalidate();
  }

  set goal(value: string) {
    this._goal = value;
    this._invalidate();
  }

  private _invalidate() {
    const state = this._state;
    const containerParams = this._containerParams;
    const contentScale = this._contentScale;
    const scoreFunc = this._scoreFunc;
    this.removeChildren();

    // Don't render anything yet if the goal is not set.
    if (this._goal === null) {
      return;
    }

    const choiceWidth = (window.innerWidth - 192.0 * contentScale) / 2;
    const choiceHeight = (containerParams.height - 192.0 * contentScale) / 2
    const info = [{
      position: new PIXI.Point(64.0 * contentScale, 64.0 * contentScale),
      backgroundColor: 0xff9f40,
      textColor: 0x402000,
      goal: GoalTypes.EACH_COLOR_2,
      showTrophy: false
    }, {
      position: new PIXI.Point(128.0 * contentScale + choiceWidth, 64.0 * contentScale),
      backgroundColor: 0xbfcad8,
      textColor: 0x303740,
      goal: GoalTypes.TWO_PAIRS,
      showTrophy: false
    }, {
      position: new PIXI.Point(64.0 * contentScale, 128.0 * contentScale + choiceHeight),
      backgroundColor: 0xffc300,
      textColor: 0x403100,
      goal: GoalTypes.FULL_HOUSE,
      showTrophy: false
    }, {
      position: new PIXI.Point(128.0 * contentScale + choiceWidth, 128.0 * contentScale + choiceHeight),
      backgroundColor: 0xffffff,
      textColor: 0x806200,
      goal: this._goal,
      showTrophy: true
    }];

    info.forEach(item => {
      const choice = new GoalChoice(state, contentScale, {
        width: choiceWidth,
        height: choiceHeight
      }, item, scoreFunc);
      this.addChild(choice);
    });
  }
}

export default GoalChoicesContainer;