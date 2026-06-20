package com.taskforge.task;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {

    List<Task> findAllByProjectIdOrderByPositionAsc(UUID projectId);

    @Query("SELECT COALESCE(MAX(t.taskNumber), 0) FROM Task t WHERE t.project.id = :projectId")
    int findMaxTaskNumber(@Param("projectId") UUID projectId);
}
