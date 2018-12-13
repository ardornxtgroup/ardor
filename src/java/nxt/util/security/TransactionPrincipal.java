package nxt.util.security;

import nxt.blockchain.Transaction;
import nxt.util.Convert;

import javax.security.auth.Subject;
import java.security.Principal;
import java.util.Objects;

public class TransactionPrincipal implements Principal {

    private String fullHash;

    public TransactionPrincipal(String fullHash) {
        this.fullHash = fullHash;
    }

    @Override
    public String getName() {
        return fullHash;
    }

    @Override
    public boolean implies(Subject subject) {
        return subject.getPrincipals().stream().anyMatch(p -> p.equals(this));
    }

    @Override
    public int hashCode() {
        return fullHash.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TransactionPrincipal that = (TransactionPrincipal)o;
        return getName().equals(that.getName());
    }
}
