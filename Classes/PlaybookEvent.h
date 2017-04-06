#ifndef PLAYBOOK_PLAYBOOK_EVENT_H
#define PLAYBOOK_PLAYBOOK_EVENT_H

#include <string>

class PlaybookEvent {
public:
    enum EventType {
        SHUTOUT_INNING,
        RUN_SCORED,
        FLY_OUT,
        TRIPLE_PLAY,
        DOUBLE_PLAY,
        GROUND_OUT,
        STEAL,
        PICK_OFF,
        WALK,
        BLOCKED_RUN,
        STRIKEOUT,
        HIT_BY_PITCH,
        HOME_RUN,
        PITCH_COUNT_16,
        PITCH_COUNT_17,
        SINGLE,
        DOUBLE,
        TRIPLE,
        BATTER_COUNT_4,
        BATTER_COUNT_5,
        MOST_IN_LEFT_OUTFIELD,
        MOST_IN_RIGHT_OUTFIELD,
        MOST_IN_INFIELD,
        UNKNOWN
    };

    enum Team {
        BATTING,
        FIELDING,
        NONE
    };

    struct EventTypeHash {
        template <typename T>
        std::size_t operator()(T t) const {
            return static_cast<std::size_t>(t);
        }
    };

    static EventType stringToEvent(const std::string &);
    static std::string eventToString(EventType event);
    static EventType intToEvent(int event);

    static Team getTeam(EventType event);
};

#endif //PLAYBOOK_PLAYBOOK_EVENT_H
