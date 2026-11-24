# Thindesk Chamados

![versão](https://img.shields.io/badge/vers%C3%A3o-1.0.0-4f46e5)

Sistema de controle de chamados técnicos feito para a disciplina, baseado no exemplo do Thindesk apresentado em sala. A base de login e perfis veio do meu projeto de Login Seguro, e em cima dela montei a parte de chamados: abertura, atendimento, resolução e um dashboard com os indicadores.

Tecnologias: Java 21, Spring Boot 4, Spring Security, Thymeleaf e MongoDB Atlas. Deploy no Render com Docker.

## Acesso online

**Link da aplicação:** https://thindesk-chamados-8dki.onrender.com

Credencial de exemplo (perfil TECNICO, consegue ver o dashboard completo, atender e resolver chamados):

| E-mail | Senha |
|---|---|
| `demo@thindesk.com` | `Demo12345` |

Também dá pra criar uma conta nova pela tela de cadastro. Quem se cadastra entra como USUARIO e consegue abrir e acompanhar os próprios chamados.

> O plano gratuito do Render desliga o sistema quando fica sem uso, então o primeiro acesso pode demorar cerca de 1 minuto para carregar.

## Dashboard

![Dashboard do sistema](docs/dashboard.png)

O dashboard é a primeira tela depois do login. Ele mostra:

- total de chamados, abertos, em andamento e resolvidos (clicando no card abre a lista já filtrada);
- taxa de resolução, tempo médio de resolução e chamados de alta prioridade ainda pendentes;
- gráficos de chamados por status, por prioridade, abertos nos últimos 7 dias e resolvidos por técnico;
- os 5 chamados mais recentes.

GESTOR, TECNICO e ADMIN veem os números de todos os chamados. O USUARIO vê só os dele.

## Versionamento

O projeto segue o **Versionamento Semântico** ([semver.org](https://semver.org/lang/pt-BR/)). A versão atual é **1.0.0**.

O formato é `MAIOR.MENOR.CORREÇÃO`, com um sufixo opcional para versões de teste:

```
1.0.0
│ │ └── CORREÇÃO: correção de bug, sem mudar o que já funciona (ex.: 1.0.1)
│ └──── MENOR: funcionalidade nova que não quebra o que já existe (ex.: 1.1.0)
└────── MAIOR: mudança que quebra compatibilidade, como alterar a API (ex.: 2.0.0)

1.1.0-beta → versão de testes da 1.1.0, ainda não estável
```

Quando um número sobe, os da direita voltam para zero (de 1.2.3 para 1.3.0, por exemplo).

Onde a versão aparece no projeto:

- `pom.xml`, na tag `<version>`;
- `application.properties`, na propriedade `app.versao` (mostrada no rodapé do sistema);
- tag no Git (`v1.0.0`), marcando o commit de cada versão.

### Histórico de versões

| Versão | O que mudou |
|---|---|
| 0.1.0 | Sistema de login seguro: cadastro, login, logout, perfis e MongoDB Atlas |
| 0.2.0 | API REST de chamados (abrir, listar, consultar e resolver) e perfil TECNICO |
| 1.0.0 | Telas de chamados, status "em andamento", prioridade, dashboard e deploy no Render. Primeira versão estável |

## O que o sistema faz

- Cadastro, login e logout, com senha salva em hash BCrypt
- Abertura de chamado com título, descrição e prioridade (baixa, média ou alta)
- Lista de chamados com filtro por status
- Atendimento: o técnico assume o chamado (fica "em andamento") e depois registra a solução
- Chamado resolvido não pode mais ser alterado
- Dashboard com indicadores e gráficos
- Área de administração onde o ADMIN muda o perfil dos usuários
- API REST de chamados

### Perfis

| Perfil | O que pode fazer |
|---|---|
| USUARIO | Abre chamados e acompanha só os próprios |
| GESTOR | Vê todos os chamados e o dashboard geral |
| TECNICO | Vê todos, assume e resolve chamados |
| ADMIN | Tudo, incluindo gerenciar os perfis dos usuários |

### API REST

| Método | Rota | Descrição |
|---|---|---|
| POST | `/api/chamados` | Abre um chamado (`titulo`, `descricao` e `prioridade` opcional) |
| GET | `/api/chamados/usuario/{id}` | Lista os chamados de um usuário |
| GET | `/api/chamados/{id}` | Consulta um chamado |
| PUT | `/api/chamados/{id}/resolver` | Resolve o chamado (`solucao`), só TECNICO e ADMIN |

## Configurando o MongoDB Atlas

1. Crie um cluster gratuito no [MongoDB Atlas](https://cloud.mongodb.com).
2. Crie um usuário de banco em Database Access.
3. Libere o acesso em Network Access. Para o deploy funcionar, precisa ser `0.0.0.0/0`.
4. Em Connect > Drivers > Java, copie a connection string.
5. Troque o usuário e a senha na string e coloque `/loginseguro` antes do `?`. Fica assim:

```
mongodb+srv://USUARIO:SENHA@cluster0.xxxxx.mongodb.net/loginseguro?retryWrites=true&w=majority
```

As coleções `users`, `sessions` e `chamados` são criadas sozinhas quando o sistema roda pela primeira vez.

## Variáveis de ambiente

A senha do banco não fica no código. O projeto lê as configurações de variáveis de ambiente:

- `MONGODB_URI`: a connection string do Atlas (obrigatória)
- `ADMIN_EMAIL`: e-mail que vai virar administrador ao iniciar o sistema (opcional)
- `ADMIN_SENHA`: senha do administrador, usada só se esse e-mail ainda não tiver conta (opcional)
- `PORT`: porta do servidor (o Render preenche sozinho; localmente usa 8080)

## Como rodar localmente

No IntelliJ:

1. Abra a pasta do projeto e espere o Maven baixar as dependências.
2. Vá em Run > Edit Configurations, selecione `LoginSeguroApplication` e coloque as variáveis em Environment variables.
3. Rode e acesse http://localhost:8080

Pelo terminal (Windows):

```powershell
$env:MONGODB_URI="sua_connection_string"
$env:ADMIN_EMAIL="seu@email.com"
$env:ADMIN_SENHA="Senha12345"
.\mvnw.cmd spring-boot:run
```

Depois de mudar o perfil de alguém, a pessoa precisa sair e entrar de novo para a mudança valer.

## Deploy

O deploy é feito no [Render](https://render.com) usando o `Dockerfile` do projeto:

1. New > Web Service, conectando este repositório do GitHub.
2. Language: Docker. Plano: Free.
3. Em Environment Variables, cadastrar `MONGODB_URI`, `ADMIN_EMAIL` e `ADMIN_SENHA`.
4. A cada push na branch principal, o Render faz um novo deploy sozinho.

## Documentação

A explicação da estrutura do projeto e das decisões de design está em [docs/ARQUITETURA.md](docs/ARQUITETURA.md).
