import * as PIXI from 'pixi.js';

declare module 'pixi.js' {
  class Action {
    sprite: PIXI.Sprite;
    delta: number;
    deltaMS: number;
  }

  class ActionManager {}

  export namespace action {
    const ActionManager: ActionManager;

    class MoveTo extends Action {
      constructor(x: number, y: number, time: number);
    }

    class MoveBy extends Action {
      constructor(x: number, y: number, time: number);
    }

    class ScaleTo extends Action {
      constructor(scaleX: number, scaleY: number, time: number);
    }

    class ScaleBy extends Action {
      constructor(scaleX: number, scaleY: number, time: number);
    }

    class RotateTo extends Action {
      constructor(rotation: number, time: number);
    }

    class RotateBy extends Action {
      constructor(rotation: number, time: number);
    }

    class FadeIn extends Action {
      constructor(time: number);
    }

    class FadeOut extends Action {
      constructor(time: number);
    }

    class SkewTo extends Action {
      constructor(x: number, y: number, time: number);
    }

    class SkewBy extends Action {
      constructor(x: number, y: number, time: number);
    }

    class PivotTo extends Action {
      constructor(x: number, y: number, time: number);
    }

    class PivotBy extends Action {
      constructor(x: number, y: number, time: number);
    }

    class Blink extends Action {
      constructor(count: number, time: number);
    }

    class TintTo extends Action {
      constructor(tint: number, time: number);
    }

    class TintBy extends Action {
      constructor(tint: number, time: number);
    }

    class AlphaTo extends Action {
      constructor(alpha: number, time: number);
    }

    class AlphaBy extends Action {
      constructor(alpha: number, time: number);
    }

    class Repeat extends Action {
      constructor(action: Action, count?: number);
    }

    class Sequence extends Action {
      constructor(actions: Action[]);
    }

    class DelayTime extends Action {
      constructor(time: number);
    }

    class CallFunc extends Action {
      constructor(func: () => void);
    }
  }

  const actionManager: any;
  const TextMetrics: any;
}
