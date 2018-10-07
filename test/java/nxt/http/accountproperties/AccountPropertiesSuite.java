package nxt.http.accountproperties;

import nxt.http.AbstractHttpApiSuite;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        AccountInfoTest.class,
        AccountPropertiesTest.class,
})
public class AccountPropertiesSuite extends AbstractHttpApiSuite {
}
