'use strict';
import * as PIXI from 'pixi.js';
import 'pixi-action';

interface ICard {
  play: string;
  team: string;
  sprite: PIXI.Sprite;
  isBeingDragged: boolean;
  dragPointerId: number | null;
  dragOffset: PIXI.Point | null;
  dragOrigPosition: PIXI.Point | null;
  dragOrigRotation: number | null;
  dragOrigScale: number | null;
  dragTarget: PIXI.Sprite | number | null;
  isAnimating: number;
  isActive: boolean;
  isDiscarding: boolean;
  tint: number;

  moveToOrigPosition: () => void;
  moveToSlot: (slot: number) => PIXI.action.Sequence;
}

export default ICard;