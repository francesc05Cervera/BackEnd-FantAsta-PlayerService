-- Additive PostgreSQL migration. Existing player IDs and auction references are untouched.
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = current_schema() AND table_name = 'players') THEN
        ALTER TABLE players ADD COLUMN IF NOT EXISTS api_football_player_id BIGINT;
        CREATE UNIQUE INDEX IF NOT EXISTS uk_players_api_football_id
            ON players (api_football_player_id) WHERE api_football_player_id IS NOT NULL;
    END IF;
END $$;
