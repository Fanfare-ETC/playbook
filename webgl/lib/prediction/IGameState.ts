'use strict';
import Ball from './Ball';

interface IGameState {
  EVENT_STAGE_CHANGED: string;
  EVENT_PREDICTION_COUNTS_CHANGED: string;
  EVENT_SCORE_CHANGED: string;
  EVENT_OVERLAY_COUNT_CHANGED: string;
  EVENT_IS_SHOWING_PAYOUTS_CHANGED: string;
  EVENT_CORRECT_BETS_CHANGED: string;

  emitter: PIXI.utils.EventEmitter;

  balls: Ball[];
  stage: string;
  predictionCounts: { [name: string]: number };
  score: number;
  overlayCount: number;
  isShowingPayouts: boolean;
}

export default IGameState;