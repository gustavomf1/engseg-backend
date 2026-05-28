ALTER TABLE nao_conformidade
    RENAME COLUMN eng_responsavel_construtora_id TO responsavel_tratativa_id;

ALTER TABLE nao_conformidade
    RENAME COLUMN eng_responsavel_verificacao_id TO responsavel_nc_id;
