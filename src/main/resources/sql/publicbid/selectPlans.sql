select id_user as user_id,
       id_cbd as opid,
       token_id as token,
       id_cbd as plan_id,
       transfer_id as transfer_token,
       null as user_date_modified
from bd00.t_bd00_plans p
where p.token_id is not null
ORDER BY p.d_announcement ASC
LIMIT :limit OFFSET :offset