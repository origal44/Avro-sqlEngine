# Graph Report - Avro-sqlEngine  (2026-11-10)

## Corpus Check
- Corpus is ~14,595 words - fits in a single context window. You may not need a graph.

## Summary
- 715 nodes · 2621 edges · 17 communities (11 shown, 6 thin omitted)
- Extraction: 88% EXTRACTED · 12% INFERRED · 0% AMBIGUOUS · INFERRED: 320 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- AST Node Types
- Expression Parser Tests
- Select and Menu Tests
- Lexer Cursor and Tests
- Parser Core and Tokens
- CLI Screens and Demos
- Token Type Enum
- Lexer Error Tests
- Character Scanning Readers
- AST Printer
- Lexical Exceptions
- Parser Integration Tests
- Lexer Reader Registry
- Character Classification
- Lexer and Token Files
- Aggregate Functions
- Maven Project

## God Nodes (most connected - your core abstractions)
1. `TokenType` - 99 edges
2. `ParseExpressionTest` - 92 edges
3. `ParseSelectTest` - 75 edges
4. `Token` - 55 edges
5. `Expr` - 54 edges
6. `Parser` - 35 edges
7. `Cursor` - 34 edges
8. `ColumnRef` - 34 edges
9. `AstTest` - 32 edges
10. `Lexer` - 30 edges

## Surprising Connections (you probably didn't know these)
- `AstPrinter` --implements--> `ExprVisitor`  [EXTRACTED]
  src/main/java/com/avrosql/cli/AstPrinter.java → src/main/java/com/avrosql/parser/ast/ExprVisitor.java
- `Lexer` --references--> `Cursor`  [EXTRACTED]
  src/main/java/com/avrosql/lexer/Lexer.java → src/main/java/com/avrosql/lexer/Cursor.java
- `TokenBuilder` --references--> `Cursor`  [EXTRACTED]
  src/main/java/com/avrosql/lexer/reader/TokenBuilder.java → src/main/java/com/avrosql/lexer/Cursor.java
- `Lexer` --references--> `TokenReader`  [EXTRACTED]
  src/main/java/com/avrosql/lexer/Lexer.java → src/main/java/com/avrosql/lexer/reader/TokenReader.java
- `IdentifierReader` --references--> `TokenType`  [EXTRACTED]
  src/main/java/com/avrosql/lexer/reader/IdentifierReader.java → src/main/java/com/avrosql/lexer/token/TokenType.java

## Import Cycles
- None detected.

## Communities (17 total, 6 thin omitted)

### Community 0 - "AST Node Types"
Cohesion: 0.05
Nodes (19): Aggregate, Between, Binary, ColumnRef, Expr, ExprVisitor, In, IsNull (+11 more)

### Community 2 - "Select and Menu Tests"
Cohesion: 0.07
Nodes (4): MainMenuTest, LexerIntegrationTest, LexerSkeletonTest, ParseSelectTest

### Community 3 - "Lexer Cursor and Tests"
Cohesion: 0.09
Nodes (4): Cursor, CursorTest, IdentifierReaderTest, OperatorReaderTest

### Community 4 - "Parser Core and Tokens"
Cohesion: 0.09
Nodes (4): Token, Parser, TokenTest, ParserCursorTest

### Community 5 - "CLI Screens and Demos"
Cohesion: 0.07
Nodes (9): HowToUseScreen, MainMenu, Screen, Terminal, TokenizeScreen, TokenPrinter, LexerDemo, ParserDemo (+1 more)

### Community 6 - "Token Type Enum"
Cohesion: 0.04
Nodes (46): Category, END, KEYWORD, LITERAL, SYMBOL, TokenType, AND, AS (+38 more)

### Community 7 - "Lexer Error Tests"
Cohesion: 0.13
Nodes (3): LexerClassifyTest, NumberReaderTest, StringReaderTest

### Community 8 - "Character Scanning Readers"
Cohesion: 0.13
Nodes (4): NumberReader, CharTest, TokenBuilder, TokenReader

### Community 10 - "Lexical Exceptions"
Cohesion: 0.21
Nodes (4): LexicalException, LexicalRuntimeException, LexicalError, LexicalRuntimeExceptionTest

### Community 12 - "Lexer Reader Registry"
Cohesion: 0.20
Nodes (4): IdentifierReader, OperatorReader, StringReader, TokenReader

### Community 15 - "Aggregate Functions"
Cohesion: 0.33
Nodes (6): Function, AVG, COUNT, MAX, MIN, SUM

## Knowledge Gaps
- **52 isolated node(s):** `com.avrosql:avro-sql-engine`, `SELECT`, `FROM`, `WHERE`, `AND` (+47 more)
  These have ≤1 connection - possible missing edges. (Counts symbols only; 83 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **6 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `TokenType` connect `Token Type Enum` to `AST Node Types`, `Expression Parser Tests`, `Select and Menu Tests`, `Lexer Cursor and Tests`, `Parser Core and Tokens`, `Lexer Error Tests`, `Character Scanning Readers`, `Lexical Exceptions`, `Parser Integration Tests`, `Lexer Reader Registry`, `Character Classification`, `Lexer and Token Files`?**
  _High betweenness centrality (0.214) - this node is a cross-community bridge._
- **Are the 6 inferred relationships involving `Token` (e.g. with `.eofTokenHasEmptyText()` and `.rejectsNullTypeOrText()`) actually correct?**
  _`Token` has 6 INFERRED edges - model-reasoned connections that need verification._
- **What connects `com.avrosql:avro-sql-engine`, `SELECT`, `FROM` to the rest of the system?**
  _52 weakly-connected nodes found - possible missing edges._
- **Should `AST Node Types` be split into smaller, more focused modules?**
  _Cohesion score 0.050822417019600115 - nodes in this community are weakly interconnected._
- **Why does `Terminal` connect `CLI Screens and Demos` to `AST Node Types`?**
  _High betweenness centrality (0.056) - this node is a cross-community bridge._
- **Should `Expression Parser Tests` be split into smaller, more focused modules?**
  _Cohesion score 0.07643758765778401 - nodes in this community are weakly interconnected._
- **Why does `Expr` connect `AST Node Types` to `Expression Parser Tests`, `Select and Menu Tests`, `Parser Core and Tokens`, `AST Printer`, `Parser Integration Tests`?**
  _High betweenness centrality (0.041) - this node is a cross-community bridge._