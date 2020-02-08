select b.id_user as user_id,
       p.id_cbd as opid,
       b.opentender_id as tender_id
from bd00.t_bd00_bid_awards p
            left join bd00.t_bd00_bids b on p.id_bid = b.id
where b.token_id is not null
LIMIT :limit OFFSET :offset