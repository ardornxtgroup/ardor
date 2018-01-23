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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A boolean expression in disjunctive normal form - i.e. a disjunction of one or more conjunctions of one or more literals
 */
public class BooleanExpression {
    public enum Value {
        TRUE, FALSE, UNKNOWN;

        public static Value not(Value v) {
            switch (v) {
                case FALSE:
                    return TRUE;
                case TRUE:
                    return FALSE;
                default:
                    return UNKNOWN;
            }
        }

        public static Value fromBoolean(boolean bool) {
            return bool ? TRUE : FALSE;
        }
    }

    public static final char CONJUNCTION_OPERATOR = '&';
    public static final char DISJUNCTION_OPERATOR = '|';
    public static final char NEGATION_OPERATOR = '!';
    private final Set<String> variables;
    private final Disjunction disjunction;
    private final BadSyntaxException syntaxException;
    private final List<SemanticWarning> semanticWarnings;
    private final int literalsCount;

    /**
     * @param expression The expression string in disjunctive normal form
     */
    public BooleanExpression(String expression) {
        semanticWarnings = new ArrayList<>();
        Disjunction disjunction;
        BadSyntaxException syntaxException;
        try {
            disjunction = new Disjunction(expression, semanticWarnings);
            syntaxException = null;
        } catch (BadSyntaxException e) {
            disjunction = null;
            syntaxException = e;
        }
        this.syntaxException = syntaxException;
        this.disjunction = disjunction;
        if (disjunction != null) {
            Set<String> variablesSet = new HashSet<>();
            for (Conjunction c : disjunction.conjunctions) {
                variablesSet.addAll(c.variables);
            }
            variables = Collections.unmodifiableSet(variablesSet);
            literalsCount = disjunction.conjunctions.stream().mapToInt(c -> c.literals.size()).sum();
        } else {
            variables = Collections.emptySet();
            literalsCount = 0;
        }
    }

    /**
     * Evaluates the expression with the provided variable values.
     * If for some of the expression variables (see {@link #getVariables()}), there is no value in the variableValues map
     * (or the value is null), the value is considered {@link Value#UNKNOWN}. In that case, the result may also be {@link Value#UNKNOWN} -
     * meaning that currently we cannot evaluate the expression to neither {@link Value#TRUE} or {@link Value#FALSE}.
     * But it is not necessary {@link Value#UNKNOWN} - the available values may still be enough to return a definitive result
     *
     * @param variableValues The keys are variable names, the values are the respective boolean {@link Value}s
     * @return The result from evaluating the expression
     * @throws BadSyntaxException in case of error in the expression syntax
     */
    public Value evaluate(Map<String, Value> variableValues) throws BadSyntaxException {
        if (syntaxException != null) {
            throw syntaxException;
        }
        return disjunction.evaluate(variableValues);
    }

    /**
     * Performs fast check whether "antecedent -&gt; consequent" evaluates to TRUE. The implication "antecedent -&gt; consequent"
     * evaluates to TRUE when the antecedent is more restrictive than the consequent, i.e. when in no case the antecedent
     * will be TRUE if the consequent is FALSE. In other words, if consequent is FALSE, then antecedent must also be FALSE.
     *
     * The current implementation only uses the boolean algebra laws and does not create the full truth table of the expression.
     * So if this method returns true, this means that "antecedent -&gt; consequent" evaluates to TRUE, but if the method returns false,
     * it is still possible that "antecedent -&gt; consequent" evaluates to TRUE - e.g. if the antecedent is always equal to FALSE, or
     * the consequent is always equal to TRUE
     *
     * @param antecedent The antecedent of the conditional
     * @param consequent The consequent of the conditional
     *
     * @return true if the boolean expression "antecedent -&gt; consequent" evaluates to TRUE
     * @throws BadSyntaxException in case of error in the expressions syntax
     */
    public static boolean fastImplicationCheck(BooleanExpression antecedent, BooleanExpression consequent) throws BadSyntaxException {
        if (antecedent.syntaxException != null) {
            throw antecedent.syntaxException;
        }
        if (consequent.syntaxException != null) {
            throw consequent.syntaxException;
        }
        //(A1 | A2 | ... | An) -> C =
        //(!A1 & !A2 & ... & !An) | C = (applying distributivity of OR over AND)
        //(!A1 | C) & (!A2 | C) & ... & (!An | C)
        // (A1 -> C) & (A2 -> C) & ... & (An -> C)
        for (Conjunction antecedentConjunction : antecedent.disjunction.conjunctions) {
            if (!antecedentConjunction.implies(consequent.disjunction)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the set of variables used is this expression
     * @return the set of variables used is this expression
     */
    public Set<String> getVariables() {
        return variables;
    }

    public int getLiteralsCount() {
        return literalsCount;
    }

    public boolean hasErrors(boolean treatWarningsAsErrors) {
        if (syntaxException != null) {
            return true;
        } else if (treatWarningsAsErrors && !semanticWarnings.isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    public BadSyntaxException getSyntaxException() {
        return syntaxException;
    }

    public List<SemanticWarning> getSemanticWarnings() {
        return semanticWarnings;
    }

    public enum SyntaxError {
        VARIABLE_EXPECTED("Variable expected"),
        LITERAL_EXPECTED("Literal expected"),
        OPERATOR_OR_AND_EXPECTED("Expected OR (|) or AND (&) operator"),
        UNEXPECTED_NEGATION("Unexpected negation operator"),
        DIGIT_AT_VARIABLE_START("Digit not allowed at variable start"),
        ILLEGAL_CHARACTER("Illegal character '%1$c', code=0x%2$04X");

        private final String messageFormat;

        SyntaxError(String messageFormat) {
            this.messageFormat = messageFormat;
        }

        public String getMessage(Object... args) {
            return String.format(messageFormat, args);
        }
    }

    public enum SemanticWarningType {
        DUPLICATE_LITERAL("Duplicate literal \"%1$s\" in conjunction \"%2$s\""),
        CONJUNCTION_ALWAYS_FALSE("Conjunction \"%1$s\" always evaluates to FALSE because \"%2$s & !%2$s\" always evaluates to FALSE"),
        ABSORPTION_2_POSSIBLE("The expression \"%1$s\" can be removed according to the second absorption law because of \"%2$s\" at position %3$d"),
        DISJUNCTION_ALWAYS_TRUE("Expression always evaluates to TRUE because  \"%1$s | !%1$s\" always evaluates to TRUE"),
        // A | !A & C = (A | !A) & (A | C) = 1 & (A | C) = A | C
        DISTRIBUTIVITY_OF_OR_POSSIBLE("Expression \"%1$s | %2$s\" can be optimized to \"%1$s | %3$s\" by applying the " +
                "law of distributivity of OR over AND");

        private final String messageFormat;

        SemanticWarningType(String messageFormat) {
            this.messageFormat = messageFormat;
        }
    }

    public static class SemanticWarning {
        private final int position;
        private final SemanticWarningType type;
        private final Object[] arguments;

        public SemanticWarning(int position, SemanticWarningType type, Object... args) {
            this.position = position;
            this.type = type;
            this.arguments = args;
        }

        public int getPosition() {
            return position;
        }

        public String getMessage() {
            return String.format(type.messageFormat, arguments);
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof SemanticWarning)) {
                return false;
            }
            SemanticWarning other = (SemanticWarning) obj;
            return this.type == other.type && Arrays.equals(this.arguments, other.arguments);
        }

        @Override
        public String toString() {
            return "Boolean expression warning '" + getMessage() + "' at position " + position;
        }
    }

    public static class BooleanExpressionException extends Exception {
        private final int position;

        BooleanExpressionException(int position, String message) {
            super(message);
            this.position = position;
        }

        public int getPosition() {
            return position;
        }
    }

    public static class BadSyntaxException extends BooleanExpressionException {
        public BadSyntaxException(int position, SyntaxError error, Object... params) {
            super(position, error.getMessage(params));
        }
    }

    /**
     * A literal is a variable or a negation of a variable
     */
    private static class Literal {
        private String variableName;
        private boolean isNegative;

        Literal(String expression, int literalStart, int literalEnd) throws BadSyntaxException {
            variableName = null;
            isNegative = false;
            StringBuilder variableBuilder = new StringBuilder();
            boolean isFinished = false;
            boolean isVariableExpected = false;
            for (int pos = literalStart; pos < literalEnd; pos++) {
                char c = expression.charAt(pos);
                if (Character.isWhitespace(c)) {
                    if (variableBuilder.length() > 0) {
                        isFinished = true;
                    }
                } else if (c == NEGATION_OPERATOR) {
                    if (isVariableExpected || isFinished) {
                        throw new BadSyntaxException(pos, SyntaxError.UNEXPECTED_NEGATION);
                    }
                    isNegative = true;
                    isVariableExpected = true;
                } else if (isValidIdentifierChar(c)) {
                    if (isFinished) {
                        throw new BadSyntaxException(pos, SyntaxError.OPERATOR_OR_AND_EXPECTED);
                    }
                    if (variableBuilder.length() == 0 && c >= '0' && c <= '9') {
                        throw new BadSyntaxException(pos, SyntaxError.DIGIT_AT_VARIABLE_START);
                    }
                    variableBuilder.append(c);
                    isVariableExpected = true;
                } else {
                    throw new BadSyntaxException(pos, SyntaxError.ILLEGAL_CHARACTER, c, (int)c);
                }
            }
            if (variableBuilder.length() == 0) {
                if (isNegative) {
                    throw new BadSyntaxException(literalEnd, SyntaxError.VARIABLE_EXPECTED);
                } else {
                    throw new BadSyntaxException(literalEnd, SyntaxError.LITERAL_EXPECTED);
                }
            }
            variableName = variableBuilder.toString();
        }

        Value evaluate(Map<String, Value> variablesValues) {
            Value variableValue = variablesValues.get(variableName);
            if (variableValue == null || variableValue == Value.UNKNOWN) {
                return Value.UNKNOWN;
            }
            return isNegative ? Value.not(variableValue) : variableValue;
        }

        private static boolean isValidIdentifierChar(char c) {
            return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9';
        }

        @Override
        public String toString() {
            return (isNegative ? "!" : "") + variableName;
        }

        @Override
        public int hashCode() {
            return 31 * variableName.hashCode() + (isNegative ? 1 : 0);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj instanceof Literal) {
                Literal other = (Literal) obj;
                return other.isNegative ==this.isNegative && variableName.equals(other.variableName);
            }
            return false;
        }
    }

    /**
     * Conjunction of literals - literals divided by the AND operator (&)
     */
    private static class Conjunction {
        private final LinkedHashSet<Literal> literals = new LinkedHashSet<>();
        private final Set<String> variables = new HashSet<>();
        private final int start;
        private final int end;

        Conjunction(String expression, int conjunctionStart, int conjunctionEnd, List<SemanticWarning> semanticWarnings) throws BadSyntaxException {
            if (conjunctionEnd - conjunctionStart <= 0) {
                throw new BadSyntaxException(conjunctionStart, SyntaxError.LITERAL_EXPECTED);
            }
            int literalStart = conjunctionStart;
            while (literalStart <= conjunctionEnd) {
                int literalEnd = expression.indexOf(CONJUNCTION_OPERATOR, literalStart);
                if (literalEnd < 0 || literalEnd > conjunctionEnd) {
                    literalEnd = conjunctionEnd;
                }
                Literal l = new Literal(expression, literalStart, literalEnd);
                if (literals.add(l)) {
                    if (!variables.add(l.variableName)) {
                        semanticWarnings.add(new SemanticWarning(literalStart, SemanticWarningType.CONJUNCTION_ALWAYS_FALSE,
                                expression.substring(conjunctionStart, conjunctionEnd), l.variableName));
                    }
                } else {
                    //note that the literal is ignored
                    semanticWarnings.add(new SemanticWarning(literalStart, SemanticWarningType.DUPLICATE_LITERAL,
                            expression.substring(literalStart, literalEnd), expression.substring(conjunctionStart, conjunctionEnd)));
                }
                literalStart = literalEnd + 1;
            }
            this.start = conjunctionStart;
            this.end = conjunctionEnd;
        }

        private boolean implies(Disjunction consequent) {
            // A -> (C1 | C2 | ... | Cn) =
            // !A | C1 | C2 | ... | Cn =
            // (!A | C1) | (!A | C2) | ... | (!A | Cn) =
            // (A -> C1) | (A -> C2) | ... | (A -> Cn)
            for (Conjunction c : consequent.conjunctions) {
                if (implies(c)) {
                    return true;
                }
            }
            return false;
        }

        private boolean implies(Conjunction consequent) {
            //(A1 & A2 & ... & An) -> (C1 & C2 & ... & Cm) =
            // !A1 | !A2 | ... | !An | (C1 & C2 & ... & Cm)
            // The later is guaranteed to be true if for all literals Ci exists a literal Aj equal to Ci
            // (this follows from the law of distributivity of OR over AND)
            return this.literals.containsAll(consequent.literals);
        }

        Value evaluate(Map<String, Value> variablesValues) {
            boolean isResultUnknown = false;
            for (Literal l : literals) {
                Value literalVal = l.evaluate(variablesValues);
                if (literalVal == Value.UNKNOWN) {
                    isResultUnknown = true;
                } else if (literalVal == Value.FALSE) {
                    return Value.FALSE;
                }
            }
            return isResultUnknown ? Value.UNKNOWN : Value.TRUE;
        }

        @Override
        public String toString() {
            return literals.stream().map(Literal::toString).collect(Collectors.joining(" & "));
        }
    }

    /**
     * Disjunction of conjunctions - conjunctions divided by the OR operator (|)
     */
    private static class Disjunction {
        private final List<Conjunction> conjunctions;

        private Disjunction(String expression, List<SemanticWarning> semanticWarnings) throws BadSyntaxException {
            conjunctions = new ArrayList<>();
            int start = 0;
            while (start <= expression.length()) {
                int end = expression.indexOf(DISJUNCTION_OPERATOR, start);
                if (end < 0) {
                    end = expression.length();
                }
                Conjunction c = new Conjunction(expression, start, end, semanticWarnings);

                for (Conjunction prevConjunction : conjunctions) {
                    checkAbsorption2(prevConjunction, c, expression, semanticWarnings);
                    checkSingleLiterals(prevConjunction, c, semanticWarnings);
                }

                conjunctions.add(c);
                start = end + 1;
            }
        }


        private Value evaluate(Map<String, Value> variableValues) {
            boolean isResultUnknown = false;
            for (Conjunction c : conjunctions) {
                Value conjunctionVal = c.evaluate(variableValues);
                if (conjunctionVal == Value.UNKNOWN) {
                    isResultUnknown = true;
                } else if (conjunctionVal == Value.TRUE) {
                    //If at least one of the conjunctions evaluates to true, the value of the whole disjunction is always true
                    return Value.TRUE;
                }
            }
            return isResultUnknown ? Value.UNKNOWN : Value.FALSE;
        }

        @Override
        public String toString() {
            return conjunctions.stream().map(Conjunction::toString).collect(Collectors.joining(" | "));
        }

        private static void checkAbsorption2(Conjunction a, Conjunction b, String expression, List<SemanticWarning> semanticWarnings) {
            if (a.literals.containsAll(b.literals)) {
                warnAbsorption2(a, b, expression, semanticWarnings);
            } else if (b.literals.containsAll(a.literals)) {
                warnAbsorption2(b, a, expression, semanticWarnings);
            }
        }

        private static void checkSingleLiterals(Conjunction a, Conjunction b, List<SemanticWarning> semanticWarnings) {
            Literal literalA = null;
            Literal literalB = null;
            if (a.literals.size() == 1) {
                literalA = a.literals.iterator().next();
            }

            if (b.literals.size() == 1) {
                literalB = b.literals.iterator().next();
            }

            if (literalA != null && literalB != null) {
                if (literalA.variableName.equals(literalB.variableName)) {
                    if (literalA.isNegative != literalB.isNegative) {
                        SemanticWarning warning = new SemanticWarning(b.start, SemanticWarningType.DISJUNCTION_ALWAYS_TRUE,
                                (literalA.isNegative ? literalB : literalA).variableName);
                        semanticWarnings.add(warning);
                    }
                    // else case handled by checkAbsorption2
                }
            } else if (literalA != null) {
                if (b.variables.contains(literalA.variableName)) {
                    warnDistributivity(literalA, b, semanticWarnings);
                }
            } else if (literalB != null) {
                if (a.variables.contains(literalB.variableName)) {
                    warnDistributivity(literalB, a, semanticWarnings);
                }
            }
        }

        private static void warnAbsorption2(Conjunction unnecessary, Conjunction subset, String expression, List<SemanticWarning> semanticWarnings) {
            SemanticWarning warning = new SemanticWarning(unnecessary.start, SemanticWarningType.ABSORPTION_2_POSSIBLE,
                    expression.substring(unnecessary.start, unnecessary.end), expression.substring(subset.start, subset.end), subset.start);
            semanticWarnings.add(warning);
        }

        private static void warnDistributivity(Literal freeLiteral, Conjunction unoptimizedConjunction, List<SemanticWarning> semanticWarnings) {
            String optimizedConjunctionStr = unoptimizedConjunction.literals.stream().
                    filter(l -> !l.variableName.equals(freeLiteral.variableName)).
                    map(Literal::toString).collect(Collectors.joining(" & "));

            SemanticWarning warning = new SemanticWarning(unoptimizedConjunction.start, SemanticWarningType.DISTRIBUTIVITY_OF_OR_POSSIBLE,
                    freeLiteral.toString(), unoptimizedConjunction.toString(), optimizedConjunctionStr);
            semanticWarnings.add(warning);
        }
    }
}
