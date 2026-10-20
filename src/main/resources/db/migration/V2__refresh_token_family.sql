alter table refresh_tokens add column family_id varchar(64);

update refresh_tokens
   set family_id = md5(id::text || ':' || token_hash)
 where family_id is null;

alter table refresh_tokens alter column family_id set not null;
create index idx_refresh_tokens_family on refresh_tokens (family_id);
