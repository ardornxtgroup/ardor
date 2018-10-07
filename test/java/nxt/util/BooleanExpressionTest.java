/*
 * Copyright © 2013-2016 The Nxt Core Developers.
 * Copyright © 2016-2018 Jelurida IP B.V.
 *
 * See the LICENSE.txt file at the top-level directory of this distribution
 * for licensing information.
 *
 * Unless otherwise agreed in a custom licensing agreement with Jelurida B.V.,
 * no part of this software, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.txt file.
 *
 * Removal or modification of this copyright notice is prohibited.
 *
 */
 
 package nxt.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class BooleanExpressionTest {

    @Test
    public void testAnd() throws BooleanExpression.BadSyntaxException {
        BooleanExpression expression = parseExpression("A & B", null);

        Assert.assertEquals(BooleanExpression.Value.TRUE, expression.evaluate(Stream.of(
                val("A", BooleanExpression.Value.TRUE),
                val("B", BooleanExpression.Value.TRUE)
        ).collect(valuesToMap())));

        Assert.assertEquals(BooleanExpression.Value.FALSE, expression.evaluate(Stream.of(
                val("A", BooleanExpression.Value.FALSE),
                val("B", BooleanExpression.Value.TRUE)
        ).collect(valuesToMap())));

        Assert.assertEquals(BooleanExpression.Value.FALSE, expression.evaluate(Stream.of(
                val("A", BooleanExpression.Value.TRUE),
                val("B", BooleanExpression.Value.FALSE)
        ).collect(valuesToMap())));

        Assert.assertEquals(BooleanExpression.Value.FALSE, expression.evaluate(Stream.of(
                val("A", BooleanExpression.Value.FALSE),
                val("B", BooleanExpression.Value.FALSE)
        ).collect(valuesToMap())));
    }

    @Test
    public void testOr() throws BooleanExpression.BadSyntaxException {
        BooleanExpression expression = parseExpression("A | B", null);

        Assert.assertEquals(BooleanExpression.Value.TRUE, expression.evaluate(Stream.of(
                val("A", BooleanExpression.Value.TRUE),
                val("B", BooleanExpression.Value.TRUE)
        ).collect(valuesToMap())));

        Assert.assertEquals(BooleanExpression.Value.TRUE, expression.evaluate(Stream.of(
                val("A", BooleanExpression.Value.FALSE),
                val("B", BooleanExpression.Value.TRUE)
        ).collect(valuesToMap())));

        Assert.assertEquals(BooleanExpression.Value.TRUE, expression.evaluate(Stream.of(
                val("A", BooleanExpression.Value.TRUE),
                val("B", BooleanExpression.Value.FALSE)
        ).collect(valuesToMap())));

        Assert.assertEquals(BooleanExpression.Value.FALSE, expression.evaluate(Stream.of(
                val("A", BooleanExpression.Value.FALSE),
                val("B", BooleanExpression.Value.FALSE)
        ).collect(valuesToMap())));
    }

    @Test
    public void testNot() throws BooleanExpression.BadSyntaxException {
        BooleanExpression expression = parseExpression("!A", null);

        Assert.assertEquals(BooleanExpression.Value.FALSE, expression.evaluate(Stream.of(
                val("A", BooleanExpression.Value.TRUE)
        ).collect(valuesToMap())));

        Assert.assertEquals(BooleanExpression.Value.TRUE, expression.evaluate(Stream.of(
                val("A", BooleanExpression.Value.FALSE)
        ).collect(valuesToMap())));
    }

    @Test
    public void testTwoOccurrencesOfVariable() throws BooleanExpression.BadSyntaxException {
        BooleanExpression expression = parseExpression("A1 & B2 | !C3 & A1", null);
        Assert.assertEquals(BooleanExpression.Value.FALSE, expression.evaluate(Stream.of(
                val("A1", BooleanExpression.Value.FALSE)
        ).collect(valuesToMap())));

        Assert.assertEquals(BooleanExpression.Value.TRUE, expression.evaluate(Stream.of(
                val("A1", BooleanExpression.Value.TRUE),
                val("B2", BooleanExpression.Value.TRUE)
        ).collect(valuesToMap())));

        Assert.assertEquals(BooleanExpression.Value.TRUE, expression.evaluate(Stream.of(
                val("A1", BooleanExpression.Value.TRUE),
                val("C3", BooleanExpression.Value.FALSE)
        ).collect(valuesToMap())));

        Assert.assertEquals(BooleanExpression.Value.UNKNOWN, expression.evaluate(Stream.of(
                val("B2", BooleanExpression.Value.TRUE),
                val("C3", BooleanExpression.Value.TRUE)
        ).collect(valuesToMap())));
    }

    @Test
    public void testComplexExpression() throws BooleanExpression.BadSyntaxException {
        BooleanExpression expression = parseExpression("A & !C | !A & B", null);

        // Test unknown results
        Assert.assertEquals(BooleanExpression.Value.UNKNOWN, expression.evaluate(Collections.EMPTY_MAP));
        Assert.assertEquals(BooleanExpression.Value.UNKNOWN, expression.evaluate(Stream.of(
                val("A", BooleanExpression.Value.FALSE)
        ).collect(valuesToMap())));
        Assert.assertEquals(BooleanExpression.Value.UNKNOWN, expression.evaluate(Stream.of(
                val("B", BooleanExpression.Value.FALSE)
        ).collect(valuesToMap())));
        Assert.assertEquals(BooleanExpression.Value.UNKNOWN, expression.evaluate(Stream.of(
                val("C", BooleanExpression.Value.FALSE)
        ).collect(valuesToMap())));
        Assert.assertEquals(BooleanExpression.Value.UNKNOWN, expression.evaluate(Stream.of(
                val("A", BooleanExpression.Value.TRUE)
        ).collect(valuesToMap())));
        Assert.assertEquals(BooleanExpression.Value.UNKNOWN, expression.evaluate(Stream.of(
                val("B", BooleanExpression.Value.TRUE)
        ).collect(valuesToMap())));
        Assert.assertEquals(BooleanExpression.Value.UNKNOWN, expression.evaluate(Stream.of(
                val("C", BooleanExpression.Value.TRUE)
        ).collect(valuesToMap())));
        Assert.assertEquals(BooleanExpression.Value.UNKNOWN, expression.evaluate(Stream.of(
                val("B", BooleanExpression.Value.TRUE),
                val("C", BooleanExpression.Value.TRUE)
        ).collect(valuesToMap())));
        Assert.assertEquals(BooleanExpression.Value.UNKNOWN, expression.evaluate(Stream.of(
                val("A", BooleanExpression.Value.FALSE),
                val("C", BooleanExpression.Value.TRUE)
        ).collect(valuesToMap())));

        // Test known results
        Assert.assertEquals(BooleanExpression.Value.FALSE, expression.evaluate(Stream.of(
                val("A", BooleanExpression.Value.TRUE),
                val("C", BooleanExpression.Value.TRUE)
        ).collect(valuesToMap())));
        Assert.assertEquals(BooleanExpression.Value.TRUE, expression.evaluate(Stream.of(
                val("A", BooleanExpression.Value.TRUE),
                val("C", BooleanExpression.Value.FALSE)
        ).collect(valuesToMap())));
        Assert.assertEquals(BooleanExpression.Value.FALSE, expression.evaluate(Stream.of(
                val("A", BooleanExpression.Value.FALSE),
                val("B", BooleanExpression.Value.FALSE)
        ).collect(valuesToMap())));
        Assert.assertEquals(BooleanExpression.Value.TRUE, expression.evaluate(Stream.of(
                val("A", BooleanExpression.Value.FALSE),
                val("B", BooleanExpression.Value.TRUE)
        ).collect(valuesToMap())));
    }

    @Test
    public void testInfixOperators() {
        parseExpression("A &", new BooleanExpression.BadSyntaxException(3, BooleanExpression.SyntaxError.LITERAL_EXPECTED));
        parseExpression("A & B | & C", new BooleanExpression.BadSyntaxException(8, BooleanExpression.SyntaxError.LITERAL_EXPECTED));
        parseExpression("A & B C", new BooleanExpression.BadSyntaxException(6, BooleanExpression.SyntaxError.OPERATOR_OR_AND_EXPECTED));
    }

    @Test
    public void testNegation() {
        parseExpression("A & ! &", new BooleanExpression.BadSyntaxException(6, BooleanExpression.SyntaxError.VARIABLE_EXPECTED));
        parseExpression("A & B ! | C", new BooleanExpression.BadSyntaxException(6, BooleanExpression.SyntaxError.UNEXPECTED_NEGATION));
    }

    @Test
    public void testVariable() {
        parseExpression("A & !0", new BooleanExpression.BadSyntaxException(5, BooleanExpression.SyntaxError.DIGIT_AT_VARIABLE_START));
        parseExpression("A & (!A)", new BooleanExpression.BadSyntaxException(4, BooleanExpression.SyntaxError.ILLEGAL_CHARACTER, '(', (int)'('));
    }

    @Test
    public void testDuplicateLiterals() {
        checkOptimality("B | A & A & A", new BooleanExpression.SemanticWarning(7,
                BooleanExpression.SemanticWarningType.DUPLICATE_LITERAL, " A ", " A & A & A"));

        checkOptimality("A | A | B", new BooleanExpression.SemanticWarning(0,
                BooleanExpression.SemanticWarningType.ABSORPTION_2_POSSIBLE, "A ", " A ", 3));
    }

    @Test
    public void testPreEvaluations() {
        checkOptimality("C1 | A & !A & F | B",new BooleanExpression.SemanticWarning(8,
                BooleanExpression.SemanticWarningType.CONJUNCTION_ALWAYS_FALSE, " A & !A & F ", "A"));

        checkOptimality("A | !A | B", new BooleanExpression.SemanticWarning(3,
                BooleanExpression.SemanticWarningType.DISJUNCTION_ALWAYS_TRUE, "A"));
    }

    @Test
    public void testAbsorptionLaw() {
        checkOptimality("E & F | !A & B & !C | B & !C", new BooleanExpression.SemanticWarning(7,
                BooleanExpression.SemanticWarningType.ABSORPTION_2_POSSIBLE, " !A & B & !C ", " B & !C", 21));

        checkOptimality("E & F | !A & B & C | B & !C", null);
    }

    @Test
    public void testDistributionOfOr() {
        checkOptimality("A | !A & B & !C | D", new BooleanExpression.SemanticWarning(3,
                BooleanExpression.SemanticWarningType.DISTRIBUTIVITY_OF_OR_POSSIBLE, "A", "!A & B & !C", "B & !C"));

        checkOptimality("!A & B & !C | !B | D", new BooleanExpression.SemanticWarning(0,
                BooleanExpression.SemanticWarningType.DISTRIBUTIVITY_OF_OR_POSSIBLE, "!B", "!A & B & !C", "!A & !C"));
    }

    @Test
    public void testImplicationCheck() throws BooleanExpression.BadSyntaxException {
        Assert.assertTrue(BooleanExpression.fastImplicationCheck(
                parseExpression("A & B & C & X & !Z"),
                parseExpression("A & B & C")));

        //the antecedent is less restrictive - consider (A & B & C)=1 and D=0
        Assert.assertFalse(BooleanExpression.fastImplicationCheck(
                parseExpression("A & B & C"),
                parseExpression("A & B & C & D")));

        Assert.assertTrue(BooleanExpression.fastImplicationCheck(
                parseExpression("A & B & C"),
                parseExpression("A & B | D")));

        //the antecedent is less restrictive - consider C=1
        Assert.assertFalse(BooleanExpression.fastImplicationCheck(
                parseExpression("A & B | C"),
                parseExpression("A & B")));

        Assert.assertTrue(BooleanExpression.fastImplicationCheck(
                parseExpression("A & !B & !X | C & Y"),
                parseExpression("A & !B | C")));

        Assert.assertFalse(BooleanExpression.fastImplicationCheck(
                parseExpression("A & !B & !X | C & Y"),
                parseExpression("A & !B | !C")));


        //Few cases when the implication check returns false for implication that is actually TRUE

        //antecedent is FALSE (need to disable the optimality check)
        Assert.assertFalse(BooleanExpression.fastImplicationCheck(
                parseExpression("A & !A"),
                parseExpression("B")));

        //consequent is TRUE, optimality check disable
        Assert.assertFalse(BooleanExpression.fastImplicationCheck(
                parseExpression("A"),
                parseExpression("B | !B")));

        //consequent is TRUE, no need to disable optimality check
        Assert.assertFalse(BooleanExpression.fastImplicationCheck(
                parseExpression("A"),
                parseExpression("B & C | B & !C | !B & C | !B & !C")));

        // (B | C) -> (B & C | B & !C | !B & C) =
        // !B & !C | B & C | B & !C | !B & C = TRUE (but the method returns false)
        Assert.assertFalse(BooleanExpression.fastImplicationCheck(
                parseExpression("B | C"),
                parseExpression("B & C | B & !C | !B & C")));
    }

    protected BooleanExpression parseExpression(String expression){
        return parseExpression(expression, null);
    }

    protected BooleanExpression parseExpression(String expression, BooleanExpression.BooleanExpressionException expectedException) {

        BooleanExpression booleanExpression = new BooleanExpression(expression);

        BooleanExpression.BadSyntaxException e = booleanExpression.getSyntaxException();
        if (e == null) {
            if (expectedException != null) {
                Assert.fail("BooleanException expected but not thrown");
            }
        } else {
            Logger.logMessage("BooleanException " + e.getMessage() + " at position " + e.getPosition());
            Logger.logMessage(expression);
            Logger.logMessage(IntStream.range(0, e.getPosition()).boxed().map(i -> "-").collect(Collectors.joining()) + "^");

            if (expectedException == null) {
                throw new AssertionError("Unexpected error at position " + e.getPosition(), e);
            } else {
                Assert.assertEquals("Message mismatch", expectedException.getMessage(), e.getMessage());
                Assert.assertEquals("Position mismatch", expectedException.getPosition(), e.getPosition());
                return null;
            }
        }
        return booleanExpression;
    }

    protected void checkOptimality(String expression, BooleanExpression.SemanticWarning expectedWarning) {
        BooleanExpression booleanExpression = new BooleanExpression(expression);
        if (booleanExpression.getSemanticWarnings().isEmpty()) {
            if (expectedWarning != null) {
                throw new AssertionError("Expected semantic warning not thrown: " + expectedWarning);
            }
        } else {
            BooleanExpression.SemanticWarning actualWarning = booleanExpression.getSemanticWarnings().get(0);
            Assert.assertEquals(expectedWarning, actualWarning);
        }
    }

    protected static <K, V> Map.Entry<K, V> val(K key, V value) {
        return new AbstractMap.SimpleEntry<>(key, value);
    }

    protected static <K, U> Collector<Map.Entry<K, U>, ?, Map<K, U>> valuesToMap() {
        return Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue);
    }
}
