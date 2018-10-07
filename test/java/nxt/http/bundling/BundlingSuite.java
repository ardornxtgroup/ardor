package nxt.http.bundling;

import nxt.http.AbstractHttpApiSuite;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        RulesTest.class,
        AssetBundlerTest.class,
        CurrencyBundlerTest.class,
        PurchaseBundlerTest.class
})
public class BundlingSuite extends AbstractHttpApiSuite {
}
