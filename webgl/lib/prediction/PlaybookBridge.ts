'use strict';
import GameStages from './GameStages';

interface PlaybookBridge {
  getAPIUrl: () => string,
  getSectionAPIUrl: () => string
  getPlayerID: () => string,
  notifyGameState: (stateJSON: string) => void,
  notifyLoaded: (state?: any) => void
  goToLeaderboard: () => void,
  goToCollection: () => void,
  goToTrophyCase: () => void,
  setShouldHandleBackPressed: (shouldHandle: boolean) => void
}

export interface PlaybookWindow extends Window {
  PlaybookBridge: PlaybookBridge
}

declare var window: PlaybookWindow;
let PlaybookBridge: PlaybookBridge;

// The Playbook Bridge is supplied via addJavaScriptInterface() on the Java
// side of the code. In the absence of that, we need to mock one.
if (!window.PlaybookBridge) {
  /** @type {Object<string, function>} */
  PlaybookBridge = {
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
      return '1';
    },

    /**
     * Notifies the hosting application of the new state of the game.
     * This saves to localStorage in the mock bridge.
     * @type {string} stateJSON
     */
    notifyGameState: function (stateJSON) {
      console.log('Saving state: ', stateJSON);
      localStorage.setItem('prediction', stateJSON);
    },

    /**
     * Notifies the hosting application that we have finished loading.
     * This restores from localStorage in the mock bridge.
     */
    notifyLoaded: function () {
      let restoredState = localStorage.getItem('prediction');
      if (restoredState === null) {
        restoredState = JSON.stringify({
          stage: GameStages.INITIAL,
          score: 0,
          balls: new Array(5).fill({ selectedTarget: null }),
          isShowingPayouts: false,
          correctBets: []
        });
      }

      console.log('Loading state: ', restoredState);
      window.dispatchEvent(new MessageEvent('message', {
        data: {
          action: 'RESTORE_GAME_STATE',
          payload: restoredState
        }
      }));
    },

    /**
     * Changes to the leaderboard screen.
     * This is a no-op for the mock bridge.
     */
    goToLeaderboard: function () {},

    /**
     * Changes to the set collection screen.
     */
    goToCollection: function () {
      window.location.href = window.location.href.replace('prediction', 'collection');
    },

    /**
     * Changes to the trophy case.
     * This is a no-op for the mock bridge.
     */
    goToTrophyCase: function () {},

    /**
     * Tells the hosting application that we should handle back button presses.
     */
    setShouldHandleBackPressed: function () {}
  };
} else {
  PlaybookBridge = window.PlaybookBridge;
}

export default PlaybookBridge;