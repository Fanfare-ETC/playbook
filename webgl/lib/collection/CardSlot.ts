'use strict';
import ICard from './ICard';

class CardSlot {
  card: ICard | null = null;
  present: boolean = false;
}

export default CardSlot;