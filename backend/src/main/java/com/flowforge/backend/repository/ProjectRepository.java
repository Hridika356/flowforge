package com.flowforge.backend.repository;

import com.flowforge.backend.model.Project;
import com.flowforge.backend.model.ProjectStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    Optional<Project> findByIdAndOwnerId(Long id, Long ownerId);

    List<Project> findByOwnerIdOrderByUpdatedAtDesc(Long ownerId);

    List<Project> findByOwnerIdAndStatusOrderByUpdatedAtDesc(Long ownerId, ProjectStatus status);

    @Query("""
            select p from Project p
            where p.owner.id = :ownerId
              and (lower(p.name) like lower(concat('%', :q, '%'))
                   or lower(coalesce(p.description, '')) like lower(concat('%', :q, '%')))
            order by p.updatedAt desc
            """)
    List<Project> searchOwned(@Param("ownerId") Long ownerId, @Param("q") String q);

    long countByOwnerId(Long ownerId);

    long countByOwnerIdAndStatus(Long ownerId, ProjectStatus status);
}
