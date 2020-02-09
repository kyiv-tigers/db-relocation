select id_user as user_id,
       id_cbd as opid,
       token_id as token,
       contract_id as contract_id,
       transfer as transfer_token,
       null as user_date_modified
from bd00.t_bd00_cdb_contracts p
where p.token_id is not null
ORDER BY p.contract_id ASC
LIMIT :limit OFFSET :offset