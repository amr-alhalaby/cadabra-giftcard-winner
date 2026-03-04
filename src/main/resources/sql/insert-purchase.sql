INSERT INTO purchases (id, user_id, amount, job_execution_id)
VALUES (gen_random_uuid(), :userId, :amount, :jobExecutionId)

