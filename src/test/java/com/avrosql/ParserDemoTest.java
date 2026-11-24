package com.avrosql;

import com.avrosql.cli.Terminal;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class ParserDemoTest {

    private static final String NL = System.lineSeparator();

    private static String run(String... lines) {
        String input = lines.length == 0 ? "" : String.join("\n", lines) + "\n";
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ParserDemo.run(new Terminal(
                new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(out, true, StandardCharsets.UTF_8)));
        return out.toString(StandardCharsets.UTF_8);
    }

    @Test
    void printsTheTree() {
        String expected = String.join(NL,
                "SelectStatement",
                "  DISTINCT",
                "  SELECT",
                "    Column city",
                "    AS c",
                "      Aggregate COUNT(*)",
                "  FROM users",
                "  WHERE",
                "    Binary GREATER",
                "      Column age",
                "      Literal 18",
                "  GROUP BY",
                "    Column city",
                "  ORDER BY",
                "    DESC",
                "      Column c",
                "  LIMIT 10",
                "  OFFSET 2",
                "");
        assertEquals(expected, ParserDemo.describe(
                "SELECT DISTINCT city, COUNT(*) AS c FROM users WHERE age > 18 GROUP BY city "
                        + "ORDER BY c DESC LIMIT 10 OFFSET 2;"));
    }

    @Test
    void pointsAtSyntaxErrors() {
        assertEquals(String.join(NL,
                        "  SELECT name FROM users",
                        "  " + " ".repeat(22) + "^",
                        "  Error: Expected SEMICOLON but found end of input (EOF) at line 1, column 23"),
                ParserDemo.describe("SELECT name FROM users"));
    }

    @Test
    void pointsAtLexicalErrors() {
        String output = ParserDemo.describe("SELECT # FROM t;");
        assertTrue(output.startsWith("  SELECT # FROM t;" + NL + "         ^" + NL + "  Error: "), output);
    }

    @Test
    void emptyLineOrEndOfInputExits() {
        String output = run("SELECT * FROM t;", "");
        assertTrue(output.contains("=== Avro SQL Parser ==="));
        assertTrue(output.contains("SELECT *"));
        assertTrue(output.endsWith("Goodbye." + NL));
        assertTrue(run().contains("Goodbye."));
    }
}
