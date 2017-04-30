'use strict';
import { EventEmitter } from 'eventemitter3';
import CardSlot from './CardSlot';
import ICard from './ICard';

export interface ILastScoredGoalInfo {
  goal: string;
  cardSet: ICard[];
}

interface IGameState {
  EVENT_ACTIVE_CARD_CHANGED: string;
  EVENT_INCOMING_CARDS_CHANGED: string;
  EVENT_GOAL_CHANGED: string;
  EVENT_CARD_SLOTS_CHANGED: string;
  EVENT_SELECTED_GOAL_CHANGED: string;
  EVENT_SCORE_CHANGED: string;

  emitter: EventEmitter;

  cards: PIXI.Sprite[];
  goalSets: { [goal: string]: ICard[] };

  reset(persist: boolean) : void;
  toJSON() : string;
  fromJSON(state: string) : void;

  activeCard: ICard | null;
  incomingCards: string[];
  goal: string | null;
  cardSlots: CardSlot[];
  selectedGoal: string | null;
  score: number;
}

export default IGameState;