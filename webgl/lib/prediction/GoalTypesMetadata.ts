'use strict';
import GoalTypes from './GoalTypes';

export interface IGoalTypeMetadata {
  description: string,
  file: string,
  serverId: number
}

interface IGoalTypesMetadataMap {
  [goalType: string]: IGoalTypeMetadata
}

const GoalTypesMetadata: IGoalTypesMetadataMap = {
  [GoalTypes.GOOD_EYE]: {
    description: 'Bet 3 balls on 1 thing correctly',
    file: 'Trophy-PredictBig-B.png',
    serverId: 16
  },
  [GoalTypes.HIGH_ROLLER]: {
    description: 'Bet 4 balls on 1 thing correctly',
    file: 'Trophy-PredictBig-S.png',
    serverId: 17
  },
  [GoalTypes.ALL_IN]: {
    description: 'Bet 5 balls on 1 thing correctly',
    file: 'Trophy-PredictBig-G.png',
    serverId: 18
  },
  [GoalTypes.LUCKY_GUESS]: {
    description: 'Bet on 3 different things correctly',
    file: 'Trophy-PredictMany-B.png',
    serverId: 19
  },
  [GoalTypes.SMARTY_PANTS]: {
    description: 'Bet on 4 different things correctly',
    file: 'Trophy-PredictMany-S.png',
    serverId: 20
  },
  [GoalTypes.MASTERMIND]: {
    description: 'Bet on 5 different things correctly',
    file: 'Trophy-PredictMany-G.png',
    serverId: 21
  }
};

export default GoalTypesMetadata;