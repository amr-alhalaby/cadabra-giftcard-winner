ALTER TABLE validation_errors DROP COLUMN id;
ALTER TABLE validation_errors ADD COLUMN id UUID DEFAULT gen_random_uuid() PRIMARY KEY;

