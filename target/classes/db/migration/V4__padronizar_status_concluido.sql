-- Padronizar status de Desvio: REGISTRADO e RESOLVIDO -> CONCLUIDO
UPDATE desvio SET status = 'CONCLUIDO' WHERE status IN ('REGISTRADO', 'RESOLVIDO');

-- Padronizar status de Não Conformidade: CONCLUIDA -> CONCLUIDO
UPDATE nao_conformidade SET status = 'CONCLUIDO' WHERE status = 'CONCLUIDA';
