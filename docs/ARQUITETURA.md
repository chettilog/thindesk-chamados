# Arquitetura do projeto

## Organização das pastas

```
src/main/java/com/example/loginseguro
├── config/       configurações (segurança, sessões, tema, admin inicial)
├── controller/   recebem as requisições e escolhem a tela
├── dto/          dados dos formulários, da API e do dashboard
├── model/        classes salvas no banco (User, Role, Chamado, StatusChamado, Prioridade)
├── repository/   acesso ao MongoDB
├── security/     busca o usuário no banco na hora do login
└── service/      regras de negócio (cadastro, perfis, chamados, dashboard)

src/main/resources
├── static/themes/   arquivos de tema (CSS)
└── templates/       telas em Thymeleaf
```

O caminho de uma requisição é: o Spring Security verifica se o usuário pode acessar a rota, o controller chama o service, o service usa o repository para falar com o banco, e no final o controller devolve uma tela do Thymeleaf.

## Integração com o MongoDB Atlas

A conexão é configurada no `application.properties` pela propriedade `spring.mongodb.uri`, que pega o valor da variável de ambiente `MONGODB_URI`. Assim o repositório pode ser público sem expor a senha do banco.

O banco tem três coleções:

- `users`: os usuários. O e-mail tem índice único, então o banco não aceita dois cadastros com o mesmo e-mail.
- `sessions`: as sessões de quem está logado. Com isso, o usuário continua logado mesmo se o sistema reiniciar.
- `chamados`: os chamados técnicos, com o id de quem abriu e do técnico que atendeu.

## Chamados e dashboard

As regras dos chamados ficam todas no `ChamadoService`, que é usado tanto pela API REST (`ChamadoRestController`) quanto pelas telas (`ChamadoPageController`). Assim a mesma regra vale pelos dois caminhos.

O chamado passa por três status: ABERTO, EM_ANDAMENTO (quando o técnico assume) e RESOLVIDO. Depois de resolvido ele não pode mais ser alterado.

O `DashboardService` recebe a lista de chamados que o usuário pode ver e calcula os indicadores (totais, taxa de resolução, tempo médio e os dados dos gráficos). Os gráficos são feitos só com HTML e CSS, sem biblioteca externa.

No Spring Boot 4, o suporte a sessões no MongoDB saiu do Spring e passou a ser mantido pela própria MongoDB. Por isso usei a biblioteca `mongodb-spring-session` e ativei manualmente na classe `SessionConfig`.

## Decisões de design

- **BCrypt para senhas**: a senha nunca é salva como texto, só o hash.
- **Formulário separado do usuário** (`CadastroForm`): o cadastro só aceita nome, e-mail e senha, então ninguém consegue se cadastrar como ADMIN mandando um campo a mais.
- **Regras de acesso num lugar só**: todas as permissões por perfil ficam na `SecurityConfig`.
- **Primeiro administrador por variável de ambiente**: o `AdminInitializer` promove o e-mail definido em `ADMIN_EMAIL` ao iniciar, sem precisar de senha fixa no código.
- **ADMIN não altera o próprio perfil**: evita que o sistema fique sem administrador por engano.
- **Proteção CSRF ativa** e logout feito por POST, para evitar ações enviadas por outros sites.

## Temas e layout

O cabeçalho, o menu e o rodapé ficam em `templates/fragments/layout.html` e são reaproveitados por todas as telas. O menu muda conforme o perfil de quem está logado.

As cores e estilos ficam em `static/themes/padrao/style.css`, todos como variáveis CSS no começo do arquivo. O tema ativo é escolhido pela linha `app.tema=padrao` no `application.properties`.

Para criar um tema novo, é só copiar a pasta `padrao`, mudar as cores e trocar o valor de `app.tema`. Não precisa mexer em nenhuma tela nem no código Java.

## Adaptando para outro projeto

- Novos perfis: alterar `Role.java` e as regras da `SecurityConfig`.
- Novos campos no usuário: alterar `User.java` (e `CadastroForm.java` se vierem do cadastro).
- Nova área restrita: criar um controller e adicionar a regra na `SecurityConfig`.
- Novo visual: criar um tema em `static/themes/`.