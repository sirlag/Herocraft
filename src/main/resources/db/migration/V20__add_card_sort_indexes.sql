-- Add indexes for card sorting performance
-- These indexes improve query performance when sorting by name, action_cost, power_cost, or color

CREATE INDEX IF NOT EXISTS idx_card_name ON card(name);
CREATE INDEX IF NOT EXISTS idx_card_action_cost ON card(action_cost);
CREATE INDEX IF NOT EXISTS idx_card_power_cost ON card(power_cost);
CREATE INDEX IF NOT EXISTS idx_card_color_pip_1 ON card(color_pip_1);
