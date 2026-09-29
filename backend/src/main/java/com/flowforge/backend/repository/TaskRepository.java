package com.flowforge.backend.repository;

import com.flowforge.backend.model.Task;
import com.flowforge.backend.model.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

    @Query("""
            select t from Task t
            left join fetch t.assignedUser
            where t.project.id = :projectId
            order by t.createdAt asc
            """)
    List<Task> findByProjectId(@Param("projectId") Long projectId);

    @Query("""
            select t from Task t
            join fetch t.project p
            left join fetch t.assignedUser
            where t.id = :taskId and p.owner.id = :ownerId
            """)
    Optional<Task> findByIdAndProjectOwnerId(@Param("taskId") Long taskId, @Param("ownerId") Long ownerId);

    /** Rows of [projectId, status, count] for the given projects. */
    @Query("""
            select t.project.id, t.status, count(t) from Task t
            where t.project.id in :projectIds
            group by t.project.id, t.status
            """)
    List<Object[]> countByProjectAndStatus(@Param("projectIds") Collection<Long> projectIds);

    /** Rows of [status, count] across every project the user owns. */
    @Query("""
            select t.status, count(t) from Task t
            where t.project.owner.id = :ownerId
            group by t.status
            """)
    List<Object[]> countByStatusForOwner(@Param("ownerId") Long ownerId);

    @Query("""
            select t from Task t
            join fetch t.project p
            left join fetch t.assignedUser
            where p.owner.id = :ownerId
              and t.status <> :excluded
              and t.dueDate is not null
              and t.dueDate <= :until
            order by t.dueDate asc
            """)
    List<Task> findOpenDueBy(@Param("ownerId") Long ownerId,
                             @Param("until") LocalDate until,
                             @Param("excluded") TaskStatus excluded);
}
