'use strict';
import * as PIXI from 'pixi.js';
import 'pixi-action';
import * as FontFaceObserver from 'fontfaceobserver';

import PlaybookBridge from './lib/prediction/PlaybookBridge';
import PlaybookRenderer from './lib/PlaybookRenderer';
import IIncomingMessage from './lib/IIncomingMessage';
import PlaybookEvents from './lib/PlaybookEvents';

import GameStages from './lib/prediction/GameStages';
import IGameState from './lib/prediction/IGameState';
import Ball from './lib/prediction/Ball';
import FieldOverlay from './lib/prediction/FieldOverlay';
import FieldOverlayArea from './lib/prediction/FieldOverlayArea';
import BallCountSprite from './lib/prediction/BallCountSprite';
import PredictionCorrectOverlay from './lib/prediction/PredictionCorrectOverlay';
import GenericOverlay from './lib/GenericOverlay';

// Receive messages from the hosting application.
window.addEventListener('message', function (e) {
  const message = e.data;
  switch (message.action) {
    case 'RESTORE_GAME_STATE':
      console.log('Restoring state from hosting application: ');
      try {
        state.fromJSON(message.payload);
      } catch (e) {
        console.error('Error restoring state due to exception: ', e);
        state.reset(true);
        window.location.href = window.location.href;
      }
      break;
    case 'HANDLE_MESSAGE':
      console.log('Handling message from hosting application: ');
      handleIncomingMessage(message.payload);
      break;
    case 'HANDLE_BACK_PRESSED':
      console.log('Handling back pressed from hosting application');
      handleBackPressed();
      break;
  }
});

const ScoreValues = {
  [PlaybookEvents.NO_RUNS]: 1,
  [PlaybookEvents.RUN_SCORED]: 2,
  [PlaybookEvents.FLY_OUT]: 2,
  [PlaybookEvents.TRIPLE_PLAY]: 1400,
  [PlaybookEvents.DOUBLE_PLAY]: 20,
  [PlaybookEvents.GROUND_OUT]: 1,
  [PlaybookEvents.STEAL]: 18,
  [PlaybookEvents.PICK_OFF]: 7,
  [PlaybookEvents.WALK]: 2,
  [PlaybookEvents.BLOCKED_RUN]: 10,
  [PlaybookEvents.STRIKEOUT]: 1,
  [PlaybookEvents.HIT_BY_PITCH]: 10,
  [PlaybookEvents.HOME_RUN]: 10,
  [PlaybookEvents.PITCH_COUNT_16]: 1,
  [PlaybookEvents.PITCH_COUNT_17]: 1,
  [PlaybookEvents.SINGLE]: 3,
  [PlaybookEvents.DOUBLE]: 5,
  [PlaybookEvents.TRIPLE]: 32,
  [PlaybookEvents.BATTER_COUNT_4]: 1,
  [PlaybookEvents.BATTER_COUNT_5]: 1,
  [PlaybookEvents.MOST_FIELDED_BY_LEFT]: 3,
  [PlaybookEvents.MOST_FIELDED_BY_RIGHT]: 5,
  [PlaybookEvents.MOST_FIELDED_BY_INFIELDERS]: 4,
  [PlaybookEvents.MOST_FIELDED_BY_CENTER]: 2
};

/**
 * Game state.
 */
class GameState implements IGameState {
  EVENT_STAGE_CHANGED: string = 'stageChanged';
  EVENT_PREDICTION_COUNTS_CHANGED = 'predictionCountsChanged';
  EVENT_SCORE_CHANGED = 'scoreChanged';
  EVENT_OVERLAY_COUNT_CHANGED: string = 'overlayCountChanged';

  emitter: PIXI.utils.EventEmitter;

  _predictionCounts: { [play: string]: number } = {};
  _stage: string = GameStages.INITIAL;
  balls: Ball[] = [];
  _score: number = 0;
  _overlayCount: number = 0;

  constructor() {
    this.emitter = new PIXI.utils.EventEmitter();
    this.reset();
  }

  /**
   * Resets the game state.
   */
  reset(persist: boolean = false) {
    if (persist) {
      PlaybookBridge.notifyGameState(this.toJSON());
    }
  }

  get stage() {
    return this._stage;
  }

  set stage(value) {
    const oldValue = this._stage;
    this._stage = value;
    console.log('stage->', value);
    this.emitter.emit(this.EVENT_STAGE_CHANGED, value, oldValue);
    PlaybookBridge.notifyGameState(this.toJSON());
  }

  get predictionCounts() {
    return this._predictionCounts;
  }

  set predictionCounts(value) {
    const oldValue = this._predictionCounts;
    this._predictionCounts = value;
    console.log('predictionCounts->', value);
    this.emitter.emit(this.EVENT_PREDICTION_COUNTS_CHANGED, value, oldValue);
    PlaybookBridge.notifyGameState(this.toJSON());
  }

  get score() {
    return this._score;
  }

  set score(value) {
    const oldValue = this._score;
    this._score = value;
    console.log('score->', value);
    this.emitter.emit(this.EVENT_SCORE_CHANGED, value, oldValue);
    PlaybookBridge.notifyGameState(this.toJSON());
  }

  get overlayCount() {
    return this._overlayCount;
  }

  set overlayCount(value) {
    const oldValue = this._overlayCount;
    this._overlayCount = value;
    console.log('overlayCount->', value);
    this.emitter.emit(this.EVENT_OVERLAY_COUNT_CHANGED, value, oldValue);
    PlaybookBridge.notifyGameState(this.toJSON());
  }

  toJSON() {
    const savedState = {
      stage: this._stage,
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
   */
  fromJSON(state: string) {
    const restoredState = JSON.parse(state);

    const fieldOverlay = stage.getChildByName('fieldOverlay') as FieldOverlay;
    restoredState.balls.forEach((ball: any, i: number) => {
      if (ball.selectedTarget !== null) {
        const area = fieldOverlay.getAreaByName(ball.selectedTarget);
        makePrediction(this, area!, this.balls[i]);
        this.balls[i].moveToField(area!, false);
      }
    });

    // Restore this later because makePrediction changes the state.
    this.stage = restoredState.stage;
    this.score = restoredState.score;

    // Trigger initial updates.
    this.emitter.emit(this.EVENT_OVERLAY_COUNT_CHANGED, this._overlayCount, null);
  }
}

const connection = new WebSocket(PlaybookBridge.getAPIUrl());
const renderer = new PlaybookRenderer();
const stage = new PIXI.Container();
const state = new GameState();

// Content scale will be updated on first draw.
let contentScale: number | null = null;

/**
 * Sets up the renderer. Adjusts the renderer according to the size of the
 * viewport, and adds it to the DOM tree.
 * @param renderer
 */
function configureRenderer(renderer: PlaybookRenderer) {
  const resizeToFitWindow = function (renderer: PlaybookRenderer) {
    renderer.renderer.resize(window.innerWidth, window.innerHeight);
  };

  renderer.renderer.view.style.position = 'absolute';
  renderer.renderer.view.style.display = 'block';
  renderer.renderer.autoResize = true;
  resizeToFitWindow(renderer);
  document.body.appendChild(renderer.renderer.view);
  window.addEventListener('resize', () => {
    resizeToFitWindow(renderer);
  });
};

/**
 * Sets up the WebSocket connection.
 * @param connection
 */
function configureWebSocket(connection: WebSocket) {
  connection.addEventListener('open', function () {
    console.log(`Connected to ${connection.url}`);
  });

  connection.addEventListener('message', function (message: any) {
    message = JSON.parse(message.data);
    handleIncomingMessage(message);
  });
};

/**
 * Sets up web fonts.
 * @param fonts Names of fonts to configure
 */
function configureFonts(fonts: string[]) {
  // Load the required font files.
  return Promise.all(fonts.map(font => new FontFaceObserver(font).load()));
}

/**
 * Handles incoming messages.
 * @param message
 */
function handleIncomingMessage(message: IIncomingMessage) {
  switch (message.event) {
    case 'server:playsCreated':
      handlePlaysCreated(message.data);
      break;
    case 'server:notifyLockPredictions':
      handleNotifyLockPredictions(message.data);
      break;
    case 'server:lockPredictions':
      handleLockPredictions(message.data);
      break;
    case 'server:clearPredictions':
      handleClearPredictions();
      break;
    default:
  }
}

/**
 * Handles back button presses.
 */
function handleBackPressed() {
  const overlay = stage.getChildByName('overlay') as GenericOverlay;
  if (overlay.active) {
    overlay.pop();
  } else {
    console.warn('Need to handle back pressed, but none of the overlays were active!');
  }
}

/**
 * Handles plays created event.
 * @param events An array of event IDs
 */
function handlePlaysCreated(events: number[]) {
  if (state.stage === GameStages.CONFIRMED) {
    const plays = events.map(PlaybookEvents.getById);
    for (const play of plays) {
      if (state.predictionCounts[play] !== undefined) {
        const addedScore = ScoreValues[play] * state.predictionCounts[play];
        state.score += addedScore;
        reportScore(addedScore);

        const overlay = new PredictionCorrectOverlay(contentScale!, play, addedScore);
        const scoreTab = stage.getChildByName('scoreTab') as PIXI.Container;
        const score = scoreTab.getChildByName('score');
        const scoreTabGlobalPosition = scoreTab.toGlobal(score.position);
        initPredictionCorrectOverlayEvents(overlay, scoreTabGlobalPosition);
        stage.addChild(overlay);
        renderer.markDirty();

        navigator.vibrate(200);
      }
    }
  }
}

/**
 * Handle notification that predictions are about to be locked.
 */
function handleNotifyLockPredictions(data: any) {
  const current = new Date();
  const lockTime = new Date(data.lockTime);
  const countdownBanner = stage.getChildByName('countdownBanner') as PIXI.Sprite;

  function showBanner() {
    countdownBanner.visible = true;
    const moveBy = new PIXI.action.MoveBy(0.0, countdownBanner.height, 0.25);
    const fadeIn = new PIXI.action.FadeIn(0.25);
    PIXI.actionManager.runAction(countdownBanner, moveBy);
    PIXI.actionManager.runAction(countdownBanner, fadeIn);
  }

  function hideBanner() {
    const moveBy = new PIXI.action.MoveBy(0.0, -countdownBanner.height, 0.25);
    const fadeOut = new PIXI.action.FadeOut(0.25);
    const callFunc = new PIXI.action.CallFunc(() => countdownBanner.visible = false);
    const sequence = new PIXI.action.Sequence([moveBy, callFunc]);
    PIXI.actionManager.runAction(countdownBanner, sequence);
    PIXI.actionManager.runAction(countdownBanner, fadeOut);
  }

  // If 10 seconds has elapsed, don't notify.
  // Otherwise, wait till the time has elapsed.
  if (current.valueOf() - lockTime.valueOf() < 10000) {
    showBanner();
    setTimeout(hideBanner, 3000);
  }
}

/**
 * Handle lock predictions event.
 */
function handleLockPredictions(data: any) {
  state.stage = GameStages.CONFIRMED;
}

/**
 * Handle clear predictions event.
 */
function handleClearPredictions() {
  const ballSlot = stage.getChildByName('ballSlot') as PIXI.Sprite;
  //renderer.resetLastRenderTime = true;
  state.balls.forEach((ball, i) => {
    if (ball.selectedTarget !== null) {
      undoPrediction(state, ball.selectedTarget, ball);
      ball.moveToSlot(ballSlot, i);
    }
  });

  state.stage = GameStages.INITIAL;
}

/**
 * Report a scoring event to the server.
 * @param score
 */
function reportScore(score: number) {
  const request = new XMLHttpRequest();
  request.open('POST', `${PlaybookBridge.getSectionAPIUrl()}/updateScore`);
  request.setRequestHeader('Content-Type', 'application/json');
  request.send(JSON.stringify({
    cat: 'predict',
    predictScore: score,
    id: PlaybookBridge.getPlayerID()
  }));
}

/**
 * Set up events for ball counts.
 */
function createBallCountSprites(fieldOverlay: FieldOverlay, ballSprite: PIXI.Sprite) : BallCountSprite[] {
  return (fieldOverlay.children as FieldOverlayArea[]).map(area => {
    const sprite = new BallCountSprite(fieldOverlay, area, ballSprite);
    area.ballCountSprite = sprite;

    // Listen for confirmed event.
    state.emitter.on(state.EVENT_STAGE_CHANGED, (value: string) => {
      if (value === GameStages.CONFIRMED) {
        sprite.tint = 0x999999;
      } else {
        sprite.tint = 0xffffff;
      }
    });

    return sprite;
  });
}

/**
 * Sets up events for a ball.
 */
function initBallEvents(ball: Ball, ballSlot: PIXI.Sprite, fieldOverlay: FieldOverlay) {
  ball.sprite!.interactive = true;
  ball.sprite!.hitArea = new PIXI.Circle(0, 0, ball.sprite!.texture.width * 1.5 / 2);

  // Listen for changes to state.
  state.emitter.on(state.EVENT_STAGE_CHANGED, function (value: string) {
    if (value === GameStages.CONFIRMED) {
      ball.sprite!.interactive = false;
      ball.sprite!.tint = 0x999999;
    } else {
      ball.sprite!.interactive = true;
      ball.sprite!.tint = 0xffffff;
    }
  });

  const onTouchStart = function (e: PIXI.interaction.InteractionEvent) {
    // Don't allow interaction if ball is being animated.
    if (ball.isAnimating) { return; }

    ball.isBeingDragged = true;
    ball.dragPointerId = e.data.identifier!;
    ball.dragOffset = e.data.getLocalPosition(ball.sprite!);
    ball.dragOffset.x *= ball.sprite!.scale.x;
    ball.dragOffset.y *= ball.sprite!.scale.y;
    ball.dragOrigPosition = new PIXI.Point(
      ball.sprite!.position.x,
      ball.sprite!.position.y
    );
  };

  const onTouchMove = function (e: PIXI.interaction.InteractionEvent) {
    if (ball.isBeingDragged &&
        ball.dragPointerId === e.data.identifier) {
      ball.sprite!.position.set(
        e.data.global.x - ball.dragOffset!.x,
        e.data.global.y - ball.dragOffset!.y
      );

      // Check if we're above a field overlay.
      const dragTarget = fieldOverlay.getAreaByPoint(new PIXI.Point(
        e.data.global.x,
        e.data.global.y
      ));
      ball.dragTarget = dragTarget !== undefined ? dragTarget : null;

      // Re-render the scene.
      renderer.markDirty();
    }
  };

  const onTouchEnd = function (e: PIXI.interaction.InteractionEvent) {
    // Don't allow interaction if ball is being animated.
    if (ball.isAnimating || !ball.isBeingDragged) { return; }
    ball.isBeingDragged = false;

    // If there's a drag target, move the ball there.
    if (ball.dragTarget) {
      makePrediction(state, ball.dragTarget, ball);
      ball.moveToField(ball.dragTarget);
    } else if (ball.selectedTarget &&
               ballSlot.getBounds().contains(e.data.global.x, e.data.global.y)) {
      undoPrediction(state, ball.selectedTarget, ball);
      ball.moveToSlot(ballSlot, state.balls.indexOf(ball));
    } else {
      ball.moveToOrigPosition();
    }

    fieldOverlay.clearHighlightAreas();
  };

  ball.sprite!
    .on('touchstart', onTouchStart)
    .on('touchmove', onTouchMove)
    .on('touchend', onTouchEnd)
    .on('touchendoutside', onTouchEnd)
    .on('touchcancel', onTouchEnd);
}

/**
 * Initializes events for the continue banner.
 * @param continueBanner
 */
function initContinueBannerEvents(continueBanner: PIXI.Graphics) {
  state.emitter.on(state.EVENT_STAGE_CHANGED, function (stage: string) {
    continueBanner.visible = stage === GameStages.CONTINUE || stage === GameStages.CONFIRMED;
    renderer.markDirty();
  });

  continueBanner.interactive = true;
  continueBanner.on('tap', function () {
    PlaybookBridge.goToCollection();
  });
}

/**
 * Initializes events for the generic overlay.
 */
function initGenericOverlayEvents(overlay: GenericOverlay) {
  overlay.on('push', () => {
    state.overlayCount++;
  });

  overlay.on('pop', () => {
    state.overlayCount--;
  });

  state.emitter.on(state.EVENT_OVERLAY_COUNT_CHANGED, (count: number) => {
    PlaybookBridge.setShouldHandleBackPressed(count > 0);
  });
}

/**
 * Initializes events for the score tab.
 * @param scoreTab
 */
function initScoreTabEvents(scoreTab: PIXI.Sprite) {
  scoreTab.interactive = true;
  scoreTab.on('tap', () => {
    PlaybookBridge.goToLeaderboard();
  });
}

/**
 * Initializes events for the field overlay.
 */
function initFieldOverlayEvents(fieldOverlay: FieldOverlay) {
  // When prediction count changes, we need to update the ball counts.
  state.emitter.on(state.EVENT_PREDICTION_COUNTS_CHANGED, (value: { [name: string]: number }, oldValue: { [name: string]: number }) => {
    const events = new Set(Object.keys(value));
    const oldEvents = new Set(Object.keys(oldValue));

    // Compute the difference between the two counts.
    const changed = [...events].filter(x => oldEvents.has(x));
    changed.forEach(name => {
      const area = fieldOverlay.getAreaByName(name);
      const count = value[name];
      if (count > 1) {
        area!.ballCountSprite!.count = count;
        area!.ballCountSprite!.visible = true;
      } else {
        area!.ballCountSprite!.visible = false;
      }
      renderer.markDirty();
    });
  });
}

/**
 * Initializes events for a field overlay area.
 */
function initFieldOverlayAreaEvents(area: FieldOverlayArea, fieldOverlay: FieldOverlay) {
  area.interactive = true;

  const moveNextBallToField = () => {
    if (state.stage === GameStages.CONFIRMED) { return; }

    const nextBall = state.balls.find(ball => ball.selectedTarget === null);
    if (nextBall !== undefined) {
      makePrediction(state, area, nextBall);
      nextBall.moveToField(area);
    }
  }

  area
    .on('touchstart', area.highlight)
    .on('tap', area.clearHighlight)
    .on('tap', moveNextBallToField)
    .on('touchendoutside', area.clearHighlight)
    .on('touchcanceled', area.clearHighlight);
}

/**
 * Initializes events for the score.
 * @param scoreText
 */
function initScoreEvents(scoreText: PIXI.Text) {
  state.emitter.on(state.EVENT_SCORE_CHANGED, function (score: number) {
    scoreText.text = ('000000' + score).substr(-3);
    renderer.markDirty();
  });
}

/**
 * Initializes events for the payouts tab.
 * @param payoutsTab
 * @param label
 * @param fieldOverlay
 * @param grassBackground
 */
function initPayoutsTabEvents(payoutsTab: PIXI.Sprite, label: PIXI.Text,
                              fieldOverlay: FieldOverlay, grassBackground: PIXI.Sprite) {
  payoutsTab.interactive = true;
  payoutsTab.on('tap', () => {
    if (fieldOverlay.isShowingPayouts()) {
      label.text = 'Payouts'.toUpperCase();
      grassBackground.texture = PIXI.loader.resources['resources/Prediction-BG.jpg'].texture;
      fieldOverlay.hidePayouts();
    } else {
      label.text = 'Labels'.toUpperCase();
      grassBackground.texture = PIXI.loader.resources['resources/Prediction-BG-Payout.jpg'].texture;
      fieldOverlay.showPayouts();
    }
    renderer.markDirty();
  });
}

/**
 * Initializes events for the prediction correct overlay.
 * @param overlay
 * @param scoreTextPosition
 */
function initPredictionCorrectOverlayEvents(overlay: PredictionCorrectOverlay, scoreTextPosition: PIXI.Point) {
  overlay.interactive = true;
  overlay.on('tap', () => {
    const backgroundFadeOut = new PIXI.action.FadeOut(0.25);
    const backgroundCallFunc = new PIXI.action.CallFunc(() => overlay.destroy());
    const backgroundSequence = new PIXI.action.Sequence([backgroundFadeOut, backgroundCallFunc]);

    const ballMoveTo = new PIXI.action.MoveTo(
      scoreTextPosition.x, scoreTextPosition.y, 0.25
    );
    const ballScaleTo = new PIXI.action.ScaleTo(0, 0, 0.25);
    const ballFadeOut = new PIXI.action.FadeOut(0.50);

    PIXI.actionManager.runAction(overlay.ball, ballMoveTo);
    PIXI.actionManager.runAction(overlay.ball, ballScaleTo);
    PIXI.actionManager.runAction(overlay.ball, ballFadeOut);
    PIXI.actionManager.runAction(overlay.textContainer, ballMoveTo);
    PIXI.actionManager.runAction(overlay.textContainer, ballScaleTo);
    PIXI.actionManager.runAction(overlay.textContainer, ballFadeOut);
    PIXI.actionManager.runAction(overlay.background, backgroundSequence);
  });
}

/**
 * Sets up the field overlay.
 * @param balls
 */
function createFieldOverlay(balls: Ball[]) : FieldOverlay {
  const areas = {
    [PlaybookEvents.HOME_RUN]: new PIXI.Polygon([
      720.0, 1078.0,
      796.0, 1046.0,
      826.0, 970.0,
      796.0, 894.0,
      720.0, 862.0,
      644.0, 894.0,
      614.0, 970.0,
      644.0, 1046.0
    ]),
    [PlaybookEvents.TRIPLE]: new PIXI.Polygon([
      530.0, 1152.0,
      632.0, 1050.0,
      602.0, 970.0,
      632.0, 890.0,
      530.0, 788.0,
      476.0, 868.0,
      456.0, 970.0,
      476.0, 1072.0
    ]),
    [PlaybookEvents.DOUBLE]: new PIXI.Polygon([
      538.0, 780.0,
      640.0, 882.0,
      720.0, 854.0,
      800.0, 882.0,
      902.0, 780.0,
      824.0, 730.0,
      720.0, 708.0,
      616.0, 730.0
    ]),
    [PlaybookEvents.SINGLE]: new PIXI.Polygon([
      910.0, 1152.0,
      964.0, 1072.0,
      984.0, 970.0,
      964.0, 868.0,
      910.0, 788.0,
      808.0, 890.0,
      838.0, 970.0,
      808.0, 1050.0
    ]),
    [PlaybookEvents.STEAL]: new PIXI.Polygon([
      538.0, 1162.0,
      616.0, 1214.0,
      720.0, 1236.0,
      824.0, 1214.0,
      902.0, 1162.0,
      800.0, 1060.0,
      720.0, 1090.0,
      640.0, 1060.0
    ]),
    [PlaybookEvents.MOST_FIELDED_BY_INFIELDERS]: new PIXI.Polygon([
      12.0, 966.0,
      172.0, 966.0,
      720.0, 422.0,
      1268.0, 966.0,

      1428.0, 966.0,
      1398.0, 766.0,
      1306.0, 562.0,
      1202.0, 452.0,
      1120.0, 388.0,
      1026.0, 332.0,
      926.0, 296.0,
      720.0, 262.0,
      514.0, 296.0,
      414.0, 332.0,
      320.0, 388.0,
      238.0, 452.0,
      134.0, 562.0,
      42.0, 766.0
    ]),
    [PlaybookEvents.MOST_FIELDED_BY_RIGHT]: new PIXI.Polygon([
      1428.0, 12.0,
      1428.0, 836.0,
      1384.0, 692.0,
      1290.0, 532.0,
      1150.0, 394.0,
      1082.0, 348.0,
      1276.0, 12.0
    ]),
    [PlaybookEvents.MOST_FIELDED_BY_CENTER]: new PIXI.Polygon([
      176.0, 12.0,
      1264.0, 12.0,
      1072.0, 342.0,
      932.0, 282.0,
      720.0, 252.0,
      508.0, 282.0,
      368.0, 342.0
    ]),
    [PlaybookEvents.MOST_FIELDED_BY_LEFT]: new PIXI.Polygon([
      12.0, 12.0,
      164.0, 12.0,
      358.0, 348.0,
      290.0, 394.0,
      150.0, 532.0,
      56.0, 692.0,
      12.0, 836.0
    ]),
    [PlaybookEvents.RUN_SCORED]: new PIXI.Polygon([
      726.0, 442.0,
      726.0, 696.0,
      916.0, 778.0,
      996.0, 966.0,
      1252.0, 966.0
    ]),
    [PlaybookEvents.FLY_OUT]: new PIXI.Polygon([
      714.0, 442.0,
      188.0, 966.0,
      444.0, 966.0,
      524.0, 778.0,
      714.0, 696.0
    ]),
    [PlaybookEvents.GROUND_OUT]: new PIXI.Polygon([
      714.0, 1526.0,
      714.0, 1250.0,
      520.0, 1168.0,
      440.0, 978.0,
      188.0, 978.0
    ]),
    [PlaybookEvents.NO_RUNS]: new PIXI.Polygon([
      726.0, 1526.0,
      1252.0, 978.0,
      1000.0, 978.0,
      920.0, 1168.0,
      726.0, 1250.0
    ]),
    [PlaybookEvents.BATTER_COUNT_5]: new PIXI.Polygon([
      1010.0, 1242.0,
      1428.0, 1242.0,
      1428.0, 980.0,
      1270.0, 980.0
    ]),
    [PlaybookEvents.BATTER_COUNT_4]: new PIXI.Polygon([
      720.0, 1512.0,
      1428.0, 1512.0,
      1428.0, 1252.0,
      998.0, 1252.0
    ]),
    [PlaybookEvents.PITCH_COUNT_17]: new PIXI.Polygon([
      12.0, 1242.0,
      430.0, 1242.0,
      170.0, 980.0,
      12.0, 980.0
    ]),
    [PlaybookEvents.PITCH_COUNT_16]: new PIXI.Polygon([
      12.0, 1512.0,
      720.0, 1512.0,
      442.0, 1252.0,
      12.0, 1252.0
    ]),
    [PlaybookEvents.STRIKEOUT]: new PIXI.Polygon([
      12.0, 1738.0,
      1428.0, 1738.0,
      1428.0, 1526.0,
      12.0, 1526.0
    ])
  };

  const fieldOverlay = new FieldOverlay(renderer, areas, balls, initFieldOverlayAreaEvents);

  // The infield area is concave - provide a centroid offset.
  const infieldArea = fieldOverlay.getAreaByName(PlaybookEvents.MOST_FIELDED_BY_INFIELDERS);
  infieldArea!.centroidOffset = new PIXI.Point(0, -246.0);

  return fieldOverlay;
};

/**
 * Returns the world space position for a ball slot.
 * @param ballTexture
 * @param ballSlot
 * @param i
 */
function getBallPositionForSlot(ballTexture: PIXI.Texture, ballSlot: PIXI.Sprite, i: number) {
  const ballScale = ballSlot.texture.height / ballTexture.height / 1.5;
  return ballSlot.toGlobal(new PIXI.Point(
    120 + ballTexture.width * i * ballScale,
    ballSlot.texture.height / 2
  ));
};


/**
 * Makes a prediction using the specified ball.
 * @param state
 * @param area
 * @param ball
 */
function makePrediction(state: GameState, area: FieldOverlayArea, ball: Ball) {
  if (ball.selectedTarget !== null) {
    undoPrediction(state, ball.selectedTarget, ball);
  }

  ball.selectedTarget = area;
  const predictionCounts = Object.assign({}, state.predictionCounts);

  if (predictionCounts[area.name!] === undefined) {
    predictionCounts[area.name!] = 0;
  }

  predictionCounts[area.name!]++;

  // Check if all the balls have selected targets.
  if (state.balls.every(ball => ball.selectedTarget !== null)) {
    state.stage = GameStages.CONTINUE;
  } else {
    state.stage = GameStages.INITIAL;
  }

  state.predictionCounts = predictionCounts;
}

/**
 * Undoes a prediction using the specified ball.
 * @param state
 * @param name
 * @param ball
 */
function undoPrediction(state: GameState, area: FieldOverlayArea, ball: Ball) {
  const predictionCounts = Object.assign({}, state.predictionCounts);
  predictionCounts[area.name!]--;
  if (predictionCounts[area.name!] === 0) {
    delete predictionCounts[area.name!];
  }

  ball.selectedTarget = null;

  // Check if all the balls have selected targets.
  if (state.stage === GameStages.CONTINUE) {
    state.stage = GameStages.INITIAL;
  }

  state.predictionCounts = predictionCounts;
}

function setup() {
  // Add grass to screen.
  const grassTexture = PIXI.loader.resources['resources/Prediction-BG.jpg'].texture;
  const grass = new PIXI.Sprite(grassTexture);
  grass.scale.x = window.innerWidth / grassTexture.width;
  grass.scale.y = window.innerHeight / grassTexture.height;

  // Add banner on top to screen.
  const bannerTexture = PIXI.loader.resources['resources/prediction.json'].textures!['Prediction-Banner.png'];
  const banner = new PIXI.Sprite(bannerTexture);
  const bannerScale = window.innerWidth / bannerTexture.width;
  const bannerHeight = bannerScale * bannerTexture.height;
  banner.scale.set(bannerScale, bannerScale);

  // Add ball slot to screen.
  const ballSlotTexture = PIXI.loader.resources['resources/prediction.json'].textures!['Prediction-Holder-BallsSlot.png'];
  const ballSlot = new PIXI.Sprite(ballSlotTexture);
  const ballSlotScale = window.innerWidth / ballSlotTexture.width;
  const ballSlotHeight = ballSlotScale * ballSlotTexture.height;
  ballSlot.name = 'ballSlot';
  ballSlot.position.set(0, window.innerHeight - ballSlotHeight);
  ballSlot.scale.set(ballSlotScale, ballSlotScale);

  // Use the ball slot as the content scale factor.
  contentScale = ballSlot.scale.x;

  // Add overlay to screen.
  const fieldOverlay = createFieldOverlay(state.balls);
  const fieldOverlayScaleX = window.innerWidth / fieldOverlay.texture.width;
  const fieldOverlayScaleY = (window.innerHeight - bannerHeight - ballSlotHeight) / fieldOverlay.texture.height;
  const fieldOverlayScale = Math.min(fieldOverlayScaleX, fieldOverlayScaleY);
  fieldOverlay.name = 'fieldOverlay';
  fieldOverlay.position.set(
    window.innerWidth / 2,
    (window.innerHeight - bannerHeight - ballSlotHeight) / 2 + bannerHeight
  );
  fieldOverlay.scale.set(fieldOverlayScale, fieldOverlayScale);
  fieldOverlay.anchor.set(0.5, 0.5);
  initFieldOverlayEvents(fieldOverlay);

  // Add lock countdown to screen.
  const countdownBanner = new PIXI.Graphics();
  const countdownBannerHeight = 128.0 * contentScale;
  countdownBanner.beginFill(0x002b65);
  countdownBanner.drawRect(0, 0, window.innerWidth, countdownBannerHeight);
  countdownBanner.endFill();
  countdownBanner.position.set(0, -countdownBannerHeight + bannerHeight / 2);
  countdownBanner.name = 'countdownBanner';
  countdownBanner.visible = false;

  const countdownBannerText = new PIXI.Text('10 seconds left to predict'.toUpperCase());
  countdownBannerText.style.fontFamily = 'proxima-nova-excn';
  countdownBannerText.style.fontSize = 104.0 * contentScale;
  countdownBannerText.style.fill = 0xffffff;
  const countdownBannerTextMetrics = PIXI.TextMetrics.measureText(countdownBannerText.text, countdownBannerText.style);
  countdownBannerText.position.set(window.innerWidth / 2, countdownBannerTextMetrics.height / 2);
  countdownBannerText.anchor.set(0.5, 0.5);
  countdownBanner.addChild(countdownBannerText);

  // Add score tab.
  const scoreTabTexture = PIXI.loader.resources['resources/prediction.json'].textures!['Prediction-Scoretab.png'];
  const scoreTab = new PIXI.Sprite(scoreTabTexture);
  const scoreTabScale = ballSlot.height / scoreTabTexture.height;
  scoreTab.name = 'scoreTab';
  scoreTab.scale.set(scoreTabScale, scoreTabScale);
  scoreTab.position.set(0, ballSlot.position.y - scoreTab.height);
  initScoreTabEvents(scoreTab);

  const scoreTabLabel = new PIXI.Text('Score:'.toUpperCase());
  const scoreTabLabelFontSize = 32.0;
  const scoreTabLabelScale = ((scoreTab.height / 2 - 8.0) / scoreTabLabelFontSize) * (1 / scoreTabScale);
  scoreTabLabel.style.fill = 0xffffff;
  scoreTabLabel.style.fontSize = scoreTabLabelFontSize;
  scoreTabLabel.style.fontFamily = 'proxima-nova-excn';
  scoreTabLabel.style.fontWeight = '900';
  scoreTabLabel.anchor.set(0.0, 1.0);
  scoreTabLabel.position.set(16.0 * scoreTabLabelScale, scoreTabTexture.height / 2);
  scoreTabLabel.scale.set(scoreTabLabelScale, scoreTabLabelScale);
  scoreTab.addChild(scoreTabLabel);

  const score = new PIXI.Text('000');
  const scoreScale = scoreTabLabelScale;
  score.name = 'score';
  score.style.fill = 0xffffff;
  score.style.fontSize = 32.0;
  score.style.fontFamily = 'SCOREBOARD';
  score.anchor.set(0.0, 0.0);
  score.position.set(16.0 * scoreScale, scoreTabTexture.height / 2);
  score.scale.set(scoreScale, scoreScale);
  initScoreEvents(score);
  scoreTab.addChild(score);

  // Add payouts tab.
  const payoutsTabTexture = PIXI.loader.resources['resources/prediction.json'].textures!['Prediction-Oddstab.png'];
  const payoutsTab = new PIXI.Sprite(payoutsTabTexture);
  const payoutsTabScale = ballSlot.height / payoutsTabTexture.height;
  payoutsTab.scale.set(payoutsTabScale, payoutsTabScale);
  payoutsTab.position.set(window.innerWidth - payoutsTab.width, ballSlot.position.y - payoutsTab.height);

  const payoutsTabLabel = new PIXI.Text('Payouts'.toUpperCase());
  const payoutsTabFontSize = 32.0;
  const payoutsTabLabelScale = ((payoutsTab.height / 2 - 8.0) / payoutsTabFontSize) * (1 / payoutsTabScale);
  payoutsTabLabel.style.fill = 0xffffff;
  payoutsTabLabel.style.fontSize = payoutsTabFontSize;
  payoutsTabLabel.style.fontFamily = 'proxima-nova-excn';
  payoutsTabLabel.style.fontWeight = '900';
  payoutsTabLabel.anchor.set(1.0, 1.0);
  payoutsTabLabel.position.set((payoutsTab.width - 16.0) * (1 / payoutsTabScale), payoutsTabTexture.height / 2);
  payoutsTabLabel.scale.set(payoutsTabLabelScale, payoutsTabLabelScale);
  payoutsTab.addChild(payoutsTabLabel);

  const payoutsTabArrow = new PIXI.Text('\u22b3');
  const payoutsTabArrowScale = payoutsTabLabelScale;
  payoutsTabArrow.style.fill = 0xffffff;
  payoutsTabArrow.style.fontSize = 32.0;
  payoutsTabArrow.style.fontFamily = 'proxima-nova-excn';
  payoutsTabArrow.anchor.set(1.0, 0.0);
  payoutsTabArrow.scale.set(-payoutsTabArrowScale, payoutsTabArrowScale);
  payoutsTabArrow.position.set(
    (payoutsTab.width - 16.0) * (1 / payoutsTabScale) - payoutsTabArrow.width,
    (payoutsTabTexture.height / 2)
  );
  initPayoutsTabEvents(payoutsTab, payoutsTabLabel, fieldOverlay, grass);
  payoutsTab.addChild(payoutsTabArrow);

  // Add balls to scene.
  const ballSprites = [];
  for (let i = 0; i < 5; i++) {
    const ballTexture = PIXI.loader.resources['resources/prediction.json'].textures!['Item-Ball.png'];
    const ballSprite = new PIXI.Sprite(ballTexture);
    const ballScale = ballSlotHeight / ballTexture.height / 1.5;
    const ballPosition = getBallPositionForSlot(ballTexture, ballSlot, i);
    ballSprite.anchor.set(0.5, 0.5);
    ballSprite.scale.set(ballScale, ballScale);
    ballSprite.position.set(ballPosition.x, ballPosition.y);

    const ball = new Ball(renderer, getBallPositionForSlot);
    ball.sprite = ballSprite;
    state.balls.push(ball);

    initBallEvents(ball, ballSlot, fieldOverlay);
    ballSprites.push(ballSprite);
  }

  // Add ball counts to overlay areas.
  const ballCountSprites = createBallCountSprites(fieldOverlay, state.balls[0].sprite!);

  // Add continue banner.
  const continueBanner = new PIXI.Graphics();
  continueBanner.beginFill(0xc53626);
  continueBanner.drawRect(0, 0, window.innerWidth, ballSlot.height);
  continueBanner.endFill();
  continueBanner.position.set(0, window.innerHeight - ballSlot.height);
  continueBanner.visible = false;
  initContinueBannerEvents(continueBanner);

  const continueBannerLabel = new PIXI.Text('Continue \u22b2'.toUpperCase());
  continueBannerLabel.style.fontFamily = 'proxima-nova-excn';
  continueBannerLabel.style.fontWeight = '900';
  continueBannerLabel.style.fontSize = 104.0 * contentScale;
  continueBannerLabel.style.fill = 0xffffff;
  continueBannerLabel.anchor.set(1.0, 0.5);
  continueBannerLabel.position.set(
    window.innerWidth - (64.0 * contentScale),
    continueBanner.height / 2
  );
  continueBanner.addChild(continueBannerLabel);

  // Add overlays.
  const genericOverlay = new GenericOverlay();
  genericOverlay.name = 'overlay';
  initGenericOverlayEvents(genericOverlay);

  // Add the items in order of appearance.
  stage.addChild(grass);
  stage.addChild(banner);
  stage.addChild(ballSlot);
  stage.addChild(fieldOverlay);
  stage.addChild(countdownBanner);
  stage.addChild(scoreTab);
  stage.addChild(payoutsTab);
  ballSprites.forEach(sprite => stage.addChild(sprite));
  ballCountSprites.forEach(sprite => stage.addChild(sprite));
  stage.addChild(continueBanner);

  /**
   * Begin the animation loop.
   * @param {DOMHighResTimeStamp} now
   */
  function beginDrawLoop(now: number) {
    const numPendingActions = Object.keys(PIXI.actionManager.actions).length;
    if (numPendingActions > 0) {
      renderer.markDirty();
    }

    // For mobile phones, we don't go full-blast at 60 fps.
    // Re-render only if dirty.
    if (renderer.dirty) {
      PIXI.actionManager.update((now - lastRenderTime) / 1000);
      fieldOverlay.update();
      renderer.render(stage);
    }

    lastRenderTime = now;
    requestAnimationFrame(beginDrawLoop);
  };

  let lastRenderTime = performance.now();
  renderer.markDirty();
  PlaybookBridge.notifyLoaded(state);
  beginDrawLoop(lastRenderTime);
};

// Create and configure the renderer.
configureRenderer(renderer);
configureWebSocket(connection);

// Load the fonts and sprites we need.
configureFonts(['proxima-nova', 'proxima-nova-excn', 'SCOREBOARD', 'rockwell'])
  .then(() => {
    PIXI.loader
      .add('resources/prediction.json')
      .add('resources/Prediction-BG.jpg')
      .add('resources/Prediction-BG-Payout.jpg')
      .add('resources/Prediction-Overlay.png')
      .add('resources/Prediction-Overlay-Payout.png')
      .add('resources/Item-Ball-Rotated.png')
      .load(setup);
  });
