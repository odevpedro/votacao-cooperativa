CREATE TABLE agendas (
    id UUID PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    description VARCHAR(2000),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE voting_sessions (
    id UUID PRIMARY KEY,
    agenda_id UUID NOT NULL REFERENCES agendas(id),
    opened_at TIMESTAMP WITH TIME ZONE NOT NULL,
    closes_at TIMESTAMP WITH TIME ZONE NOT NULL,
    requested_duration_seconds BIGINT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT voting_sessions_agenda_unique UNIQUE (agenda_id),
    CONSTRAINT voting_sessions_time_check CHECK (closes_at > opened_at),
    CONSTRAINT voting_sessions_duration_check CHECK (requested_duration_seconds > 0)
);

CREATE TABLE votes (
    id UUID PRIMARY KEY,
    session_id UUID NOT NULL REFERENCES voting_sessions(id),
    associate_id VARCHAR(64) NOT NULL,
    choice VARCHAR(3) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT votes_session_associate_unique UNIQUE (session_id, associate_id),
    CONSTRAINT votes_choice_check CHECK (choice IN ('SIM', 'NAO'))
);

CREATE INDEX votes_session_id_idx ON votes(session_id);
CREATE INDEX votes_session_choice_idx ON votes(session_id, choice);
