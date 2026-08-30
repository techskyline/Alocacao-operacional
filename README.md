# ALOC.OP — Sistema Web de Alocação Operacional

O **ALOC.OP** é um protótipo funcional desenvolvido para auxiliar a distribuição de colaboradores entre postos de trabalho em ambientes operacionais.

O sistema permite acompanhar colaboradores disponíveis, situação dos postos, compatibilidade de alocação, movimentações pendentes e histórico das alterações realizadas.

## Funcionalidades

- Dashboard operacional
- Seleção de turno: Manhã, Tarde e Noite
- Visualização de colaboradores aguardando alocação
- Filtro de postos compatíveis
- Validação de habilidade e capacidade do posto
- Alocação e retirada de colaboradores
- Alterações pendentes antes da confirmação
- Confirmação de movimentações
- Histórico de alterações
- Visualização dos postos em lista e mapa
- Painel TV
- Persistência local dos dados da demonstração
- Restauração do estado inicial do protótipo

## Tecnologias

- Java 21
- HTML
- CSS
- Servidor HTTP Java

A aplicação foi desenvolvida sem dependência de Node.js, npm, Maven ou bibliotecas externas.

## Estrutura do projeto

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
├─ run.sh
└─ README.md
