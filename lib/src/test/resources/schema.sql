
CREATE TABLE if not exists Login_Attempt (username varchar PRIMARY KEY, user_id UUID,
 attempt_count int,  ip varchar, status varchar, date_time timestamp);