'use strict';
import GoalTypes from './GoalTypes';

export interface IGoalTypeMetadata {
  description: string,
  file: string,
  score: number,
  isHidden: boolean,
  serverId: number,
  example: string
}

interface IGoalTypesMetadataMap {
  [goalType: string]: IGoalTypeMetadata
}

const GoalTypesMetadata: IGoalTypesMetadataMap = {
  [GoalTypes.EACH_COLOR_2]: {
    description: '2 RED & 2 BLUE',
    file: 'trophy1.png',
    score: 6,
    isHidden: true,
    serverId: 1,
    example: 'Collection-Example-2-2.png'
  },
  [GoalTypes.TWO_PAIRS]: {
    description: 'ANY 2 PAIRS',
    file: 'trophy2.png',
    score: 8,
    isHidden: true,
    serverId: 2,
    example: 'Collection-Example-2-2.png'
  },
  [GoalTypes.FULL_HOUSE]: {
    description: 'FULL HOUSE',
    file: 'trophy3.png',
    score: 12,
    isHidden: true,
    serverId: 3,
    example: 'Collection-Example-2-3.png'
  },
  [GoalTypes.SAME_COLOR_3]: {
    description: '3 CARDS OF SAME COLOR',
    file: 'trophy4.png',
    score: 6,
    isHidden: false,
    serverId: 4,
    example: 'Collection-Example-3.png'
  },
  [GoalTypes.SAME_COLOR_4]: {
    description: '4 CARDS OF SAME COLOR',
    file: 'trophy5.png',
    score: 8,
    isHidden: false,
    serverId: 5,
    example: 'Collection-Example-4.png'
  },
  [GoalTypes.SAME_COLOR_5]: {
    description: '5 CARDS OF SAME COLOR',
    file: 'trophy6.png',
    score: 12,
    isHidden: false,
    serverId: 6,
    example: 'Collection-Example-5.png'
  },
  [GoalTypes.IDENTICAL_CARDS_3]: {
    description: '3 IDENTICAL CARDS',
    file: 'trophy7.png',
    score: 6,
    isHidden: false,
    serverId: 7,
    example: 'Collection-Example-3.png'
  },
  [GoalTypes.IDENTICAL_CARDS_4]: {
    description: '4 IDENTICAL CARDS',
    file: 'trophy8.png',
    score: 8,
    isHidden: false,
    serverId: 8,
    example: 'Collection-Example-4.png'
  },
  [GoalTypes.IDENTICAL_CARDS_5]: {
    description: '5 IDENTICAL CARDS',
    file: 'trophy9.png',
    score: 12,
    isHidden: false,
    serverId: 9,
    example: 'Collection-Example-5.png'
  },
  [GoalTypes.OUT_3]: {
    description: 'SET SHOWS 3 OUTS',
    file: 'trophy10.png',
    score: 6,
    isHidden: false,
    serverId: 10,
    example: 'Collection-Example-3.png'
  },
  [GoalTypes.UNIQUE_OUT_CARDS_3]: {
    description: '3 UNIQUE OUT CARDS',
    file: 'trophy12.png',
    score: 8,
    isHidden: false,
    serverId: 11,
    example: 'Collection-Example-3.png'
  },
  [GoalTypes.UNIQUE_OUT_CARDS_4]: {
    description: '4 UNIQUE OUT CARDS',
    file: 'trophy11.png',
    score: 12,
    isHidden: false,
    serverId: 12,
    example: 'Collection-Example-4.png'
  },
  [GoalTypes.BASES_3]: {
    description: 'ANY 3 BASE CARDS',
    file: 'trophy13.png',
    score: 8,
    isHidden: false,
    serverId: 13,
    example: 'Collection-Example-3.png'
  },
  [GoalTypes.BASES_RBI_4]: {
    description: 'CARDS WITH 4 BASES',
    file: 'trophy14.png',
    score: 8,
    isHidden: false,
    serverId: 14,
    example: 'Collection-Example-4.png'
  },
  [GoalTypes.BASES_SEQ_3]: {
    description: '3 BASE CARDS IN ORDER',
    file: 'trophy15.png',
    score: 8,
    isHidden: false,
    serverId: 15,
    example: 'Collection-Example-3.png'
  }
};

export default GoalTypesMetadata;