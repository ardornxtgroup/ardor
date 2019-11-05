// Auto generated code, do not modify
package nxt.http.callers;

public class CreatePollCall extends CreateTransactionCallBuilder<CreatePollCall> {
    private CreatePollCall() {
        super(ApiSpec.createPoll);
    }

    public static CreatePollCall create(int chain) {
        return new CreatePollCall().param("chain", chain);
    }

    public CreatePollCall minRangeValue(String minRangeValue) {
        return param("minRangeValue", minRangeValue);
    }

    public CreatePollCall votingModel(String votingModel) {
        return param("votingModel", votingModel);
    }

    public CreatePollCall description(String description) {
        return param("description", description);
    }

    public CreatePollCall holding(String holding) {
        return param("holding", holding);
    }

    public CreatePollCall holding(long holding) {
        return unsignedLongParam("holding", holding);
    }

    public CreatePollCall minNumberOfOptions(String minNumberOfOptions) {
        return param("minNumberOfOptions", minNumberOfOptions);
    }

    public CreatePollCall minBalance(String minBalance) {
        return param("minBalance", minBalance);
    }

    public CreatePollCall finishHeight(String finishHeight) {
        return param("finishHeight", finishHeight);
    }

    public CreatePollCall name(String name) {
        return param("name", name);
    }

    public CreatePollCall maxNumberOfOptions(String maxNumberOfOptions) {
        return param("maxNumberOfOptions", maxNumberOfOptions);
    }

    public CreatePollCall option01(String option01) {
        return param("option01", option01);
    }

    public CreatePollCall minBalanceModel(String minBalanceModel) {
        return param("minBalanceModel", minBalanceModel);
    }

    public CreatePollCall option02(String option02) {
        return param("option02", option02);
    }

    public CreatePollCall option00(String option00) {
        return param("option00", option00);
    }

    public CreatePollCall maxRangeValue(String maxRangeValue) {
        return param("maxRangeValue", maxRangeValue);
    }
}
