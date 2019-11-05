// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class EvaluateExpressionCall extends APICall.Builder<EvaluateExpressionCall> {
    private EvaluateExpressionCall() {
        super(ApiSpec.evaluateExpression);
    }

    public static EvaluateExpressionCall create() {
        return new EvaluateExpressionCall();
    }

    public EvaluateExpressionCall expression(String expression) {
        return param("expression", expression);
    }

    public EvaluateExpressionCall values(String... values) {
        return param("values", values);
    }

    public EvaluateExpressionCall vars(String... vars) {
        return param("vars", vars);
    }

    public EvaluateExpressionCall evaluate(String evaluate) {
        return param("evaluate", evaluate);
    }

    public EvaluateExpressionCall checkOptimality(String checkOptimality) {
        return param("checkOptimality", checkOptimality);
    }
}
