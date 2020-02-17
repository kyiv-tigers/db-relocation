select p.user_id as user_id,
       p.id as opid,
       b.opentender_id as tender_id,
       p.id_agreement as agreement_id,
       p.token as token,
       p.transfer as tranfer_token,
       p.date as user_date_modified
from bd00.t_bd00_agreements p
left join bd00.t_bd00_bids b on p.id_bid = b.id
where b.token_id is not null
and b.opentender_id is not null
LIMIT :limit OFFSET :offset