#include <unordered_map>
#include <vector>
#include "PlaybookEvent.h"

PlaybookEvent::EventType PlaybookEvent::stringToEvent(const std::string &event) {
    std::unordered_map<std::string, EventType> map = {
        {"error", EventType::error},
        {"grand_slam", EventType::grandslam},
        {"shutout_inning", EventType::shutout_inning},
        {"long_out", EventType::longout},
        {"runs_batted_in", EventType::runs_batted},
        {"pop_fly", EventType::pop_fly},
        {"triple_play", EventType::triple_play},
        {"grounder", EventType::grounder},
        {"double_play", EventType::double_play},
        {"second_base", EventType::twob},
        {"steal", EventType::steal},
        {"pick_off", EventType::pick_off},
        {"strike_out", EventType::strike_out},
        {"walk", EventType::walk},
        {"third_base", EventType::threeb},
        {"first_base", EventType::oneb},
        {"hit", EventType::hit},
        {"home_run", EventType::homerun},
        {"pitch_count_16", EventType::pitchcount_16},
        {"blocked_run", EventType::blocked_run},
        {"walk_off", EventType::walk_off},
        {"pitch_count_17", EventType::pitchcount_17}
    };

    return map[event];
}

std::string PlaybookEvent::eventToString(EventType event) {
    std::unordered_map<EventType, std::string, EventTypeHash> map = {
        {EventType::error, "error"},
        {EventType::grandslam, "grand_slam"},
        {EventType::shutout_inning, "shutout_inning"},
        {EventType::longout, "long_out"},
        {EventType::runs_batted, "runs_batted_in"},
        {EventType::pop_fly, "pop_fly"},
        {EventType::triple_play, "triple_play"},
        {EventType::grounder, "grounder"},
        {EventType::double_play, "double_play"},
        {EventType::twob, "second_base"},
        {EventType::steal, "steal"},
        {EventType::pick_off, "pick_off"},
        {EventType::strike_out, "strike_out"},
        {EventType::walk, "walk"},
        {EventType::threeb, "third_base"},
        {EventType::oneb, "first_base"},
        {EventType::hit, "hit"},
        {EventType::homerun, "home_run"},
        {EventType::pitchcount_16, "pitch_count_16"},
        {EventType::blocked_run, "blocked_run"},
        {EventType::walk_off, "walk_off"},
        {EventType::pitchcount_17, "pitch_count_17"}
    };

    return map[event];
}

PlaybookEvent::EventType PlaybookEvent::intToEvent(int event) {
    std::vector<EventType> map = {
        EventType::error,
        EventType::grandslam,
        EventType::shutout_inning,
        EventType::longout,
        EventType::runs_batted,
        EventType::pop_fly,
        EventType::triple_play,
        EventType::double_play,
        EventType::grounder,
        EventType::steal,
        EventType::pick_off,
        EventType::walk,
        EventType::blocked_run,
        EventType::strike_out,
        EventType::hit,
        EventType::homerun,
        EventType::pitchcount_16,
        EventType::walk_off,
        EventType::pitchcount_17,
        EventType::oneb,
        EventType::twob,
        EventType::threeb
    };

    return map[event];
}