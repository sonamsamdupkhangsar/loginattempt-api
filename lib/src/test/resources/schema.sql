
CREATE TABLE if not exists User_Login (username varchar PRIMARY KEY, user_id UUID,
 attempt_count int,  ip varchar, status varchar, date_time timestamp);