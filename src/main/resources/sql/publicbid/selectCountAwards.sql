select count(*)
from bd00.t_bd00_bid_awards p
left join bd00.t_bd00_bids b on p.id_bid = b.id
where b.token_id is not null;