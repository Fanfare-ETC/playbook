#ifndef PLAYBOOK_PLAYBOOK_EVENT_H
#define PLAYBOOK_PLAYBOOK_EVENT_H

#include <string>

class PlaybookEvent {
public:
    enum EventType {
        error,
        grandslam,
        shutout_inning,
        longout,
        runs_batted,
        pop_fly,
        triple_play,
        double_play,
        grounder,
        steal,
        pick_off,
        walk,
        blocked_run,
        strike_out,
        hit,
        homerun,
        pitchcount_16,
        walk_off,
        pitchcount_17,
        oneb,
        twob,
        threeb,
        unknown
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
