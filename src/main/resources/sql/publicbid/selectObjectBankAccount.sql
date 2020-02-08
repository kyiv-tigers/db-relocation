select obj_count.bank_count                                          as bank_account,
       json_extract_path_text(obj_count.bank_name::json, 'ua'::text) as bank_name,
       obj_count.bank_mfo                                            as bank_mfo
from bd00.t_bd00_objects obj
         left join bd00.t_bd00_contracts contract on obj.id = contract.id_object
         left join bd00.t_bd00_object_counts obj_count on contract.id_count = obj_count.id
where obj.id = :publicbid_id
limit 1;