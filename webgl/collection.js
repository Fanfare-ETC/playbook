'use strict';
import * as PIXI from 'pixi.js';
import 'pixi-action';
import EventEmitter from 'eventemitter3';

import PlaybookEvents,
  { FriendlyNames as PlaybookEventsFriendlyNames, 
  Teams as PlaybookEventsTeams,
  StringMap as PlaybookEventsStringMap} from './lib/PlaybookEvents';

// The Playbook Bridge is supplied via addJavaScriptInterface() on the Java
// side of the code. In the absence of that, we need to mock one.
if (!global.PlaybookCollectionBridge) {
  /** @type {Object<string, function>} */
  global.PlaybookCollectionBridge = {
    /**
     * Returns the URL of the WebSocket server.
     * @returns {string}
     */
    getAPIUrl: function () {
      return 'ws://localhost:9001';
    },

    /**
     * Returns the URL of the Section API server.
     * @returns {string}
     */
    getSectionAPIUrl: function () {
      return 'http://localhost:9002';
    },

    /**
     * Returns the ID of the current player.
     * This is stubbed.
     * @returns {string}
     */
    getPlayerID: function () {
      return 1;
    },

    /**
     * Notifies the hosting application of the new state of the game.
     * This is a no-op for the mock bridge.
     * @type {string} stateJSON
     */
    notifyGameState: function (stateJSON) {
      console.log('Saving state: ', stateJSON);
      localStorage.setItem('prediction', stateJSON);
    },

    /**
     * Notifies the hosting application that we have finished loading.
     * This is a no-op for the mock bridge.
     */
    notifyLoaded: function () {
      const restoredState = localStorage.getItem('prediction');
      console.log('Loading state: ', restoredState);
      if (restoredState != null) {
        state.fromJSON(restoredState);
      }
    }
  };
} else {
  // Receive messages from the hosting application.
  global.addEventListener('message', function (e) {
    const message = e.data;
    switch (message.action) {
      case 'RESTORE_GAME_STATE':
        console.log('Restoring state from hosting application: ');
        state.fromJSON(message.payload);
        break;
      case 'HANDLE_MESSAGE':
        console.log('Handling message from hosting application: ');
        handleIncomingMessage(message.payload);
        break;
    }
  });
}

const GoalTypes = {
  IDENTICAL_CARDS_3: "IDENTICAL_CARDS_3",
  IDENTICAL_CARDS_4: "IDENTICAL_CARDS_4",
  IDENTICAL_CARDS_5: "IDENTICAL_CARDS_5",
  UNIQUE_OUT_CARDS_3: "UNIQUE_OUT_CARDS_3",
  UNIQUE_OUT_CARDS_4: "UNIQUE_OUT_CARDS_4",
  WALK_OR_HIT_BY_PITCH_3: "WALK_OR_HIT_BY_PITCH_3",
  OUT_3: "OUT_3",
  BASES_RBI_3: "BASES_RBI_3",
  EACH_COLOR_1: "EACH_COLOR_1",
  EACH_COLOR_2: "EACH_COLOR_2",
  SAME_COLOR_3: "SAME_COLOR_3",
  SAME_COLOR_4: "SAME_COLOR_4",
  SAME_COLOR_5: "SAME_COLOR_5",
  BASE_STEAL_RBI: "BASE_STEAL_RBI",
  ON_BASE_STEAL_PICK_OFF: "ON_BASE_STEAL_PICK_OFF",
  FULL_HOUSE: "FULL_HOUSE",
  UNKNOWN: "UNKNOWN"
};
/*
const finder = function (goal) {
  return function (id) {
    return GoalTypesMetadata[goal].serverId === id;
  };
};
Object.keys(GoalTypesMetadata)
  .find(finder(15))

std::find_if()
*/
const GoalTypesMetadata = {
  [GoalTypes.IDENTICAL_CARDS_3]: {
    description: "3 IDENTICAL CARDS",
    file: "goal/goal1.png",
    score: 8,
    isHidden: true,
    serverId: 1
  },
  [GoalTypes.IDENTICAL_CARDS_4]: {
    description: "4 IDENTICAL CARDS",
    file: "goal/goal2.png",
    score: 12,
    isHidden: false,
    serverId: 9
  },
  [GoalTypes.UNIQUE_OUT_CARDS_4]: {
    description: "4 DIFFERENT OUT CARDS",
    file: "goal/goal3.png",
    score: 12,
    isHidden: false,
    serverId: 17
  },
  [GoalTypes.IDENTICAL_CARDS_5]: {
    description: "5 IDENTICAL CARDS",
    file: "goal/goal4.png",
    score: 20,
    isHidden: false,
    serverId: 13
  },
  [GoalTypes.WALK_OR_HIT_BY_PITCH_3]: {
    description: "3 OF WALK OR HIT BY PITCH",
    file: "goal/goal5.png",
    score: 8,
    isHidden: true,
    serverId: 4
  },
  [GoalTypes.OUT_3]: {
    description: "SET SHOWS 3 OUTS",
    file: "goal/goal6.png",
    score: 6,
    isHidden: false,
    serverId: 7
  },
  [GoalTypes.BASES_RBI_3]: {
    description: "SET SHOWS 3 BASES",
    file: "goal/goal7.png",
    score: 12,
    isHidden: false,
    serverId: 16
  },
  [GoalTypes.EACH_COLOR_2]: {
    description: "2 CARDS OF EACH COLOR",
    file: "goal/goal8.png",
    score: 12,
    isHidden: false,
    serverId: 12
  },
  [GoalTypes.SAME_COLOR_3]: {
    description: "3 CARDS OF SAME COLOR",
    file: "goal/goal9.png",
    score: 8,
    isHidden: true,
    serverId: 2
  },
  [GoalTypes.EACH_COLOR_1]: {
    description: "1 CARD OF EACH COLOR",
    file: "goal/goal11.png",
    score: 4,
    isHidden: true,
    serverId: 3
  },
  [GoalTypes.UNIQUE_OUT_CARDS_3]: {
    description: "3 DIFFERENT OUT CARDS",
    file: "goal/goal12.png",
    score: 8,
    isHidden: false,
    serverId: 8
  },
  [GoalTypes.SAME_COLOR_4]: {
    description: "4 CARDS OF SAME COLOR",
    file: "goal/goal13.png",
    score: 12,
    isHidden: false,
    serverId: 10
  },
  [GoalTypes.SAME_COLOR_5]: {
    description: "5 CARDS OF SAME COLOR",
    file: "goal/goal14.png",
    score: 20,
    isHidden: false,
    serverId: 14
  },
  [GoalTypes.BASE_STEAL_RBI]: {
    description: "BASE, STEAL & RBI",
    file: "goal/goal15.png",
    score: 8,
    isHidden: false,
    serverId: 11
  },
  [GoalTypes.ON_BASE_STEAL_PICK_OFF]: {
    description: "BASE, STEAL & PICK OFF",
    file: "",
    score: 8,
    isHidden: true,
    serverId: 5
  },
  [GoalTypes.FULL_HOUSE]: {
    description: "FULL HOUSE",
    file: "",
    score: 16,
    isHidden: true,
    serverId: 6
  }
};

class GameState {
  constructor() {
    //this.EVENT_STAGE_CHANGED = 'stageChanged';
    //this.EVENT_PREDICTION_COUNTS_CHANGED = 'predictionCountsChanged';

    /** @type {Object.<string, number>} */
    this._cardCounts = {};

    /** @type {string} */
    //this._stage = GameStages.INITIAL; //? Do we need it here?

    /** @type {Array<Card>} */
    this.cards = new Array();

    /** @type {Array<Card>} */
    this.incomingCards = new Array();

    /** @type {number} */
    this.score = 0;

    /** @type {EventEmitter} */
    this.emitter = new EventEmitter();
  }

  /**
   * @returns {string}
   */
  /*get stage() {
    return this._stage;
  }*/

  /**
   * @param {string} value
   */
  set stage(value) {
    //const oldValue = this._stage;
    //this._stage = value;
    console.log('stage->', value);
    this.emitter.emit(this.EVENT_STAGE_CHANGED, value, oldValue);
    PlaybookBridge.notifyGameState(this.toJSON());
  }

  /**
   * @returns {Object.<string, number>}
   */
  get predictionCounts() {
    return this._predictionCounts;
  }

  /**
   * @param {Object.<string, number>}
   */
  set predictionCounts(value) {
    const oldValue = this._predictionCounts;
    this._predictionCounts = value;
    console.log('predictionCounts->', value);
    this.emitter.emit(this.EVENT_PREDICTION_COUNTS_CHANGED, value, oldValue);
    PlaybookBridge.notifyGameState(this.toJSON());
  }

  /**
   * Returns the game state as JSON.
   * @returns {string}
   */
  toJSON() {
    const savedState = {
     // stage: this._stage,
      score: this.score,
      balls: this.balls.map(ball => {
        return {
          selectedTarget: ball.selectedTarget ? ball.selectedTarget.name : null
        };
      })
    };

    return JSON.stringify(savedState);
  }

  /**
   * Restores the game state from JSON.
   * @param {string} state
   */
  fromJSON(state) {
    const restoredState = JSON.parse(state);

    const fieldOverlay = stage.getChildByName('fieldOverlay');
    restoredState.balls.forEach((ball, i) => {
      if (ball.selectedTarget !== null) {
        const area = fieldOverlay.getChildByName(ball.selectedTarget);
        makePrediction(this, area, this.balls[i]);
        this.balls[i].moveToField(area, false);
      }
    });

    // Restore this later because makePrediction changes the state.
    this.stage = restoredState.stage;
    this.score = restoredState.score;
  }
}

const connection = new WebSocket(PlaybookCollectionBridge.getAPIUrl());
const renderer = PIXI.autoDetectRenderer(1080, 1920, { resolution: window.devicePixelRatio });
const stage = new PIXI.Container();
const state = new GameState();

/**
 * Card.
 */
class Card {
  constructor() {
    /** @type {PIXI.Sprite?} */
    this.sprite = null;

    /** @type {bool} */
    this.isBeingDragged = false;

    /** @type {int?} */
    this.dragPointerId = null;

    /** @type {PIXI.Point?} */
    this.dragOffset = null;

    /** @type {PIXI.Point?} */
    this.dragOrigPosition = null;

    /** @type {FieldOverlayArea?} */
    this.dragTarget = null; //either discard or score

    /** @type {bool} */
    this.isAnimating = false;

    /** @type {FieldOverlayArea?} */
    this.selectedTarget = null; //only score
  }

  /**
   * Moves this card to a specific position in world space with animation.
   * @param {PIXI.Point} position
   * @return {PIXI.action.Sequence}
   */
  _moveToWithAnimation(position) {
    const moveTo = new PIXI.action.MoveTo(position.x, position.y, 0.25);
    const callFunc = new PIXI.action.CallFunc(() => this.isAnimating = false);
    const sequence = new PIXI.action.Sequence([moveTo, callFunc]);
    this.isAnimating = true;
    PIXI.actionManager.runAction(this.sprite, sequence);
    return sequence;
  }

  /**
   * Moves this card to its original location.
   */
  moveToOrigPosition() {
    this._moveToWithAnimation(this.dragOrigPosition); //probably the original slot it was residing
  }

  /**
   * Moves this ball to a specific slot.
   * @param {PIXI.Sprite} cardSlot
   * @param {number} slot
   */
  moveToSlot(cardSlot, slot) {
    this._moveToWithAnimation(getCardSlotPosition(this.sprite.texture, cardSlot, slot));
  }

  /**
   * Moves this ball to the field.
   * @param {FieldOverlayArea} area
   * @param {bool} withAnimation
   */
  moveToField(area, withAnimation = true) {
    let center = area.parent.toGlobal(area.getCentroid());

    // Determine if we need to run an animation.
    if (withAnimation) {
      this._moveToWithAnimation(center);
    } else {
      this.sprite.position.set(center.x, center.y);
      renderer.isDirty = true;
    }
  }
}

/**
 * Returns the world space position for a ball slot.
 * @param {PIXI.Texture} cardTexture
 * @param {PIXI.Sprite} cardSlot
 * @param {Number} i
 */
function getCardSlotPositionFor(cardTexture, cardSlot, i) {
  const cardScale = cardSlot.texture.height / cardTexture.height / 1.5;
  return cardSlot.toGlobal(new PIXI.Point(
    120 + cardTexture.width * i * cardScale,
    cardSlot.texture.height / 2
  ));
};

// Receive messages from the hosting application.
  global.addEventListener('message', function (e) {
    const message = e.data;
    switch (message.action) {
      case 'RESTORE_GAME_STATE':
        console.log('Restoring state from hosting application: ');
        state.fromJSON(message.payload);
        break;
      case 'HANDLE_MESSAGE':
        console.log('Handling message from hosting application: ');
        handleIncomingMessage(message.payload);
        break;
    }
  });

function createRandomGoal(goalContainer) {
  //remove existing goal

  //only set visible goals
  const visibleGoals = Object.keys(GoalTypesMetadata)
    .filter(goal => !GoalTypesMetadata[goal].isHidden)
    .map(goal => GoalTypesMetadata[goal]);
  //console.log(visibleGoals);
  const randomChoice = Math.floor((Math.random() * visibleGoals.length));
  setActiveGoal(visibleGoals[randomChoice], goalContainer);

}

function setActiveGoal(goal, goalContainer) {
  const newTexture = PIXI.loader.resources[`resources/${goal.file}`].texture;
  //debugger;
  goalContainer.texture = newTexture;
}

function handleIncomingMessage(message) {
  switch (message.event) {
    case 'server:playsCreated':
      handlePlaysCreated(message.data);
      break;
    case 'server:clearPredictions':
      handleClearPredictions();
      break;
    default:
  }
}

/**
 * Handles plays created event.
 * @param {Array.<number>} events
 */
function handlePlaysCreated(events) {
 // if (state.stage === GameStages.CONFIRMED) {
   console.log('Events created!');
    const plays = events.map(PlaybookEvents.getById);
    for (const play of plays) {
      console.log('Play string: ' + play);
      receiveCard(play);

     /* state.score += ScoreValues[play] * state.predictionCounts[play];
      reportScore(ScoreValues[play] * state.predictionCounts[play]);
      const overlay = new PredictionCorrectOverlay(play);
      initPredictionCorrectOverlayEvents(overlay);
      stage.addChild(overlay);*/
      renderer.isDirty = true;
    }
  
}
function receiveCard(play){
  createCard(play);
}
function createCard(play){
  const team = PlaybookEventsTeams[play];
  if(team === 'NONE'){
    return;
  }
  let teamString;
  if(team === "FIELDING"){
    teamString = 'F-';
  } 
  else if (team === "BATTING"){
    teamString = 'B-';
  }
  const mapString = PlaybookEventsStringMap[play];
  const cardTexture = PIXI.loader.resources[`resources/cards/Card-${teamString}${mapString}.jpg`].texture;
  const card = new PIXI.Sprite(cardTexture);
  const cardScale = (window.innerWidth / 2) / cardTexture.width;
  const cardHeight = 96.0;
  card.name = 'card';
  card.position.set(window.innerWidth / 2, window.innerHeight / 2);
  card.scale.set(cardScale, cardScale);
  card.anchor.set(0.5, 0.5);
  card.rotation = PIXI.DEG_TO_RAD * (Math.floor(Math.random() * 10) + -5);
  card.zOrder = 2;
  stage.addChild(card);
}
/**
 * Sets up the renderer. Adjusts the renderer according to the size of the
 * viewport, and adds it to the DOM tree.
 * @param {PIXI.WebGLRenderer} renderer
 */
function configureRenderer(renderer) {
  const resizeToFitWindow = function (renderer) {
    renderer.resize(window.innerWidth, window.innerHeight);
  };

  renderer.view.style.position = 'absolute';
  renderer.view.style.display = 'block';
  renderer.autoResize = true;
  resizeToFitWindow(renderer);
  document.body.appendChild(renderer.view);
  window.addEventListener('resize', resizeToFitWindow.bind(this, renderer));
};

/**
 * Sets up the WebSocket connection.
 * @param {WebSocket} connection
 */
function configureWebSocket(connection) {
  connection.addEventListener('open', function () {
    console.log(`Connected to ${connection.url}`);
  });

  connection.addEventListener('message', function (message) {
    message = JSON.parse(message.data);
    handleIncomingMessage(message);
  });
};

function getBallPositionForSlot(ballTexture, ballSlot, i) {
  const ballScale = ballSlot.texture.height / ballTexture.height / 1.5;
  return ballSlot.toGlobal(new PIXI.Point(
    120 + ballTexture.width * i * ballScale,
    ballSlot.texture.height / 2
  ));
};

function setup() {

  //console.log('Test from JS!');
  // Add background to screen.
  const bgTexture = PIXI.loader.resources['resources/Collection-BG-Wood.jpg'].texture;
  const bg = new PIXI.Sprite(bgTexture);
  bg.scale.x = window.innerWidth / bgTexture.width;
  bg.scale.y = window.innerHeight / bgTexture.height;
  bg.zOrder = 0;
  stage.addChild(bg);

  // Add banner on top to screen.
  /*
  const bannerTexture = PIXI.loader.resources['resources/Collection-Banner-9x16.png'].texture
  const banner = new PIXI.Sprite(bannerTexture);
  const bannerScale = window.innerWidth / bannerTexture.width;
  const bannerHeight = bannerScale * bannerTexture.height;
  banner.scale.set(bannerScale, bannerScale);
  banner.zOrder = 1;
  stage.addChild(banner);
*/
  // Add card tray to screen.
  const trayTexture = PIXI.loader.resources['resources/Collection-Tray-9x16.png'].texture;
  const tray = new PIXI.Sprite(trayTexture);
  const trayScale = window.innerWidth / trayTexture.width;
  const trayHeight = trayScale * trayTexture.height;
  tray.name = 'tray';
  tray.position.set(0, window.innerHeight - trayHeight);
  tray.scale.set(trayScale, trayScale);
  tray.zOrder = 2;
  stage.addChild(tray);

 //Add top shadow
  const shadow2Texture = PIXI.loader.resources['resources/Collection-Shadow-Overturn.png'].texture;
  const shadow2 = new PIXI.Sprite(shadow2Texture);
  const shadow2Scale = window.innerWidth;
  const shadow2Height = 64.0;
  shadow2.name = 'shadowTop';
  shadow2.position.set(0, 48);
  shadow2.scale.set(shadow2Scale, 1);
  shadow2.zOrder = 3;
  stage.addChild(shadow2);

  //Add score bar
  const scoreBarTexture = PIXI.loader.resources['resources/Collection-Bar-Gold-9x16.png'].texture;
  const scoreBar = new PIXI.Sprite(scoreBarTexture);
  const scoreBarScale = window.innerWidth / 2;
  const scoreBarHeight = 96.0;
  scoreBar.name = 'scoreBar';
  scoreBar.position.set(0, window.innerHeight - scoreBarHeight / 2  - trayHeight - 16.0);
  scoreBar.scale.set(scoreBarScale, 0.5);
  scoreBar.zOrder = 2;
  stage.addChild(scoreBar);

//Add score bar shadow
  const shadowTexture = PIXI.loader.resources['resources/Collection-Shadow-9x16.png'].texture;
  const shadow = new PIXI.extras.TilingSprite(shadowTexture, window.innerWidth / 2, 96.0);
  const shadowScale = window.innerWidth / 2;
  const shadowHeight = 96.0;
  shadow.name = 'shadow';
  shadow.position.set(0, window.innerHeight - shadowHeight / 2  - trayHeight - 16.0);
  shadow.tileScale.set(1, 0.5);
  shadow.zOrder = 3;
  stage.addChild(shadow);

//Add score label
 /** @type {PIXI.Text} */
    const shadowText = new PIXI.Text();
    shadowText.position.set(16, shadowHeight / 4);
    //shadowText.scale.set(1 / shadowScale, 2);
    //shadowText.position.set(0, 1);
    shadowText.anchor.set(0.0, 0.5);
    shadowText.text = 'Score:'.toUpperCase();
    shadowText.style.fontFamily = 'proxima-nova-excn';
    
    shadowText.style.fill = 0xffffff;
    shadowText.style.fontWeight = 900;

    //shadowText.zOrder = 4;
    //this.text.style.fontWeight = 'bold';
    shadowText.style.fontSize = 36.0;
    //this.text.style.align = 'center';
    shadow.addChild(shadowText);

 //Add score
    const scoreText = new PIXI.Text();
    scoreText.position.set(32 + shadowText.width, shadowHeight / 4);
    //scoreText.scale.set(1 / shadowScale, 2);
    //scoreText.position.set(0, 1);
    scoreText.anchor.set(0.0, 0.5);
    scoreText.text = '000';
    scoreText.style.fontFamily = 'SCOREBOARD';
    renderer.isDirty = true;
    
    scoreText.style.fill = 0xffffff;


    //scoreText.zOrder = 4;
    //this.text.style.fontWeight = 'bold';
    scoreText.style.fontSize = 36.0;
    //this.text.style.align = 'center';
    shadow.addChild(scoreText);

  //Add bottom shadow
  const shadow3Texture = PIXI.loader.resources['resources/Collection-Shadow-9x16.png'].texture;
  const shadow3 = new PIXI.Sprite(shadow3Texture);
  const shadow3Scale = window.innerWidth;
  const shadow3Height = 64.0;
  shadow3.name = 'shadowBottom';
  shadow3.position.set(0, window.innerHeight - scoreBarHeight*2  - trayHeight);
  shadow3.scale.set(shadow3Scale, 1);
  shadow3.zOrder = 0;
  stage.addChild(shadow3);

 //Add goal bar
 const goalBarTexture = PIXI.loader.resources['resources/Collection-Bar-Yellow-9x16.png'].texture;
 const goalBar = new PIXI.Sprite(goalBarTexture);
 const goalBarScale = window.innerWidth / 2;
 const goalBarHeight = 96.0;
 goalBar.name = 'scoreBar';
 goalBar.position.set(window.innerWidth / 2, window.innerHeight - goalBarHeight / 2 - trayHeight - 16.0);
 goalBar.scale.set(goalBarScale, 0.5);
 goalBar.zOrder = 2;
 stage.addChild(goalBar);

 //Add goal bar shadow
  const shadow1Texture = PIXI.loader.resources['resources/Collection-Shadow-9x16.png'].texture;
  const shadow1 = new PIXI.extras.TilingSprite(shadow1Texture, window.innerWidth / 2, 96);
  const shadow1Scale = window.innerWidth / 2;
  const shadow1Height = 96.0;
  shadow1.name = 'shadowGoal';
  shadow1.position.set(window.innerWidth / 2, window.innerHeight - shadow1Height / 2 - trayHeight - 16.0);
  shadow1.tileScale.set(1, 0.5);
  shadow1.zOrder = 3;
  stage.addChild(shadow1);

  //Add goal bar label
  /** @type {PIXI.Text} */
    const goalText = new PIXI.Text();
    goalText.position.set(16, shadowHeight / 4);
    //goalText.scale.set(1 / shadowScale, 2);
    //goalText.position.set(0, 1);
    goalText.anchor.set(0.0, 0.5);
    goalText.text = 'Goal:'.toUpperCase();
    goalText.style.fontFamily = 'proxima-nova-excn';
    
    goalText.style.fill = 0x806200;
    goalText.style.fontWeight = 900;

    //goalText.zOrder = 4;
    //this.text.style.fontWeight = 'bold';
    goalText.style.fontSize = 36.0;
    //this.text.style.align = 'center';
    shadow1.addChild(goalText);

//Add goal container?
  const goalContainerTexture = PIXI.loader.resources['resources/goal/goal1.png'].texture;
  const goalContainer = new PIXI.Sprite(goalContainerTexture);
  const goalContainerScale = (window.innerWidth / 2 - goalText.width - 48) / goalContainer.width;
  const goalContainerHeight = 96.0;
  goalContainer.name = 'goalContainer';
  goalContainer.position.set(window.innerWidth / 2 - 16, goalContainerHeight * goalContainerScale);
  goalContainer.scale.set(goalContainerScale, goalContainerScale);
  goalContainer.anchor.set(1,0.5);
  goalContainer.zOrder = 4;
  shadow1.addChild(goalContainer);


  //Add banner on top
  const cbTexture = PIXI.loader.resources['resources/Prediction-Banner.png'].texture;
  const cb = new PIXI.Sprite(cbTexture);
  const cbScale = window.innerWidth / cbTexture.width;
  const cbHeight = cbScale * cbTexture.height;
  cb.scale.set(cbScale, cbScale);
  cb.zOrder = 1;
  stage.addChild(cb);

  //Add Drag to Discard Banner
  const discardTexture = PIXI.loader.resources['resources/Collection-Banner-9x16.png'].texture;
  const discard = new PIXI.extras.TilingSprite(discardTexture, window.innerWidth, 36);
  //const discardScale = window.innerWidth;
  const discardHeight = 36.0;
  discard.name = 'discard';
  discard.position.set(0, cbHeight / 2);
  discard.tileScale.set(1, 0.5);
  discard.zOrder = 2;
  stage.addChild(discard);

  //Add discard label
  /** @type {PIXI.Text} */
    const discardText = new PIXI.Text();
    discardText.position.set(window.innerWidth / 2, discardHeight / 2);
    //discardText.scale.set(1 / shadowScale, 2);
    //discardText.position.set(0, 1);
    discardText.anchor.set(0.5, 0.5);
    discardText.text = 'drag plays up to discard'.toUpperCase();
    discardText.style.fontFamily = 'proxima-nova-excn';
    
    discardText.style.fill = 0xffffff;
    discardText.style.fontWeight = 900;

    //discardText.zOrder = 4;
    //this.text.style.fontWeight = 'bold';
    discardText.style.fontSize = 36.0;
    discardText.style.align = 'center';
    discard.addChild(discardText);

  //Add Drag to Score button

  //Add score button
  const scoreButtonTexture = PIXI.loader.resources['resources/Collection-Star-9x16.png'].texture;
  const scoreButton = new PIXI.Sprite(scoreButtonTexture);
  const scoreButtonScale = (window.innerWidth - 128*2) / scoreButtonTexture.width;
  const scoreButtonHeight = window.innerHeight - 128*2 - trayHeight - scoreBarHeight - discardHeight;
  scoreButton.name = 'scoreButton';
  scoreButton.position.set(window.innerWidth / 2, discardHeight * 2);
  scoreButton.scale.set(scoreButtonScale, scoreButtonScale);
  scoreButton.anchor.set(0.5, 0);
  scoreButton.zOrder = 2;
  stage.addChild(scoreButton);

  //Generate random goal
  createRandomGoal(goalContainer);

  /**
   * Begin the animation loop.
   * @param {DOMHighResTimeStamp} now
   */
  function beginDrawLoop(now) {
    const numPendingActions = Object.keys(PIXI.actionManager.actions).length;
    if (numPendingActions > 0) {
      renderer.isDirty = true;
    }

    // For mobile phones, we don't go full-blast at 60 fps.
    // Re-render only if dirty.
    if (renderer.isDirty) {
      PIXI.actionManager.update((now - lastRenderTime) / 1000);
      //fieldOverlay.update();
      renderer.render(stage);
      renderer.isDirty = false;
      
    }

    lastRenderTime = now;
    requestAnimationFrame(beginDrawLoop);
  };

  let lastRenderTime = performance.now();
  renderer.isDirty = true;
  //PlaybookBridge.notifyLoaded();
  beginDrawLoop(lastRenderTime);
};

// Create and configure the renderer.
configureRenderer(renderer);
configureWebSocket(connection);

// Load the sprites we need.
PIXI.loader
  .add('resources/Collection-BG-Wood.jpg')
  .add('resources/Collection-Banner-9x16.png')
  .add('resources/Collection-Tray-9x16.png')
  .add('resources/Collection-Star-9x16.png')
  .add('resources/Collection-Bar-Gold-9x16.png')
  .add('resources/Collection-Bar-Green-9x16.png')
  .add('resources/Collection-Bar-Yellow-9x16.png')
  .add('resources/Collection-Shadow-9x16.png')
  .add('resources/Collection-Shadow-Overturn.png')
  .add('resources/Prediction-Banner.png')
  .add('resources/goal/goal1.png')
  .add('resources/goal/goal2.png')
  .add('resources/goal/goal3.png')
  .add('resources/goal/goal4.png')
  .add('resources/goal/goal5.png')
  .add('resources/goal/goal6.png')
  .add('resources/goal/goal7.png')
  .add('resources/goal/goal8.png')
  .add('resources/goal/goal9.png')
  .add('resources/goal/goal10.png')
  .add('resources/goal/goal11.png')
  .add('resources/goal/goal12.png')
  .add('resources/goal/goal13.png')
  .add('resources/goal/goal14.png')
  .add('resources/goal/goal15.png')
  .add('resources/cards/Card-B-FirstBase.jpg')
  .add('resources/cards/Card-B-GrandSlam.jpg')
  .add('resources/cards/Card-B-HitByPitch.jpg')
  .add('resources/cards/Card-B-HomeRun.jpg')
  .add('resources/cards/Card-B-RunScored.jpg')
  .add('resources/cards/Card-B-SecondBase.jpg')
  .add('resources/cards/Card-B-Steal.jpg')
  .add('resources/cards/Card-B-ThirdBase.jpg')
  .add('resources/cards/Card-B-Walk.jpg')
  .add('resources/cards/Card-F-BlockedRun.jpg')
  .add('resources/cards/Card-F-DoublePlay.jpg')
  .add('resources/cards/Card-F-FieldersChoice.jpg')
  .add('resources/cards/Card-F-FlyOut.jpg')
  .add('resources/cards/Card-F-GroundOut.jpg')
  .add('resources/cards/Card-F-LongOut.jpg')
  .add('resources/cards/Card-F-PickOff.jpg')
  .add('resources/cards/Card-F-Strikeout.jpg')
  .add('resources/cards/Card-F-TriplePlay.jpg')
  .add('resources/cards/Card-F-UnopposedStrikeout.jpg')
  .load(setup);