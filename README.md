# api.sobei.org.br

API REST central da SOBEI (Sociedade Beneficente Equilíbrio de Interlagos).

## Módulos Atendidos
- **Portal de Denúncias:** Registro anônimo/identificado, consulta pública por protocolo, apuração e gestão de status.
- **Banco de Talentos / Trabalhe Conosco:** Cadastro e consulta de currículos e candidaturas.
- **Vagas de Emprego:** Publicação, atualização e inscrição em vagas abertas.
- **Fale Conosco:** Mensagens direcionadas às unidades SOBEI.
- **Congresso SOBEI:** Inscrições e credenciamento.
- **Autenticação e Usuários:** Gestão de administradores com autenticação JWT.

## Stack Tecnológica
- Java 21 + Spring Boot 4.x
- Spring Security (JWT)
- Spring Data JPA + PostgreSQL 15
- Flyway Migrations
- SpringDoc OpenAPI (Swagger)

## Execução Local
```bash
./mvnw spring-boot:run
```
Porta padrão: `8080`.