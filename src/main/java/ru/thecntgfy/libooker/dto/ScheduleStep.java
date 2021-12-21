package ru.thecntgfy.libooker.dto;

import lombok.Data;
import ru.thecntgfy.libooker.utils.TimeRange;

@Data
public class ScheduleStep {
    public enum ScheduleState {FREE, SELF, OCCUPIED, CLOSED  }

    TimeRange range;
    ScheduleState state;

    public static ScheduleStep free(TimeRange range) {
        return new ScheduleStep(range, ScheduleState.FREE);
    }

    public static ScheduleStep self(TimeRange range) {
        return new ScheduleStep(range, ScheduleState.SELF);
    }

    public static ScheduleStep occupied(TimeRange range) {
        return new ScheduleStep(range, ScheduleState.OCCUPIED);
    }

    public static ScheduleStep closed(TimeRange range) {
        return new ScheduleStep(range, ScheduleState.CLOSED);
    }

    public ScheduleStep(TimeRange range, ScheduleState state) {
        this.range = range;
        this.state = state;
    }
}
