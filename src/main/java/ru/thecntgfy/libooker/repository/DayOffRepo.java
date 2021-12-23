package ru.thecntgfy.libooker.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ru.thecntgfy.libooker.model.DayOff;

import java.time.LocalDate;
import java.util.Optional;

@RepositoryRestResource(exported = false)
public interface DayOffRepo extends CrudRepository<DayOff, LocalDate> {
    Optional<DayOff> findByDate(LocalDate date);
}
