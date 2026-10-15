package com.avrosql.cli;

import com.avrosql.lexer.Lexer;
import com.avrosql.lexer.LexicalException;
import com.avrosql.lexer.token.TokenType;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class MainMenuTest {

    /** Runs the menu with the given lines as keyboard input and returns everything it printed. */
    private static String run(String... lines) {
        String input = lines.length == 0 ? "" : String.join("\n", lines) + "\n";
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Terminal terminal = new Terminal(
                new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(out, true, StandardCharsets.UTF_8));
        new MainMenu(terminal).run();
        return out.toString(StandardCharsets.UTF_8);
    }

    @Test
    void showsEveryOptionAndPointsNewUsersToInstructions() {
        String output = run("0");

        assertTrue(output.contains("1. Tokenize SQL"));
        assertTrue(output.contains("2. How to use"));
        assertTrue(output.contains("3. Token reference"));
        assertTrue(output.contains("4. Error explanations"));
        assertTrue(output.contains("0. Exit"));
        assertTrue(output.contains("New here? Choose 2 for instructions."));
        assertTrue(output.contains("Goodbye."));
    }

    @Test
    void rejectsAChoiceThatIsNotOnTheMenu() {
        String output = run("9", "abc", "0");

        assertTrue(output.contains("'9' is not an option."));
        assertTrue(output.contains("'abc' is not an option."));
    }

    @Test
    void endOfInputExitsInsteadOfLooping() {
        assertTrue(run().contains("Goodbye."));
        assertTrue(run("1", "SELECT a").contains("Goodbye."));
    }

    @Test
    void tokenizePrintsATableAndReturnsOnEmptyLine() {
        String output = run("1", "SELECT a", "", "0");

        assertTrue(output.contains("LINE:COL"));
        assertTrue(output.contains("1:1      SELECT         SELECT"));
        assertTrue(output.contains("1:8      IDENTIFIER     a"));
        assertTrue(output.contains("1:9      EOF"));
        assertTrue(output.contains("Goodbye."));
    }

    @Test
    void tokenizeShowsTheErrorWithACaret() {
        String output = run("1", "a # b", "", "0");

        assertTrue(output.contains("  a # b"));
        assertTrue(output.contains("    ^"));
        assertTrue(output.contains("Error: Unexpected character '#' at line 1, column 3"));
    }

    @Test
    void tokenReferenceListsEveryTokenType() {
        String output = run("3", "", "0");

        for (TokenType type : TokenType.values()) {
            assertTrue(output.contains(type.name()), "missing " + type);
        }
        assertTrue(output.contains("<>  or  !="));
    }

    @Test
    void howToUseAndErrorGuideReturnToTheMenu() {
        String output = run("2", "", "4", "", "0");

        assertTrue(output.contains("HOW TO USE"));
        assertTrue(output.contains("ERROR EXPLANATIONS"));
        assertTrue(output.contains("Goodbye."));
    }

    @Test
    void everyErrorExampleReallyFails() {
        for (ErrorGuideScreen.ErrorExample example : ErrorGuideScreen.EXAMPLES) {
            assertThrows(LexicalException.class, () -> new Lexer(example.sql()).tokenize(), example.name());
        }
    }
}
