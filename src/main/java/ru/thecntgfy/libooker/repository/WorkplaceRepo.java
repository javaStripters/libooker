package ru.thecntgfy.libooker.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ru.thecntgfy.libooker.model.Workplace;
import ru.thecntgfy.libooker.model.projection.DayStats;
import ru.thecntgfy.libooker.model.projection.OverallStats;

import java.time.LocalDate;
import java.util.List;

@RepositoryRestResource(exported = false)
public interface WorkplaceRepo extends CrudRepository<Workplace, Long> {
    //TODO: Move to separate repo
    @Query(value = """
            select extract(hour from sum(booking.end_time - booking.start_time)) as sumHours,
                   count(distinct user_id) as visitors,
                   extract(minutes from avg(booking.end_time - booking.start_time)) as avgSessionMin
            from booking
            where date >= ?1
            and date <= ?2
            and not canceled
           """, nativeQuery = true)
    OverallStats calcOverallStats(LocalDate start, LocalDate end);

    @Query(value = """
        select count(*)
        from booking
        where date >= ?1 
        and date <= ?2
        and canceled;
        """, nativeQuery = true)
    Integer countCancelled(LocalDate start, LocalDate end);

    @Query(value = """
            with hours as (
                select generate_series as hour from generate_series(9, 21)
            ),
            day as (
              select * from booking
                where date = ?1
                and not canceled
            )
            select hour, count(distinct user_id) from hours
                              left outer join day on
                                  hour >= extract(hour from day.start_time)
                                      and cast(hour * interval '1 hour' as time) < day.end_time
            group by hour
            order by hour;
            """, nativeQuery = true)
    List<DayStats[]> calcDayStats(LocalDate date);
}
