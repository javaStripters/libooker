package ru.thecntgfy.libooker.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ru.thecntgfy.libooker.model.Workplace;

@RepositoryRestResource(exported = false)
public interface WorkplaceRepo extends CrudRepository<Workplace, Long> {
}
