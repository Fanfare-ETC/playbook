'use strict';
import Ball from './Ball';

interface IGameState {
  EVENT_STAGE_CHANGED: string;
  EVENT_PREDICTION_COUNTS_CHANGED: string;
  EVENT_SCORE_CHANGED: string;

  emitter: PIXI.utils.EventEmitter;

  balls: Ball[];
  stage: string;
  predictionCounts: { [name: string]: number };
  score: number;
}

export default IGameState;