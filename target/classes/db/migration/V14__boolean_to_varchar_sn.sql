-- Converte todos os campos BOOLEAN para VARCHAR(1) com 'S'/'N'

-- empresa.ativo
ALTER TABLE empresa ALTER COLUMN ativo TYPE VARCHAR(1) USING CASE WHEN ativo THEN 'S' ELSE 'N' END;
ALTER TABLE empresa ALTER COLUMN ativo SET DEFAULT 'S';

-- estabelecimento.ativo
ALTER TABLE estabelecimento ALTER COLUMN ativo TYPE VARCHAR(1) USING CASE WHEN ativo THEN 'S' ELSE 'N' END;
ALTER TABLE estabelecimento ALTER COLUMN ativo SET DEFAULT 'S';

-- usuario.ativo
ALTER TABLE usuario ALTER COLUMN ativo TYPE VARCHAR(1) USING CASE WHEN ativo THEN 'S' ELSE 'N' END;
ALTER TABLE usuario ALTER COLUMN ativo SET DEFAULT 'S';

-- nao_conformidade.regra_de_ouro
ALTER TABLE nao_conformidade ALTER COLUMN regra_de_ouro TYPE VARCHAR(1) USING CASE WHEN regra_de_ouro THEN 'S' ELSE 'N' END;
ALTER TABLE nao_conformidade ALTER COLUMN regra_de_ouro SET DEFAULT 'N';

-- desvio.regra_de_ouro
ALTER TABLE desvio ALTER COLUMN regra_de_ouro TYPE VARCHAR(1) USING CASE WHEN regra_de_ouro THEN 'S' ELSE 'N' END;
ALTER TABLE desvio ALTER COLUMN regra_de_ouro SET DEFAULT 'N';

-- localizacao.ativo
ALTER TABLE localizacao ALTER COLUMN ativo TYPE VARCHAR(1) USING CASE WHEN ativo THEN 'S' ELSE 'N' END;
ALTER TABLE localizacao ALTER COLUMN ativo SET DEFAULT 'S';

-- norma.ativo
ALTER TABLE norma ALTER COLUMN ativo TYPE VARCHAR(1) USING CASE WHEN ativo THEN 'S' ELSE 'N' END;
ALTER TABLE norma ALTER COLUMN ativo SET DEFAULT 'S';

-- estabelecimento_empresa.ativo
ALTER TABLE estabelecimento_empresa ALTER COLUMN ativo TYPE VARCHAR(1) USING CASE WHEN ativo THEN 'S' ELSE 'N' END;
ALTER TABLE estabelecimento_empresa ALTER COLUMN ativo SET DEFAULT 'S';
