ALTER TABLE eclaw_agents
  ADD COLUMN default_chat_mode VARCHAR(32) NOT NULL DEFAULT 'standard' AFTER system_prompt;

ALTER TABLE eclaw_sessions
  ADD COLUMN session_mode VARCHAR(32) NOT NULL DEFAULT 'standard' AFTER messages,
  ADD COLUMN pending_action TEXT NULL AFTER session_mode,
  ADD COLUMN pending_action_status VARCHAR(20) NOT NULL DEFAULT 'none' AFTER pending_action;
