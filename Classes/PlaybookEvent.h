#ifndef PLAYBOOK_PLAYBOOK_EVENT_H
#define PLAYBOOK_PLAYBOOK_EVENT_H

#include <string>

class PlaybookEvent {
public:
    enum EventType {
        ERROR,
        GRAND_SLAM,
        SHUTOUT_INNING,
        LONG_OUT,
        RUN_BATTED_IN,
        POP_FLY,
        TRIPLE_PLAY,
        DOUBLE_PLAY,
        GROUNDER,
        STEAL,
        PICK_OFF,
        WALK,
        BLOCKED_RUN,
        STRIKE_OUT,
        HIT,
        HOME_RUN,
        PITCH_COUNT_16,
        WALK_OFF,
        PITCH_COUNT_17,
        SINGLE,
        DOUBLE,
        TRIPLE,
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
