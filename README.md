# ALOC.OP — Alocação Operacional Java Web

Conversão do protótipo React/TypeScript para uma aplicação web em **Java puro**, sem Node.js, npm, Maven ou bibliotecas externas.

## O que foi convertido

- Dashboard operacional
- Seleção de turno: Manhã / Tarde / Noite
- Lista de colaboradores aguardando
- Filtro por compatibilidade de posto
- Tela de alocação com validação de habilidade e capacidade máxima
- Alterações pendentes antes da aplicação
- Retirada de colaborador de um posto
- Confirmação em lote
- Histórico das alterações
- Postos em modo lista e mapa
- Painel TV
- Persistência local do estado em `data/alocacao-state.bin`
- Restauração da demonstração

## Requisito

**JDK 21 recomendado**. Java 17 ou superior também deve funcionar.

Confirme no Prompt de Comando:

```bat
java -version
javac -version
```

## Executar no Windows

A forma mais simples é dar duplo clique em:

```text
run.bat
```

O script compila os arquivos `.java`, abre `http://localhost:8080` e inicia o servidor.

Para encerrar, volte à janela do terminal e pressione `Ctrl+C`.

## Visual Studio 2022 / 2026

O **Visual Studio** tradicional é voltado principalmente a .NET/C++ e não oferece suporte oficial completo para projetos Java. Você pode abrir esta pasta e usar o terminal integrado para executar `run.bat`, mas isso não transforma o Visual Studio em uma IDE Java completa.

Para editar Java com IntelliSense, depuração e navegação de código, use preferencialmente:

1. IntelliJ IDEA
2. VS Code + Extension Pack for Java
3. Eclipse

Se você pretende obrigatoriamente desenvolver dentro do Visual Studio 2022/2026, a tecnologia tecnicamente superior é **ASP.NET Core com C#**, não Java.

## Estrutura

```text
AlocacaoOperacionalJavaWeb/
├─ src/br/com/alocacao/
│  ├─ Main.java
│  ├─ Models.java
│  ├─ OperationService.java
│  ├─ Html.java
│  └─ WebServer.java
├─ web/
│  └─ styles.css
├─ data/
├─ run.bat
└─ README.md
```

## Arquitetura

- `Main`: inicialização.
- `WebServer`: servidor HTTP e rotas.
- `OperationService`: regras de alocação, pendências, confirmação, histórico e persistência.
- `Models`: entidades do domínio.
- `Html`: renderização das páginas.
- `styles.css`: identidade visual reaproveitada e adaptada do protótipo original.

## Próxima evolução recomendada

Esta versão é adequada como protótipo funcional/local. Para produção empresarial, a evolução correta é migrar o backend para **Spring Boot**, adicionar banco **PostgreSQL**, autenticação/perfis, API REST, logs/auditoria e integração com o sistema de ponto/Sênior.
