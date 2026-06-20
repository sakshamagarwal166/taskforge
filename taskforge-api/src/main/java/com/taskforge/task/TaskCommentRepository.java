package com.taskforge.task;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TaskCommentRepository extends JpaRepository<TaskComment, UUID> {

    List<TaskComment> findAllByTaskIdOrderByCreatedAtAsc(UUID taskId);
}
