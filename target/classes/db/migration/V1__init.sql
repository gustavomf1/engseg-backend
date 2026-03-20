-- Empresa
CREATE TABLE empresa (
    id UUID PRIMARY KEY,
    razao_social VARCHAR(255) NOT NULL,
    cnpj VARCHAR(18) NOT NULL UNIQUE,
    nome_fantasia VARCHAR(255),
    email VARCHAR(255),
    telefone VARCHAR(20),
    ativo BOOLEAN NOT NULL DEFAULT TRUE
);

-- Estabelecimento
CREATE TABLE estabelecimento (
    id UUID PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    codigo VARCHAR(100) NOT NULL,
    empresa_id UUID NOT NULL REFERENCES empresa(id),
    cidade VARCHAR(255),
    estado VARCHAR(2),
    ativo BOOLEAN NOT NULL DEFAULT TRUE
);

-- Usuario
CREATE TABLE usuario (
    id UUID PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    senha VARCHAR(255) NOT NULL,
    perfil VARCHAR(20) NOT NULL,
    empresa_id UUID NOT NULL REFERENCES empresa(id),
    telefone VARCHAR(20),
    ativo BOOLEAN NOT NULL DEFAULT TRUE
);

-- Nao Conformidade
CREATE TABLE nao_conformidade (
    id UUID PRIMARY KEY,
    estabelecimento_id UUID NOT NULL REFERENCES estabelecimento(id),
    descricao TEXT NOT NULL,
    data_registro TIMESTAMP NOT NULL,
    tecnico_id UUID REFERENCES usuario(id),
    regra_de_ouro BOOLEAN NOT NULL DEFAULT FALSE,
    nr_relacionada VARCHAR(255) NOT NULL,
    nivel_severidade VARCHAR(20) NOT NULL,
    eng_responsavel_construtora_id UUID NOT NULL REFERENCES usuario(id),
    eng_responsavel_verificacao_id UUID NOT NULL REFERENCES usuario(id),
    data_limite_resolucao DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ABERTA'
);

-- Desvio
CREATE TABLE desvio (
    id UUID PRIMARY KEY,
    estabelecimento_id UUID NOT NULL REFERENCES estabelecimento(id),
    descricao TEXT NOT NULL,
    data_registro TIMESTAMP NOT NULL,
    tecnico_id UUID REFERENCES usuario(id),
    regra_de_ouro BOOLEAN NOT NULL DEFAULT FALSE,
    orientacao_realizada TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'REGISTRADO'
);

-- Devolutiva
CREATE TABLE devolutiva (
    id UUID PRIMARY KEY,
    nao_conformidade_id UUID NOT NULL REFERENCES nao_conformidade(id),
    descricao_plano_acao TEXT NOT NULL,
    data_devolutiva TIMESTAMP NOT NULL,
    engenheiro_id UUID REFERENCES usuario(id)
);

-- Execucao Acao
CREATE TABLE execucao_acao (
    id UUID PRIMARY KEY,
    nao_conformidade_id UUID NOT NULL REFERENCES nao_conformidade(id),
    descricao_acao_executada TEXT NOT NULL,
    data_execucao TIMESTAMP NOT NULL,
    engenheiro_id UUID REFERENCES usuario(id)
);

-- Validacao
CREATE TABLE validacao (
    id UUID PRIMARY KEY,
    nao_conformidade_id UUID NOT NULL REFERENCES nao_conformidade(id),
    parecer VARCHAR(20) NOT NULL,
    observacao TEXT,
    data_validacao TIMESTAMP NOT NULL,
    engenheiro_id UUID REFERENCES usuario(id)
);

-- Evidencia
CREATE TABLE evidencia (
    id UUID PRIMARY KEY,
    nome_arquivo VARCHAR(255) NOT NULL,
    url_arquivo TEXT NOT NULL,
    data_upload TIMESTAMP NOT NULL,
    nao_conformidade_id UUID REFERENCES nao_conformidade(id),
    execucao_acao_id UUID REFERENCES execucao_acao(id)
);

-- Default admin data
INSERT INTO empresa (id, razao_social, cnpj, nome_fantasia, email, telefone, ativo)
VALUES ('00000000-0000-0000-0000-000000000001', 'EngSeg Administração', '00000000000000', 'EngSeg', 'contato@engseg.com', '(00) 0000-0000', true);

-- BCrypt hash of "admin123"
INSERT INTO usuario (id, nome, email, senha, perfil, empresa_id, telefone, ativo)
VALUES ('00000000-0000-0000-0000-000000000001', 'Administrador', 'admin@engseg.com', '$2a$10$BWoAiefODks4bb7jkkrH5uUieEvDcgArjI0dJIlaIvnnJeo19h1Eq', 'ENGENHEIRO', '00000000-0000-0000-0000-000000000001', null, true);
