package com.engseg.repository;

import com.engseg.entity.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, UUID> {
    boolean existsByCnpj(String cnpj);
    List<Empresa> findAllByAtivo(boolean ativo);
    List<Empresa> findAllByEmpresaMaeIsNullAndAtivo(boolean ativo);
    List<Empresa> findAllByEmpresaMaeIsNullAndAtivoAndExibirNoSeletor(boolean ativo, boolean exibirNoSeletor);
    List<Empresa> findAllByEmpresaMaeIsNull();
    List<Empresa> findAllByEmpresaMaeIdAndAtivo(UUID empresaMaeId, boolean ativo);
    List<Empresa> findAllByEmpresaMaeId(UUID empresaMaeId);
}
