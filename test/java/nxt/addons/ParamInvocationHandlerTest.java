/*
 * Copyright © 2013-2016 The Nxt Core Developers.
 * Copyright © 2016-2019 Jelurida IP B.V.
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

package nxt.addons;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ParamInvocationHandlerTest {

    @Test
    public void testReadRunnerParams() {
        JO params = new JO();
        params.put("intValue", "3");
        params.put("stringValue", "some string");
        RunnerParams tested = ParamInvocationHandler.getParams(RunnerParams.class, params, new JO(), new JO());
        assertEquals("some string", tested.stringValue());
        assertEquals(3, tested.intValue());
    }

    @Test
    public void testContractSetupParams() {
        JO params = new JO();
        params.put("booleanValue", "true");
        params.put("stringValue", "some other string");
        ContractSetupParams tested = ParamInvocationHandler.getParams(ContractSetupParams.class, new JO(), params, new JO());
        assertEquals("some other string", tested.stringValue());
        assertTrue(tested.booleanValue());
    }

    @Test
    public void testInvocationParams() {
        JO params = new JO();
        params.put("someId", Long.toUnsignedString(-2));
        params.put("stringValue", "some other string");
        InvocationParams tested = ParamInvocationHandler.getParams(InvocationParams.class, new JO(), new JO(), params);
        assertEquals("some other string", tested.stringValue());
        assertEquals(-2L, tested.someId());
    }

    @Test
    public void testParamsScopePriority() {
        JO invocation = new JO();
        invocation.put("stringValue", "invocation");
        JO setup = new JO();
        setup.put("stringValue", "setup");
        JO runner = new JO();
        runner.put("stringValue", "runner");
        MultiAnnotatedParams tested = ParamInvocationHandler.getParams(MultiAnnotatedParams.class, runner, setup, invocation);
        assertEquals("invocation", tested.stringValue());
    }

    @Test
    public void testDefaultParameterValue() {
        DefaultParams tested = ParamInvocationHandler.getParams(DefaultParams.class, new JO(), new JO(), new JO());
        assertEquals(-1, tested.longValueWithDefault());
        assertEquals(2 * 3, tested.intValueWithDefaultAndParameters(2, 3));
        assertEquals("some default string", tested.objectValueWithDefault());
    }

    @Test
    public void testDefaultParameterValueOverride() {
        JO runnerConfigParams = new JO();
        runnerConfigParams.put("longValueWithDefault", 2L);
        runnerConfigParams.put("intValueWithDefaultAndParameters", 10);
        DefaultParams tested = ParamInvocationHandler.getParams(DefaultParams.class, runnerConfigParams, new JO(), new JO());
        assertEquals(2, tested.longValueWithDefault());
        assertEquals(10, tested.intValueWithDefaultAndParameters(2, 3));
    }

    @Test
    public void testMissingParameterWithoutDefaultValue() {
        DefaultParams tested = ParamInvocationHandler.getParams(DefaultParams.class, new JO(), new JO(), new JO());

        assertEquals(0, tested.intValue());
        assertEquals(0L, tested.longValue());
        assertEquals(0L, tested.idValue());
        assertFalse(tested.booleanValue());
        assertNull(tested.objectValue());
    }

    public interface DefaultParams {
        @ContractRunnerParameter
        int intValue();

        @ContractRunnerParameter
        boolean booleanValue();

        @ContractRunnerParameter
        long longValue();

        @BlockchainEntity
        @ContractRunnerParameter
        long idValue();

        @ContractRunnerParameter
        Object objectValue();


        @ContractRunnerParameter
        default long longValueWithDefault() {
            return -1;
        }

        @ContractRunnerParameter
        default Object objectValueWithDefault() {
            return "some default string";
        }

        @ContractRunnerParameter
        default int intValueWithDefaultAndParameters(int a, int b) {
            return a * b;
        }
    }

    private interface RunnerParams {
        @ContractRunnerParameter
        int intValue();

        @ContractRunnerParameter
        String stringValue();
    }

    private interface MultiAnnotatedParams {
        @ContractInvocationParameter
        @ContractSetupParameter
        @ContractRunnerParameter
        String stringValue();
    }

    private interface ContractSetupParams {
        @ContractSetupParameter
        boolean booleanValue();

        @ContractSetupParameter
        String stringValue();
    }

    private interface InvocationParams {
        @ContractInvocationParameter
        @BlockchainEntity
        long someId();

        @ContractInvocationParameter
        String stringValue();
    }
}