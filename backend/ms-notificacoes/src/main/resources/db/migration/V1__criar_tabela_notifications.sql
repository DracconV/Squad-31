CREATE TABLE notifications (
  id UUID PRIMARY KEY,
  user_id UUID NOT NULL,
  title VARCHAR(160) NOT NULL,
  message VARCHAR(500) NOT NULL,
  type VARCHAR(50) NOT NULL,
  channel VARCHAR(30) NOT NULL,
  status VARCHAR(30) NOT NULL,
  reference_id UUID NULL,
  reference_type VARCHAR(80) NULL,
  created_at TIMESTAMP NOT NULL,
  read_at TIMESTAMP NULL
);

CREATE INDEX idx_notifications_user_id ON notifications (user_id);
CREATE INDEX idx_notifications_user_status ON notifications (user_id, status);
CREATE INDEX idx_notifications_created_at ON notifications (created_at DESC);
