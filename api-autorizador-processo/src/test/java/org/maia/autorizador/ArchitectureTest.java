package org.maia.autorizador;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

/**
 * As 10 regras de {@code TARGET_ARCHITECTURE.md} §2.1. Falha de regra falha o {@code verify}.
 */
@AnalyzeClasses(packages = "org.maia.autorizador", importOptions = {ImportOption.DoNotIncludeTests.class, ArchitectureTest.SemPackageInfo.class})
class ArchitectureTest {

    private static final String BASE = "org.maia.autorizador";

    /** Ignora {@code package-info} (só carregam anotações do Modulith, como {@code @NamedInterface}). */
    static final class SemPackageInfo implements ImportOption {
        @Override
        public boolean includes(com.tngtech.archunit.core.importer.Location location) {
            return !location.contains("package-info");
        }
    }

    /** Regra 1: o domínio não depende de framework. */
    @ArchTest
    static final ArchRule dominio_sem_framework = noClasses().that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..", "jakarta.persistence..", "com.fasterxml.jackson..",
                    "tools.jackson..", "lombok..", "org.slf4j..");

    /** Regra 2: dependências só para dentro. */
    @ArchTest
    static final ArchRule domain_nao_importa_camadas_externas = noClasses().that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage("..application..", "..api..", "..infrastructure..");

    @ArchTest
    static final ArchRule application_nao_importa_api_nem_infrastructure = noClasses().that().resideInAPackage("..application..")
            .should().dependOnClassesThat().resideInAnyPackage("..api..", "..infrastructure..");

    /** Regra 3: a api nunca depende de infrastructure. */
    @ArchTest
    static final ArchRule api_nao_importa_infrastructure = noClasses().that().resideInAPackage("..api..")
            .should().dependOnClassesThat().resideInAPackage("..infrastructure..");

    /** Regra 4: JPA e Spring Data só em infrastructure.persistence. */
    @ArchTest
    static final ArchRule persistencia_so_em_infrastructure_persistence = noClasses()
            .that().resideOutsideOfPackage("..infrastructure.persistence..")
            .should().beAnnotatedWith("jakarta.persistence.Entity")
            .orShould().beAnnotatedWith("jakarta.persistence.Table")
            .orShould().dependOnClassesThat().resideInAPackage("org.springframework.data..");

    /** Regra 5: casos de uso e portas. */
    @ArchTest
    static final ArchRule casos_de_uso_ficam_em_usecase = classes().that().haveSimpleNameEndingWith("UseCase")
            .and().areNotNestedClasses().should().resideInAPackage("..application.usecase..");

    @ArchTest
    static final ArchRule casos_de_uso_sao_concretos_com_service_e_transactional = classes()
            .that().resideInAPackage("..application.usecase..").and().areNotNestedClasses()
            .should().notBeInterfaces().andShould().notHaveModifier(com.tngtech.archunit.core.domain.JavaModifier.ABSTRACT)
            .andShould().beAnnotatedWith(Service.class)
            .andShould().beAnnotatedWith(Transactional.class);

    @ArchTest
    static final ArchRule application_sem_service_nem_impl = noClasses().that().resideInAPackage("..application..")
            .should().haveSimpleNameEndingWith("Service").orShould().haveSimpleNameEndingWith("Impl");

    @ArchTest
    static final ArchRule portas_sao_interfaces = classes().that().resideInAPackage("..application.port..")
            .should().beInterfaces();

    /** Regra 6: fatias sem ciclos; uma funcionalidade só usa outra por usecase/domain. */
    @ArchTest
    static final ArchRule fatias_sem_ciclos = slices().matching(BASE + ".(*)..").should().beFreeOfCycles();

    @ArchTest
    static final ArchRule funcionalidade_nao_usa_api_nem_infrastructure_de_outra =
            classes().should(new ArchCondition<JavaClass>("não usar api/infrastructure de outra funcionalidade") {
                @Override
                public void check(JavaClass origem, ConditionEvents eventos) {
                    String propria = funcionalidade(origem.getPackageName());
                    for (Dependency d : origem.getDirectDependenciesFromSelf()) {
                        String pacote = d.getTargetClass().getPackageName();
                        String alvo = funcionalidade(pacote);
                        if (propria != null && alvo != null && !alvo.equals(propria) && !alvo.equals("shared")
                                && !alvo.equals("generated") && (pacote.contains(".api") || pacote.contains(".infrastructure"))) {
                            eventos.add(SimpleConditionEvent.violated(origem,
                                    origem.getName() + " usa " + d.getTargetClass().getName()));
                        }
                    }
                }
            });

    /** Regra 7: shared não importa funcionalidade; shared.security isolado. */
    @ArchTest
    static final ArchRule shared_nao_importa_funcionalidade =
            classes().that().resideInAPackage(BASE + ".shared..")
                    .should(new ArchCondition<JavaClass>("não depender de funcionalidade") {
                        @Override
                        public void check(JavaClass origem, ConditionEvents eventos) {
                            for (Dependency d : origem.getDirectDependenciesFromSelf()) {
                                String alvo = funcionalidade(d.getTargetClass().getPackageName());
                                if (alvo != null && !alvo.equals("shared") && !alvo.equals("generated")) {
                                    eventos.add(SimpleConditionEvent.violated(origem,
                                            origem.getName() + " depende da funcionalidade " + alvo));
                                }
                            }
                        }
                    });

    @ArchTest
    static final ArchRule shared_security_isolado = noClasses().that().resideInAPackage(BASE + ".shared.security..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    BASE + ".procedimento..", BASE + ".autorizacao..", BASE + ".painel..", BASE + ".autenticacao..",
                    BASE + ".shared.logging..");

    /** SPEC-07: o domínio de autenticação não depende de Spring, JPA nem Jackson (nem de segurança compartilhada). */
    @ArchTest
    static final ArchRule autenticacao_domain_sem_framework = noClasses()
            .that().resideInAPackage(BASE + ".autenticacao.domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..", "jakarta.persistence..", "com.fasterxml.jackson..", "tools.jackson..",
                    BASE + ".shared..");

    /** SPEC-08: o domínio de procedimento é Java puro (sem framework, gerados, api nem infraestrutura; só usa shared.error). */
    @ArchTest
    static final ArchRule procedimento_domain_sem_framework = noClasses()
            .that().resideInAPackage(BASE + ".procedimento.domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..", "jakarta.persistence..", "com.fasterxml.jackson..", "tools.jackson..",
                    "org.hibernate..", BASE + ".generated..", BASE + ".procedimento.api..",
                    BASE + ".procedimento.infrastructure..");

    /** SPEC-10: o domínio de autorizacao é Java puro (só depende de procedimento.domain e shared.error). */
    @ArchTest
    static final ArchRule autorizacao_domain_sem_framework = noClasses()
            .that().resideInAPackage(BASE + ".autorizacao.domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..", "jakarta.persistence..", "com.fasterxml.jackson..", "tools.jackson..",
                    "org.hibernate..", BASE + ".generated..", BASE + ".autorizacao.api..",
                    BASE + ".autorizacao.infrastructure..", BASE + ".procedimento.api..",
                    BASE + ".procedimento.infrastructure..");

    /** SPEC-09: painel é módulo de leitura (sem domínio próprio); a aplicação não toca persistência, gerados nem adaptadores. */
    @ArchTest
    static final ArchRule painel_application_sem_infraestrutura_nem_api = noClasses()
            .that().resideInAPackage(BASE + ".painel.application..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "jakarta.persistence..", "org.hibernate..", "org.springframework.jdbc..",
                    "org.springframework.data..", BASE + ".generated..", BASE + ".painel.api..",
                    BASE + ".painel.infrastructure..", BASE + ".autorizacao.infrastructure..",
                    BASE + ".autorizacao.api..", BASE + ".procedimento.infrastructure..",
                    BASE + ".procedimento.api..");

    /** Regra 8: controllers sem Repository e sem entidade/domínio. */
    @ArchTest
    static final ArchRule controllers_sem_repository = noClasses()
            .that().areAnnotatedWith(RestController.class).or().areAnnotatedWith(Controller.class)
            .should().dependOnClassesThat().areAnnotatedWith(Repository.class)
            .orShould().dependOnClassesThat().haveSimpleNameEndingWith("Repository");

    @ArchTest
    static final ArchRule controllers_nao_usam_entidade_nem_dominio = noClasses()
            .that().areAnnotatedWith(RestController.class).or().areAnnotatedWith(Controller.class)
            .should().dependOnClassesThat().areAnnotatedWith("jakarta.persistence.Entity")
            .orShould().dependOnClassesThat().resideInAPackage("..domain.model..");

    /** Regra 9: nenhum pacote ou classe com "legado" ou "legacy" no nome. */
    @ArchTest
    static final ArchRule sem_nome_legado = classes().should(new ArchCondition<JavaClass>("não conter legado nem legacy no nome") {
        @Override
        public void check(JavaClass classe, ConditionEvents eventos) {
            String nome = classe.getName().toLowerCase();
            if (nome.contains("legado") || nome.contains("legacy")) {
                eventos.add(SimpleConditionEvent.violated(classe, classe.getName() + " contém legado/legacy"));
            }
        }
    });

    /** Regra 10: nomenclatura. */
    @ArchTest
    static final ArchRule adaptadores_de_porta_terminam_em_adapter = classes()
            .that().resideInAPackage("..infrastructure..").and().areNotNestedClasses().and().areNotInterfaces()
            .and().implement(DescribedPredicate.describe(
                    "uma porta de saída", (JavaClass c) -> c.getPackageName().contains(".application.port")))
            .should().haveSimpleNameEndingWith("Adapter");

    @ArchTest
    static final ArchRule portas_sem_sufixo_impl = noClasses().that().resideInAPackage("..application.port..")
            .should().haveSimpleNameEndingWith("Impl");

    @ArchTest
    static final ArchRule sem_injecao_por_campo = noFields().should().beAnnotatedWith(Autowired.class);

    private static String funcionalidade(String pacote) {
        if (!pacote.startsWith(BASE + ".")) {
            return null;
        }
        String resto = pacote.substring(BASE.length() + 1);
        int ponto = resto.indexOf('.');
        return ponto < 0 ? resto : resto.substring(0, ponto);
    }
}
