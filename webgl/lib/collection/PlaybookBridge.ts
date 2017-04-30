'use strict';
interface PlaybookBridge {
  getAPIUrl: () => string,
  getSectionAPIUrl: () => string,
  getPlayerID: () => string,
  notifyGameState: (stateJSON: string) => void,
  notifyLoaded: (state?: any) => void,
  goToLeaderboard: () => void,
  goToTrophyCase: () => void
}

declare global {
  interface Window {
    PlaybookBridge: PlaybookBridge
  }
}

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
     * This is a no-op for the mock bridge.
     * @type {string} stateJSON
     */
    notifyGameState: function (stateJSON: string) {
      console.log('Saving state: ', stateJSON);
      localStorage.setItem('collection', stateJSON);
    },

    /**
     * Notifies the hosting application that we have finished loading.
     * This is a no-op for the mock bridge.
     */
    notifyLoaded: function (state?: any) {
      let restoredState = localStorage.getItem('collection');
      if (restoredState === null) {
        restoredState = JSON.stringify({
          cardSlots: [
            { present: false, card: null },
            { present: false, card: null },
            { present: false, card: null },
            { present: false, card: null },
            { present: false, card: null }
          ],
          goal: null,
          score: 0,
          selectedGoal: null
        });
      }

      console.log('Loading state: ', restoredState);
      try {
        state.fromJSON(restoredState);
      } catch (e) {
        console.error('Error restoring state due to exception: ', e);
        state.reset(true);
        //window.location.href = window.location.href;
      }
    },

    /**
     * Changes to the leaderboard screen.
     * This is a no-op for the mock bridge.
     */
    goToLeaderboard: function () {},

    /**
     * Changes to the trophy case.
     * This is a no-op for the mock bridge.
     */
    goToTrophyCase: function () {}
  };
} else {
  PlaybookBridge = window.PlaybookBridge;
}

export default PlaybookBridge;
